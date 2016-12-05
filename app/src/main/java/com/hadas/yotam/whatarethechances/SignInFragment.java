package com.hadas.yotam.whatarethechances;

import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by Yotam on 07/11/2016.
 */

public class SignInFragment extends Fragment {

    //widgets
    TextView mNewUser;
    Button mSignInButton;
    EditText mEmailView;
    EditText mPasswordView;
    SignInButton mGoogleSignInButton;
    //activity
    LoginActivity mLoginActivity;
    //google activity resault ID
    private static final int mGoogleUniqueId=1;

    //Toast strings
    public static String INVALID_PASSWORD;
    public static String INVALID_EMAIL;
    public static String EMPTY_TEXT_VIEW;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoginActivity = (LoginActivity)getActivity();

        //get strings from xml
        INVALID_PASSWORD = getResources().getString(R.string.invalid_password);
        INVALID_EMAIL = getResources().getString(R.string.invalid_email);
        EMPTY_TEXT_VIEW = getResources().getString(R.string.empty_text);

        //set new user text underline
        mNewUser = (TextView)getActivity().findViewById(R.id.sign_in_new_user);
        mNewUser.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        //get sign in button
        mSignInButton = (Button)getActivity().findViewById(R.id.sign_in_button);
        //get Google sign in button
        mGoogleSignInButton = (SignInButton)getActivity().findViewById(R.id.google_sign_in);
        //get edit texts and set underline color
        mEmailView = (EditText)getActivity().findViewById(R.id.sign_in_email);
        mPasswordView = (EditText) getActivity().findViewById(R.id.sign_in_password);
        mEmailView.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        mPasswordView.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        //change fragment to Sign Up fragment
        mNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    getFragmentManager().beginTransaction().replace(android.R.id.content,new SignUpFragment()).commit();

            }
        });

        //Attempt sign in
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get strings and trim spaces
                String email = mEmailView.getText().toString().trim();
                String password = mPasswordView.getText().toString().trim();

                //check strings
                if(!checkStrings(email,password))
                    return;
                Utilities.showProgressDialog(getActivity(),false);
                mSignInButton.setEnabled(false);
                //attempt to sign up
                signInAttempt(email,password);
            }
        });

        //Attempt google sign in
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoginActivity.googleSignIn){
                    Intent intent = Auth.GoogleSignInApi.getSignInIntent(mLoginActivity.mGoogleApiClient);
                    startActivityForResult(intent,mGoogleUniqueId);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if the resault are for google sign in
        if(requestCode==mGoogleUniqueId){
            //get the resaults from the intent
            GoogleSignInResult mGoogleSignInResault = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //check if resault equal success
            if(mGoogleSignInResault.isSuccess()){
                //get google sign in account
                GoogleSignInAccount mGoogleSignInAccount = mGoogleSignInResault.getSignInAccount();
                //handle the account with firebase
                googleSignInFirebase(mGoogleSignInAccount);
            }
        }
    }

    private void googleSignInFirebase(GoogleSignInAccount mAccount){
        Utilities.showProgressDialog(getActivity(),false);
        AuthCredential mAuthCredential = GoogleAuthProvider.getCredential(mAccount.getIdToken(),null);
        mLoginActivity.mAuth.signInWithCredential(mAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()) {
                    Log.d("tag", "Message:      " + task.getException().getMessage());
                    Utilities.fragmentToast(getContext().getApplicationContext(), task.getException().getMessage());
                }
                Utilities.dismissProgressDialog();
            }
        });
    }

    public Boolean checkStrings(String email, String password){
        //check if the strings arent empty
        if(TextUtils.isEmpty(password)||TextUtils.isEmpty(email)){
            Utilities.fragmentToast(getContext().getApplicationContext(),EMPTY_TEXT_VIEW);
            return false;
        }
        //check if email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Utilities.fragmentToast(getContext().getApplicationContext(),INVALID_EMAIL);
            return false;
        }

        //check if password is valid
        if(!Utilities.stringCheck(password) ||password.length()<6){
            Utilities.fragmentToast(getContext().getApplicationContext(),INVALID_PASSWORD);
            return false;
        }
        return true;
    }

    private void signInAttempt(String email,String password){
        mLoginActivity.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mSignInButton.setEnabled(true);
                Utilities.dismissProgressDialog();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("tag","Message:      "+e.getMessage());
                Utilities.fragmentToast(getContext().getApplicationContext(),e.getMessage());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sign_in_fragment,container,false);
        return v;
    }
}
