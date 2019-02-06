package com.foxmike.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.fragments.NotificationsFragment;
import com.foxmike.android.models.FoxmikeNotification;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2018-09-22.
 */


public class ListNotificationsFirebaseAdapter extends FirebaseRecyclerAdapter<FoxmikeNotification, ListNotificationsFirebaseAdapter.NotificationsViewHolder> {

    private Context context;
    private NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener;
    HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListNotificationsFirebaseAdapter(FirebaseRecyclerOptions<FoxmikeNotification> options, Context context, NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener) {
        super(options);
        this.context = context;
        this.onNotificationClickedListener = onNotificationClickedListener;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_layout, parent, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position, @NonNull FoxmikeNotification model) {

        holder.setNotificationClickedListener(model);
        if (model.getType().equals("sessionPost") | model.getType().equals("advertisementPost")) {
            holder.setNotificationImage(model.getThumbNail(),context);

            String notificationText = model.getParam1() + context.getString(R.string.has_made_a_post_in) + model.getParam3();

            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
            StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold1, 0, model.getParam1().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold
            StyleSpan bold2 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold2, model.getParam1().length() + context.getString(R.string.has_made_a_post_in).length(), notificationText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

            holder.setNotificationText(notificationTextFormatted);

        }
        if (model.getType().equals("sessionPostComment") | model.getType().equals("advertisementPostComment")) {

            // TODO ta bort null när gammal version 0.2.3-beta inte längre används
            if (model.getThumbNail()!=null) {
                populateUserPublicHashMap(model.getThumbNail(), new MessageFirebaseAdapter.OnUsersLoadedListener() {
                    @Override
                    public void OnUsersLoaded(UserPublic userPublic) {
                        holder.setNotificationImage(userPublic.getThumb_image(),context);
                    }
                });
            }
            String notificationText = model.getParam1() + context.getString(R.string.has_made_a_comment_to_your_post_in) + model.getParam3();

            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
            StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold1, 0, model.getParam1().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold
            StyleSpan bold2 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold2, model.getParam1().length() + context.getString(R.string.has_made_a_comment_to_your_post_in).length(), notificationText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

            holder.setNotificationText(notificationTextFormatted);
        }
        if (model.getType().equals("advertisementParticipant")) {
            holder.setNotificationImage(model.getThumbNail(),context);

            if (!model.getParam2().equals("none")) {
                String notificationText = context.getString(R.string.you_have_a_new_participant_in) + model.getParam3();

                SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
                StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
                notificationTextFormatted.setSpan(bold1, context.getString(R.string.you_have_a_new_participant_in).length(), notificationText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

                holder.setNotificationText(notificationTextFormatted);
            } else {
                String notificationText = context.getString(R.string.you_have_a_cancellation_in) + model.getParam3();

                SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
                StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
                notificationTextFormatted.setSpan(bold1, context.getString(R.string.you_have_a_cancellation_in).length(), notificationText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

                holder.setNotificationText(notificationTextFormatted);
            }
        }

        if (model.getType().equals("sessionCancellation")) {
            holder.setNotificationImage(model.getThumbNail(),context);

            String notificationText = context.getString(R.string.the_session) + model.getParam3() + context.getString(R.string.on) + TextTimestamp.textSessionDate(Long.parseLong(model.getParam2())) + context.getString(R.string.has_been_cancelled);

            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
            StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold1, context.getString(R.string.the_session).length(), context.getString(R.string.the_session).length() + model.getParam3().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

            holder.setNotificationText(notificationTextFormatted);

        }

        if (model.getType().equals("friendRequestAccepted")) {
            holder.setNotificationImage(model.getThumbNail(),context);

            String notificationText = model.getParam1() + " has accepted your friend request.";

            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
            StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
            notificationTextFormatted.setSpan(bold1, 0, model.getParam1().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

            holder.setNotificationText(notificationTextFormatted);

        }

        if (model.getType().equals("adminNotification")) {
            holder.setAdminNotificationImage(context);

            String notificationText = model.getParam1();

            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);

            holder.setNotificationText(notificationTextFormatted);

        }

        holder.setNotificationTime(TextTimestamp.textShortDateAndTime(model.getTimestamp()));
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public void setNotificationClickedListener(FoxmikeNotification FoxmikeNotification) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNotificationClickedListener.OnNotificationClicked(FoxmikeNotification);
                }
            });
        }

        public NotificationsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNotificationText(SpannableStringBuilder notificationText) {
            TextView notificationTextTV = (TextView) mView.findViewById(R.id.notification_text);
            notificationTextTV.setText(notificationText);
        }

        public void setNotificationTime(String notificationTime) {
            TextView notificationTimeTV = (TextView) mView.findViewById(R.id.notification_time);
            notificationTimeTV.setText(notificationTime);
        }

        public void setNotificationImage(String thumb_image, android.content.Context context) {
            CircleImageView notificationImageIV = (CircleImageView) mView.findViewById(R.id.notification_image);
            Glide.with(context).load(thumb_image).into(notificationImageIV);
        }

        public void setAdminNotificationImage(android.content.Context context) {
            CircleImageView notificationImageIV = (CircleImageView) mView.findViewById(R.id.notification_image);
            notificationImageIV.setImageDrawable(context.getResources().getDrawable(R.drawable.foxmike_icon_transparent));
        }
    }



    private void populateUserPublicHashMap(String userId, MessageFirebaseAdapter.OnUsersLoadedListener onUsersLoadedListener) {
        if (!userPublicHashMap.containsKey(userId)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserPublic userPublic = dataSnapshot.getValue(UserPublic.class);
                    userPublicHashMap.put(userId, userPublic);
                    onUsersLoadedListener.OnUsersLoaded(userPublic);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            onUsersLoadedListener.OnUsersLoaded(userPublicHashMap.get(userId));
        }
    }
}
