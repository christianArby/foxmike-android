package com.foxmike.android.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.utils.GetTimeAgo;
import com.foxmike.android.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * Created by chris on 2017-12-30.
 */

public class MessageFirebaseAdapter extends FirebaseRecyclerAdapter<Message, MessageFirebaseAdapter.MessageViewHolder> {
    boolean slalom;
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
    protected void onBindViewHolder(MessageViewHolder holder, int position, Message model) {

        FirebaseAuth mAuth;
        String currentUserID;
        String fromUser = model.getSenderUserID();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        holder.messageUser.setText(model.getSenderName());
        Glide.with(holder.profileImage.getContext()).load(model.getSenderThumbImage()).into(holder.profileImage);

        if (fromUser.equals(currentUserID) && slalom) {
            holder.singleMessageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_host);
            holder.messageText.setTextColor(Color.WHITE);
            holder.profileImage.setVisibility(View.GONE);
            holder.messageUser.setVisibility(View.GONE);
            holder.messageTime.setVisibility(View.GONE);
        } else {
            holder.singleMessageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.BLACK);
            holder.profileImage.setVisibility(View.VISIBLE);
            holder.messageUser.setVisibility(View.VISIBLE);
        }

        GetTimeAgo getTimeAgo = new GetTimeAgo();
        String timeText = getTimeAgo.getTimeAgo(model.getTime(),holder.messageText.getContext());
        holder.messageText.setText(model.getMessage());
        holder.messageTime.setText(timeText);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageUser;
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView messageTime;
        public RelativeLayout singleMessageContainer;
        public MessageViewHolder(View view) {
            super(view);
            messageUser = (TextView) view.findViewById(R.id.message_user_name);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image);
            singleMessageContainer = (RelativeLayout) view.findViewById(R.id.message_relative_layout);
        }
    }
}
