package com.example.chris.kungsbrostrand;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2017-12-18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image);
        }
    }
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {

        String currentUserID = mAuth.getCurrentUser().getUid();


        Message c = messageList.get(position);
        String fromUser = c.getFrom();


        if (fromUser.equals(currentUserID)) {

            holder.messageText.setBackgroundColor(Color.WHITE);

        } else {

            holder.messageText.setBackgroundResource(R.drawable.message_text_background);


        }

        holder.messageText.setText(c.getMessage());

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
