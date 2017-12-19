package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2017-12-18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messageList;
    private DatabaseReference mUserDatabase;


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
        public TextView messageUser;
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView messageTime;

        public MessageViewHolder(View view) {
            super(view);

            messageUser = (TextView) view.findViewById(R.id.message_user_name);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image);

        }
    }
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        Message c = messageList.get(position);
        String fromUser = c.getFrom();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(fromUser);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                String name = user.getName();
                String image = user.getThumb_image();

                holder.messageUser.setText(name);

                Glide.with(holder.profileImage.getContext()).load(image).into(holder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });







        /*if (fromUser.equals(currentUserID)) {

            holder.messageText.setBackgroundColor(Color.WHITE);

        } else {

            holder.messageText.setBackgroundResource(R.drawable.message_text_background);


        }*/

        holder.messageText.setText(c.getMessage());




        //Glide.with(ctx).load(holder.get).into(userProfileImageIV);

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
