package com.foxmike.android.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.models.Message;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * Created by chris on 2017-12-30.
 */

public class MessageFirebaseAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {
    boolean slalom;
    FirebaseAuth mAuth;
    HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public MessageFirebaseAdapter(FirebaseRecyclerOptions<Message> options, boolean slalom) {
        super(options);
        this.slalom = slalom;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType==1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_this,parent,false);
            return new ThisMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, int position, Message model) {
        FirebaseAuth mAuth;
        String currentUserID;
        String fromUser = model.getSenderUserID();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        if (fromUser.equals(currentUserID) && slalom) {
            ((ThisMessageViewHolder) holder).messageText.setBackgroundResource(R.drawable.message_text_background_host);
            ((ThisMessageViewHolder) holder).messageText.setTextColor(Color.WHITE);
            ((ThisMessageViewHolder) holder).messageText.setText(model.getMessage());

            TextTimestamp textTimestamp = new TextTimestamp(model.getTime());
            String timeText = textTimestamp.textDateAndTime();
            ((ThisMessageViewHolder) holder).messageTime.setVisibility(View.VISIBLE);
            ((ThisMessageViewHolder) holder).messageTime.setText(timeText);

            if (position>0) {
                if (Math.abs(model.getTime()-getItem(position-1).getTime())<300000){
                    //previous message is within 5 minutes of current message
                    ((ThisMessageViewHolder) holder).messageTime.setVisibility(View.GONE);
                }
            }

        } else {
            populateUserPublicHashMap(model.getSenderUserID(), new OnUsersLoadedListener() {
                @Override
                public void OnUsersLoaded() {
                    Glide.with(((OtherMessageViewHolder) holder).profileImage.getContext()).load(userPublicHashMap.get(model.getSenderUserID()).getThumb_image()).into(((OtherMessageViewHolder) holder).profileImage);
                    if (!slalom) {
                        ((OtherMessageViewHolder) holder).messageUser.setVisibility(View.VISIBLE);
                        ((OtherMessageViewHolder) holder).messageUser.setText(userPublicHashMap.get(model.getSenderUserID()).getFirstName());
                    }
                }
            });

            ((OtherMessageViewHolder) holder).messageText.setBackgroundResource(R.drawable.message_text_background);
            ((OtherMessageViewHolder) holder).messageText.setTextColor(Color.BLACK);
            ((OtherMessageViewHolder) holder).profileImage.setVisibility(View.VISIBLE);
            ((OtherMessageViewHolder) holder).messageText.setText(model.getMessage());
            if (slalom) {
                ((OtherMessageViewHolder) holder).messageUser.setVisibility(View.GONE);
            }

            TextTimestamp textTimestamp = new TextTimestamp(model.getTime());
            String timeText = textTimestamp.textDateAndTime();
            ((OtherMessageViewHolder) holder).messageTime.setVisibility(View.VISIBLE);
            ((OtherMessageViewHolder) holder).messageTime.setText(timeText);



            if (position>0) {
                if (Math.abs(model.getTime()-getItem(position-1).getTime())<300000){
                    //previous message is within 5 minutes of current message
                    ((OtherMessageViewHolder) holder).messageTime.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        if (getItem(position).getSenderUserID().equals(mAuth.getCurrentUser().getUid())  && slalom) {
            return 1;
        } else {
            return 0;
        }
    }

    public class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageUser;
        public CircleImageView profileImage;
        public TextView messageTime;
        public RelativeLayout singleMessageContainer;
        public OtherMessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageUser = (TextView) view.findViewById(R.id.message_user);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image);
            singleMessageContainer = (RelativeLayout) view.findViewById(R.id.message_relative_layout);
        }
    }

    public class ThisMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageTime;
        public RelativeLayout singleMessageContainer;
        public ThisMessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            singleMessageContainer = (RelativeLayout) view.findViewById(R.id.message_relative_layout);
        }
    }

    private void populateUserPublicHashMap(String userId, OnUsersLoadedListener onUsersLoadedListener) {
        if (!userPublicHashMap.containsKey(userId)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    UserPublic userPublic = dataSnapshot.getValue(UserPublic.class);
                    userPublicHashMap.put(userId, userPublic);
                    onUsersLoadedListener.OnUsersLoaded();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            onUsersLoadedListener.OnUsersLoaded();
        }
    }
    public interface OnUsersLoadedListener{
        void OnUsersLoaded();
    }
}
