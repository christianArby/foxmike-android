package com.foxmike.android.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListChatsFirebaseAdapter(FirebaseRecyclerOptions<Chats> options, Context context, OnChatClickedListener onChatClickedListener) {
        super(options);
        this.context = context;
        this.onChatClickedListener = onChatClickedListener;
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

        String chatFriend = "none";

        for (String chatMember : model.getUsers().keySet()) {
            if (!chatMember.equals(currentUserId)) {
                chatFriend = chatMember;
            }
        }

        final String finalChatFriend = chatFriend;
        final String chatID = model.getChatId();

        if (!chatFriend.equals("none")) {
            populateUserPublicHashMap(chatFriend, new OnChatFriendLoadedListener() {
                @Override
                public void OnChatFriendLoaded(UserPublic friend) {
                    final String chatFriendName = userPublicHashMap.get(finalChatFriend).getFirstName();
                    holder.setHeading(chatFriendName);

                    final String chatFriendImage = friend.getThumb_image();
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

    private void populateUserPublicHashMap(String chatFriend, OnChatFriendLoadedListener onChatFriendLoadedListener) {

        if (!userPublicHashMap.containsKey(chatFriend)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(chatFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserPublic friendUserPublic = dataSnapshot.getValue(UserPublic.class);
                    userPublicHashMap.put(chatFriend,friendUserPublic);
                    onChatFriendLoadedListener.OnChatFriendLoaded(friendUserPublic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            onChatFriendLoadedListener.OnChatFriendLoaded(userPublicHashMap.get(chatFriend));
        }
    }

    public interface OnChatFriendLoadedListener{
        void OnChatFriendLoaded(UserPublic friend);
    }
}
