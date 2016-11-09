package com.hadas.yotam.whatarethechances;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

/**
 * Created by Yotam on 09/11/2016.
 */

public class AppSharedPreference {
    public static boolean isUserSet(Context context){
        SharedPreferences mSharedPreference = context.getSharedPreferences(AppConstants.SHARED_PREFERENCE_GLOBAL, Context.MODE_PRIVATE);
        return mSharedPreference.getBoolean(AppConstants.SHARED_PREFERENCE_USER_SET,false);
    }
    public static void userSet(Context context){
        SharedPreferences mSharedPreference = context.getSharedPreferences(AppConstants.SHARED_PREFERENCE_GLOBAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(AppConstants.SHARED_PREFERENCE_USER_SET,true);
        editor.commit();
    }
}
