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
    public static final String DEFAULT_PROFILE="https://firebasestorage.googleapis.com/v0/b/what-are-the-chances.appspot.com/o/ic_person_black_24dp.png?alt=media&token=269bc869-ec30-4cf1-8a41-b290afb2f937";
    public static final String QUESTION="QUESTION ";
    public static final String CATEGORY="CATEGORY";
    public static final String DIFFICULTY="DIFFICULTY";
    public static final String PUBLIC="PUBLIC";
    public static final String PRIVATE="PRIVATE";
    public static final String USERS_STATUS_LIST="USERS_STATUS_LIST";
    public static final String USER_STATUS="status";
    public static final String TEXT="TEXT";
    public static final String USERS="USERS";
    public static final String UID="UID";
    public static final String NAME="name";
    public static final String PROFILE_PIC="PROFILE_PIC";
    public static final String MATCHES_HISTORY="MATCHES_HISTORY";
    public static final String OPP_NAME ="OPP_NAME";
    public static final String OPP_PROF ="OPP_PROF";
    public static final String OPP_POINTS="OPP_POINTS";
    public static final String MY_POINTS="MY_POINTS";
    public static final String ROOMS="Rooms";
    public static final String DONE="DONE";
    public static final String PLAYER1ID="PLAYER1ID";
    public static final String PLAYER2ID ="PLAYER2ID";
    public static final String MESSAGES="MESSAGES";
    public static final String MESSAGE_NAME="messageName";
    public static final String MESSAGE_TEXT="MESSAGE_TEXT";
    public static final String GAME_DATE="GAME_DATE";
    public static final String POINTS1="POINTS1";
    public static final String POINTS2="POINTS2";
    public static final String PLAYER1_READY="PLAYER1_READY";
    public static final String PLAYER2_READY="PLAYER2_READY";
    public static final String TURN="TURN";
    public static final String PLAYER_TURN="PLAYER_TURN";
    public static final String GAME_QUESTION="GAME_QUESTION";
    public static final String RESPOND_PROOF="RESPOND_PROOF";
    public static final String RESPOND_TYPE="RESPOND_TYPE";
    public static final String RESPOND_NUMBER="RESPOND_NUMBER";
    public static final String RESPOND_RAND="RESPOND_RAND";
    public static final String ANSWER_ACCEPT="ANSWER_ACCEPT";
    public static final String USER_SET="USER_SET";
    public static final String QUESTION_ACCEPT="QUESTION_ACCEPT";
    public static final String USER_ONLINE="1Online";
    public static final String USER_BUSY="2Busy";
    public static final String USER_OFFLINE="3Offline";
    public static final String PLAYER1_IMAGE="PLAYER1_IMAGE";
    public static final String PLAYER2_IMAGE="PLAYER2_IMAGE";
    public static final String PLAYER1_NAME="PLAYER1_NAME";
    public static final String PLAYER2_NAME="PLAYER2_NAME";
    public static final String WAS_PLAYER1="WAS_PLAYER1";
    public static final String INVITES="INVITES";
    public static final String BLOCK="BLOCK";
    public static final String FRIENDS="FRIENDS";
    public static final String GPS_COORDINATES="GPS_COORDINATES";
    public static final String GAME_INVITE="GAME_INVITE";


    //Service static

    //Player respond media type
    public static final String TYPE_VIDEO="TYPE_VIDEO";
    public static final String TYPE_AUDIO="TYPE_AUDIO";
    public static final String TYPE_IMAGE="TYPE_IMAGE";
    //questions difficulties
    public static final String EASY="קל";
    public static final String MEDIUM="בינוני";
    public static final String HARD="קשה";

    //App Constants
    public static String MY_UID=null;
    public static String MY_NAME=null;
    public static String MY_PROFILE=null;
    public static String CURRENT_ROOM=null;


}
