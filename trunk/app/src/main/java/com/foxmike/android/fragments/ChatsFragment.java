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
import com.foxmike.android.adapters.ListChatsFirebaseAdapter;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ChatsFragment extends Fragment {

    public static final String TAG = ChatsFragment.class.getSimpleName();

    private OnChatClickedListener onChatClickedListener;
    private RecyclerView chatListRV;
    private TextView noContent;
    private ListChatsFirebaseAdapter listChatsFirebaseAdapter;
    private boolean listChatsFirebaseAdapterCreated;
    private DatabaseReference friendsRef;
    private ValueEventListener friendsListener;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        Long currentTimestamp = System.currentTimeMillis();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatsRef = rootDbRef.child("chats");

        Query chatsQuery = rootDbRef.child("userChats").child(currentUserId).orderByKey();

        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId);
        friendsListener = friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                    listChatsFirebaseAdapter = new ListChatsFirebaseAdapter(chatsOptions, getContext(), friendsMap, onChatClickedListener, new ListChatsFirebaseAdapter.OnNoContentListener() {
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatListRV.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) chatListRV.getItemAnimator()).setSupportsChangeAnimations(false);
        chatListRV.setAdapter(listChatsFirebaseAdapter);
        noContent = view.findViewById(R.id.noContent);
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
        friendsRef.removeEventListener(friendsListener);
    }
}
