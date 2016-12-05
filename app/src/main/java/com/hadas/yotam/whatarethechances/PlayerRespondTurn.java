package com.hadas.yotam.whatarethechances;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.camera2.CameraDevice;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.media.session.MediaControllerCompat;
//import android.support.v4.os.EnvironmentCompat;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Yotam on 25/11/2016.
 */

public class PlayerRespondTurn extends Fragment {

    TextView waitingForOpponetText;
    TextView questionText;
    TextView questionDifficulty;
    LinearLayout mButtonsLayout;
    OnlineGame mOnlineGame;
    LinearLayout mMediaLayout;
    Button acceptedChallengButton;
    Button declinedChallengButton;
    ImageButton mVideoMediaButton;
    ImageButton mPictureMediaButton;
    ImageButton mAudioMediaButton;
    Boolean gotQuestion;
    StorageReference mRoomMedia;
    Animation waitingAnimation;
    public static final int VIDEO_REQUEST_CODE=1;
    public static final int IMAGE_REQUEST_CODE=2;
    public static final int AUDIO_REQUEST_CODE=3;
    MediaRecorder mAudioRecorder;
    MediaPlayer mAudioPlayer;
    LinearLayout mAudioLayout;
    Button mAudioSendButton;
    FloatingActionButton mAudioRecordButton;
    SeekBar mAudioSeekBar;
    String audioFile;
    CountDownTimer audioTimer;
    String tempQuestion=null;
    String tempDifficulty=null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gotQuestion=false;
        audioFile=null;
        mRoomMedia = AppConstants.mStorageReference.child(AppConstants.ROOMS).child(AppConstants.CURRENT_ROOM).child(AppConstants.RESPOND_PROOF);
        mAudioLayout = (LinearLayout) getActivity().findViewById(R.id.player_respond_audio_layout);
        mAudioRecordButton = (FloatingActionButton) getActivity().findViewById(R.id.player_respond_audio_recorder);

        mAudioRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    if(mAudioRecorder==null){
                        mAudioRecordButton.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                        mAudioRecorder = new MediaRecorder();
                        audioFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/whatAreTheChances"+Calendar.getInstance().getTimeInMillis()+".3gp";
                        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mAudioRecorder.setOutputFile(audioFile);
                        mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        try {
                            mAudioRecorder.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mAudioRecorder.start();
                        return true;
                    }
                }
                if(event.getAction()==MotionEvent.ACTION_UP) {
                        if(mAudioRecorder!=null){
                            mAudioRecordButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                            try {
                                mAudioRecorder.stop();
                                mAudioRecorder.release();
                            }catch (RuntimeException e){

                            }

                            mAudioRecorder=null;
                            if(mAudioPlayer!=null) {
                                mAudioPlayer.stop();
                                mAudioPlayer.release();
                            }
                            mAudioPlayer  = new MediaPlayer();
                            try {
                                mAudioPlayer.setDataSource(audioFile);
                                mAudioPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mAudioSeekBar.setMax(mAudioPlayer.getDuration());
                            int timerDuration  = mAudioPlayer.getDuration();
                            setTimer(timerDuration);
                            mAudioPlayer.start();
                        }

                }

                return true;
            }
        });
        mAudioSendButton = (Button) getActivity().findViewById(R.id.player_respond_send_audio);
        mAudioSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioFile==null || mAudioPlayer.getDuration()<100) {
                    Utilities.activityToast(getActivity(),getString(R.string.error_no_audio));
                    return;
                }
                Utilities.showProgressDialog(getActivity(), true);
                Uri file = Uri.fromFile(new File(audioFile));
                mRoomMedia.putFile(file).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((double) ((double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount()) * 100);
                        Utilities.dialogProgress(progress);
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Utilities.dismissProgressDialog();
                        if (task.isSuccessful()) {
                            mMediaLayout.setVisibility(View.GONE);
                            DatabaseReference mRoomRef = mOnlineGame.mRoomRef;
                            HashMap<String, Object> values = new HashMap();
                            values.put(AppConstants.RESPOND_PROOF, task.getResult().getDownloadUrl().toString());
                            values.put(AppConstants.RESPOND_TYPE, AppConstants.TYPE_AUDIO);
                            mRoomRef.child(AppConstants.RESPOND_NUMBER).updateChildren(values);
                            Utilities.fragmentToast(getActivity(), getString(R.string.upload_complete));
                            waitingForOpponetText.setText(R.string.waiting_for_opponet_acceptDecline);
                            waitingForOpponetText.setVisibility(View.VISIBLE);
                            waitingForOpponetText.startAnimation(waitingAnimation);
                            mAudioLayout.setVisibility(View.GONE);
                        } else {
                            Utilities.fragmentToast(getActivity(), getString(R.string.video_upload_failure));
                        }

                    }
                });

            }
        });
        mAudioSeekBar = (SeekBar) getActivity().findViewById(R.id.player_respond_audio_seek);
        mAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && mAudioPlayer!=null){
                mAudioPlayer.seekTo(progress);
                int timerDuration  = mAudioPlayer.getDuration();
                timerDuration-= progress;
                setTimer(timerDuration);
                mAudioPlayer.start();
            }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                mAudioPlayer.stop();
                if(mAudioPlayer!=null) {
                    mAudioPlayer.pause();
                    audioTimer.cancel();
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        acceptedChallengButton = (Button) getActivity().findViewById(R.id.player_respond_accept_question);
        acceptedChallengButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptedQuestion();
            }
        });
        declinedChallengButton = (Button) getActivity().findViewById(R.id.player_respond_decline_question);
        declinedChallengButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declinedQuestion();
            }
        });
        questionText = (TextView)getActivity().findViewById(R.id.opponet_question_text);
        questionDifficulty = (TextView)getActivity().findViewById(R.id.opponet_question_difficulty);
        mButtonsLayout = (LinearLayout) getActivity().findViewById(R.id.player_respond_buttons_layout);
        mMediaLayout = (LinearLayout) getActivity().findViewById(R.id.player_respond_media_layout);
        mOnlineGame = (OnlineGame)getActivity();
         waitingAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.waiting_for_opponet_animation);
        waitingForOpponetText = (TextView)getActivity().findViewById(R.id.waiting_for_opponet);
        waitingForOpponetText.startAnimation(waitingAnimation);
        mVideoMediaButton = (ImageButton)getActivity().findViewById(R.id.respond_video_button);

        mVideoMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION_CODES.M<=Build.VERSION.SDK_INT){
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA},VIDEO_REQUEST_CODE);
                    }else{
                        videoMedia();
                    }
                }else{
                    videoMedia();
                }
            }
        });

        mPictureMediaButton = (ImageButton)getActivity().findViewById(R.id.respond_picture_button);

        mPictureMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION_CODES.M<=Build.VERSION.SDK_INT){
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA},IMAGE_REQUEST_CODE);
                    }else{
                        imageMedia();
                    }
                }else{
                    imageMedia();
                }
            }
        });

        mAudioMediaButton = (ImageButton)getActivity().findViewById(R.id.respond_audio_button);

        mAudioMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION_CODES.M<=Build.VERSION.SDK_INT){
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},AUDIO_REQUEST_CODE);
                    }else{
                        audioMedia();
                    }
                }else{
                    audioMedia();
                }
            }
        });

        if(tempDifficulty!=null && tempQuestion!=null){
            setQuestion(tempQuestion,tempDifficulty);
            tempDifficulty=null;
            tempQuestion=null;
        }

    }

    public void setTimer(int duration){
        if(audioTimer!=null){
            audioTimer.cancel();
            audioTimer=null;
        }
        mAudioSeekBar.setVisibility(View.VISIBLE);
        audioTimer = new CountDownTimer(duration,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                mAudioSeekBar.setProgress((int)((mAudioPlayer.getDuration())-millisUntilFinished));
            }

            @Override
            public void onFinish() {
                mAudioSeekBar.setProgress(mAudioSeekBar.getMax());
            }
        }.start();
    }
    interface playerRespond{
        public void sendPlayerRespond(Boolean accepted);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_respond_turn,container,false);
        return v;
    }
    public void setQuestion(String question,String difficulty){
        if(gotQuestion!=null && !gotQuestion) {
            gotQuestion = true;
            questionText.setText(question);
            questionDifficulty.setText(difficulty);
            switch (difficulty) {
                case AppConstants.EASY:
                    questionDifficulty.setTextColor(getResources().getColor(R.color.greenDifficulty));
                    break;
                case AppConstants.MEDIUM:
                    questionDifficulty.setTextColor(getResources().getColor(R.color.yellowDifficulty));
                    break;
                case AppConstants.HARD:
                    questionDifficulty.setTextColor(getResources().getColor(R.color.redDifficulty));
                    break;
            }
            waitingForOpponetText.clearAnimation();
            waitingForOpponetText.setVisibility(View.GONE);
            mButtonsLayout.setVisibility(View.VISIBLE);
        }else{
            tempQuestion=question;
            tempDifficulty=difficulty;
        }
    }

    public void acceptedQuestion(){
        mOnlineGame.sendPlayerRespond(true);
        mMediaLayout.setVisibility(View.VISIBLE);
        mButtonsLayout.setVisibility(View.GONE);
    }

    public void declinedQuestion(){
        mOnlineGame.sendPlayerRespond(false);
    }

    private void videoMedia(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,30);
        long maxVideoSize = 10*1024*1024; // 10 MB
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
        if(intent.resolveActivity(getActivity().getPackageManager())!=null)
            startActivityForResult(intent,VIDEO_REQUEST_CODE);
    }
    private void imageMedia(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getActivity().getPackageManager())!=null)
            startActivityForResult(intent,IMAGE_REQUEST_CODE);
    }
    private void audioMedia(){

        mAudioLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults!=null && grantResults[0]==PackageManager.PERMISSION_GRANTED && requestCode==VIDEO_REQUEST_CODE){
            videoMedia();
            }else
                if(grantResults!=null && grantResults[0]==PackageManager.PERMISSION_GRANTED && requestCode==IMAGE_REQUEST_CODE){
                    imageMedia();
                }
            else
                if(grantResults!=null && grantResults[0]==PackageManager.PERMISSION_GRANTED && requestCode==AUDIO_REQUEST_CODE){
                    audioMedia();
                }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("tag","requestCode: "+requestCode+" resultCode: "+resultCode+"   ok: "+getActivity().RESULT_OK);
        if(resultCode==getActivity().RESULT_OK){
            if(requestCode==VIDEO_REQUEST_CODE) {
                final Uri mVideoUri = data.getData();
                Utilities.showProgressDialog(getActivity(), true);

                mRoomMedia.putFile(mVideoUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((double) ((double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount()) * 100);
                        Utilities.dialogProgress(progress);
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Utilities.dismissProgressDialog();
                        if (task.isSuccessful()) {
                            mMediaLayout.setVisibility(View.GONE);
                            DatabaseReference mRoomRef = mOnlineGame.mRoomRef;
                            HashMap<String, Object> values = new HashMap();
                            values.put(AppConstants.RESPOND_PROOF, task.getResult().getDownloadUrl().toString());
                            values.put(AppConstants.RESPOND_TYPE, AppConstants.TYPE_VIDEO);
                            mRoomRef.child(AppConstants.RESPOND_NUMBER).updateChildren(values);
                            Utilities.fragmentToast(getActivity(), getString(R.string.upload_complete));
                        } else {
                            Utilities.fragmentToast(getActivity(), getString(R.string.video_upload_failure));
                        }
                        waitingForOpponetText.setText(R.string.waiting_for_opponet_acceptDecline);
                        waitingForOpponetText.setVisibility(View.VISIBLE);
                        waitingForOpponetText.startAnimation(waitingAnimation);

                    }
                });
            }else
                if(requestCode==AUDIO_REQUEST_CODE){
                    Uri imageData = data.getData();
                    Utilities.showProgressDialog(getActivity(), true);

                    mRoomMedia.putFile(imageData).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) ((double) ((double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount()) * 100);
                            Utilities.dialogProgress(progress);
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Utilities.dismissProgressDialog();
                            if (task.isSuccessful()) {
                                mMediaLayout.setVisibility(View.GONE);
                                DatabaseReference mRoomRef = mOnlineGame.mRoomRef;
                                HashMap<String, Object> values = new HashMap();
                                values.put(AppConstants.RESPOND_PROOF, task.getResult().getDownloadUrl().toString());
                                values.put(AppConstants.RESPOND_TYPE, AppConstants.TYPE_IMAGE);
                                mRoomRef.child(AppConstants.RESPOND_NUMBER).updateChildren(values);
                                Utilities.fragmentToast(getActivity(), getString(R.string.upload_complete));
                            } else {
                                Utilities.fragmentToast(getActivity(), getString(R.string.video_upload_failure));
                            }
                            waitingForOpponetText.setText(R.string.waiting_for_opponet_acceptDecline);
                            waitingForOpponetText.setVisibility(View.VISIBLE);
                            waitingForOpponetText.startAnimation(waitingAnimation);

                        }
                    });

                }
                else
                if(requestCode==IMAGE_REQUEST_CODE){
                    Uri imageData = data.getData();
                    Utilities.showProgressDialog(getActivity(), true);

                    mRoomMedia.putFile(imageData).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) ((double) ((double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount()) * 100);
                            Utilities.dialogProgress(progress);
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Utilities.dismissProgressDialog();
                            if (task.isSuccessful()) {
                                mMediaLayout.setVisibility(View.GONE);
                                DatabaseReference mRoomRef = mOnlineGame.mRoomRef;
                                HashMap<String, Object> values = new HashMap();
                                values.put(AppConstants.RESPOND_PROOF, task.getResult().getDownloadUrl().toString());
                                values.put(AppConstants.RESPOND_TYPE, AppConstants.TYPE_IMAGE);
                                mRoomRef.child(AppConstants.RESPOND_NUMBER).updateChildren(values);
                                Utilities.fragmentToast(getActivity(), getString(R.string.upload_complete));
                            } else {
                                Utilities.fragmentToast(getActivity(), getString(R.string.video_upload_failure));
                            }
                            waitingForOpponetText.setText(R.string.waiting_for_opponet_acceptDecline);
                            waitingForOpponetText.setVisibility(View.VISIBLE);
                            waitingForOpponetText.startAnimation(waitingAnimation);

                        }
                    });

                }
        }
    }
}
