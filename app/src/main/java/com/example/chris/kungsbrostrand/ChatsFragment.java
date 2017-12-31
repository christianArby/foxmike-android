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

    private DatabaseReference chatMembersDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference rootDbRef;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private HashMap<DatabaseReference, String> singleListenerMap = new HashMap<DatabaseReference, String>();

    private OnNewMessageListener onNewMessageListener;
    private View mMainView;

    private ArrayList<String> chatIDs = new ArrayList<String>();
    private HashMap<Integer, Chats> chats = new HashMap<Integer, Chats>();
    private HashMap<Integer, User> users = new HashMap<Integer, User>();
    private HashMap<Integer, String> userIDs = new HashMap<Integer, String>();

    private RecyclerView.Adapter<chatsViewHolder> chatsViewHolderAdapter;


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

        chatMembersDatabase = FirebaseDatabase.getInstance().getReference().child("chatMembers");

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        chatMembersDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // Lyssnare triggas varje gång någon chat som användaren hör till ändras (eller då det skapas en ny)
        mUsersDatabase.child(mCurrent_user_id).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //nollställ alla IDs
                chatIDs.clear();

                // Samla alla ChatIDs som användaren är en del av i chatIDs
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    chatIDs.add(child.getKey());
                }

                // Loopa alla chatIDs
                for (String key : chatIDs) {

                    if (!listenerMap.containsKey(rootDbRef.child("chats").child(key))) {

                        // Läggs till och triggas när någon chat har lagts till eller när någon chat ändrats
                        ValueEventListener chatsListener = rootDbRef.child("chats").child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Chats chat = dataSnapshot.getValue(Chats.class);

                                // Om det inte är en chat som tagits bort //TODO lägg till ta bort chat
                                if (chat!=null) {

                                    // hitta positionen i chatIDs
                                    int pos = chatIDs.indexOf(dataSnapshot.getKey());
                                    if (chats.containsKey(pos)) {
                                        chats.put(pos,chat);
                                        chatsViewHolderAdapter.notifyItemChanged(pos);
                                    } else {
                                        chats.put(pos,chat);
                                    }

                                    for (String userID : chat.getUsers().keySet()) {
                                        if (!userID.equals(mCurrent_user_id)) {
                                            userIDs.put(pos,userID);
                                        }
                                    }
                                }

                                // om size chats är lika med ChatIDs betyder det att alla lyssnare har triggats
                                if (chats.size() == chatIDs.size()) {

                                    for (final String userID : userIDs.values()) {

                                        if (!singleListenerMap.containsKey(rootDbRef.child("users").child(userID))) {
                                            singleListenerMap.put(rootDbRef.child("users").child(userID),"addedSingleListener");

                                            rootDbRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    User user = dataSnapshot.getValue(User.class);

                                                    for (String friendChats : user.getChats().keySet()) {
                                                        if (chatIDs.contains(friendChats)) {
                                                            int pos = chatIDs.indexOf(friendChats);
                                                            if (users.containsKey(pos)) {
                                                                users.put(pos,user);
                                                                chatsViewHolderAdapter.notifyItemChanged(pos);
                                                            } else {
                                                                users.put(pos,user);
                                                                if (users.size()==chats.size()) {
                                                                    chatsViewHolderAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                        }
                                                    }

                                                    if (!listenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                                        ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.getValue()!=null) {
                                                                    String isOnline = dataSnapshot.getValue().toString();
                                                                    for (Integer userIdInt : userIDs.keySet()) {
                                                                        if (userIDs.get(userIdInt) == dataSnapshot.getKey()) {
                                                                            if (isOnline.equals("true")) {
                                                                                users.get(userIdInt).setOnline(true);
                                                                                chatsViewHolderAdapter.notifyItemChanged(userIdInt);
                                                                            } else {
                                                                                users.get(userIdInt).setOnline(false);
                                                                                chatsViewHolderAdapter.notifyItemChanged(userIdInt);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        listenerMap.put(rootDbRef.child("users").child(dataSnapshot.getKey()).child("online"),onlineListener);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        listenerMap.put(rootDbRef.child("chats").child(key), chatsListener);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        chatsViewHolderAdapter = new RecyclerView.Adapter<chatsViewHolder>() {
            @Override
            public chatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new chatsViewHolder(view);
            }

            @Override
            public void onBindViewHolder(chatsViewHolder holder, int position) {

                holder.setMessage(chats.get(position).getLastMessage(), true);
                String chatFriend = "none";
                for (String chatMember : chats.get(position).getUsers().keySet()) {
                    if (!chatMember.equals(mCurrent_user_id)) {
                        chatFriend = chatMember;
                    }
                }

                holder.setUserOnline(users.get(position).isOnline());
                holder.setName(users.get(position).getName());
                holder.setUserImage(users.get(position).getThumb_image(), getContext());

                final String finalChatFriend = chatFriend;
                final String chatFriendName = users.get(position).getName();
                final String chatFriendImage = users.get(position).getThumb_image();
                final String chatID = chatIDs.get(position);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("userID", finalChatFriend);
                        chatIntent.putExtra("userName", chatFriendName);
                        chatIntent.putExtra("userThumbImage", chatFriendImage);
                        chatIntent.putExtra("chatID", chatID);
                        startActivity(chatIntent);
                    }
                });

            }

            @Override
            public int getItemCount() {
                return chats.size();
            }
        };

        mConvList.setAdapter(chatsViewHolderAdapter);
        return mMainView;
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public chatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Glide.with(ctx).load(thumb_image).into(userImageView);

        }

        public void setUserOnline(Boolean online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_iconIV);

            if (online_status) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }
    }

    public void cleanListeners() {

    }

    @Override
    public void onStop() {
        super.onStop();

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
