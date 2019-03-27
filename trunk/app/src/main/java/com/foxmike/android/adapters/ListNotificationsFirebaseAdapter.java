package com.foxmike.android.adapters;
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
import com.foxmike.android.models.InAppNotification;
import com.foxmike.android.models.Message;
import com.foxmike.android.models.Post;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2018-09-22.
 */


public class ListNotificationsFirebaseAdapter extends FirebaseRecyclerAdapter<FoxmikeNotification, ListNotificationsFirebaseAdapter.NotificationsViewHolder> {

    private NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener;
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<String, InAppNotification> notificationHashMap = new HashMap<>();
    private HashMap<String, String> stringHashMap = new HashMap<>();
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListNotificationsFirebaseAdapter(FirebaseRecyclerOptions<FoxmikeNotification> options, HashMap<String, String> stringHashMap, NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener) {
        super(options);
        this.onNotificationClickedListener = onNotificationClickedListener;
        this.stringHashMap = stringHashMap;
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


        if (notificationHashMap.containsKey(model.getNotificationId())) {
            InAppNotification inAppNotification = notificationHashMap.get(model.getNotificationId());
            holder.setNotificationImage(inAppNotification.getNotificationThumbnail());
            holder.setNotificationText(inAppNotification.getNotificationText());
        } else {
            populateNotificationHashMap(model, new OnNotificationLoadedListener() {
                @Override
                public void OnNotificationLoaded() {
                    InAppNotification inAppNotification = notificationHashMap.get(model.getNotificationId());
                    holder.setNotificationImage(inAppNotification.getNotificationThumbnail());
                    holder.setNotificationText(inAppNotification.getNotificationText());
                }
            });
        }

        holder.setNotificationClickedListener(model);
        holder.setNotificationTime(TextTimestamp.textShortDateAndTime(model.getTimestamp()));
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public void setNotificationClickedListener(FoxmikeNotification foxmikeNotification) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNotificationClickedListener.OnNotificationClicked(foxmikeNotification);
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

        public void setNotificationImage(String thumb_image) {
            CircleImageView notificationImageIV = (CircleImageView) mView.findViewById(R.id.notification_image);
            Glide.with(notificationImageIV.getContext()).load(thumb_image).into(notificationImageIV);
        }
    }

    private void populateNotificationHashMap(FoxmikeNotification foxmikeNotification, OnNotificationLoadedListener onNotificationLoadedListener) {
        InAppNotification inAppNotification = new InAppNotification();

        if (foxmikeNotification.getType().equals("sessionPost")) {
            ArrayList<Task<?>> asyncTasks = new ArrayList<>();
            // GET SESSION IMAGE URL
            TaskCompletionSource<DataSnapshot> sessionImageSource = new TaskCompletionSource<>();
            Task sessionImageTask = sessionImageSource.getTask();
            asyncTasks.add(sessionImageTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP1()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionImageSource.trySetResult(dataSnapshot);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            // GET SESSION IMAGE NAME
            TaskCompletionSource<DataSnapshot> sessionNameSource = new TaskCompletionSource<>();
            Task sessionNameTask = sessionNameSource.getTask();
            asyncTasks.add(sessionNameTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP1()).child("sessionName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionNameSource.trySetResult(dataSnapshot);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            // GET POST
            TaskCompletionSource<DataSnapshot> postSource = new TaskCompletionSource<>();
            Task postTask = postSource.getTask();
            asyncTasks.add(postTask);

            TaskCompletionSource<DataSnapshot> senderNameSource = new TaskCompletionSource<>();
            Task senderNameTask = senderNameSource.getTask();
            asyncTasks.add(senderNameTask);


            rootDbRef.child("sessionPosts").child(foxmikeNotification.getP1()).child(foxmikeNotification.getSourceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    postSource.trySetResult(dataSnapshot);
                    Post post = dataSnapshot.getValue(Post.class);
                    rootDbRef.child("usersPublic").child(post.getAuthorId()).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()==null) {
                                return;
                            }
                            senderNameSource.trySetResult(dataSnapshot);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            // WHEN ALL LOADED
            Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (sessionImageTask.isSuccessful() && sessionNameTask.isSuccessful() && postTask.isSuccessful()) {
                        String imageUrl = ((DataSnapshot) sessionImageTask.getResult()).getValue().toString();
                        String sessionName = ((DataSnapshot) sessionNameTask.getResult()).getValue().toString();
                        Post post = ((DataSnapshot) postTask.getResult()).getValue(Post.class);
                        String authorName = ((DataSnapshot) senderNameTask.getResult()).getValue().toString();
                        String notificationText = authorName + stringHashMap.get("has_made_a_post_in") + sessionName + ": " + post.getMessage();

                        SpannableStringBuilder notificationTextFormatted = mixBoldAndRegular(
                                notificationText,
                                0,
                                authorName.length(),
                                authorName.length() + stringHashMap.get("has_made_a_post_in").length(),
                                authorName.length() + stringHashMap.get("has_made_a_post_in").length() + sessionName.length());

                        inAppNotification.setNotificationThumbnail(imageUrl);
                        inAppNotification.setNotificationText(notificationTextFormatted);
                        notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);
                        onNotificationLoadedListener.OnNotificationLoaded();
                    }
                }
            });
        }
        if (foxmikeNotification.getType().equals("sessionPostComment")) {
            ArrayList<Task<?>> asyncTasks = new ArrayList<>();

            // GET COMMENT
            TaskCompletionSource<DataSnapshot> messageSource = new TaskCompletionSource<>();
            Task messageTask = messageSource.getTask();
            asyncTasks.add(messageTask);

            TaskCompletionSource<DataSnapshot> senderNameSource = new TaskCompletionSource<>();
            Task senderNameTask = senderNameSource.getTask();
            asyncTasks.add(senderNameTask);

            rootDbRef.child("sessionPostComments").child(foxmikeNotification.getP2()).child(foxmikeNotification.getP1()).child(foxmikeNotification.getSourceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    messageSource.trySetResult(dataSnapshot);
                    Message comment = dataSnapshot.getValue(Message.class);
                    rootDbRef.child("usersPublic").child(comment.getSenderUserID()).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()==null) {
                                return;
                            }
                            senderNameSource.trySetResult(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION IMAGE URL
            TaskCompletionSource<DataSnapshot> sessionImageSource = new TaskCompletionSource<>();
            Task sessionImageTask = sessionImageSource.getTask();
            asyncTasks.add(sessionImageTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP2()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionImageSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION IMAGE NAME
            TaskCompletionSource<DataSnapshot> sessionNameSource = new TaskCompletionSource<>();
            Task sessionNameTask = sessionNameSource.getTask();
            asyncTasks.add(sessionNameTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP2()).child("sessionName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionNameSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // WHEN LOADED
            Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (messageTask.isSuccessful() && sessionImageTask.isSuccessful() && sessionNameTask.isSuccessful()) {
                        Message message = ((DataSnapshot) messageTask.getResult()).getValue(Message.class);
                        String imageUrl = ((DataSnapshot) sessionImageTask.getResult()).getValue().toString();
                        String sessionName = ((DataSnapshot) sessionNameTask.getResult()).getValue().toString();
                        String commentId = ((DataSnapshot) messageTask.getResult()).getKey();
                        String senderName = ((DataSnapshot) senderNameTask.getResult()).getValue().toString();

                        String notificationText = senderName + stringHashMap.get("has_made_a_comment_to_your_post_in") + sessionName + ": " + message.getMessage();

                        SpannableStringBuilder notificationTextFormatted = mixBoldAndRegular(
                                notificationText,
                                0,
                                senderName.length(),
                                senderName.length() + stringHashMap.get("has_made_a_comment_to_your_post_in").length(),
                                senderName.length() + stringHashMap.get("has_made_a_comment_to_your_post_in").length() + sessionName.length());

                        inAppNotification.setNotificationThumbnail(imageUrl);
                        inAppNotification.setNotificationText(notificationTextFormatted);
                        notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);
                        onNotificationLoadedListener.OnNotificationLoaded();
                    }
                }
            });

        }
        if (foxmikeNotification.getType().equals("participantNew") || foxmikeNotification.getType().equals("participantCancellation")) {

            ArrayList<Task<?>> asyncTasks = new ArrayList<>();

            // GET USER NAME
            TaskCompletionSource<DataSnapshot> userNameSource = new TaskCompletionSource<>();
            Task userNameTask = userNameSource.getTask();
            asyncTasks.add(userNameTask);
            rootDbRef.child("usersPublic").child(foxmikeNotification.getSourceId()).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    userNameSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION IMAGE URL
            TaskCompletionSource<DataSnapshot> sessionImageSource = new TaskCompletionSource<>();
            Task sessionImageTask = sessionImageSource.getTask();
            asyncTasks.add(sessionImageTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP2()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionImageSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION NAME
            TaskCompletionSource<DataSnapshot> sessionNameSource = new TaskCompletionSource<>();
            Task sessionNameTask = sessionNameSource.getTask();
            asyncTasks.add(sessionNameTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP2()).child("sessionName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionNameSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // WHEN LOADED
            Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (userNameTask.isSuccessful() && sessionImageTask.isSuccessful() && sessionNameTask.isSuccessful()) {
                        String participantName = ((DataSnapshot) userNameTask.getResult()).getValue().toString();
                        String imageUrl = ((DataSnapshot) sessionImageTask.getResult()).getValue().toString();
                        String sessionName = ((DataSnapshot) sessionNameTask.getResult()).getValue().toString();

                        String text = "";

                        if (foxmikeNotification.getType().equals("participantNew")) {
                            text = stringHashMap.get("has_booked_your_session");
                        }
                        if (foxmikeNotification.getType().equals("participantCancellation")) {
                            text = stringHashMap.get("has_cancelled_you_session");
                        }

                        String notificationText = participantName + text + sessionName;
                        SpannableStringBuilder notificationTextFormatted = mixBoldAndRegular(
                                notificationText,
                                0,
                                participantName.length(),
                                participantName.length() + text.length(),
                                participantName.length() + text.length() + sessionName.length());

                        inAppNotification.setNotificationThumbnail(imageUrl);
                        inAppNotification.setNotificationText(notificationTextFormatted);
                        notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);
                        onNotificationLoadedListener.OnNotificationLoaded();
                    }
                }
            });
        }

        if (foxmikeNotification.getType().equals("sessionCancellation") || foxmikeNotification.getType().equals("freeSessionCancellation")) {

            ArrayList<Task<?>> asyncTasks = new ArrayList<>();

            // GET AD Date
            TaskCompletionSource<DataSnapshot> adDateSource = new TaskCompletionSource<>();
            Task adDateTask = adDateSource.getTask();
            asyncTasks.add(adDateTask);
            rootDbRef.child("advertisements").child(foxmikeNotification.getSourceId()).child("advertisementTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    adDateSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION IMAGE URL
            TaskCompletionSource<DataSnapshot> sessionImageSource = new TaskCompletionSource<>();
            Task sessionImageTask = sessionImageSource.getTask();
            asyncTasks.add(sessionImageTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP1()).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionImageSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // GET SESSION NAME
            TaskCompletionSource<DataSnapshot> sessionNameSource = new TaskCompletionSource<>();
            Task sessionNameTask = sessionNameSource.getTask();
            asyncTasks.add(sessionNameTask);
            rootDbRef.child("sessions").child(foxmikeNotification.getP1()).child("sessionName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    sessionNameSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // WHEN LOADED
            Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (adDateTask.isSuccessful() && sessionImageTask.isSuccessful() && sessionNameTask.isSuccessful()) {
                        Long adTimestamp = (Long) ((DataSnapshot) adDateTask.getResult()).getValue();
                        String imageUrl = ((DataSnapshot) sessionImageTask.getResult()).getValue().toString();
                        String sessionName = ((DataSnapshot) sessionNameTask.getResult()).getValue().toString();

                        String notificationText = "";

                        if (foxmikeNotification.getType().equals("sessionCancellation")) {
                            notificationText = stringHashMap.get("the_session") + sessionName + stringHashMap.get("on") + TextTimestamp.textSessionDate(adTimestamp) + stringHashMap.get("has_been_cancelled") +
                                    stringHashMap.get("you_will_be_refunded");
                        }
                        if (foxmikeNotification.getType().equals("freeSessionCancellation")) {
                            notificationText = stringHashMap.get("the_session") + sessionName + stringHashMap.get("on") + TextTimestamp.textSessionDate(adTimestamp) + stringHashMap.get("has_been_cancelled");
                        }

                        SpannableStringBuilder notificationTextFormatted = mixBoldAndRegular(
                                notificationText,
                                stringHashMap.get("the_session").length(),
                                stringHashMap.get("the_session").length() + sessionName.length(),
                                stringHashMap.get("the_session").length() + sessionName.length() + stringHashMap.get("on").length() +TextTimestamp.textSessionDate(adTimestamp).length(),
                                stringHashMap.get("the_session").length() + sessionName.length() + stringHashMap.get("on").length() +TextTimestamp.textSessionDate(adTimestamp).length() + stringHashMap.get("R.string.has_been_cancelled").length());

                        inAppNotification.setNotificationThumbnail(imageUrl);
                        inAppNotification.setNotificationText(notificationTextFormatted);
                        notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);
                        onNotificationLoadedListener.OnNotificationLoaded();
                    }
                }
            });

        }
        if (foxmikeNotification.getType().equals("friendRequestAccepted")) {

            ArrayList<Task<?>> asyncTasks = new ArrayList<>();

            // GET fullName
            TaskCompletionSource<DataSnapshot> nameSource = new TaskCompletionSource<>();
            Task nameTask = nameSource.getTask();
            asyncTasks.add(nameTask);
            rootDbRef.child("usersPublic").child(foxmikeNotification.getSourceId()).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    nameSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // Get user image
            TaskCompletionSource<DataSnapshot> imageSource = new TaskCompletionSource<>();
            Task imageTask = imageSource.getTask();
            asyncTasks.add(imageTask);
            rootDbRef.child("usersPublic").child(foxmikeNotification.getSourceId()).child("thumb_image").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    imageSource.trySetResult(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // WHEN LOADED
            Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (nameTask.isSuccessful() && imageTask.isSuccessful()) {
                        String imageUrl = ((DataSnapshot) imageTask.getResult()).getValue().toString();
                        String name = ((DataSnapshot) nameTask.getResult()).getValue().toString();

                        String notificationText = name + stringHashMap.get("has_accepted_your_friend_request");

                        SpannableStringBuilder notificationTextFormatted = mixBoldAndRegular(
                                notificationText,
                                0,
                                name.length());

                        inAppNotification.setNotificationThumbnail(imageUrl);
                        inAppNotification.setNotificationText(notificationTextFormatted);
                        notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);
                        onNotificationLoadedListener.OnNotificationLoaded();
                    }
                }
            });


        }
        if (foxmikeNotification.getType().equals("adminNotification")) {

            String notificationText = foxmikeNotification.getP1();
            SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(notificationText);
            inAppNotification.setNotificationText(notificationTextFormatted);
            notificationHashMap.put(foxmikeNotification.getNotificationId(), inAppNotification);

        }


    }

    private SpannableStringBuilder mixBoldAndRegular(String text, int boldStart, int boldStop) {
        SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(text);
        StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
        notificationTextFormatted.setSpan(bold1, boldStart, boldStop, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

        return notificationTextFormatted;
    }

    private SpannableStringBuilder mixBoldAndRegular(String text, int boldStart1, int boldStop1, int boldStart2, int boldStop2) {
        SpannableStringBuilder notificationTextFormatted = new SpannableStringBuilder(text);
        StyleSpan bold1 = new StyleSpan(Typeface.BOLD); // Span to make text bold
        notificationTextFormatted.setSpan(bold1, boldStart1, boldStop1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make name bold

        StyleSpan bold2 = new StyleSpan(Typeface.BOLD); // Span to make text bold
        notificationTextFormatted.setSpan(bold2, boldStart2, boldStop2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return notificationTextFormatted;
    }

    public interface OnNotificationLoadedListener{
        void OnNotificationLoaded();
    }
}
