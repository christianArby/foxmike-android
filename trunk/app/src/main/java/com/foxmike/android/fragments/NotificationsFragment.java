package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListNotificationsFirebaseAdapter;
import com.foxmike.android.models.FoxmikeNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

public class NotificationsFragment extends Fragment {

    public static final String TAG = NotificationsFragment.class.getSimpleName();

    private OnNotificationClickedListener onNotificationClickedListener;
    private RecyclerView notificationsListRV;
    private TextView noContent;
    private ListNotificationsFirebaseAdapter listNotificationsFirebaseAdapter;
    private RecyclerView.AdapterDataObserver dataObserver;

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

        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set database reference to chat id in message root and build query
        DatabaseReference notificationsRef = rootDbRef.child("notifications").child(currentUserId);
        Query notificationsQuery = notificationsRef.limitToLast(100);
        FirebaseRecyclerOptions<FoxmikeNotification> options =
                new FirebaseRecyclerOptions.Builder<FoxmikeNotification>()
                        .setQuery(notificationsQuery, new SnapshotParser<FoxmikeNotification>() {
                            @NonNull
                            @Override
                            public FoxmikeNotification parseSnapshot(@NonNull DataSnapshot snapshot) {
                                FoxmikeNotification foxmikeNotification = snapshot.getValue(FoxmikeNotification.class);
                                foxmikeNotification.setNotificationId(snapshot.getKey());
                                return foxmikeNotification;
                            }
                        })
                        .build();
        HashMap<String,String> stringHashMap = new HashMap<>();
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
        listNotificationsFirebaseAdapter = new ListNotificationsFirebaseAdapter(options, stringHashMap, onNotificationClickedListener);

        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                if (listNotificationsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (listNotificationsFirebaseAdapter.getItemCount()>0) {
                    notificationsListRV.smoothScrollToPosition(listNotificationsFirebaseAdapter.getItemCount() - 1);
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }
        };

        listNotificationsFirebaseAdapter.registerAdapterDataObserver(dataObserver);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (notificationsListRV!=null && listNotificationsFirebaseAdapter!=null) {
            notificationsListRV.setAdapter(listNotificationsFirebaseAdapter);
        }
        listNotificationsFirebaseAdapter.startListening();
        if (dataObserver!=null) {
            listNotificationsFirebaseAdapter.registerAdapterDataObserver(dataObserver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        listNotificationsFirebaseAdapter.stopListening();
        if (notificationsListRV!=null) {
            notificationsListRV.setAdapter(null);
        }
        if (dataObserver!=null) {
            listNotificationsFirebaseAdapter.unregisterAdapterDataObserver(dataObserver);
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

        if (listNotificationsFirebaseAdapter!=null) {
            listNotificationsFirebaseAdapter.stopListening();
            listNotificationsFirebaseAdapter=null;
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
        notificationsListRV.setAdapter(listNotificationsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);
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
}