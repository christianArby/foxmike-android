package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsFirebaseAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class HostListSmallSessionsFragment extends Fragment {

    private OnSessionClickedListener onSessionClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsFirebaseAdapter listSmallSessionsFirebaseAdapter;
    private TextView noContent;


    public HostListSmallSessionsFragment() {
        // Required empty public constructor
    }

    public static HostListSmallSessionsFragment newInstance() {
        HostListSmallSessionsFragment fragment = new HostListSmallSessionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sessionsDbRef = rootDbRef.child("sessions");

        Query sessionsQuery = rootDbRef.child("users").child(currentUserId).child("sessionsHosting").orderByKey();
        FirebaseRecyclerOptions<Session> sessionsOptions = new FirebaseRecyclerOptions.Builder<Session>()
                .setIndexedQuery(sessionsQuery, sessionsDbRef, Session.class)
                .build();
        listSmallSessionsFirebaseAdapter = new ListSmallSessionsFirebaseAdapter(sessionsOptions, getContext(), onSessionClickedListener);
        listSmallSessionsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (listSmallSessionsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        listSmallSessionsFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        listSmallSessionsFirebaseAdapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view =  inflater.inflate(R.layout.fragment_host_list_small_sessions, container, false);
        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        smallSessionsListRV.setAdapter(listSmallSessionsFirebaseAdapter);
        ((SimpleItemAnimator) smallSessionsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        noContent = view.findViewById(R.id.noContent);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }
}
