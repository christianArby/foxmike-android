package com.foxmike.android.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.activities.FoxmikeApplication;
import com.foxmike.android.adapters.MessageFirebaseAdapter;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.Message;
import com.foxmike.android.models.UserPublic;
import com.google.firebase.auth.FirebaseAuth;
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


public class ChatFragment extends Fragment {

    private String chatUserID;
    private String chatUserName;
    private String chatThumbImage;
    private Toolbar chatToolbar;
    private TextView titleView;
    //private TextView lastSeenView;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private EditText chatMessage;
    private ImageButton chatSendBtn;
    private RecyclerView messagesListRV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageFirebaseAdapter messageFirebaseAdapter;
    private DatabaseReference rootDbRef;
    private Query messageQuery;
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private ValueEventListener usersChatUserIDListener;
    private OnUserClickedListener onUserClickedListener;
    private String chatID;
    boolean refreshTriggeredByScroll;
    private int lastVisiblePosition;
    int itemsLoaded;
    int itemsDifference;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String userID, String userName, String userThumbImage, String chatID) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("userID",userID);
        args.putString("userName",userName);
        args.putString("userThumbImage",userThumbImage);
        args.putString("chatID",chatID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Get extra sent from previous activity
            chatUserID = getArguments().getString("userID");
            chatUserName = getArguments().getString("userName");
            chatThumbImage = getArguments().getString("userThumbImage");
            chatID = getArguments().getString("chatID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatToolbar = (Toolbar) view.findViewById(R.id.chat_app_bar);
        chatMessage = (EditText) view.findViewById(R.id.chat_message_ET);
        chatSendBtn = (ImageButton) view.findViewById(R.id.chat_send_btn);
        messagesListRV = (RecyclerView) view.findViewById(R.id.messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.message_swipe_layout);

        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(chatToolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        //chatToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        actionBar.setDisplayShowCustomEnabled(true);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        titleView = (TextView) view.findViewById(R.id.custom_bar_name);
        profileImage = (CircleImageView) view.findViewById(R.id.custom_bar_image);
        //lastSeenView = (TextView) view.findViewById(R.id.custom_bar_lastSeen);

        // Setup meessage recyclerview
        linearLayoutManager = new LinearLayoutManager(getActivity());
        // Make sure keyboard is not hiding recyclerview
        linearLayoutManager.setStackFromEnd(true);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);

        valueEventListenerMap = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // Fill toolbar
        titleView.setText(chatUserName);
        Glide.with(this).load(chatThumbImage).into(profileImage);

        // if user info icon is clicked, display user profile
        action_bar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUserClickedListener.OnUserClicked(chatUserID);
            }
        });

        // Set database references
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        // see if current user has a chat with the friend already and add listener to send message button

        rootDbRef.child("chatMembers").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DataSnapshot currentUserChats = dataSnapshot;

                rootDbRef.child("chatMembers").child(chatUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (chatID==null) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                if (currentUserChats.hasChild(snapshot.getKey())) {
                                    chatID = snapshot.getKey();
                                }
                            }
                            // If chat did not exist between users create a new chatID
                            if (chatID==null) {
                                chatID = rootDbRef.child("chats").push().getKey();
                            }
                        }

                        // --------------- chatID IS NOW SET ----------------

                        // Set database reference to chat id in message root and build query
                        DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                        messageQuery = messageRef.limitToLast(getResources().getInteger(R.integer.TOTAL_ITEMS_TO_LOAD));
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
                                // Set that current user has seen the chat
                                if (FoxmikeApplication.isActivityVisible()) {
                                    rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
                                    rootDbRef.child("chatMembers").child(currentUserID).child(chatID).setValue(true);
                                }
                            }
                        });
                        messagesListRV.setAdapter(messageFirebaseAdapter);
                        // Setup send button, when button is clicked send message to database
                        rootDbRef.child("usersPublic").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserPublic currentUser = dataSnapshot.getValue(UserPublic.class);
                                chatSendBtn.setOnClickListener(view1 -> {
                                    refreshTriggeredByScroll = false;
                                    sendMessage(currentUser.getFirstName(), currentUser.getImage());
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        // Start listening to changes in database
                        messageFirebaseAdapter.startListening();


                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootDbRef.child("chatsMembers").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot currentUserChatsCnapshot = dataSnapshot;
                rootDbRef.child("chatMembers").child(chatUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If chatID is null it means that the activity was started by clicking on friend (and not on a chat), check if chat with friend exists
                        if (chatID== null) {
                            for (DataSnapshot friendSnapshot: dataSnapshot.getChildren()) {
                                if (currentUserChatsCnapshot.hasChild(friendSnapshot.getKey())) {
                                    chatID = friendSnapshot.getKey();
                                }
                            }
                        }
                        // If chat did not exist between users create a new chatID
                        if (chatID==null) {
                            chatID = rootDbRef.child("chats").push().getKey();
                        }
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
        /*// Listen to Online change of friend and change status in toolbar
        usersChatUserIDListener = rootDbRef.child("presence").child(chatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("connections").getChildrenCount()!=0) {
                    lastSeenView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    if (dataSnapshot.hasChild("lastOnline")) {
                        String lastSeenText = getString(R.string.last_seen_text) + getTimeAgo.getTimeAgo(Long.valueOf(dataSnapshot.child("lastOnline").getValue().toString()), getActivity().getApplicationContext());
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
        valueEventListenerMap.put(rootDbRef.child("presence").child(chatUserID), usersChatUserIDListener);*/
        // Setup refresh event
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final int currentItems = messageFirebaseAdapter.getItemCount();
                LinearLayoutManager layoutManager = ((LinearLayoutManager)messagesListRV.getLayoutManager());
                lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                itemsLoaded = 0;

                refreshTriggeredByScroll = true;
                messageFirebaseAdapter.stopListening();

                // Set query to current messages + 10 more items
                DatabaseReference messageRef = rootDbRef.child("messages").child(chatID);
                messageQuery = messageRef.limitToLast(currentItems+getResources().getInteger(R.integer.TOTAL_ITEMS_TO_LOAD));
                FirebaseRecyclerOptions<Message> options =
                        new FirebaseRecyclerOptions.Builder<Message>()
                                .setQuery(messageQuery, Message.class)
                                .build();
                messageFirebaseAdapter = new MessageFirebaseAdapter(options, true);

                messageFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        itemsLoaded++;
                        itemsDifference = itemsLoaded-currentItems;
                        if (refreshTriggeredByScroll) {
                            messagesListRV.scrollToPosition(itemsDifference+lastVisiblePosition-1);
                        } else {
                            messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-1);
                        }
                        // make sure the above if is only valid for refresh event and not every onItemRangeInserted (for example if a message has been added)
                        if (positionStart== currentItems + itemsLoaded -1) {
                            refreshTriggeredByScroll = false;
                        }
                        // Set that current user has seen the chat
                        if (FoxmikeApplication.isActivityVisible()) {
                            rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
                            rootDbRef.child("chatMembers").child(currentUserID).child(chatID).setValue(true);
                        }
                    }
                });

                messagesListRV.setAdapter(messageFirebaseAdapter);
                messageFirebaseAdapter.startListening();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    /* This function writes the message and its parameters to the database */
    private void sendMessage(String userName, String userThumbImage) {
        // get message from EditText
        String message = chatMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            // create a message ID in database under messages/chatID/
            String messageID = rootDbRef.child("messages").child(chatID).push().getKey();
            // Create a message map which is used to write all this data to the database at once
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("seen", false);
            messageMap.put("senderUserID", currentUserID);
            messageMap.put("senderName", userName);
            messageMap.put("senderThumbImage", userThumbImage);
            // Write the message map to the database
            rootDbRef.child("messages").child(chatID).child(messageID).setValue(messageMap);
            // Set the last message in the chat Object to the current message
            rootDbRef.child("chats").child(chatID).child("lastMessage").setValue(message);
            // Set current time to the chat object in the dataabce
            rootDbRef.child("chats").child(chatID).child("timestamp").setValue(ServerValue.TIMESTAMP);
            // Current users ID is set to true in chats/users since current user has seen the message
            rootDbRef.child("chats").child(chatID).child("users").child(currentUserID).setValue(true);
            // Other users ID is set to false in chats/users since other user has not seen the message
            rootDbRef.child("chats").child(chatID).child("users").child(chatUserID).setValue(false);
            // Same thing as above but in the chatMembers objects
            rootDbRef.child("chatMembers").child(currentUserID).child(chatID).setValue(true);
            rootDbRef.child("chatMembers").child(chatUserID).child(chatID).setValue(false);
            // Clear the input field
            chatMessage.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        messageFirebaseAdapter.stopListening();
        hideKeyboard(getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserClickedListener) {
            onUserClickedListener = (OnUserClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onUserClickedListener = null;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
