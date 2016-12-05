package com.hadas.yotam.whatarethechances;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.TimeUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yotam on 03/12/2016.
 */

public class MatchesFragment extends Fragment {
    RecyclerView mMatchesRecycler;
    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    DatabaseReference mMatchesReference;
    TextView mNoItemsText;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.match_history));
        mMatchesRecycler = (RecyclerView)getActivity().findViewById(R.id.matches_recyclerView);
        mNoItemsText = (TextView) getActivity().findViewById(R.id.matches_no_items_text);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mMatchesRecycler.setLayoutManager(layoutManager);
        mMatchesReference = AppConstants.mDatabaseReference.child(AppConstants.USERS).child(AppConstants.MY_UID).child(AppConstants.MATCHES_HISTORY);
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Matches,MatchesHolder>(Matches.class,R.layout.match_view,MatchesHolder.class,mMatchesReference) {
            @Override
            protected void populateViewHolder(MatchesHolder viewHolder, Matches model, int position) {
                viewHolder.setView(model,getActivity());
            }

            @Override
            public void onViewAttachedToWindow(MatchesHolder holder) {
                super.onViewAttachedToWindow(holder);
                if(mFirebaseRecyclerAdapter.getItemCount()>0)
                    mNoItemsText.setVisibility(View.GONE);
                else
                    mNoItemsText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onViewDetachedFromWindow(MatchesHolder holder) {
                super.onViewDetachedFromWindow(holder);
                if(mFirebaseRecyclerAdapter.getItemCount()>0)
                    mNoItemsText.setVisibility(View.GONE);
                else
                    mNoItemsText.setVisibility(View.VISIBLE);
            }
        };
        mMatchesRecycler.setAdapter(mFirebaseRecyclerAdapter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.matches_history,container,false);
        return v;
    }

    public static class MatchesHolder extends RecyclerView.ViewHolder{
        View v;
        public MatchesHolder(View itemView) {
            super(itemView);
            v = itemView;
        }
        public void setView(Matches match, Context context){
            TextView playerName = (TextView) v.findViewById(R.id.matches_player_name);
            TextView playerWin = (TextView) v.findViewById(R.id.matches_player_win);
            TextView matchDate = (TextView) v.findViewById(R.id.matches_game_date);
            ImageView playerImage = (ImageView)v.findViewById(R.id.matches_player_image);

            ImageButton gpsButton = (ImageButton)v.findViewById(R.id.matches_gps_button);
            ImageButton addFriendButton = (ImageButton)v.findViewById(R.id.matches_add_friend_button);
            ImageButton playGameButton = (ImageButton)v.findViewById(R.id.matches_play_button);


            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(match.getGAME_DATE()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            matchDate.setText(simpleDateFormat.format(calendar.getTime()));

            playerName.setText(match.getname());
            if(match.getWAS_PLAYER1())
                 if(match.getPOINTS1()>match.getPOINTS2()) {
                     playerWin.setTextColor(Color.GREEN);
                     playerWin.setText("ניצחת!");
                 }else if(match.getPOINTS1()<match.getPOINTS2()) {
                     playerWin.setTextColor(Color.RED);
                     playerWin.setText("הפסדת");
                 }else{
                     playerWin.setTextColor(Color.BLUE);
                     playerWin.setText("תיקו");
                 }
            else{
                if(match.getPOINTS1()<match.getPOINTS2()) {
                    playerWin.setTextColor(Color.GREEN);
                    playerWin.setText("ניצחת!");
                }else if(match.getPOINTS1()>match.getPOINTS2()) {
                    playerWin.setTextColor(Color.RED);
                    playerWin.setText("הפסדת");
                }else{
                    playerWin.setTextColor(Color.BLUE);
                    playerWin.setText("תיקו");
                }
            }
            Glide.with(context).load(match.getPROFILE_PIC()).into(playerImage);
        }

    }
    public static class Matches{
        String name;
        String PROFILE_PIC;
        Boolean WAS_PLAYER1;
        int POINTS1;
        int POINTS2;
        long GAME_DATE;

        public Boolean getWAS_PLAYER1() {
            return WAS_PLAYER1;
        }

        public String getname() {
            return name;
        }

        public String getPROFILE_PIC() {
            return PROFILE_PIC;
        }

        public int getPOINTS1() {
            return POINTS1;
        }

        public int getPOINTS2() {
            return POINTS2;
        }

        public long getGAME_DATE() {
            return GAME_DATE;
        }

        public Matches() {
        }

    }
}
