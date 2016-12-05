package com.hadas.yotam.whatarethechances;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Yotam on 28/11/2016.
 */

public class UserService extends Service {
    public static Boolean mServiceRunning = false;
    Boolean isRunningService;
    ChildEventListener mInvitesListener;
    DatabaseReference mInvitesReference;
    Boolean firstRun;
    @Override
    public void onCreate() {
        super.onCreate();
        isRunningService=false;
        if(mServiceRunning || AppConstants.mDatabaseReference==null || AppConstants.MY_UID==null)
            stopSelf();

        mServiceRunning=true;
        isRunningService=true;

        mInvitesReference = AppConstants.mDatabaseReference.child(AppConstants.INVITES).child(AppConstants.MY_UID).child(AppConstants.INVITES);
        setListener();
        firstRun=true;
        mInvitesReference.addChildEventListener(mInvitesListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    private void setListener(){
        mInvitesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!firstRun){
                    Log.d("unique_tag",dataSnapshot.getKey());
                }
                firstRun=false;
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

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isRunningService)
            mServiceRunning=false;
        else{
            if(mInvitesListener!=null)
                mInvitesReference.removeEventListener(mInvitesListener);
        }
    }
}
