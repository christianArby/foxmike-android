package com.foxmike.android.activities;
//Checked
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
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
import com.foxmike.android.R;
import com.foxmike.android.utils.GetTimeAgo;
import com.foxmike.android.models.Message;
import com.foxmike.android.adapters.MessageFirebaseAdapter;
import com.foxmike.android.models.User;
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
    private static final int TOTAL_ITEMS_TO_LOAD = R.integer.TOTAL_ITEMS_TO_LOAD;
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

        chatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        chatMessage = (EditText) findViewById(R.id.chat_message_ET);
        chatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        chatAddBtn = (ImageButton) findViewById(R.id.chatAddBtn);
        messagesListRV = (RecyclerView) findViewById(R.id.messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        // Setup toolbar
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        chatToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        titleView = (TextView) findViewById(R.id.custom_bar_name);
        profileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        lastSeenView = (TextView) findViewById(R.id.custom_bar_lastSeen);

        // Setup meessage recyclerview
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

        // Fill toolbar
        titleView.setText(chatUserName);
        Glide.with(this).load(chatThumbImage).into(profileImage);

        // Set database references
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        userDbRef = rootDbRef.child("users").child(currentUserID);
        friendDbRef = rootDbRef.child("users").child(chatUserID);

        // see if current user has a chat with the friend already and add listener to send message button
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User currentUser = dataSnapshot.getValue(User.class);
                friendDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User friendUser = dataSnapshot.getValue(User.class);

                        // If chatID is null it means that the activity was started by clicking on friend, check if chat with friend exists
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
                            // If chat did not exist between users create a new chatID
                            if (chatID==null) {
                                chatID = rootDbRef.child("chats").push().getKey();
                            }
                        }

                        // --------------- chatID SET ----------------

                        // Set database reference to chat id in message root and build query
                        DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                        messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
                        FirebaseRecyclerOptions<Message> options =
                                new FirebaseRecyclerOptions.Builder<Message>()
                                        .setQuery(messageQuery, Message.class)
                                        .build();
                        //Setup message firebase adapter which loads 10 first messages
                        messageFirebaseAdapter = new MessageFirebaseAdapter(options, true);
                        messageFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                super.onItemRangeInserted(positionStart, itemCount);
                                messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-1);
                            }
                        });
                        messagesListRV.setAdapter(messageFirebaseAdapter);

                        // Setup send button, when button is clicked send message to database
                        chatSendBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessage(currentUser.getName(), currentUser.getThumb_image());
                            }
                        });

                        // Start listening to changes in database
                        messageFirebaseAdapter.startListening();

                        // Set current user as a participant in the chat and set the values to true meaning current user has seen the messages
                        rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
                        rootDbRef.child("users").child(currentUserID).child("chats").child(chatID).setValue(true);
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

        // Listen to Online change of friend and change status in toolbar
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

        // Setup refresh event
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                final int currentItems = messageFirebaseAdapter.getItemCount();
                messageFirebaseAdapter.stopListening();

                // Set query to current messages + 10 more items
                DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                messageQuery = messageRef.limitToLast(currentItems+TOTAL_ITEMS_TO_LOAD);
                FirebaseRecyclerOptions<Message> options =
                        new FirebaseRecyclerOptions.Builder<Message>()
                                .setQuery(messageQuery, Message.class)
                                .build();
                messageFirebaseAdapter = new MessageFirebaseAdapter(options, true);

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

    /* This function writes the message and its parameters to the database */
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
            // Current users ID is set to true in chats/users since current user has seen the message
            rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
            // Other users ID is set to false in chats/users since other user has not seen the message
            rootDbRef.child("chats").child(chatID).child("users").child(chatUserID).setValue(false);
            // Same thing as above but in the users objects
            rootDbRef.child("users").child(currentUserID).child("chats").child(chatID).setValue(true);
            rootDbRef.child("users").child(chatUserID).child("chats").child(chatID).setValue(false);
            // Clear the input field
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

        if (currentUser==null) {
            //User is signed out
            Intent loginIntent = new Intent(ChatActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }
}