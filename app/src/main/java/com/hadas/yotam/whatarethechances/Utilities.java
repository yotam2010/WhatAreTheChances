package com.hadas.yotam.whatarethechances;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    public static void setStorageReference() {
        AppConstants.mFirebaseStorage = FirebaseStorage.getInstance();
        AppConstants.mStorageReference = AppConstants.mFirebaseStorage.getReference();
    }

    public static FirebaseStorage getStorage() {
        return AppConstants.mFirebaseStorage;
    }


    public static StorageReference getStorageReference() {
        return AppConstants.mStorageReference;
    }

    public static void setDatabaseReference() {
        AppConstants.mFirebaseDatabase = FirebaseDatabase.getInstance();
//        if(AppConstants.mDatabaseReference==null)
//             AppConstants.mFirebaseDatabase.setPersistenceEnabled(true);
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

    public static void showProgressDialog(Context context,boolean progressFlag){
        final String progressDialogString = context.getResources().getString(R.string.progress_dialog_string);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(progressDialogString);
        if(progressFlag)
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();
    }
    public static void dismissProgressDialog(){
        if(mProgressDialog!=null)
            mProgressDialog.cancel();
    }
    public static void dialogProgress(int progress){
        if(mProgressDialog!=null && mProgressDialog.isShowing())
            mProgressDialog.setProgress(progress);
    }
    public static boolean checkInternetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isConnected()){
            return true;
        }
        return false;
    }

    public static void setFirebaseProfileConstants(){
        DatabaseReference mRef = AppConstants.mDatabaseReference.child(AppConstants.USERS).child(AppConstants.MY_UID);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().equals(AppConstants.NAME))
                        AppConstants.MY_NAME=ds.getValue(String.class);
                    else if(ds.getKey().equals(AppConstants.PROFILE_PIC)) {
                        AppConstants.MY_PROFILE=ds.getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public static void endLoaderAnimation(final View animateView){
        animateView.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animateView.setVisibility(View.GONE);
                animateView.clearAnimation();
                animateView.setAlpha(1f);
                animateView.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }


}
