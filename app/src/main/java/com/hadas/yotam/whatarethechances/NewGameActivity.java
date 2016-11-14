package com.hadas.yotam.whatarethechances;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Yotam on 12/11/2016.
 */

public class NewGameActivity extends Fragment {
    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    RecyclerView mRecyclerView;
    MainActivity mActivity;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (MainActivity)getActivity();
        mActivity.setTitle(getActivity().getString(R.string.new_game));
        mRecyclerView = (RecyclerView)mActivity.findViewById(R.id.new_game_users_status);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PlayerStatus,PlayerStatusViewHolder>(PlayerStatus.class,R.layout.player_list_item,PlayerStatusViewHolder.class,mActivity.mUsersStatus) {
            @Override
            protected void populateViewHolder(PlayerStatusViewHolder viewHolder, PlayerStatus model, int position) {
            viewHolder.setName(model.name);
            viewHolder.setImagePath(getActivity().getApplicationContext(),model.imagePath);
            viewHolder.setStatus(mActivity.getApplicationContext(),model.status);
            viewHolder.setUid(model.getuId());
            }
        };
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
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
            Glide.with(context).load(Uri.parse(imagePath)).placeholder(R.drawable.ic_person_black_24dp).dontAnimate().into(mImageView);
        }
        public void setStatus(Context context, String status){
            ImageView mStatusView = (ImageView)mView.findViewById(R.id.player_list_status);
            if(status.equals(AppConstants.USER_ONLINE))
                mStatusView.setImageDrawable(context.getResources().getDrawable(R.drawable.online_user));
            else
                mStatusView.setImageDrawable(context.getResources().getDrawable(R.drawable.offline_user));
        }
    }

}



