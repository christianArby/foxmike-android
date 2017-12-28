package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConversationDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference usersChatUserDbRef;
    private Query lastMessageQuery;

    FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> firebaseConversationAdapter;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerMap;

    private ChildEventListener messageChildEventListener;
    private ValueEventListener usersValueEventListener;

    private OnNewMessageListener onNewMessageListener;

    private View mMainView;

    private ArrayList<User> users = new ArrayList<User>();


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConversationDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConversationDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // ------------------download user information and notify item changed

        //Query conversationQuery = mConversationDatabase.orderByChild("time");






        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        childEventListenerMap = new HashMap<>();
        valueEventListenerMap = new HashMap<>();

        Query conversationQuery = mConversationDatabase.orderByChild("time");

        FirebaseRecyclerOptions<Conversation> options =
                new FirebaseRecyclerOptions.Builder<Conversation>()
                        .setQuery(conversationQuery, Conversation.class)
                        .build();

        firebaseConversationAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new ConversationViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(final ConversationViewHolder holder, int position, final Conversation model) {

                // from messageDB
                String test = model.getLastMessage();
                holder.setMessage(model.getLastMessage(), model.isSeen());

                final String list_user_id = getRef(position).getKey();

                // from userDB

                mUsersDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        holder.setUserOnline(user.isOnline());

                        holder.setName(user.getName());
                        holder.setUserImage(user.getThumb_image(), getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("userID", list_user_id);
                                chatIntent.putExtra("userName", user.getName());
                                chatIntent.putExtra("userThumbImage", user.getThumb_image());
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mConvList.setAdapter(firebaseConversationAdapter);

        // TODO Väldigt bra, vi kan stoppa lyssnaren (och ha addvalue eventlisteners kanske?)
        firebaseConversationAdapter.startListening();


    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConversationViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Glide.with(ctx).load(thumb_image).into(userImageView);

        }

        public void setUserOnline(Boolean online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_iconIV);

            if(online_status){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }
    }

    public void cleanListeners () {
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
    public void onStop() {
        super.onStop();
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
        firebaseConversationAdapter.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewMessageListener) {
            onNewMessageListener = (OnNewMessageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewMessageListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNewMessageListener = null;
    }
}
