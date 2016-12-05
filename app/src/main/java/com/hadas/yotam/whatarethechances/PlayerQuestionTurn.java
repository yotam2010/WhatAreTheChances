package com.hadas.yotam.whatarethechances;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by Yotam on 25/11/2016.
 */

public class PlayerQuestionTurn extends Fragment {

    interface mQuestionSend{
        public void sendQuestion(String questionText,String questionDifficulty);
    }
    EditText mQuestionText;
    Spinner mQuestionDifficulty;
    Button mQuestionSendButton;
    String questionLastText;
    TextView waitingForOpponetText;
    VideoView mRespondVideo;
    MediaController mMediaController;
    LinearLayout mQuestionMediaButtonsLayout;
    ImageView mMediaRespondAccept;
    ImageView mMediaRespondDecline;
    OnlineGame mActivity;
    ImageView mRespondImageView;
    SeekBar mAudioSeekBar;
    MediaPlayer mAudioPlayer;
    CountDownTimer audioTimer;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        questionLastText="";
        mActivity = (OnlineGame) getActivity();
        mMediaController = new MediaController(getActivity());
        mRespondVideo = (VideoView)getActivity().findViewById(R.id.player_respond_videoView);
        mRespondImageView = (ImageView)getActivity().findViewById(R.id.player_respond_imageView);
        mMediaRespondAccept = (ImageView)getActivity().findViewById(R.id.question_respond_media_accept);
        mMediaRespondAccept.setColorFilter(Color.GREEN);
//        mMediaRespondAccept.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        mMediaRespondDecline = (ImageView)getActivity().findViewById(R.id.question_respond_media_decline);
        mMediaRespondDecline.setColorFilter(Color.RED);
//        mMediaRespondDecline.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        mAudioSeekBar = (SeekBar)getActivity().findViewById(R.id.player_question_audio_seek);
        mAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && mAudioPlayer!=null){
                    mAudioPlayer.seekTo(progress);
                    setTimer(mAudioPlayer.getDuration()-progress);
                    mAudioPlayer.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mAudioPlayer!=null) {
                    mAudioPlayer.pause();
                    audioTimer.cancel();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mMediaRespondAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference scoreRef = mActivity.mRoomRef;
                scoreRef.child(mActivity.isPlayer1 ? AppConstants.POINTS2 : AppConstants.POINTS1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    int points = 0;
                        if(dataSnapshot.exists())
                            points = dataSnapshot.getValue(Integer.class);
                        points += (mQuestionDifficulty.getSelectedItemPosition()+1)*5;
                        HashMap<String, Object> values = new HashMap();
                        values.put(mActivity.isPlayer1 ? AppConstants.POINTS2 : AppConstants.POINTS1,points);
                        values.put(AppConstants.ANSWER_ACCEPT,true);
                        scoreRef.updateChildren(values);
                        mQuestionMediaButtonsLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        mMediaRespondDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference scoreRef = mActivity.mRoomRef;
                mQuestionMediaButtonsLayout.setVisibility(View.GONE);
                HashMap<String, Object> values = new HashMap();
                values.put(AppConstants.ANSWER_ACCEPT,false);
                scoreRef.updateChildren(values);
            }
        });

        mQuestionMediaButtonsLayout = (LinearLayout)getActivity().findViewById(R.id.question_respond_buttons);
        mRespondVideo.setMediaController(mMediaController);
        mMediaController.setAnchorView(mRespondVideo);
        mQuestionDifficulty = (Spinner)getActivity().findViewById(R.id.player_question_difficulty_spinner);
        mQuestionText = (EditText)getActivity().findViewById(R.id.player_question_editText);
        mQuestionSendButton = (Button)getActivity().findViewById(R.id.player_question_send_button);
        mQuestionSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClick();
            }
        });
        mQuestionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                questionLastText=s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            if(mQuestionText.getLineCount()>3) {
                mQuestionText.setText(questionLastText);
                mQuestionText.setSelection(questionLastText.length()-1);
            }
            }
        });
    }

    public void onSendClick(){
        if(mQuestionText.getText().toString().trim().length()>10) {
            mActivity.sendQuestion(mQuestionText.getText().toString(), mQuestionDifficulty.getSelectedItem().toString());
            disableWidgets();
            waitingForOpponetText = (TextView)getActivity().findViewById(R.id.waiting_for_respond);
            waitingForOpponetText.setVisibility(View.VISIBLE);
            Animation waitingAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.waiting_for_opponet_animation);
            waitingForOpponetText.startAnimation(waitingAnimation);
        }else{
            Utilities.fragmentToast(getActivity(),getString(R.string.string_too_short));
        }
    }

    public void setTimer(int duration){
        if(audioTimer!=null){
            audioTimer.cancel();
            audioTimer=null;
        }
        audioTimer = new CountDownTimer(duration,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                mAudioSeekBar.setProgress((int)((mAudioPlayer.getDuration())-millisUntilFinished));
            }

            @Override
            public void onFinish() {
                mAudioSeekBar.setProgress(mAudioSeekBar.getMax());
            }
        }.start();
    }


    public void gotRespond(String respondString,String respondFormat){

        try {
            switch (respondFormat) {
                case AppConstants.TYPE_AUDIO:
                    mAudioPlayer = new MediaPlayer();
                    mAudioPlayer.setDataSource(respondString);
                    mAudioPlayer.prepareAsync();
                    mAudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mAudioSeekBar.setMax(mAudioPlayer.getDuration());
                            mAudioSeekBar.setVisibility(View.VISIBLE);
                            setTimer(mAudioPlayer.getDuration());
                            mAudioPlayer.start();
                            waitingForOpponetText.clearAnimation();
                            waitingForOpponetText.setVisibility(View.GONE);
                            mQuestionMediaButtonsLayout.setVisibility(View.VISIBLE);
                            hideWidgets();
                        }
                    });

                    break;
                case AppConstants.TYPE_IMAGE:
                    mRespondImageView.setVisibility(View.VISIBLE);
                    Glide.with(getActivity()).load(respondString).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            userDeclined();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            waitingForOpponetText.clearAnimation();
                            waitingForOpponetText.setVisibility(View.GONE);
                            mQuestionMediaButtonsLayout.setVisibility(View.VISIBLE);
                            hideWidgets();
                            return false;
                        }
                    }).into(mRespondImageView);
                    break;

                case AppConstants.TYPE_VIDEO:
                    mRespondVideo.setVisibility(View.VISIBLE);
                    mRespondVideo.setVideoURI(Uri.parse(respondString));
                    mRespondVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            waitingForOpponetText.clearAnimation();
                            waitingForOpponetText.setVisibility(View.GONE);
                            mQuestionMediaButtonsLayout.setVisibility(View.VISIBLE);
                            hideWidgets();
                            mRespondVideo.start();
                        }
                    });
                    break;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Utilities.activityToast(getActivity(),"נכשל לטעון קובץ");
            userDeclined();
        }
    }

    public void userDeclined(){
        DatabaseReference scoreRef = mActivity.mRoomRef;
        mQuestionMediaButtonsLayout.setVisibility(View.GONE);
        HashMap<String, Object> values = new HashMap();
        values.put(AppConstants.ANSWER_ACCEPT,false);
        scoreRef.updateChildren(values);
    }
    public void disableWidgets(){
        mQuestionSendButton.setEnabled(false);
        mQuestionDifficulty.setEnabled(false);
        mQuestionText.setEnabled(false);
    }
    public void hideWidgets(){
        mQuestionSendButton.setVisibility(View.GONE);
        mQuestionDifficulty.setVisibility(View.GONE);
        mQuestionText.setVisibility(View.GONE);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_question_turn,container,false);
        return v;
    }
}
