package com.hadas.yotam.whatarethechances;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    RecyclerView mRecyclerView;
    EditText mChatInput;
    DatabaseReference mChatRef;
    ChildEventListener mRefListener;
    MediaPlayer mMediaPlayer;
    long openChatTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setTitle("");
        setChildListener();
        mMediaPlayer = MediaPlayer.create(this,R.raw.message);
        mChatInput = (EditText)findViewById(R.id.chat_input_edittext);
        mChatRef = AppConstants.mDatabaseReference.child(AppConstants.ROOMS).child(AppConstants.CURRENT_ROOM).child(AppConstants.MESSAGES);
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage,MessageHandler>(ChatMessage.class,R.layout.chat_recycler_view,MessageHandler.class,mChatRef) {
            @Override
            protected void populateViewHolder(MessageHandler viewHolder, ChatMessage model, int position) {
                viewHolder.setName(model.getMessageName());
                viewHolder.setMessage(model.getMessage());
            }

        };

        mRecyclerView = (RecyclerView) findViewById(R.id.chat_body_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
        mChatInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(!TextUtils.isEmpty(v.getText().toString().trim()))
                   if(actionId == EditorInfo.IME_ACTION_SEND){
                    Calendar cal = Calendar.getInstance();
                    ChatMessage message = new ChatMessage(v.getText().toString(), String.valueOf(cal.getTimeInMillis()),AppConstants.MY_NAME);
                    mChatRef.push().setValue(message);
                    v.setText("");
                    mRecyclerView.smoothScrollToPosition(mFirebaseRecyclerAdapter.getItemCount());
                }
                return false;
            }
        });
    }

    public void setChildListener(){
        mRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(Long.valueOf(dataSnapshot.child("time").getValue(String.class))>openChatTime)
                if(!dataSnapshot.child(AppConstants.MESSAGE_NAME).getValue(String.class).equals(AppConstants.MY_NAME))
                    mMediaPlayer.start();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        openChatTime=Calendar.getInstance().getTimeInMillis();
        mChatRef.addChildEventListener(mRefListener);
        OnlineGame.chatOpen=true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        OnlineGame.chatOpen=false;
        OnlineGame.closeChatTime=Calendar.getInstance().getTimeInMillis();
        mChatRef.removeEventListener(mRefListener);
    }

    public static class MessageHandler extends RecyclerView.ViewHolder{
        View v;
        public MessageHandler(View itemView) {
            super(itemView);
            v=itemView;
        }
        public void setName(String s){
            TextView mNameView = (TextView)v.findViewById(R.id.chat_view_name);
            mNameView.setText(s);
        }
        public void setMessage(String s){
            TextView mMessageView = (TextView)v.findViewById(R.id.chat_view_message);
            mMessageView.setText(s);
        }
    }

   public static class ChatMessage{
        private String message;
        private String time;
        private String messageName;

        public ChatMessage() {
        }

        public ChatMessage(String message, String time, String messageName) {
            this.message = message;
            this.time = time;
            this.messageName = messageName;
        }

        public String getMessage() {
            return message;
        }

       public String getTime() {
            return time;
        }

       public String getMessageName() {
            return messageName;
        }
    }


}


