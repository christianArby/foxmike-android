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
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallAdvertisementsFirebaseAdapter;
import com.foxmike.android.interfaces.AlertOccasionCancelledListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PlayerListSmallAdvertisementsHistoryFragment extends Fragment {

    public static final String TAG = PlayerListSmallAdvertisementsHistoryFragment.class.getSimpleName();

    private OnSessionClickedListener onSessionClickedListener;
    private RecyclerView smallAdvertisementsListRV;
    private TextView noContent;
    private ListSmallAdvertisementsFirebaseAdapter pastAdvertisementsFirebaseAdapter;
    private AlertOccasionCancelledListener alertOccasionCancelledListener;
    private HashMap<String, String> sessionTypeDictionary;
    private DotProgressBar loading;

    public PlayerListSmallAdvertisementsHistoryFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallAdvertisementsHistoryFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        PlayerListSmallAdvertisementsHistoryFragment fragment = new PlayerListSmallAdvertisementsHistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }
        //initData();
        Long currentTimestamp = System.currentTimeMillis();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference adDbRef = rootDbRef.child("advertisements");

        Query pastAdsQuery = rootDbRef.child("advertisementAttendees").child(currentUserId).orderByValue().startAt(0).endAt(currentTimestamp).limitToLast(100);
        FirebaseRecyclerOptions<Advertisement> pastOptions = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(pastAdsQuery, adDbRef, Advertisement.class)
                .build();

        pastAdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    loading.setVisibility(View.GONE);
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        pastAdvertisementsFirebaseAdapter = new ListSmallAdvertisementsFirebaseAdapter(pastOptions, getActivity().getApplicationContext(), alertOccasionCancelledListener, sessionTypeDictionary, onSessionClickedListener);

        pastAdvertisementsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (pastAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (pastAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                loading.setVisibility(View.GONE);
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        smallAdvertisementsListRV.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) smallAdvertisementsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        smallAdvertisementsListRV.setAdapter(pastAdvertisementsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);
        loading = view.findViewById(R.id.firstLoadProgressBar);
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
        if (context instanceof AlertOccasionCancelledListener) {
            alertOccasionCancelledListener = (AlertOccasionCancelledListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlertOccasionCancelledListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        alertOccasionCancelledListener = null;
    }

}
