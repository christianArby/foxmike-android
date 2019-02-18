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
import com.foxmike.android.adapters.ListSmallAdvertisementsFirebaseAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class PlayerListSmallAdvertisementsHistoryFragment extends Fragment {

    private OnSessionClickedListener onSessionClickedListener;
    private RecyclerView smallAdvertisementsListRV;
    private TextView noContent;
    private ListSmallAdvertisementsFirebaseAdapter pastAdvertisementsFirebaseAdapter;

    public PlayerListSmallAdvertisementsHistoryFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallAdvertisementsHistoryFragment newInstance() {
        PlayerListSmallAdvertisementsHistoryFragment fragment = new PlayerListSmallAdvertisementsHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        //initData();
        Long currentTimestamp = System.currentTimeMillis();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference adDbRef = rootDbRef.child("advertisements");

        Query pastAdsQuery = rootDbRef.child("advertisementAttendees").child(currentUserId).orderByValue().startAt(0).endAt(currentTimestamp);
        FirebaseRecyclerOptions<Advertisement> pastOptions = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(pastAdsQuery, adDbRef, Advertisement.class)
                .build();
        pastAdvertisementsFirebaseAdapter = new ListSmallAdvertisementsFirebaseAdapter(pastOptions, getContext(), onSessionClickedListener);

        pastAdvertisementsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                if (pastAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (pastAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        pastAdvertisementsFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        pastAdvertisementsFirebaseAdapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view = inflater.inflate(R.layout.fragment_player_list_small_advertisements_history, container, false);
        smallAdvertisementsListRV = (RecyclerView) view.findViewById(R.id.small_advertisement_list_RV);
        smallAdvertisementsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        ((SimpleItemAnimator) smallAdvertisementsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        smallAdvertisementsListRV.setAdapter(pastAdvertisementsFirebaseAdapter);
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
