package com.foxmike.android.adapters;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
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
import com.foxmike.android.models.SessionDate;
import com.foxmike.android.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * Created by chris on 2017-12-30.
 */

public class MessageFirebaseAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {
    boolean slalom;
    FirebaseAuth mAuth;
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

            Date d = new Date(model.getTime());
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            SessionDate sessionDate = new SessionDate(c);
            String timeText = sessionDate.textDateAndTime();
            ((ThisMessageViewHolder) holder).messageTime.setVisibility(View.VISIBLE);
            ((ThisMessageViewHolder) holder).messageTime.setText(timeText);

            if (position>0) {
                if (Math.abs(model.getTime()-getItem(position-1).getTime())<300000){
                    //previous message is within 5 minutes of current message
                    ((ThisMessageViewHolder) holder).messageTime.setVisibility(View.GONE);
                }
            }

        } else {
            Glide.with(((OtherMessageViewHolder) holder).profileImage.getContext()).load(model.getSenderThumbImage()).into(((OtherMessageViewHolder) holder).profileImage);
            ((OtherMessageViewHolder) holder).messageText.setBackgroundResource(R.drawable.message_text_background);
            ((OtherMessageViewHolder) holder).messageText.setTextColor(Color.BLACK);
            ((OtherMessageViewHolder) holder).profileImage.setVisibility(View.VISIBLE);
            ((OtherMessageViewHolder) holder).messageText.setText(model.getMessage());
            ((OtherMessageViewHolder) holder).messageUser.setVisibility(View.GONE);

            Date d = new Date(model.getTime());
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            SessionDate sessionDate = new SessionDate(c);
            String timeText = sessionDate.textDateAndTime();
            ((OtherMessageViewHolder) holder).messageTime.setVisibility(View.VISIBLE);
            ((OtherMessageViewHolder) holder).messageTime.setText(timeText);

            if (!slalom) {
                ((OtherMessageViewHolder) holder).messageUser.setVisibility(View.VISIBLE);
                ((OtherMessageViewHolder) holder).messageUser.setText(model.getSenderName());
            }

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
        public ConstraintLayout singleMessageContainer;
        public ThisMessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            singleMessageContainer = (ConstraintLayout) view.findViewById(R.id.message_relative_layout);
        }
    }
}
