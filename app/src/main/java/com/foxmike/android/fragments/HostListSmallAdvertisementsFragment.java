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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallAdvertisementsFirebaseAdapter;
import com.foxmike.android.interfaces.AlertOccasionCancelledListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.utils.SmallAdvertisementViewHolder;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class HostListSmallAdvertisementsFragment extends Fragment {

    public static final String TAG = HostListSmallAdvertisementsFragment.class.getSimpleName();

    private OnSessionClickedListener onSessionClickedListener;
    private AlertOccasionCancelledListener alertOccasionCancelledListener;
    private RecyclerView comingAdvertisementsRV;
    private RecyclerView pastAdvertisementsRV;
    private FirebaseRecyclerAdapter<Advertisement, SmallAdvertisementViewHolder> pastFirebaseAdvertisementsAdapter;
    private FirebaseRecyclerAdapter<Advertisement, SmallAdvertisementViewHolder> comingFirebaseAdvertisementsAdapter;
    private TextView noContent;
    private TextView upcomingHeading;
    private TextView pastHeading;
    private HashMap<String, String> sessionTypeDictionary;
    private DotProgressBar loading;


    public HostListSmallAdvertisementsFragment() {
        // Required empty public constructor
    }

    public static HostListSmallAdvertisementsFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        HostListSmallAdvertisementsFragment fragment = new HostListSmallAdvertisementsFragment();
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

        Query futureAdsQuery = rootDbRef.child("advertisementHosts").child(currentUserId).orderByValue().startAt(currentTimestamp);
        FirebaseRecyclerOptions<Advertisement> futureOptions = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(futureAdsQuery, adDbRef, Advertisement.class)
                .build();

        futureAdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
        comingFirebaseAdvertisementsAdapter = new ListSmallAdvertisementsFirebaseAdapter(futureOptions, getActivity().getApplicationContext(), alertOccasionCancelledListener, sessionTypeDictionary,onSessionClickedListener);

        comingFirebaseAdvertisementsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                updateHeadings();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateHeadings();
            }
        });

        Query pastAdsQuery = rootDbRef.child("advertisementHosts").child(currentUserId).orderByValue().startAt(0).endAt(currentTimestamp).limitToLast(100);
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
        pastFirebaseAdvertisementsAdapter = new ListSmallAdvertisementsFirebaseAdapter(pastOptions, getActivity().getApplicationContext(), alertOccasionCancelledListener, sessionTypeDictionary,onSessionClickedListener);

        pastFirebaseAdvertisementsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                updateHeadings();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateHeadings();
            }
        });
    }

    private void updateHeadings() {
        loading.setVisibility(View.GONE);
        if (comingFirebaseAdvertisementsAdapter.getItemCount()>0) {
            noContent.setVisibility(View.GONE);
            upcomingHeading.setVisibility(View.VISIBLE);
        } else {
            upcomingHeading.setVisibility(View.GONE);
        }
        if (pastFirebaseAdvertisementsAdapter.getItemCount()>0) {
            noContent.setVisibility(View.GONE);
            pastHeading.setVisibility(View.VISIBLE);
        } else {
            pastHeading.setVisibility(View.GONE);
        }
        if (comingFirebaseAdvertisementsAdapter.getItemCount()==0 && pastFirebaseAdvertisementsAdapter.getItemCount()==0) {
            noContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        comingFirebaseAdvertisementsAdapter.startListening();
        pastFirebaseAdvertisementsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        comingFirebaseAdvertisementsAdapter.stopListening();
        pastFirebaseAdvertisementsAdapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view =  inflater.inflate(R.layout.fragment_host_list_small_advertisements, container, false);

        upcomingHeading = view.findViewById(R.id.upcomingHeading);
        pastHeading = view.findViewById(R.id.pastHeading);

        comingAdvertisementsRV = (RecyclerView) view.findViewById(R.id.comingAdvertisementsRV);
        comingAdvertisementsRV.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        comingAdvertisementsRV.setAdapter(comingFirebaseAdvertisementsAdapter);
        ((SimpleItemAnimator) comingAdvertisementsRV.getItemAnimator()).setSupportsChangeAnimations(false);

        pastAdvertisementsRV = (RecyclerView) view.findViewById(R.id.pastAdvertisementsRV);
        pastAdvertisementsRV.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        pastAdvertisementsRV.setAdapter(pastFirebaseAdvertisementsAdapter);
        ((SimpleItemAnimator) pastAdvertisementsRV.getItemAnimator()).setSupportsChangeAnimations(false);


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
                    + " must implement OnAdvertisementClickedListener");
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
