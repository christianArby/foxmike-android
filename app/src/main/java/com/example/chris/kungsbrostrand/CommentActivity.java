package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserID;
    private EditText postMessage;
    private ImageButton postSendBtn;
    private ImageButton postAddBtn;
    private RecyclerView messagesListRV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private MessageFirebaseAdapter messageFirebaseAdapter;
    private DatabaseReference rootDbRef;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private Query messageQuery;

    private DatabaseReference userDbRef;

    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;

    private String postID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        postMessage = (EditText) findViewById(R.id.post_message_ET);
        postSendBtn = (ImageButton) findViewById(R.id.post_message_send_btn);
        postAddBtn = (ImageButton) findViewById(R.id.post_message_AddBtn);

        messagesListRV = (RecyclerView) findViewById(R.id.post_messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.post_message_swipe_layout);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);

        valueEventListenerMap = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // Get extra sent from previous activity
        postID = getIntent().getStringExtra("postID");

        userDbRef = rootDbRef.child("users").child(currentUserID);

        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final User currentUser = dataSnapshot.getValue(User.class);

                // When button is clicked send message to database
                postSendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage(currentUser.getName(), currentUser.getThumb_image());
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference messageRef = rootDbRef.child("postMessages").child(postID);
        messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        //messageAdapter = new MessageAdapter(messageList);
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messageQuery, Message.class)
                        .build();
        messageFirebaseAdapter = new MessageFirebaseAdapter(options, false);

        messageFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-1);
            }
        });

        messagesListRV.setAdapter(messageFirebaseAdapter);

        messageFirebaseAdapter.startListening();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                final int currentItems = messageFirebaseAdapter.getItemCount();

                messageFirebaseAdapter.stopListening();

                DatabaseReference messageRef = rootDbRef.child("postMessages").child(postID);
                messageQuery = messageRef.limitToLast(currentItems+TOTAL_ITEMS_TO_LOAD);

                FirebaseRecyclerOptions<Message> options =
                        new FirebaseRecyclerOptions.Builder<Message>()
                                .setQuery(messageQuery, Message.class)
                                .build();
                messageFirebaseAdapter = new MessageFirebaseAdapter(options, false);

                messageFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-currentItems);
                    }
                });

                messagesListRV.setAdapter(messageFirebaseAdapter);

                messageFirebaseAdapter.startListening();

                swipeRefreshLayout.setRefreshing(false);

            }
        });

    }

    private void sendMessage(String userName, String userThumbImage) {

        String message = postMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String messageID = rootDbRef.child("postMessages").child(postID).push().getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("seen", false);
            messageMap.put("senderUserID", currentUserID);
            messageMap.put("senderName", userName);
            messageMap.put("senderThumbImage", userThumbImage);

            rootDbRef.child("postMessages").child(postID).child(messageID).setValue(messageMap);

            postMessage.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            //User is signed out
            Intent loginIntent = new Intent(CommentActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }
}
