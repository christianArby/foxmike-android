package com.foxmike.android.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.Chats;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by chris on 2018-09-22.
 */

public class ListChatsFirebaseAdapter extends FirebaseRecyclerAdapter<Chats, UsersViewHolder> {

    private Context context;
    private OnChatClickedListener onChatClickedListener;;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();
    private long mLastClickTime = 0;
    private String chatFriend;
    private HashMap<String, Boolean> friendsMap = new HashMap<>();
    public HashMap<String, Boolean> chatsVisible = new HashMap<String, Boolean>();
    private OnNoContentListener onNoContentListener;
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListChatsFirebaseAdapter(FirebaseRecyclerOptions<Chats> options, Context context, OnChatClickedListener onChatClickedListener, OnNoContentListener onNoContentListener) {
        super(options);
        this.context = context;
        this.onChatClickedListener = onChatClickedListener;
        this.onNoContentListener = onNoContentListener;
    }

    public void updateFriends(HashMap<String, Boolean> friendsMap) {
        this.friendsMap = friendsMap;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_single_layout, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Chats model) {

        chatFriend = "none";

        for (String chatMember : model.getUsers().keySet()) {
            if (!chatMember.equals(currentUserId)) {
                chatFriend = chatMember;
            }
        }

        if (!friendsMap.containsKey(chatFriend)) {
            this.chatsVisible.put(model.getChatId(), false);
            if (!this.chatsVisible.containsValue(true)) {
                onNoContentListener.OnNoContentVisible(true);
            }
            if (model.getChatId()!=null) {
                FirebaseDatabase.getInstance().getReference().child("userChats").child(currentUserId).child(model.getChatId()).setValue(true);
            }

            holder.mView.setVisibility(View.GONE);
            holder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            this.chatsVisible.put(model.getChatId(), true);
            onNoContentListener.OnNoContentVisible(false);
            holder.mView.setVisibility(View.VISIBLE);
            holder.mView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        final String finalChatFriend = chatFriend;
        final String chatID = model.getChatId();

        if (!chatFriend.equals("none")) {
            populateUserPublicHashMap(chatFriend, new OnChatInfoLoadedListener() {
                @Override
                public void OnChatFriendLoaded() {
                    final String chatFriendName = userPublicHashMap.get(finalChatFriend).getFirstName();
                    holder.setHeading(chatFriendName);

                    final String chatFriendImage = userPublicHashMap.get(finalChatFriend).getThumb_image();
                    holder.setUserImage(chatFriendImage, context);
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            onChatClickedListener.OnChatClicked(finalChatFriend,chatFriendName,chatFriendImage,chatID);
                        }
                    });

                }
            });
        }

        Boolean isSeen = model.getUsers().get(currentUserId);
        holder.setText(model.getLastMessage(), isSeen);
    }

    private void populateUserPublicHashMap(String chatFriend, OnChatInfoLoadedListener onChatInfoLoadedListener) {

        if (!userPublicHashMap.containsKey(chatFriend)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(chatFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        UserPublic friendUserPublic = dataSnapshot.getValue(UserPublic.class);
                        userPublicHashMap.put(chatFriend,friendUserPublic);
                        onChatInfoLoadedListener.OnChatFriendLoaded();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            onChatInfoLoadedListener.OnChatFriendLoaded();
        }

    }

    public interface OnChatInfoLoadedListener {
        void OnChatFriendLoaded();
    }



    public interface OnNoContentListener {
        void OnNoContentVisible(Boolean noContentVisible);
    }

}
