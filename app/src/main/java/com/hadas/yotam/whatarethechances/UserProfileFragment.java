package com.hadas.yotam.whatarethechances;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Yotam on 09/11/2016.
 */

public class UserProfileFragment extends Fragment {
    private static final int REQUEST_READ_PERMISSIONS = 1;
    private static final int ACTIVITY_IMAGE_RESULT = 2;
    private String FRAGMENT_NAME;
    private String mImageLocation;
    private Uri mImageLocationUri;
    private CircleImageView mProfileView;
    private EditText mNameView;
    private TextView mNameText;
    private ImageView mEditButton;
    private Button mSaveButton;
    private FloatingActionButton mImagePicker;
    private MainActivity mActivity;
    private String IMAGE_UPLOAD_FAILURE;
    private String NO_NETWORK_CONNECTION;
    private String NAME_REQUIRE;
    public static String UPLOAD_SUCESS;


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
        mImageLocation=null;
        //get activity
        mActivity = (MainActivity)getActivity();
        //get activity strings
        IMAGE_UPLOAD_FAILURE = mActivity.getResources().getString(R.string.image_upload_failure);
        UPLOAD_SUCESS = getString(R.string.upload_sucess);
        NO_NETWORK_CONNECTION=mActivity.getString(R.string.no_network_connection);
        NAME_REQUIRE=mActivity.getString(R.string.name_require);
        //set activity title
        FRAGMENT_NAME = getActivity().getResources().getString(R.string.main_use_profile);
        mActivity.setTitle(FRAGMENT_NAME);
        //get fragment view
        mProfileView = (CircleImageView) mActivity.findViewById(R.id.user_profile_image);
        mImagePicker = (FloatingActionButton) mActivity.findViewById(R.id.user_profile_image_picker);
        mNameView = (EditText) mActivity.findViewById(R.id.user_profile_name);
        mNameText = (TextView) mActivity.findViewById(R.id.user_profile_name_textView);
        mEditButton = (ImageView) mActivity.findViewById(R.id.user_profile_name_edit_button);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showSave();
            }
        });

        mSaveButton = (Button) mActivity.findViewById(R.id.user_profile_save);

        if(mActivity.userSet){
            setUserData();
        }
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            saveImageToStorage();
            }
        });

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

    private void showSave(){
        String name = TextUtils.isEmpty(mNameText.getText().toString()) ? null : mNameText.getText().toString();
        mEditButton.setVisibility(View.GONE);
        mNameText.setVisibility(View.GONE);
        mNameView.setVisibility(View.VISIBLE);
        mSaveButton.setVisibility(View.VISIBLE);
        if(name!=null)
            mNameView.setText(name);
    }
    private void setUserData(){
        DatabaseReference mUserDataRead = AppConstants.mDatabaseReference.child(AppConstants.USERS).child(AppConstants.MY_UID);
        mUserDataRead.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(getActivity()==null)
                    return;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().equals(AppConstants.NAME))
                        mNameText.setText(ds.getValue(String.class));
                    else if(ds.getKey().equals(AppConstants.PROFILE_PIC)){
                        if(Utilities.checkInternetConnection(getActivity())) {
                            Glide.with(mActivity).load(ds.getValue()).placeholder(R.drawable.ic_person_black_24dp).dontAnimate().centerCrop().into(mProfileView);
                            mImageLocation = ds.getValue(String.class);
                        }
                        else
                            Utilities.fragmentToast(getContext(),NO_NETWORK_CONNECTION);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveImageToStorage() {
        final String name = mNameView.getText().toString().trim();
        //check if name field is null or empty, if it is dont save
        if(name==null|| TextUtils.isEmpty(name)) {
            Utilities.fragmentToast(mActivity,NAME_REQUIRE);
            return;
        }
        //get database reference to the user data
        final DatabaseReference mUserData = AppConstants.mDatabaseReference.child(AppConstants.USERS)
                .child(AppConstants.MY_UID);
        //if user picked image
        if(!Utilities.checkInternetConnection(getActivity())){
            Utilities.fragmentToast(getActivity(),NO_NETWORK_CONNECTION);
            return;
        }

            if (mImageLocationUri != null) {
            //show progress bar
            Utilities.showProgressDialog(getActivity(),true);
            FirebaseAuth.getInstance().getCurrentUser();
            //get profile file location
            StorageReference mImageUploadReference = AppConstants.mStorageReference.child(AppConstants.PROFILE_PIC)
                    .child(AppConstants.mFirebaseUser.getUid());
            //upload file
            mImageUploadReference.putFile(mImageLocationUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //upload successful
                    mUserData.child(AppConstants.PROFILE_PIC).setValue(taskSnapshot.getDownloadUrl().toString());
                    mUserData.child(AppConstants.NAME).setValue(name);
                    mUserData.child(AppConstants.USER_SET).setValue(true);
                    saveUserFinal(name,taskSnapshot.getDownloadUrl().toString());
                    mActivity.navigationView.setEnabled(true);
                    Utilities.fragmentToast(getActivity(),UPLOAD_SUCESS);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //faield to upload, document
                    Utilities.fragmentToast(mActivity, IMAGE_UPLOAD_FAILURE);
                    Log.d("tag", e.getMessage());
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    hideSave(name);

                    //when uplaod complete, eitehr fail or success, dismiss dialog
                    Utilities.dismissProgressDialog();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    long current = taskSnapshot.getBytesTransferred();
                    long total = taskSnapshot.getTotalByteCount();
                    int progress = (int)(((double)current/total)*100);
                    Utilities.dialogProgress(progress);
                    Log.d("tag",taskSnapshot.getBytesTransferred()+"/"+taskSnapshot.getTotalByteCount()+"   "+progress);
                }
            });

        }else{
            //if user didnt pick image, save the user data with the default image
                if(mImageLocation==null) {
                    mImageLocation=AppConstants.DEFAULT_PROFILE;
                    mUserData.child(AppConstants.PROFILE_PIC).setValue(mImageLocation);
                }
                else
                        mUserData.child(AppConstants.PROFILE_PIC).setValue(mImageLocation);

            hideSave(name);
            mActivity.navigationView.setEnabled(true);
            Utilities.fragmentToast(getActivity(),UPLOAD_SUCESS);

            }
    }

    public void hideSave(String name){
        mEditButton.setVisibility(View.VISIBLE);
        mNameText.setVisibility(View.VISIBLE);
        mSaveButton.setVisibility(View.GONE);
        mNameView.setVisibility(View.GONE);
        mNameView.setText(name);
    }
    public void saveUserFinal(String name, String profile){
        mActivity.userSet=true;
        Utilities.setFirebaseProfileConstants();
        NewGameActivity.PlayerStatus playerStatus = new NewGameActivity.PlayerStatus(name, AppConstants.MY_UID, profile, AppConstants.USER_ONLINE);
        mActivity.mUsersStatus.child(AppConstants.MY_UID).setValue(playerStatus);
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
            //get image uri
            mImageLocationUri = data.getData();
            //get image location to later uplaod to firebase
            mImageLocation = getImageLocation(mImageLocationUri);
            //make bitmap from teh file location
            Bitmap mImage = BitmapFactory.decodeFile(mImageLocation);
            //check image rotation
            mImage = imageExifInterface(mImageLocation,mImage);
            if(mImage!=null){
                mProfileView.setImageBitmap(mImage);
                if(mNameView.getVisibility()==View.GONE)
                    showSave();
            }
        }
    }

    private String getImageLocation(Uri uri) {
        String projection[]= {MediaStore.Images.ImageColumns.DATA};
        Cursor c = getActivity().getContentResolver().query(uri,projection,null,null,null);
        if(c.moveToFirst()){
            String myPic=c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            c.close();
            return myPic;
        }
        c.close();
        return null;
    }

    public static Bitmap imageExifInterface(String photoPath,Bitmap bitmap) {
        //ExifInterface get the image data, such as rotation
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(ei==null)
            return null;
        //if there is ExifInterface then get int value of the rotation
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        //check for each rotation possibility and return the new format bitmap
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return  rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }
        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        //start new matrix and set rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //create and return bitmap from the sourch with the new rotation
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }


}































