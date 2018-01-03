package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView requestsList;

    private DatabaseReference myRequestsDbRef;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View mainView;
    private ValueEventListener userListener;
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private DatabaseReference rootDbRef;

    private ArrayList<String> userIDs = new ArrayList<String>();
    private HashMap<Integer, User> users = new HashMap<Integer, User>();
    private HashMap<String, String> requests = new HashMap<String, String>();

    private RecyclerView.Adapter<UsersViewHolder> requestsViewHolderAdapter;
    private OnUserClickedListener onUserClickedListener;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        requestsList = (RecyclerView) view.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        requestsList.setHasFixedSize(true);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        ValueEventListener friendRequestsListener = rootDbRef.child("friend_requests").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userIDs.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                        userIDs.add(requestSnapshot.getKey());
                        requests.put(requestSnapshot.getKey(), requestSnapshot.child("request_type").getValue().toString());
                    }
                }

                for (String userID : userIDs) {
                    rootDbRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            // hitta positionen i userIDs
                            int pos = userIDs.indexOf(dataSnapshot.getKey());
                            if (users.containsKey(pos)) {
                                users.put(pos,user);
                                requestsViewHolderAdapter.notifyItemChanged(pos);
                            } else {
                                users.put(pos,user);
                                if (users.size()==userIDs.size()) {
                                    requestsViewHolderAdapter.notifyDataSetChanged();
                                }
                            }

                            if (!listenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        int pos = userIDs.indexOf(dataSnapshot.getKey());

                                        if (dataSnapshot.child("connections").getChildrenCount()!=0) {
                                            users.get(pos).setOnline(true);
                                            requestsViewHolderAdapter.notifyItemChanged(pos);
                                        } else {
                                            users.get(pos).setOnline(false);
                                            requestsViewHolderAdapter.notifyItemChanged(pos);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                listenerMap.put(rootDbRef.child("users").child(dataSnapshot.getKey()).child("online"),onlineListener);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listenerMap.put(rootDbRef.child("friend_requests").child(currentUserID), friendRequestsListener);

        requestsViewHolderAdapter = new RecyclerView.Adapter<UsersViewHolder>() {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            public void onBindViewHolder(UsersViewHolder holder, final int position) {

                holder.setText(requests.get(userIDs.get(position)), true);

                final User friend = users.get(position);

                holder.setHeading(friend.getName());
                holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                holder.setOnlineIcon(friend.isOnline());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserClickedListener.OnUserClicked(userIDs.get(position));
                    }
                });

            }

            @Override
            public int getItemCount() {
                return users.size();
            }
        };

        requestsList.setAdapter(requestsViewHolderAdapter);




        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserClickedListener) {
            onUserClickedListener = (OnUserClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onUserClickedListener = null;
    }

}
