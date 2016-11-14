package com.hadas.yotam.whatarethechances;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.text.Format;

/**
 * Created by Yotam on 07/11/2016.
 */

public class SignUpFragment extends Fragment {

    //Toast strings
    public static String INVALID_PASSWORD;
    public static String INVALID_EMAIL;
    public static String EMPTY_TEXT_VIEW;

    TextView mExistUser;
    Button mSignUpButton;
    EditText mEmailView;
    EditText mPasswordView;
    LoginActivity mLoginActivity;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //get strings from xml
        INVALID_PASSWORD = getResources().getString(R.string.invalid_password);
        INVALID_EMAIL = getResources().getString(R.string.invalid_email);
        EMPTY_TEXT_VIEW = getResources().getString(R.string.empty_text);
        //set my Activity verb
        mLoginActivity = (LoginActivity)getActivity();
        //set new user text underline
        mExistUser = (TextView)getActivity().findViewById(R.id.sign_up_existing_user);
        mExistUser.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        //get sign in button
        mSignUpButton = (Button)getActivity().findViewById(R.id.sign_up_button);

        //get edit texts and set underline color
        mEmailView = (EditText)getActivity().findViewById(R.id.sign_up_email);
        mPasswordView = (EditText) getActivity().findViewById(R.id.sign_up_password);
        mEmailView.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        mPasswordView.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        //change fragment to Sign In fragment.
        mExistUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(android.R.id.content,new SignInFragment()).commit();
            }
        });

        // on Sign up button click, start sign up process
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get strings and trim spaces
                String email = mEmailView.getText().toString().trim();
                String password = mPasswordView.getText().toString().trim();
               if(!checkStrings(email,password))
                   return;

                Utilities.showProgressDialog(getActivity(),false);
                mSignUpButton.setEnabled(false);
                //attempt to sign up
                signUpAttempt(email,password);


            }
        });

    }
    public Boolean checkStrings(String email,String password){
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

    private void signUpAttempt(String email, String password){
        mLoginActivity.mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mSignUpButton.setEnabled(true);
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
        View v = inflater.inflate(R.layout.sign_up_fragment,container,false);
        return v;
    }
}
