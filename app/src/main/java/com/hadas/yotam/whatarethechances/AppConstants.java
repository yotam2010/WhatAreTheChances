package com.hadas.yotam.whatarethechances;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Yotam on 07/11/2016.
 */

public class AppConstants {
    //Firebase Constants

    public static FirebaseAuth mFirebaseAuth=null;
    public static FirebaseUser mFirebaseUser=null;
    public static FirebaseDatabase mFirebaseDatabase=null;
    public static DatabaseReference mDatabaseReference=null;
    public static FirebaseStorage mFirebaseStorage=null;
    public static StorageReference mStorageReference = null;

    public static final String QUESTIONS="QUESTIONS";
    public static final String QUESTION="QUESTION ";
    public static final String CATEGORY="CATEGORY";
    public static final String DIFFICULTY="DIFFICULTY";
    public static final String TEXT="TEX ";
    public static final String USERS="USERS";
    public static final String UID="UID";
    public static final String NAME="NAME";
    public static final String PROFILE_PIC="PROFILE_PIC";
    public static final String MATCHES="MATCHES";
    public static final String OPP_NAME ="OPP_NAME";
    public static final String OPP_PROF ="OPP_PROF";
    public static final String OPP_POINTS="OPP_POINTS";
    public static final String MY_POINTS="MY_POINTS";
    public static final String ROOMS="ROOMS";
    public static final String FULL="FULL";
    public static final String PLAYER1ID="PLAYER1ID";
    public static final String PLAYER2ID ="PLAYER2ID";
    public static final String MESSAGES="MESSAGES";
    public static final String MESSAGE_NAME="MESSAGE_NAME";
    public static final String MESSAGE_TEXT="MESSAGE_TEXT";
    public static final String GAME_DATE="GAME_DATA";
    public static final String POINTS1="POINTS1";
    public static final String POINTS2="POINTS2";
    public static final String TURN="TURN";
    public static final String PLAYER_TURN="PLAYER_TURN";
    public static final String GAME_QUESTION="GAME_QUESTION";
    public static final String RESPOND_PROOF="RESPOND_PROOF";
    public static final String RESPOND_NUMBER="RESPOND_NUMBER";
    public static final String RESPOND_RAND="RESPOND_RAND";
    public static final String ANSWER_ACCEPT="ANSWER_ACCEPT";
    public static final String QUESTION_ACCEPT="QUESTION_ACCEPT";

    //App Constants
    public static String MY_UID=null;
    public static String MY_NAME=null;
    public static String MY_PROFILE=null;

    //Shared Preference
    public static final String SHARED_PREFERENCE_GLOBAL="SHARED_PREFERENCE_GLOBAL";
    public static final String SHARED_PREFERENCE_USER_SET="SHARED_PREFERENCE_USER_SET";

}
