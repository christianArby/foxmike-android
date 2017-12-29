package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

public class ChatActivity extends AppCompatActivity {

    private String chatUserID;
    private String chatUserName;
    private String chatThumbImage;
    private Toolbar chatToolbar;
    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private EditText chatMessage;
    private ImageButton chatSendBtn;
    private ImageButton chatAddBtn;
    private RecyclerView messagesListRV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private DatabaseReference rootRefDb;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private Query messageQuery;

    private DatabaseReference usersChatUserIDDbRef;
    private DatabaseReference chatCurrentUserIDDbRef;
    private DatabaseReference userDbRef;

    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerMap;

    private ValueEventListener usersChatUserIDListener;
    private ValueEventListener chatCurrentUserIDListener;
    private ChildEventListener messageQueryChildListener1;
    private ChildEventListener messageQueryChildListener2;

    private int currentPage = 1;
    private int itemPos = 0;
    private String lastKey = "";
    private String prevKey = "";

    private String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        valueEventListenerMap = new HashMap<>();
        childEventListenerMap = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRefDb = FirebaseDatabase.getInstance().getReference();

        chatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        // Get extra sent from previous activity
        chatUserID = getIntent().getStringExtra("userID");
        chatUserName = getIntent().getStringExtra("userName");
        chatThumbImage = getIntent().getStringExtra("userThumbImage");
        chatID = getIntent().getStringExtra("chatID");
        if (chatID==null) {
            chatID = rootRefDb.child("chats").push().getKey();
        }

        usersChatUserIDDbRef= rootRefDb.child("users").child(chatUserID);
        chatCurrentUserIDDbRef = rootRefDb.child("chats").child(currentUserID);
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());



        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        titleView = (TextView) findViewById(R.id.custom_bar_name);
        lastSeenView = (TextView) findViewById(R.id.custom_bar_lastSeen);
        profileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        chatMessage = (EditText) findViewById(R.id.chat_message_ET);
        chatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        chatAddBtn = (ImageButton) findViewById(R.id.chatAddBtn);


        messageAdapter = new MessageAdapter(messageList);
        messagesListRV = (RecyclerView) findViewById(R.id.messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);
        messagesListRV.setAdapter(messageAdapter);


        // Set the ChatID to seen at current user
        //rootRefDb.child("chats").child(currentUserID).child(chatUserID).child("seen").setValue(true);

        // Load messages function
        loadMessages();

        titleView.setText(chatUserName);
        Glide.with(this).load(chatThumbImage).into(profileImage);

        // Listen to Online change of friend
        usersChatUserIDListener = usersChatUserIDDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User friend = dataSnapshot.getValue(User.class);

                if (friend.isOnline()) {
                    lastSeenView.setText("Online");

                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    String lastSeenText = getTimeAgo.getTimeAgo(friend.getLastSeen(), getApplicationContext());
                    lastSeenView.setText(lastSeenText);
                }

                valueEventListenerMap.put(dataSnapshot.getRef(), usersChatUserIDListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // see if current user has a chat with the friend already, if not add a chatmap (both to current users id in and friends

        // When button is clicked send message to database
        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
    }

    private void sendMessage() {

        String message = chatMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String messageID = rootRefDb.child("messages").push().getKey();

            String currentUserRef = "messages/" + currentUserID + "/" + chatUserID;
            String chatUserRef = "messages/" + chatUserID + "/" + currentUserID;
            DatabaseReference userMessageDbRef = rootRefDb.child("messages")
                    .child(currentUserID).child(chatUserID).push();
            String userMessagePushID = userMessageDbRef.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserID);

            rootRefDb.child("messages").child(chatID).child(messageID).setValue(messageMap);
            rootRefDb.child("chats").child(chatID).child("lastMessage").setValue(message);
            rootRefDb.child("chats").child(chatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            rootRefDb.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
            rootRefDb.child("chats").child(chatID).child("users").child(chatUserID).setValue(true);
            rootRefDb.child("chatMembers").child(chatID).child(currentUserID).setValue(true);
            rootRefDb.child("chatMembers").child(chatID).child(chatUserID).setValue(false);
            rootRefDb.child("users").child(currentUserID).child("chats").child(chatID).setValue(true);
            rootRefDb.child("users").child(chatUserID).child("chats").child(chatID).setValue(true);

            chatMessage.setText("");

            /*Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + userMessagePushID, messageMap);
            messageUserMap.put( chatUserRef + "/" + userMessagePushID, messageMap);



            rootRefDb.child("Chat").child(currentUserID).child(chatUserID).child("seen").setValue(true);
            rootRefDb.child("Chat").child(currentUserID).child(chatUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            rootRefDb.child("Chat").child(currentUserID).child(chatUserID).child("lastMessage").setValue(message);

            rootRefDb.child("Chat").child(chatUserID).child(currentUserID).child("seen").setValue(false);
            rootRefDb.child("Chat").child(chatUserID).child(currentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            rootRefDb.child("Chat").child(chatUserID).child(currentUserID).child("lastMessage").setValue(message);

            rootRefDb.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError!=null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });*/
        }
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = rootRefDb.child("messages").child(chatID);
        messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);
        messageQueryChildListener1 = messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();

                if(!prevKey.equals(messageKey)) {
                    messageList.add(itemPos++, message);
                } else {
                    prevKey = lastKey;
                }

                if(itemPos == 1) {
                    lastKey = messageKey;
                }

                messageAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                linearLayoutManager.scrollToPositionWithOffset(9,0);

                childEventListenerMap.put(dataSnapshot.getRef(), messageQueryChildListener1);

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
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = rootRefDb.child("messages").child(chatID);
        messageQuery = messageRef.limitToLast(currentPage*TOTAL_ITEMS_TO_LOAD);
        messageQueryChildListener2 = messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);

                itemPos++;

                if(itemPos == 1) {
                    lastKey = dataSnapshot.getKey();
                    prevKey = dataSnapshot.getKey();
                }

                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                messagesListRV.scrollToPosition(messageList.size()-1);
                swipeRefreshLayout.setRefreshing(false);

                childEventListenerMap.put(dataSnapshot.getRef(), messageQueryChildListener2);
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
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }

        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null) {
            // User is signed in
            userDbRef.child("online").setValue(true);

        } else {
            //User is signed out
            Intent loginIntent = new Intent(ChatActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userDbRef.child("online").setValue(false);
            userDbRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);

        }
    }
}
