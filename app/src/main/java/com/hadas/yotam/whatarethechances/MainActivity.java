package com.hadas.yotam.whatarethechances;

import android.animation.Animator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager mFragmentManager;
    int drawerSelected;
    NavigationView navigationView;
    DatabaseReference mUsersStatus;
    DatabaseReference mUserReference;
    Boolean userSet;
    Boolean connected;
    Boolean gameSearch;
    Intent searchServiceIntent;
    ImageView mImageLoader;
    Boolean setFragmentsBoolean;
    public static String NO_NETWORK_CONNECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragmentsBoolean=false;
        if(AppConstants.mFirebaseDatabase==null)
        {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           this.finish();
            startActivity(intent);
            return;
        }
        NO_NETWORK_CONNECTION= getString(R.string.no_network_connection);
        AppConstants.CURRENT_ROOM=null;
        drawerSelected=-1;
        connected=true;
        userSet=false;
        gameSearch=false;
        mImageLoader= (ImageView)findViewById(R.id.main_activity_load_image);
        Animation mLoaderAnimation = AnimationUtils.loadAnimation(this,R.anim.loader_animation);
        mImageLoader.startAnimation(mLoaderAnimation);
        mUsersStatus= AppConstants.mDatabaseReference.child(AppConstants.USERS_STATUS_LIST);
        mUsersStatus.keepSynced(true);
        mUserReference= AppConstants.mDatabaseReference.child(AppConstants.USERS).child(AppConstants.MY_UID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
         navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Utilities.checkInternetConnection(this);
        setUserDetails();
//        Intent intent = new Intent(this,UserService.class);
//        startService(intent);
    }



    public void setUserDetails(){

        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            Utilities.endLoaderAnimation(mImageLoader);

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(AppConstants.USER_SET)) {
                        if (ds.getValue(Boolean.class)) {
                            userSet = true;
                        }
                    } else if (ds.getKey().equals(AppConstants.NAME))
                        AppConstants.MY_NAME = ds.getValue(String.class);
                    else if (ds.getKey().equals(AppConstants.PROFILE_PIC)) {
                        AppConstants.MY_PROFILE = ds.getValue(String.class);
                    }
                }
                setFragmentsBoolean=true;
                setFragments();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setFragments(){
        try {
            if(userSet){
                navigationView.setCheckedItem(R.id.nav_new_game);
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment, new NewGameActivity()).commit();
                if(AppConstants.MY_NAME!=null)
                    setPlayerStatus(AppConstants.USER_ONLINE);
            }else{
                navigationView.setCheckedItem(R.id.nav_user_profile);
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment, new UserProfileFragment()).commit();
            }
            setFragmentsBoolean=false;
        }catch (Exception e){
            setFragmentsBoolean=true;
        }
    }
    public void startSearchService(){
        Log.d("service","SERVICE ------------ START");
         searchServiceIntent = new Intent(this,FindGameService.class);
        startService(searchServiceIntent);
    }

    public void stopSearchService(){
        Log.d("service","SERVICE ------------ STOP");
        if(searchServiceIntent!=null) {
            stopService(searchServiceIntent);
            searchServiceIntent=null;
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.menu_sign_out:
                if(mImageLoader.getVisibility()==View.VISIBLE) {
                    Utilities.activityToast(this,getString(R.string.toast_loading_data));
                    return true;
                }
                AlertDialog signOutDialog = new AlertDialog.Builder(MainActivity.this).setMessage(getString(R.string.alert_dialog_signOut)).setPositiveButton(R.string.respond_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setPlayerStatus(AppConstants.USER_OFFLINE);
                                stopSearchService();
                                Utilities.getAuth().signOut();
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                MainActivity.this.finish();
                                startActivity(intent);
                            }
                        }).setNegativeButton(R.string.respond_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
                signOutDialog.show();
                Button negativeButton = signOutDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(getResources().getColor(R.color.colorAccentDark));

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        if(!userSet){
            Utilities.activityToast(this,getString(R.string.set_account_error));
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        int id = item.getItemId();

        if(id!=drawerSelected) {
            if (id == R.id.nav_new_game) {
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment, new NewGameActivity()).commit();
            } else if (id == R.id.nav_points) {
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment, new MatchesFragment()).commit();
            }
//            else if (id == R.id.nav_questions) {
//
//            }
            else if (id == R.id.nav_user_profile) {
                mFragmentManager.beginTransaction().replace(R.id.main_activity_fragment, new UserProfileFragment()).commit();
            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }
        }
        drawerSelected=id;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(setFragmentsBoolean)
            setFragments();
        AppSharedPreference.setGameStarted(this,false);
        AppConstants.mDatabaseReference.child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Boolean.class))
                    if(!gameSearch)
                        connected=true;
                else
                      connected=false;
                if(AppConstants.MY_PROFILE!=null && AppConstants.MY_NAME !=null) {
                    setPlayerStatus(connected ? AppConstants.USER_ONLINE.toString() : AppConstants.USER_BUSY.toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setPlayerStatus(String status){
        if(AppConstants.MY_PROFILE!=null && AppConstants.MY_NAME !=null) {
            NewGameActivity.PlayerStatus playerStatus = new NewGameActivity.PlayerStatus(AppConstants.MY_NAME, AppConstants.MY_UID, AppConstants.MY_PROFILE, status);
            mUsersStatus.child(AppConstants.MY_UID).setValue(playerStatus);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        setPlayerStatus(AppConstants.USER_BUSY);
    }

    public void searchForGame(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(Fragment fragment : mFragmentManager.getFragments())
        fragment.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }
}
