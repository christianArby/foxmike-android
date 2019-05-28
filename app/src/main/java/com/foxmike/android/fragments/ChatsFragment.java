package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.foxmike.android.adapters.ListChatsFirebaseAdapter;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.Chats;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

public class ChatsFragment extends Fragment {

    public static final String TAG = ChatsFragment.class.getSimpleName();

    private OnChatClickedListener onChatClickedListener;
    private RecyclerView chatListRV;
    private TextView noContent;
    private ListChatsFirebaseAdapter listChatsFirebaseAdapter;
    private boolean listChatsFirebaseAdapterCreated;
    private DatabaseReference friendsRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    public static ChatsFragment newInstance() {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listChatsFirebaseAdapter!=null) {
            listChatsFirebaseAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listChatsFirebaseAdapter!=null) {
            listChatsFirebaseAdapter.stopListening();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chatListRV = (RecyclerView) view.findViewById(R.id.chatsRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatListRV.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) chatListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        chatListRV.setAdapter(listChatsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);

        Long currentTimestamp = System.currentTimeMillis();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatsRef = rootDbRef.child("chats");

        Query chatsQuery = rootDbRef.child("userChats").child(currentUserId).orderByKey();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId);

        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(friendsRef);
        firebaseDatabaseLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> friendsMap = new HashMap<>();
                if (dataSnapshot.getValue()!=null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        friendsMap.put(snapshot.getKey(), true);
                    }
                }
                if (!listChatsFirebaseAdapterCreated) {
                    FirebaseRecyclerOptions<Chats> chatsOptions = new FirebaseRecyclerOptions.Builder<Chats>()
                            .setIndexedQuery(chatsQuery, chatsRef, Chats.class)
                            .build();
                    listChatsFirebaseAdapter = new ListChatsFirebaseAdapter(chatsOptions, getActivity().getApplicationContext(), friendsMap, onChatClickedListener, new ListChatsFirebaseAdapter.OnNoContentListener() {
                        @Override
                        public void OnNoContentVisible(Boolean noContentVisible) {
                            if (noContentVisible) {
                                noContent.setVisibility(View.VISIBLE);
                            } else {
                                noContent.setVisibility(View.GONE);
                            }
                        }
                    });
                    listChatsFirebaseAdapter.startListening();
                    if (getView()!=null) {
                        chatListRV.setAdapter(listChatsFirebaseAdapter);
                    }
                    listChatsFirebaseAdapterCreated = true;
                } else {
                    listChatsFirebaseAdapter.friendsUpdated(friendsMap);
                }

            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatClickedListener) {
            onChatClickedListener = (OnChatClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onChatClickedListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listChatsFirebaseAdapterCreated = false;
    }
}
