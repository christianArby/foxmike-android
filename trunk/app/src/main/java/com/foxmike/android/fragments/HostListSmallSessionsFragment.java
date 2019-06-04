package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsFirebaseAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionAdvertisements;
import com.foxmike.android.viewmodels.FirebaseDatabaseChildListenerViewModel;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class HostListSmallSessionsFragment extends Fragment {

    public static final String TAG = HostListSmallSessionsFragment.class.getSimpleName();

    private OnSessionClickedListener onSessionClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsFirebaseAdapter listSmallSessionsFirebaseAdapter;
    private TextView noContent;
    private HashMap<String, String> sessionTypeDictionary;
    private DotProgressBar loading;
    private HashMap<Query, Boolean> liveDataQueries = new HashMap<>();
    private HashMap<String, SessionAdvertisements> sessionAdvertisementsHashMap = new HashMap<>();


    public HostListSmallSessionsFragment() {
        // Required empty public constructor
    }

    public static HostListSmallSessionsFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        HostListSmallSessionsFragment fragment = new HostListSmallSessionsFragment();
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

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sessionsDbRef = rootDbRef.child("sessions");

        Query sessionsQuery = rootDbRef.child("sessionHosts").child(currentUserId).orderByKey();
        FirebaseRecyclerOptions<Session> sessionsOptions = new FirebaseRecyclerOptions.Builder<Session>()
                .setIndexedQuery(sessionsQuery, sessionsDbRef, Session.class)
                .build();

        // To find out if there are no sessions
        sessionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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


        listSmallSessionsFirebaseAdapter = new ListSmallSessionsFirebaseAdapter(sessionsOptions, getActivity().getApplicationContext(), sessionTypeDictionary, onSessionClickedListener);
        listSmallSessionsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (listSmallSessionsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                loading.setVisibility(View.GONE);
                if (listSmallSessionsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // To listen to changes in sessionAdvertisements
        Query sessionsQuery = FirebaseDatabase.getInstance().getReference().child("sessionHosts").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByKey();
        FirebaseDatabaseChildListenerViewModel sessionsViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseChildListenerViewModel.class);
        LiveData<DataSnapshot> sessionsLiveData = sessionsViewModel.getDataSnapshotLiveData(sessionsQuery);
        sessionsLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    Long currentTimestamp = System.currentTimeMillis();
                    if (!liveDataQueries.containsKey(FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(dataSnapshot.getKey()).orderByValue().startAt(currentTimestamp))) {
                        FirebaseDatabaseViewModel sessionAdvertisementsViewModel = ViewModelProviders.of(HostListSmallSessionsFragment.this).get(FirebaseDatabaseViewModel.class);
                        LiveData<DataSnapshot> sessionAdvertisementsLiveData = sessionAdvertisementsViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(dataSnapshot.getKey()).orderByValue().startAt(currentTimestamp));
                        liveDataQueries.put(FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(dataSnapshot.getKey()).orderByValue().startAt(currentTimestamp), true);
                        sessionAdvertisementsLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
                            @Override
                            public void onChanged(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue()==null) {
                                    sessionAdvertisementsHashMap.put(dataSnapshot.getKey(), new SessionAdvertisements());
                                    // notify
                                    if (listSmallSessionsFirebaseAdapter!=null) {
                                        listSmallSessionsFirebaseAdapter.updateSessionAdvertisements(sessionAdvertisementsHashMap);
                                    }
                                    return;
                                }
                                SessionAdvertisements sessionAdvertisements = new SessionAdvertisements();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    sessionAdvertisements.put(snapshot.getKey(), (Long) snapshot.getValue());
                                }
                                sessionAdvertisementsHashMap.put(dataSnapshot.getKey(), sessionAdvertisements);
                                if (listSmallSessionsFirebaseAdapter!=null) {
                                    listSmallSessionsFirebaseAdapter.updateSessionAdvertisements(sessionAdvertisementsHashMap);
                                }
                            }
                        });
                    }
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
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        smallSessionsListRV.setAdapter(listSmallSessionsFirebaseAdapter);
        ((SimpleItemAnimator) smallSessionsListRV.getItemAnimator()).setSupportsChangeAnimations(false);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }
}
