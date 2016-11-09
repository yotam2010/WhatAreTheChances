package com.hadas.yotam.whatarethechances;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.internal.GamesContract;
import com.google.android.gms.plus.model.people.Person;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Yotam on 09/11/2016.
 */

public class UserProfileFragment extends Fragment {
    private String FRAGMENT_NAME;
    private String mImageLocation;
    private CircleImageView mProfileView;
    private EditText mNameView;
    private Button mSaveButton;
    private FloatingActionButton mImagePicker;
    private static final int REQUEST_READ_PERMISSIONS = 1;
    private static final int ACTIVITY_IMAGE_RESULT = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //set fragment's layout
        View v = inflater.inflate(R.layout.user_profile, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //set activity title
        FRAGMENT_NAME = getActivity().getResources().getString(R.string.main_use_profile);
        getActivity().setTitle(FRAGMENT_NAME);
        //get fragment view
        mProfileView = (CircleImageView) getActivity().findViewById(R.id.user_profile_image);
        mImagePicker = (FloatingActionButton) getActivity().findViewById(R.id.user_profile_image_picker);
        mNameView = (EditText) getActivity().findViewById(R.id.user_profile_name);
        mSaveButton = (Button) getActivity().findViewById(R.id.user_profile_save);

        mImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if permission needed
                int premissionRequired = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (premissionRequired != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSIONS);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, ACTIVITY_IMAGE_RESULT);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, ACTIVITY_IMAGE_RESULT);
            } else {
                Utilities.fragmentToast(getContext(), getActivity().getString(R.string.permission_read_denied));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_IMAGE_RESULT && resultCode == getActivity().RESULT_OK) {
            Uri uri = data.getData();
            mImageLocation = getImageLocation(uri);
            if(mImageLocation!=null){
                mProfileView.setImageURI(Uri.parse(mImageLocation));
            }
        }
    }

    private String getImageLocation(Uri uri) {
        String projection[]= {MediaStore.Images.ImageColumns.DATA};
        Cursor c = getActivity().getContentResolver().query(uri,projection,null,null,null);
        if(c.moveToFirst()){
            return c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
        }
        return null;
    }


}































