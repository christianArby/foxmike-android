/*
package com.example.chris.kungsbrostrand;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

*/
/**
 * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
 * {@link FirebaseRecyclerOptions} for configuration options.
 *
 * @param options
 *//*


*/
/**
 * Created by chris on 2017-12-21.
 *//*


public class MessageFirebaseAdapter extends FirebaseRecyclerAdapter<Message, MessageFirebaseAdapter.MessageViewHolder> {

    private List<Message> messageList;


    public MessageFirebaseAdapter(FirebaseRecyclerOptions<Message> options, List<Message> messageList) {
        super(options);
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(MessageViewHolder holder, int position, Message model) {

        Message c = messageList.get(position);
        String fromUser = c.getFrom();

        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

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


*/
