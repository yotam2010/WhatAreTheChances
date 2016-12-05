package com.hadas.yotam.whatarethechances;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Yotam on 15/11/2016.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(AppConstants.mDatabaseReference!=null && AppConstants.MY_UID!=null && AppConstants.MY_PROFILE!=null && AppConstants.MY_NAME !=null){
            AppConstants.mDatabaseReference.child(AppConstants.USERS_STATUS_LIST).child(AppConstants.MY_UID).child(AppConstants.USER_STATUS).onDisconnect().setValue(AppConstants.USER_OFFLINE);
        }
    }
}
