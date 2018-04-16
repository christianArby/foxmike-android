package com.foxmike.android.fragments;
//Checked
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.Chats;
import com.foxmike.android.models.Presence;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * This fragment checks which chats the current user is part of, downloads the chat objects and there corresponding
 * chatusers object and the chatusers presence status and saves all these information in Hashmaps to then populate a list
 * with information of each chat.
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
    private View mMainView;
    private ArrayList<String> chatIDs = new ArrayList<String>();
    private HashMap<Integer, Chats> chats = new HashMap<Integer, Chats>();
    private HashMap<Integer, User> users = new HashMap<Integer, User>();
    private HashMap<Integer, String> userIDs = new HashMap<Integer, String>();
    private HashMap<Integer, Presence> presenceHashMap = new HashMap<Integer, Presence>();
    private RecyclerView.Adapter<UsersViewHolder> chatsViewHolderAdapter;
    private OnChatClickedListener onChatClickedListener;

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

        // Listener which triggers each time a chat which the current user is part of changes or is created
        if (!listenerMap.containsKey(mUsersDatabase.child(mCurrent_user_id).child("chats"))) {
            ValueEventListener currentUserChatsListener = mUsersDatabase.child(mCurrent_user_id).child("chats").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatIDs.clear();

                    // Collect all chat IDs current user is part of
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        chatIDs.add(child.getKey());
                    }

                    // Loop thorugh all chat IDs
                    for (String key : chatIDs) {

                        if (!listenerMap.containsKey(rootDbRef.child("chats").child(key))) {

                            // Add a listener to each chat
                            ValueEventListener chatsListener = rootDbRef.child("chats").child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Chats chat = dataSnapshot.getValue(Chats.class);

                                    // If chat is not deleted it means that listener was triggered by other event than chat deleted //
                                    // TODO add function to delete chats
                                    if (chat!=null) {

                                        // find the current chat index position in chat IDs
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

                                    // if the size of the hashmup full of chat objects is the same size as the size of the hashmap with chatIDs it
                                    // means that all the listeners have been triggered
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
                                                        // Add listener the presence of the chatuser
                                                        if (!listenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                                            ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    Presence presence = dataSnapshot.getValue(Presence.class);

                                                                    for (Integer userIdInt : userIDs.keySet()) {
                                                                        if (userIDs.get(userIdInt).equals(dataSnapshot.getKey()) ) {
                                                                            presenceHashMap.put(userIdInt,presence);
                                                                            chatsViewHolderAdapter.notifyItemChanged(userIdInt);
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
            listenerMap.put(mUsersDatabase.child(mCurrent_user_id).child("chats"),currentUserChatsListener);
        }

        chatsViewHolderAdapter = new RecyclerView.Adapter<UsersViewHolder>() {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            public void onBindViewHolder(UsersViewHolder holder, int position) {

                Boolean isSeen = chats.get(position).getUsers().get(mCurrent_user_id);
                holder.setText(chats.get(position).getLastMessage(), isSeen);
                String chatFriend = "none";
                for (String chatMember : chats.get(position).getUsers().keySet()) {
                    if (!chatMember.equals(mCurrent_user_id)) {
                        chatFriend = chatMember;
                    }
                }

                if (presenceHashMap.get(position) == null) {
                    Presence noPresence = new Presence();
                    presenceHashMap.put(position,noPresence);
                }

                holder.setOnlineIcon(presenceHashMap.get(position).isOnline());
                holder.setHeading(users.get(position).getFirstName());
                holder.setUserImage(users.get(position).getThumb_image(), getContext());

                final String finalChatFriend = chatFriend;
                final String chatFriendName = users.get(position).getFirstName();
                final String chatFriendImage = users.get(position).getThumb_image();
                final String chatID = chatIDs.get(position);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        onChatClickedListener.OnChatClicked(finalChatFriend,chatFriendName,chatFriendImage,chatID);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatClickedListener) {
            onChatClickedListener = (OnChatClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onChatClickedListener = null;
    }

}
