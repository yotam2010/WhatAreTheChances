package com.hadas.yotam.whatarethechances;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineGame extends AppCompatActivity implements PlayerQuestionTurn.mQuestionSend, PlayerRespondTurn.playerRespond {
    public static long closeChatTime;
    public static Boolean chatOpen;
    DatabaseReference mChatRef;
    DatabaseReference mRoomRef;
    ChildEventListener mRefListener;
    ChildEventListener mRoomListener;
    FloatingActionButton fab;
    Animation mFabChatAnimation;
    int accentColor;
    int newMessageColor;
    MediaPlayer mMediaPlayer;
    Animation mPlayersStartAnimation;
    CircleImageView mPerson1AnimationImage;
    CircleImageView mPerson2AnimationImage;
    String player1Name;
    String player2Name;
    String player1Image;
    String player2Image;
    Boolean startAnimation;
    Boolean firstAnimation;
    Boolean firstAnimationComplete;
    TextView mPlayer1NameTextView;
    TextView mPlayer2NameTextView;
    TextView mPlayer1Score;
    TextView mPlayer2Score;
    CircleImageView mPlayer1Image;
    CircleImageView mPlayer2Image;
    Boolean isPlayer1;
    FrameLayout mContainerLayout;
    Boolean player1Turn;
    PlayerQuestionTurn mPlayerQuestionTurn;
    PlayerRespondTurn mPlayerRespondTurn;
    public static Boolean gameStarted;
    int dataSnapCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);
        gameStarted=true;
        setTitle("");
        intalizeWidgets();
        intalizeVariables();
        setChildListener();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppSharedPreference.setGameStarted(this,true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(OnlineGame.this,ChatActivity.class);
                startActivity(intent);
                fab.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            }
        });
        mRoomRef.child(AppConstants.PLAYER_TURN).setValue(player1Turn);
    }


    public void intalizeWidgets(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);
        mContainerLayout=(FrameLayout)findViewById(R.id.online_game_fragment_container);
        mPlayer1NameTextView = (TextView)findViewById(R.id.player1_name);
        mPlayer2NameTextView = (TextView)findViewById(R.id.player2_name);
        mPlayer1Score = (TextView)findViewById(R.id.player1_points);
        mPlayer2Score = (TextView)findViewById(R.id.player2_points);
        mPlayer1Image = (CircleImageView)findViewById(R.id.player1_image);
        mPlayer2Image = (CircleImageView)findViewById(R.id.player2_image);
        mPerson1AnimationImage = (CircleImageView)findViewById(R.id.player1_image_animation);
        mPerson2AnimationImage = (CircleImageView)findViewById(R.id.player2_image_animation);
        mMediaPlayer = MediaPlayer.create(this,R.raw.message);
    }

    public void intalizeVariables(){
        closeChatTime=Calendar.getInstance().getTimeInMillis();
        isPlayer1=getIntent().getBooleanExtra(AppConstants.PLAYER_TURN,false);
        chatOpen=false;
        dataSnapCounter=0;
        mPlayerRespondTurn = new PlayerRespondTurn();
        mPlayerQuestionTurn = new PlayerQuestionTurn();
        player1Turn = true;
        firstAnimationComplete=false;
        firstAnimation=true;
        player1Name=null;
        player2Name=null;
        player1Image=null;
        player2Image=null;
        startAnimation=false;
        accentColor = getResources().getColor(R.color.colorAccent);
        newMessageColor= getResources().getColor(R.color.newMessageColor);
        mRoomRef = AppConstants.mDatabaseReference.child(AppConstants.ROOMS).child(AppConstants.CURRENT_ROOM);
        mChatRef = mRoomRef.child(AppConstants.MESSAGES);
        mRoomRef.keepSynced(true);
        mChatRef.keepSynced(true);
        mFabChatAnimation = AnimationUtils.loadAnimation(this,R.anim.chat_fab_animation);

    }

    public void startGameAnimation(){
        mPlayersStartAnimation = AnimationUtils.loadAnimation(this,R.anim.start_game_player_animation);
        mPlayersStartAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                    LinearLayout startAnimationLayout;
                    LinearLayout player1Information;
                    LinearLayout player2Information;
                    startAnimationLayout = (LinearLayout) findViewById(R.id.start_game_animation_layout);
                    player1Information = (LinearLayout) findViewById(R.id.player1_information);
                    player2Information = (LinearLayout) findViewById(R.id.player2_information);
                    Utilities.endLoaderAnimation(startAnimationLayout);
                    player1Information.setVisibility(View.VISIBLE);
                    player2Information.setVisibility(View.VISIBLE);
                    player1Information.animate().alpha(1f).setDuration(1000).start();
                    player2Information.animate().alpha(1f).setDuration(1000).start();
                    mContainerLayout.setVisibility(View.VISIBLE);
                    if(isPlayer1)
                        getSupportFragmentManager().beginTransaction().replace(R.id.online_game_fragment_container,mPlayerQuestionTurn).commit();
                    else
                        getSupportFragmentManager().beginTransaction().replace(R.id.online_game_fragment_container, mPlayerRespondTurn).commit();
                    fab.setEnabled(true);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(firstAnimation) {

//            Utilities.endLoaderAnimation(mPerson1AnimationImage);
//            Utilities.endLoaderAnimation(mPerson2AnimationImage);
            Handler h = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return false;
                }
            });
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPerson1AnimationImage.startAnimation(mPlayersStartAnimation);
                    mPerson2AnimationImage.startAnimation(mPlayersStartAnimation);
                    mPerson1AnimationImage.setVisibility(View.VISIBLE);
                    mPerson2AnimationImage.setVisibility(View.VISIBLE);
                }
            }, 20);

            firstAnimation=false;
        }
    }

    public void setChildListener(){
        mRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!chatOpen)
                    if(Long.valueOf(dataSnapshot.child("time").getValue(String.class))>closeChatTime)
                        if(!dataSnapshot.child(AppConstants.MESSAGE_NAME).getValue(String.class).equals(AppConstants.MY_NAME)) {
                    fab.startAnimation(mFabChatAnimation);
                    fab.setBackgroundTintList(ColorStateList.valueOf(newMessageColor));
                        mMediaPlayer.start();

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRoomListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(firstAnimation)
                 switch (dataSnapshot.getKey()){
                    case AppConstants.PLAYER1_NAME:
                        player1Name=dataSnapshot.getValue(String.class);
                        playersSet();
                        break;
                    case AppConstants.PLAYER2_NAME:
                        player2Name=dataSnapshot.getValue(String.class);
                        playersSet();
                        break;
                    case AppConstants.PLAYER1_IMAGE:
                        player1Image=dataSnapshot.getValue(String.class);
                        playersSet();
                        break;
                    case AppConstants.PLAYER2_IMAGE:
                        player2Image=dataSnapshot.getValue(String.class);
                        playersSet();
                        break;
                }

                switch (dataSnapshot.getKey()) {
                    case AppConstants.PLAYER_TURN:
                        if(dataSnapCounter>0)
                            break;
                        dataSnapCounter++;
                        player1Turn = dataSnapshot.getValue(Boolean.class);
                        newRound();
                        break;

                    case AppConstants.QUESTION:
                        if(dataSnapCounter>1)
                            break;
                        dataSnapCounter++;
                        if((isPlayer1&&!player1Turn)||(!isPlayer1&&player1Turn)){
                            String questionText = dataSnapshot.child(AppConstants.TEXT).getValue(String.class);
                            String questionDifficulty = dataSnapshot.child(AppConstants.DIFFICULTY).getValue(String.class);
                            mPlayerRespondTurn.setQuestion(questionText,questionDifficulty);
                        }
                        break;

                    case AppConstants.QUESTION_ACCEPT:
                        if(dataSnapCounter>2)
                            break;
                        dataSnapCounter++;

                        if (!dataSnapshot.getValue(Boolean.class)) {
                            newRound();
                        }

                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)) {
                            if (!dataSnapshot.getValue(Boolean.class))
                                Utilities.activityToast(getApplicationContext(),getString(R.string.opponet_declined));
                            else
                                Utilities.activityToast(getApplicationContext(),getString(R.string.opponet_accepted));

                        }
                        break;
                    case AppConstants.RESPOND_NUMBER:
                        if(dataSnapCounter>3)
                            break;
                        dataSnapCounter++;
                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)){
                            String respondFormat = dataSnapshot.child(AppConstants.RESPOND_TYPE).getValue(String.class);
                            String respondString = dataSnapshot.child(AppConstants.RESPOND_PROOF).getValue(String.class);
                            mPlayerQuestionTurn.gotRespond(respondString,respondFormat);
                        }
                        break;
                    case AppConstants.ANSWER_ACCEPT:
                        if(dataSnapCounter>4)
                            break;
                        dataSnapCounter++;
                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)) {
                            HashMap<String, Object> values2 = new HashMap();
                            values2.put(AppConstants.PLAYER_TURN, !player1Turn);
                            mRoomRef.updateChildren(values2);
                        } else
                            Utilities.activityToast(getApplicationContext(),dataSnapshot.getValue(Boolean.class) ? getString(R.string.opponet_accepted) : getString(R.string.opponet_declined));
                        break;
                    case AppConstants.POINTS1:
                        String points = String.valueOf(dataSnapshot.getValue(Integer.class));
                        mPlayer1Score.setText(points);
                        break;
                    case AppConstants.POINTS2:
                        String points2 = String.valueOf(dataSnapshot.getValue(Integer.class));
                        mPlayer2Score.setText(points2);
                        break;
                    case AppConstants.DONE:
                        saveAndLeave();
                        Utilities.activityToast(getApplicationContext(),getString(R.string.opponet_left_match));
                        break;

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                switch (dataSnapshot.getKey()) {
                    case AppConstants.PLAYER_TURN:
                        player1Turn = dataSnapshot.getValue(Boolean.class);
                        newRound();
                        break;

                    case AppConstants.QUESTION:
                        if((isPlayer1&&!player1Turn)||(!isPlayer1&&player1Turn)){
                            String questionText = dataSnapshot.child(AppConstants.TEXT).getValue(String.class);
                            String questionDifficulty = dataSnapshot.child(AppConstants.DIFFICULTY).getValue(String.class);
                            mPlayerRespondTurn.setQuestion(questionText,questionDifficulty);
                        }
                        break;
                    case AppConstants.RESPOND_NUMBER:
                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)){
                            String respondFormat = dataSnapshot.child(AppConstants.RESPOND_TYPE).getValue(String.class);
                            String respondString = dataSnapshot.child(AppConstants.RESPOND_PROOF).getValue(String.class);
                            mPlayerQuestionTurn.gotRespond(respondString,respondFormat);
                        }
                        break;
                    case AppConstants.ANSWER_ACCEPT:
                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn))
                            if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)){
                            HashMap<String, Object> values2 = new HashMap();
                            values2.put(AppConstants.PLAYER_TURN,!player1Turn);
                            mRoomRef.updateChildren(values2);
                        }
                        else
                            Utilities.activityToast(getApplicationContext(),dataSnapshot.getValue(Boolean.class) ? getString(R.string.opponet_accepted) : getString(R.string.opponet_declined));
                        break;

                    case AppConstants.POINTS1:
                        String points = String.valueOf(dataSnapshot.getValue(Integer.class));
                        mPlayer1Score.setText(points);
                        break;
                    case AppConstants.POINTS2:
                        String points2 = String.valueOf(dataSnapshot.getValue(Integer.class));
                        mPlayer2Score.setText(points2);
                        break;

                    case AppConstants.QUESTION_ACCEPT:

                        if (!dataSnapshot.getValue(Boolean.class)) {
                            newRound();
                        }

                        if((isPlayer1&&player1Turn)||(!isPlayer1&&!player1Turn)) {
                            if (!dataSnapshot.getValue(Boolean.class))
                                Utilities.activityToast(getApplicationContext(),getString(R.string.opponet_declined));
                            else
                                Utilities.activityToast(getApplicationContext(),getString(R.string.opponet_accepted));

                        }
                        break;
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public void newRound(){
        dataSnapCounter=0;
        mPlayerRespondTurn = new PlayerRespondTurn();
        mPlayerQuestionTurn = new PlayerQuestionTurn();
        mRoomRef.child(AppConstants.QUESTION).removeValue();
        mRoomRef.child(AppConstants.RESPOND_NUMBER).removeValue();
        mRoomRef.child(AppConstants.ANSWER_ACCEPT).removeValue();
        mRoomRef.child(AppConstants.QUESTION_ACCEPT).removeValue();
        if(player1Turn==isPlayer1)
            getSupportFragmentManager().beginTransaction().replace(R.id.online_game_fragment_container,mPlayerQuestionTurn).commit();
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.online_game_fragment_container,mPlayerRespondTurn).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mChatRef.addChildEventListener(mRefListener);
        mRoomRef.addChildEventListener(mRoomListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mChatRef.removeEventListener(mRefListener);
        mRoomRef.removeEventListener(mRoomListener);
    }

    public void playersSet(){
        if(player2Image!=null&&player1Image!=null){
            Glide.with(this).load(player1Image).centerCrop().listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if(startAnimation)
                        startGameAnimation();
                    else
                        startAnimation=true;
                    return false;
                }
            }).into(mPerson1AnimationImage);
            Glide.with(this).load(player2Image).centerCrop().listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if(startAnimation)
                        startGameAnimation();
                    else
                        startAnimation=true;
                    return false;
                }
            }).into(mPerson2AnimationImage);

            mPlayer1NameTextView.setText(player1Name);
            mPlayer2NameTextView.setText(player2Name);
            Glide.with(this).load(player1Image).into(mPlayer1Image);
            Glide.with(this).load(player2Image).into(mPlayer2Image);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.online_game_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.quit_game)
            leaveGame();
        return super.onOptionsItemSelected(item);
    }
    public void leaveGame(){
        AlertDialog signOutDialog = new AlertDialog.Builder(OnlineGame.this).setMessage(getString(R.string.alert_dialog_signOut)).setPositiveButton(R.string.respond_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       saveAndLeave();
                    }
                }).setNegativeButton(R.string.respond_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create();
        signOutDialog.show();
        Button negativeButton = signOutDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorAccentDark));
    }
    public void saveAndLeave(){

        mRoomRef.removeEventListener(mRoomListener);
        Intent intent = new Intent(OnlineGame.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        HashMap<String,Object> values = new HashMap<>();
        values.put(AppConstants.DONE,true);
        mRoomRef.updateChildren(values);
        values.clear();
        if(!isPlayer1) {
            values.put(AppConstants.NAME, player1Name);
            values.put(AppConstants.PROFILE_PIC, player1Image);
        }else {
            values.put(AppConstants.NAME, player2Name);
            values.put(AppConstants.PROFILE_PIC, player2Image);
        }
        int player1ScoreInt = !TextUtils.isEmpty(mPlayer1Score.getText().toString()) ? Integer.valueOf(mPlayer1Score.getText().toString()) : 0;
        values.put(AppConstants.POINTS1, player1ScoreInt);
        int player2ScoreInt = !TextUtils.isEmpty(mPlayer2Score.getText().toString()) ? Integer.valueOf(mPlayer2Score.getText().toString()) : 0;
        values.put(AppConstants.POINTS2, player2ScoreInt);
        values.put(AppConstants.WAS_PLAYER1, isPlayer1);
        values.put(AppConstants.GAME_DATE,Calendar.getInstance().getTimeInMillis());
        DatabaseReference mRef =  AppConstants.mDatabaseReference.child(AppConstants.USERS).child(AppConstants.MY_UID).child(AppConstants.MATCHES_HISTORY).push();
        mRef.updateChildren(values);
        OnlineGame.this.finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        leaveGame();
    }

    @Override
    public void sendQuestion(String questionText,String questionDifficulty) {
        HashMap<String,Object> values = new HashMap<>();
        values.put(AppConstants.TEXT,questionText);
        values.put(AppConstants.DIFFICULTY,questionDifficulty);
        mRoomRef.child(AppConstants.QUESTION).updateChildren(values);

    }

    @Override
    public void sendPlayerRespond(Boolean accepted) {
        HashMap<String,Object> values = new HashMap<>();
        values.put(AppConstants.QUESTION_ACCEPT,accepted);
        mRoomRef.updateChildren(values);
    }

}

























