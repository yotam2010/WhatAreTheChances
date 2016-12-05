package com.hadas.yotam.whatarethechances;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Yotam on 16/11/2016.
 */

public class FindGameService extends Service {

    DatabaseReference mRoomsListRef;
    Boolean player1;
    ChildEventListener mRoomListener;
    CountDownTimer mPlayer2ReadyTimer;
    Boolean gameStarted;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service","SERVICE ------------ CREATED");
        //intalize releveant variables
        AppConstants.CURRENT_ROOM=null;
        startPlayer2Timer();
        gameStarted=false;
        player1=null;
        //if mDatabaseReference is not intalize then stop service
        if(AppConstants.mDatabaseReference==null)
            stopSelf();
        //get data for ordered list of rooms
        mRoomsListRef = AppConstants.mDatabaseReference.child(AppConstants.ROOMS);
        mRoomsListRef.child(AppConstants.MY_UID).removeValue();
        mRoomsListRef.orderByChild(AppConstants.PLAYER2_READY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if there are rooms get rooms list
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //get value if the second player in the room is already exist (if it does means room is full so skip to next room)
                        Boolean player2Ready = ds.child(AppConstants.PLAYER2_READY).getValue(Boolean.class);
                        Log.d("tag", ds.getKey() + " " + ds.getValue());
                        //if play 1 ID not equal to current ID (so the player wont play against him self) then check player 2 boolean
                        if(ds.child(AppConstants.PLAYER1ID).getValue(String.class)!=null &&
                                !ds.child(AppConstants.PLAYER1ID).getValue(String.class).equals( AppConstants.MY_UID))
                        if (player2Ready != null && !player2Ready) {
                            //set the room list to the current room
                            AppConstants.CURRENT_ROOM = ds.getKey();
                            mRoomsListRef= mRoomsListRef.child(AppConstants.CURRENT_ROOM);
                            //update the room's player 2 data and set player1 boolean to false
                            mRoomsListRef.child(AppConstants.PLAYER2_READY).setValue(true);
                            mRoomsListRef.child(AppConstants.PLAYER2_IMAGE).setValue(AppConstants.MY_PROFILE);
                            mRoomsListRef.child(AppConstants.PLAYER2_NAME).setValue(AppConstants.MY_NAME);
                            mRoomsListRef.child(AppConstants.PLAYER2ID).setValue(AppConstants.MY_UID);
                            player1=false;
                            mPlayer2ReadyTimer.start();
                        }
                    }
                }
                if(AppConstants.CURRENT_ROOM==null) {
                    //if CURRENT_ROOM equals null meaning it couldn't find a room so open a new 1
//                    mRoomsListRef = mRoomsListRef.push();
                    mRoomsListRef= mRoomsListRef.child(AppConstants.MY_UID);
                    AppConstants.CURRENT_ROOM = mRoomsListRef.getKey();
                    //set the room data and player1 boolean to true
                    mRoomsListRef.child(AppConstants.PLAYER2_READY).setValue(false);
                    mRoomsListRef.child(AppConstants.PLAYER1_READY).setValue(false);
                    mRoomsListRef.child(AppConstants.PLAYER1_IMAGE).setValue(AppConstants.MY_PROFILE);
                    mRoomsListRef.child(AppConstants.PLAYER1_NAME).setValue(AppConstants.MY_NAME);
                    mRoomsListRef.child(AppConstants.PLAYER1ID).setValue(AppConstants.MY_UID);
                    player1=true;
                }
                //set listener to the room
            setListeners();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            stopSelf();
            }
        });

    }
    public void startPlayer2Timer(){
         mPlayer2ReadyTimer = new CountDownTimer(15000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(mRoomListener!=null) {
                    mRoomsListRef.child(AppConstants.PLAYER1_READY).removeValue();
                    mRoomsListRef.removeEventListener(mRoomListener);
                    mRoomListener = null;
                    onCreate();
                }
            }
        };
    }

    public void startGame(){
        Intent intent = new Intent(getApplicationContext(), OnlineGame.class);
        intent.putExtra(AppConstants.PLAYER_TURN, player1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        gameStarted=true;
        startActivity(intent);
        stopSelf();
    }

    public void setListeners(){
        //intalize mRoomListener
        buildListener();
        //add the listener to the room reference
        mRoomsListRef.addChildEventListener(mRoomListener);

        if(player1==null){
            //if player1 boolean not intalize meaning there is a problem, stop the service
            stopSelf();
        }
    }

    public void buildListener(){
        mRoomListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //if PLAYER2_READY is set to true then set player1 ready to true
                if(Utilities.checkInternetConnection(getApplicationContext())) {

                    if (player1!=null&& player1 && dataSnapshot.getKey().equals(AppConstants.PLAYER2_READY) && dataSnapshot.getValue(Boolean.class)) {
                        mRoomsListRef.removeEventListener(mRoomListener);
                        mRoomListener=null;
                        mRoomsListRef.child(AppConstants.PLAYER1_READY).setValue(true);
                        startGame();
                    }
                    else {
                        //if both player1 and player2 are ready then stop service and start activity
                        if (player1!=null&&!player1&& dataSnapshot.getKey().equals(AppConstants.PLAYER1_READY) && dataSnapshot.getValue(Boolean.class)) {
                            mPlayer2ReadyTimer.cancel();
                            mPlayer2ReadyTimer=null;
                            startGame();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(mPlayer2ReadyTimer!=null){
                    mPlayer2ReadyTimer.cancel();
                    mPlayer2ReadyTimer=null;
                }
                mRoomsListRef.removeEventListener(mRoomListener);
                mRoomsListRef.removeValue();
                onCreate();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service","SERVICE ------------ STARTED");

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPlayer2ReadyTimer!=null){
            mPlayer2ReadyTimer.cancel();
            mPlayer2ReadyTimer=null;
        }
        if(mRoomListener!=null)
            mRoomsListRef.removeEventListener(mRoomListener);
        if(player1!=null && player1 && mRoomsListRef!=null && !gameStarted)
            mRoomsListRef.removeValue();
        Log.d("service","SERVICE ------------ DESTROYED");

    }
}
