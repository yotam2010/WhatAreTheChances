package com.hadas.yotam.whatarethechances;

import android.app.ProgressDialog;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Yotam on 07/11/2016.
 */

public class Utilities {

    private static ProgressDialog mProgressDialog;

    public static void setUserAuth() {
        AppConstants.mFirebaseAuth = FirebaseAuth.getInstance();
        AppConstants.mFirebaseUser = AppConstants.mFirebaseAuth.getCurrentUser();
    }

    public static FirebaseUser getUser() {
        return AppConstants.mFirebaseUser;
    }


    public static FirebaseAuth getAuth() {
        return AppConstants.mFirebaseAuth;
    }

    public static void setDatabaseReference() {
        AppConstants.mFirebaseDatabase = FirebaseDatabase.getInstance();
        AppConstants.mDatabaseReference = AppConstants.mFirebaseDatabase.getReference();
    }

    public static FirebaseDatabase getDatabase() {
        return AppConstants.mFirebaseDatabase;
    }


    public static DatabaseReference getReference() {
        return AppConstants.mDatabaseReference;
    }

    public static boolean stringCheck(String s){
        return s.matches("[-\\p{Alnum}]+");
    }
    public static void fragmentToast(Context context,String s){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
    public static void activityToast(Context context,String s){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }

    public static void showProgressDialog(Context context){
        final String progressDialogString = context.getResources().getString(R.string.progress_dialog_string);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(progressDialogString);
        mProgressDialog.show();
    }
    public static void dismissProgressDialog(){
        if(mProgressDialog!=null)
            mProgressDialog.cancel();
    }
    public static boolean checkInternetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isConnected()){
            return true;
        }
        return false;
    }


}
