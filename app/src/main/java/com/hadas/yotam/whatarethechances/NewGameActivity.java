package com.hadas.yotam.whatarethechances;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Yotam on 12/11/2016.
 */

public class NewGameActivity extends Fragment {
    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    RecyclerView mRecyclerView;
    MainActivity mActivity;
    Button mOnlineGameButton;
    TextView mCancelSearchTextView;
    Timer recyclerTimer;
    long recyclerLastTouch;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerLastTouch= Calendar.getInstance().getTimeInMillis();
        recyclerTimer = new Timer();
        mActivity = (MainActivity)getActivity();
        mActivity.setTitle(getActivity().getString(R.string.new_game));
        mOnlineGameButton=(Button)mActivity.findViewById(R.id.online_game_button);
        mCancelSearchTextView=(TextView)mActivity.findViewById(R.id.new_game_cancel_search);
        mRecyclerView = (RecyclerView)mActivity.findViewById(R.id.new_game_users_status);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PlayerStatus,PlayerStatusViewHolder>
                (PlayerStatus.class,R.layout.player_list_item,PlayerStatusViewHolder.class,mActivity.mUsersStatus.orderByChild(AppConstants.USER_STATUS)) {
            @Override
            protected void populateViewHolder(PlayerStatusViewHolder viewHolder, PlayerStatus model, int position) {
            viewHolder.setName(model.name);
            viewHolder.setImagePath(getActivity().getApplicationContext(),model.imagePath);
            viewHolder.setStatus(mActivity.getApplicationContext(),model.status);
            viewHolder.setUid(model.getuId());
            viewHolder.setPlay(!mActivity.gameSearch && model.status.equals(AppConstants.USER_ONLINE)
                    &&!model.getuId().equals(AppConstants.MY_UID), getActivity());
            }
        };
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
        mOnlineGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForGame();
            }
        });
        if(mActivity.gameSearch) {
            mOnlineGameButton.setEnabled(false);
            mCancelSearchTextView.setVisibility(View.VISIBLE);
        }
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recyclerLastTouch= Calendar.getInstance().getTimeInMillis();
                return false;
            }
        });
        mCancelSearchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSearchForGame();
            }
        });

    }


    public void searchForGame(){
        if(Utilities.checkInternetConnection(mActivity)) {
            mOnlineGameButton.setEnabled(false);
            mActivity.gameSearch = true;
            mFirebaseRecyclerAdapter.notifyDataSetChanged();
            mCancelSearchTextView.setEnabled(false);
            mCancelSearchTextView.setVisibility(View.VISIBLE);
            mActivity.startSearchService();
            mActivity.setPlayerStatus(AppConstants.USER_BUSY);
            Animation mCancelAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.game_search_animation);
            mCancelSearchTextView.startAnimation(mCancelAnimation);
            new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return false;
                }
            }).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCancelSearchTextView.setEnabled(true);

                }
            },1000);
        }
    }
    public void stopSearchForGame(){
         resetFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(AppSharedPreference.getGameStarted(mActivity))
            resetFragment();
        recyclerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long lastTimeScrolled = Calendar.getInstance().getTimeInMillis()-recyclerLastTouch;
            if(mFirebaseRecyclerAdapter!=null &&mFirebaseRecyclerAdapter.getItemCount()>0&&lastTimeScrolled>10000)
                    mRecyclerView.smoothScrollToPosition(0);
            }
        },10000,10000);
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerTimer.purge();
    }

    private void resetFragment(){
        mActivity.stopSearchService();
        mActivity.gameSearch=false;
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        }).postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnlineGameButton.setEnabled(true);
                mActivity.setPlayerStatus(AppConstants.USER_ONLINE);
                mFirebaseRecyclerAdapter.notifyDataSetChanged();
            }
        },1000);
        mCancelSearchTextView.setEnabled(false);
        Utilities.endLoaderAnimation(mCancelSearchTextView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.new_game,container,false);
        return v;
    }



    public static class PlayerStatus{
        String name;
        String uId;
        String imagePath;
        String status;

        public PlayerStatus() {
        }

        public PlayerStatus(String name, String uId, String imagePath,String status) {
            this.name = name;
            this.uId = uId;
            this.imagePath = imagePath;
            this.status=status;
        }

        public String getName() {
            return name;
        }

        public String getuId() {
            return uId;
        }


        public String getImagePath() {
            return imagePath;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class PlayerStatusViewHolder extends RecyclerView.ViewHolder {
        View mView;
        String uId;


        public PlayerStatusViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setUid(String uid){
            this.uId=uid;
        }
        public void setName(String name){
            TextView mNameView = (TextView)mView.findViewById(R.id.player_list_name);
            mNameView.setText(name);
        }
        public void setImagePath(Context context, String imagePath){
            CircleImageView mImageView = (CircleImageView)mView.findViewById(R.id.player_list_image);
            if(imagePath!=null)
              Glide.with(context).load(Uri.parse(imagePath)).placeholder(R.drawable.ic_person_black_24dp).dontAnimate().into(mImageView);
        }
        public void setStatus(Context context, String status){
            CircleImageView mImageView = (CircleImageView)mView.findViewById(R.id.player_list_image);

            if(status.equals(AppConstants.USER_ONLINE))
                mImageView.setBorderColor(Color.GREEN);
            else if(status.equals(AppConstants.USER_BUSY))
                 mImageView.setBorderColor(Color.RED);
            else
                mImageView.setBorderColor(Color.GRAY);

        }
        public void setPlay(Boolean searchForGame, Context context){
            FloatingActionButton imageButton = (FloatingActionButton)mView.findViewById(R.id.player_list_startGame);
                imageButton.setEnabled(searchForGame);
            if(!searchForGame)
                 imageButton.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.ButtonDisable)));
            else
                imageButton.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorAccent)));


        }
    }

}



