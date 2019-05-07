package com.foxmike.android.fragments;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListNotificationsAdapter;
import com.foxmike.android.models.FoxmikeNotification;
import com.foxmike.android.models.InAppNotification;
import com.foxmike.android.models.Message;
import com.foxmike.android.models.Post;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsFragment extends Fragment {

    public static final String TAG = NotificationsFragment.class.getSimpleName();

    private OnNotificationClickedListener onNotificationClickedListener;
    private RecyclerView notificationsListRV;
    private TextView noContent;
    private ListNotificationsAdapter listNotificationsAdapter;
    private RecyclerView.AdapterDataObserver dataObserver;
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DotProgressBar loading;
    private HashMap<String, String> stringHashMap;
    private HashMap<String, InAppNotification> notificationHashMap = new HashMap<>();

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance() {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }



        // Set database reference to chat id in message root and build query
        DatabaseReference notificationsRef = rootDbRef.child("notifications").child(currentUserId);
        Query notificationsQuery = notificationsRef.limitToLast(100);

        notificationsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    if (isAdded()) {
                        loading.setVisibility(View.GONE);
                        noContent.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        notificationsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FoxmikeNotification foxmikeNotification = dataSnapshot.getValue(FoxmikeNotification.class);
                foxmikeNotification.setNotificationId(dataSnapshot.getKey());
                populateNotificationHashMap(foxmikeNotification, new OnNotificationLoadedListener() {
                    @Override
                    public void OnNotificationLoaded() {
                        listNotificationsAdapter.updateNotificationHashMap(notificationHashMap);
                        listNotificationsAdapter.add(foxmikeNotification);
                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        stringHashMap = new HashMap<>();
        stringHashMap.put("has_made_a_post_in", getResources().getString(R.string.has_made_a_post_in));
        stringHashMap.put("has_made_a_comment_to_your_post_in", getResources().getString(R.string.has_made_a_comment_to_your_post_in));
        stringHashMap.put("has_booked_your_session", getResources().getString(R.string.has_booked_your_session));
        stringHashMap.put("has_cancelled_you_session", getResources().getString(R.string.has_cancelled_you_session));
        stringHashMap.put("the_session", getResources().getString(R.string.the_session));
        stringHashMap.put("on", getResources().getString(R.string.on));
        stringHashMap.put("has_been_cancelled", getResources().getString(R.string.has_been_cancelled));
        stringHashMap.put("you_will_be_refunded", getResources().getString(R.string.you_will_be_refunded));
        stringHashMap.put("has_accepted_your_friend_request", getResources().getString(R.string.has_accepted_your_friend_request));
        //Setup message firebase adapter which loads 10 first messages

        int unreadColor = getResources().getColor(R.color.foxmikeSelectedColor);
        int readColor = getResources().getColor(R.color.color_background_light);


        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (listNotificationsAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (listNotificationsAdapter.getItemCount()>0) {
                    notificationsListRV.smoothScrollToPosition(listNotificationsAdapter.getItemCount() - 1);
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }
        };

        listNotificationsAdapter = new ListNotificationsAdapter(stringHashMap, null, readColor, unreadColor, onNotificationClickedListener);
        listNotificationsAdapter.registerAdapterDataObserver(dataObserver);

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
                                stringHashMap.get("the_session").length() + sessionName.length() + stringHashMap.get("on").length() +TextTimestamp.textSessionDate(adTimestamp).length() + stringHashMap.get("has_been_cancelled").length());

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

    @Override
    public void onStart() {
        super.onStart();
        if (notificationsListRV!=null && listNotificationsAdapter!=null) {
            notificationsListRV.setAdapter(listNotificationsAdapter);
        }
        if (dataObserver!=null) {
            listNotificationsAdapter.registerAdapterDataObserver(dataObserver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (notificationsListRV!=null) {
            notificationsListRV.setAdapter(null);
        }
        if (dataObserver!=null) {
            listNotificationsAdapter.unregisterAdapterDataObserver(dataObserver);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (notificationsListRV!=null) {
            notificationsListRV.setAdapter(null);
            notificationsListRV = null;
        }
        if (noContent!=null) {
            noContent=null;
        }

        if (listNotificationsAdapter!=null) {
            listNotificationsAdapter=null;
        }
        if (dataObserver!=null) {
            dataObserver = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationsListRV = (RecyclerView) view.findViewById(R.id.notificationsListRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        notificationsListRV.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) notificationsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        //notificationsListRV.setAdapter(listNotificationsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);
        loading = view.findViewById(R.id.firstLoadProgressBar);



        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> notificationsLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("unreadNotifications").child(currentUserId));
        notificationsLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (listNotificationsAdapter!=null) {
                    listNotificationsAdapter.updateUnreadNotifications(dataSnapshot);
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNotificationClickedListener) {
            onNotificationClickedListener = (OnNotificationClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNotificationClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNotificationClickedListener = null;
    }

    public interface OnNotificationClickedListener {
        void OnNotificationClicked(FoxmikeNotification foxmikeNotification);
    }

    public interface OnNotificationLoadedListener{
        void OnNotificationLoaded();
    }
}