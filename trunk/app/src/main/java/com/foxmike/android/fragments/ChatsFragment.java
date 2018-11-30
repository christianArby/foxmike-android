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
import com.foxmike.android.adapters.ListChatsFirebaseAdapter;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatsFragment extends Fragment {

    private OnChatClickedListener onChatClickedListener;
    private RecyclerView chatListRV;
    private TextView noContent;
    private ListChatsFirebaseAdapter listChatsFirebaseAdapter;

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

        Query chatsQuery = rootDbRef.child("chatMembers").child(currentUserId).orderByKey();
        FirebaseRecyclerOptions<Chats> chatsOptions = new FirebaseRecyclerOptions.Builder<Chats>()
                .setIndexedQuery(chatsQuery, chatsRef, Chats.class)
                .build();
        listChatsFirebaseAdapter = new ListChatsFirebaseAdapter(chatsOptions, getContext(), onChatClickedListener);

        listChatsFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                if (listChatsFirebaseAdapter.getItemCount()>0) {
                    noContent.setVisibility(View.GONE);
                } else {
                    noContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (listChatsFirebaseAdapter.getItemCount()>0) {
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
        listChatsFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        listChatsFirebaseAdapter.stopListening();
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

}
