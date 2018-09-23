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
import com.foxmike.android.interfaces.OnAdvertisementClickedListener;
import com.foxmike.android.models.Advertisement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class PlayerListSmallAdvertisementsBookedFragment extends Fragment {

    private OnAdvertisementClickedListener onAdvertisementClickedListener;
    private RecyclerView smallAdvertisementsListRV;
    private TextView noContent;
    private ListSmallAdvertisementsFirebaseAdapter bookedAdvertisementsFirebaseAdapter;

    public PlayerListSmallAdvertisementsBookedFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallAdvertisementsBookedFragment newInstance() {
        PlayerListSmallAdvertisementsBookedFragment fragment = new PlayerListSmallAdvertisementsBookedFragment();
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

        Query futureAdsQuery = rootDbRef.child("users").child(currentUserId).child("sessionsAttending").orderByValue().startAt(currentTimestamp);
        FirebaseRecyclerOptions<Advertisement> futureOptions = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(futureAdsQuery, adDbRef, Advertisement.class)
                .build();
        bookedAdvertisementsFirebaseAdapter = new ListSmallAdvertisementsFirebaseAdapter(futureOptions, getContext(), onAdvertisementClickedListener);

    }

    @Override
    public void onStart() {
        super.onStart();
        bookedAdvertisementsFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        bookedAdvertisementsFirebaseAdapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view = inflater.inflate(R.layout.fragment_player_list_small_advertisements_booked, container, false);
        smallAdvertisementsListRV = (RecyclerView) view.findViewById(R.id.small_advertisement_list_RV);
        smallAdvertisementsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        ((SimpleItemAnimator) smallAdvertisementsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        smallAdvertisementsListRV.setAdapter(bookedAdvertisementsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);
        noContent.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAdvertisementClickedListener) {
            onAdvertisementClickedListener = (OnAdvertisementClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdvertisementClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onAdvertisementClickedListener = null;
    }

}
