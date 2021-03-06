package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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

public class PlayerListSmallAdvertisementsBookedFragment extends Fragment {

    public static final String TAG = PlayerListSmallAdvertisementsBookedFragment.class.getSimpleName();

    private OnSessionClickedListener onSessionClickedListener;
    private RecyclerView smallAdvertisementsListRV;
    private TextView noContent;
    private DotProgressBar loading;
    private ListSmallAdvertisementsFirebaseAdapter bookedAdvertisementsFirebaseAdapter;
    private AlertOccasionCancelledListener alertOccasionCancelledListener;
    private HashMap<String, String> sessionTypeDictionary;
    private Query futureAdsQuery;
    private boolean hasContent;

    public PlayerListSmallAdvertisementsBookedFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallAdvertisementsBookedFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        PlayerListSmallAdvertisementsBookedFragment fragment = new PlayerListSmallAdvertisementsBookedFragment();
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

        futureAdsQuery = rootDbRef.child("advertisementAttendees").child(currentUserId).orderByValue().startAt(currentTimestamp);
        FirebaseRecyclerOptions<Advertisement> futureOptions = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(futureAdsQuery, adDbRef, Advertisement.class)
                .build();

        ValueEventListener futureAdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    hasContent = false;
                } else {
                    hasContent = true;
                }
                checkIfHasContent();
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        futureAdsQuery.addValueEventListener(futureAdsListener);

        bookedAdvertisementsFirebaseAdapter = new ListSmallAdvertisementsFirebaseAdapter(futureOptions, getActivity().getApplicationContext(), alertOccasionCancelledListener, sessionTypeDictionary, onSessionClickedListener);

        bookedAdvertisementsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (bookedAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (bookedAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (bookedAdvertisementsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void checkIfHasContent() {
        if (hasContent && isAdded()) {
            noContent.setVisibility(View.GONE);
        } else {
            noContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bookedAdvertisementsFirebaseAdapter.startListening();
        checkIfHasContent();
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
