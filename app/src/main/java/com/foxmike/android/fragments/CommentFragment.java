package com.foxmike.android.fragments;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.MessageFirebaseAdapter;
import com.foxmike.android.models.Message;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentFragment extends Fragment {

    public static final String TAG = CommentFragment.class.getSimpleName();

    private FirebaseAuth mAuth;
    private String currentUserID;
    private EditText postMessage;
    private ImageButton postSendBtn;
    private RecyclerView messagesListRV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private MessageFirebaseAdapter messageFirebaseAdapter;
    private DatabaseReference rootDbRef;
    private static final int TOTAL_ITEMS_TO_LOAD = R.integer.TOTAL_ITEMS_TO_LOAD;
    private Query messageQuery;
    private DatabaseReference userDbRef;
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private String sourceID;
    private String postID;
    private String heading;
    private String time;
    private String message;
    private String thumb_image;
    private String wallType;
    private String postCommentsDatabaseRef;
    private Toolbar commentToolbar;
    private boolean refreshTriggeredByScroll;
    private long mLastClickTime = 0;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(String sourceID, String postID, String heading, String time, String message, String thumb_image, String wallType) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString("sourceID", sourceID);
        args.putString("postID", postID);
        args.putString("heading", heading);
        args.putString("time", time);
        args.putString("message", message);
        args.putString("thumb_image", thumb_image);
        args.putString("wallType", wallType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sourceID = getArguments().getString("sourceID");
            postID = getArguments().getString("postID");
            heading = getArguments().getString("heading");
            time = getArguments().getString("time");
            message = getArguments().getString("message");
            thumb_image = getArguments().getString("thumb_image");
            wallType = getArguments().getString("wallType");
        }
        postCommentsDatabaseRef = "sessionPostComments";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        postMessage = (EditText) view.findViewById(R.id.post_message_ET);
        postSendBtn = (ImageButton) view.findViewById(R.id.post_message_send_btn);
        messagesListRV = (RecyclerView) view.findViewById(R.id.post_messages_list);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.post_message_swipe_layout);

        view.findViewById(R.id.post_nr_comments_container).setVisibility(View.GONE);

        view.findViewById(R.id.session_post_comment_text).setVisibility(View.GONE);



        TextView headingTV = (TextView) view.findViewById(R.id.session_post_name);
        headingTV.setText(heading);

        TextView timeView = (TextView) view.findViewById(R.id.session_post_time);
        timeView.setText(time);

        TextView messageView = (TextView) view.findViewById(R.id.session_post_message);
        messageView.setText(message);

        CircleImageView userProfileImageIV = (CircleImageView) view.findViewById(R.id.session_post_image);
        Glide.with(getActivity().getApplicationContext()).load(thumb_image).into(userProfileImageIV);

        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        // Make sure keyboard is not hiding recyclerview
        //linearLayoutManager.setStackFromEnd(true);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);

        if (Build.VERSION.SDK_INT >= 11) {
            messagesListRV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        messagesListRV.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (messagesListRV.getAdapter().getItemCount()>0) {
                                    messagesListRV.smoothScrollToPosition(
                                            messagesListRV.getAdapter().getItemCount() - 1);
                                }
                            }
                        }, 100);
                    }
                }
            });
        }

        valueEventListenerMap = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootDbRef = FirebaseDatabase.getInstance().getReference();
        userDbRef = rootDbRef.child("users").child(currentUserID);

        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User currentUser = dataSnapshot.getValue(User.class);
                // When button is clicked send message to database
                postSendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postSendBtn.setEnabled(false);
                        refreshTriggeredByScroll = false;
                        sendMessage(currentUser.getFirstName());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference commentsRef = rootDbRef.child(postCommentsDatabaseRef).child(sourceID).child(postID);
        messageQuery = commentsRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshTriggeredByScroll = true;

                final int currentItems = messageFirebaseAdapter.getItemCount();
                messageFirebaseAdapter.stopListening();
                DatabaseReference messageRef = rootDbRef.child(postCommentsDatabaseRef).child(sourceID).child(postID);
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
                        if (refreshTriggeredByScroll) {
                            messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-currentItems);
                        } else {
                            messagesListRV.scrollToPosition(messageFirebaseAdapter.getItemCount()-1);
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

    private void sendMessage(String userName) {
        String message = postMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String messageID = rootDbRef.child(postCommentsDatabaseRef).child(sourceID).child(postID).push().getKey();
            Long currentTimestamp = System.currentTimeMillis();
            Message comment = new Message(message, currentTimestamp, false, currentUserID);
            rootDbRef.child(postCommentsDatabaseRef).child(sourceID).child(postID).child(messageID).setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Long currentTimestamp = System.currentTimeMillis();
                    rootDbRef.child("userComments").child(mAuth.getCurrentUser().getUid()).child(sourceID).child(postID).child(messageID).setValue(currentTimestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            postMessage.setText("");
                            postSendBtn.setEnabled(true);
                        }
                    });
                }
            });
        } else {
            postSendBtn.setEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        messageFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        messageFirebaseAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        if (messagesListRV!=null) {
            messagesListRV.setAdapter(null);
        }
    }
}
