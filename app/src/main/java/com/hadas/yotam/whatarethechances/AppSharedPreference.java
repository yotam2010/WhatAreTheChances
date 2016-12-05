package com.hadas.yotam.whatarethechances;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

/**
 * Created by Yotam on 09/11/2016.
 */

public class AppSharedPreference {

    //Shared Preference
    public static final String SHARED_PREFERENCE_GLOBAL="SHARED_PREFERENCE_GLOBAL";
    public static final String STARTED_GAME_PREFERENCE="STARTED_GAME";


    public static void setGameStarted(Context context,boolean flag){
    SharedPreferences mSharedPreference = context.getSharedPreferences(SHARED_PREFERENCE_GLOBAL,Context.MODE_PRIVATE);
        mSharedPreference.edit().putBoolean(STARTED_GAME_PREFERENCE,flag).commit();
        }
    public static Boolean getGameStarted(Context context){
        SharedPreferences mSharedPreference = context.getSharedPreferences(SHARED_PREFERENCE_GLOBAL,Context.MODE_PRIVATE);
        return mSharedPreference.getBoolean(STARTED_GAME_PREFERENCE,false);
    }

}
