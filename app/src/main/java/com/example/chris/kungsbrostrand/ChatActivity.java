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
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
    private MessageFirebaseAdapter messageFirebaseAdapter;
    private DatabaseReference rootDbRef;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private Query messageQuery;

    private DatabaseReference userDbRef;
    private DatabaseReference friendDbRef;

    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;

    private ValueEventListener usersChatUserIDListener;

    private String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        chatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        titleView = (TextView) findViewById(R.id.custom_bar_name);
        lastSeenView = (TextView) findViewById(R.id.custom_bar_lastSeen);
        profileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        chatMessage = (EditText) findViewById(R.id.chat_message_ET);
        chatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        chatAddBtn = (ImageButton) findViewById(R.id.chatAddBtn);

        messagesListRV = (RecyclerView) findViewById(R.id.messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);

        valueEventListenerMap = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // Get extra sent from previous activity
        chatUserID = getIntent().getStringExtra("userID");
        chatUserName = getIntent().getStringExtra("userName");
        chatThumbImage = getIntent().getStringExtra("userThumbImage");
        chatID = getIntent().getStringExtra("chatID");

        titleView.setText(chatUserName);
        Glide.with(this).load(chatThumbImage).into(profileImage);

        userDbRef = rootDbRef.child("users").child(currentUserID);
        friendDbRef = rootDbRef.child("users").child(chatUserID);

        // see if current user has a chat with the friend already, add listener to send message button
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User currentUser = dataSnapshot.getValue(User.class);

                friendDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User friendUser = dataSnapshot.getValue(User.class);

                        if (chatID==null) {
                            if (currentUser.getChats()!=null) {
                                for (String userChatID : currentUser.getChats().keySet()) {
                                    if (friendUser.getChats()!= null) {
                                        if (friendUser.getChats().containsKey(userChatID)) {
                                            chatID = userChatID;
                                        }
                                    }
                                }
                            }
                            if (chatID==null) {
                                chatID = rootDbRef.child("chats").push().getKey();
                            }
                        }

                        // --------------- chatID SET ----------------

                        DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                        messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

                        //messageAdapter = new MessageAdapter(messageList);
                        FirebaseRecyclerOptions<Message> options =
                                new FirebaseRecyclerOptions.Builder<Message>()
                                        .setQuery(messageQuery, Message.class)
                                        .build();
                        messageFirebaseAdapter = new MessageFirebaseAdapter(options);

                        messageFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                super.onItemRangeInserted(positionStart, itemCount);
                                messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-1);
                            }
                        });

                        messagesListRV.setAdapter(messageFirebaseAdapter);

                        // When button is clicked send message to database
                        chatSendBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessage(currentUser.getName(), currentUser.getThumb_image());
                            }
                        });

                        messageFirebaseAdapter.startListening();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Listen to Online change of friend
        usersChatUserIDListener = rootDbRef.child("presence").child(chatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("connections").getChildrenCount()!=0) {
                    lastSeenView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    if (dataSnapshot.hasChild("lastOnline")) {
                        String lastSeenText = getTimeAgo.getTimeAgo(Long.valueOf(dataSnapshot.child("lastOnline").getValue().toString()), getApplicationContext());
                        lastSeenView.setText(lastSeenText);
                    } else {
                        lastSeenView.setText("");
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        valueEventListenerMap.put(rootDbRef.child("presence").child(chatUserID), usersChatUserIDListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                final int currentItems = messageFirebaseAdapter.getItemCount();

                messageFirebaseAdapter.stopListening();

                DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                messageQuery = messageRef.limitToLast(currentItems+TOTAL_ITEMS_TO_LOAD);

                FirebaseRecyclerOptions<Message> options =
                        new FirebaseRecyclerOptions.Builder<Message>()
                                .setQuery(messageQuery, Message.class)
                                .build();
                messageFirebaseAdapter = new MessageFirebaseAdapter(options);

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

        String message = chatMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String messageID = rootDbRef.child("messages").child(chatID).push().getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("seen", false);
            messageMap.put("senderUserID", currentUserID);
            messageMap.put("senderName", userName);
            messageMap.put("senderThumbImage", userThumbImage);

            rootDbRef.child("messages").child(chatID).child(messageID).setValue(messageMap);
            rootDbRef.child("chats").child(chatID).child("lastMessage").setValue(message);
            rootDbRef.child("chats").child(chatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
            rootDbRef.child("chats").child(chatID).child("users").child(chatUserID).setValue(true);
            //rootRefDb.child("chatMembers").child(chatID).child(currentUserID).setValue(true);
            //rootRefDb.child("chatMembers").child(chatID).child(chatUserID).setValue(false);
            rootDbRef.child("users").child(currentUserID).child("chats").child(chatID).setValue(true);
            rootDbRef.child("users").child(chatUserID).child("chats").child(chatID).setValue(true);

            chatMessage.setText("");
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

        if (currentUser!=null) {
            //User is signed out
            Intent loginIntent = new Intent(ChatActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }
}
