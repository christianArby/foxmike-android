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
import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.data.Freezable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;

    private DatabaseReference myFriendsDbRef;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View mainView;
    private ValueEventListener userListener;
    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter;
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerMap;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private DatabaseReference rootDbRef;

    private ArrayList<String> userIDs = new ArrayList<String>();
    private HashMap<Integer, User> users = new HashMap<Integer, User>();

    private OnUserClickedListener onUserClickedListener;

    private RecyclerView.Adapter<FriendsViewHolder> friendsViewHolderAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        valueEventListenerMap = new HashMap<>();

        currentUserID = mAuth.getCurrentUser().getUid();


        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        myFriendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        ValueEventListener friendsListener = myFriendsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //nollställ alla IDs
                userIDs.clear();

                // Samla alla userIDs som användaren är vän med
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userIDs.add(child.getKey());
                }

                // Loopa alla userIDs
                for (String key : userIDs) {

                    usersDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            // hitta positionen i userIDs
                            int pos = userIDs.indexOf(dataSnapshot.getKey());
                            if (users.containsKey(pos)) {
                                users.put(pos,user);
                                friendsViewHolderAdapter.notifyItemChanged(pos);
                            } else {
                                users.put(pos,user);
                                if (users.size()==userIDs.size()) {
                                    friendsViewHolderAdapter.notifyDataSetChanged();
                                }
                            }

                            if (!listenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue()!=null) {
                                            String isOnline = dataSnapshot.getValue().toString();

                                            int pos = userIDs.indexOf(dataSnapshot.getKey());

                                            if (isOnline.equals("true")) {
                                                users.get(pos).setOnline(true);
                                                friendsViewHolderAdapter.notifyItemChanged(pos);
                                            } else {
                                                users.get(pos).setOnline(false);
                                                friendsViewHolderAdapter.notifyItemChanged(pos);
                                            }
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
        listenerMap.put(myFriendsDbRef,friendsListener);

        friendsViewHolderAdapter = new RecyclerView.Adapter<FriendsViewHolder>() {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            public void onBindViewHolder(FriendsViewHolder holder, final int position) {

                holder.setDate("nothing");

                final User friend = users.get(position);

                holder.setName(friend.getName());
                holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                holder.setOnlineIcon(friend.isOnline());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        CharSequence options[] = new CharSequence[]{"Open profile", "Send message"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setTitle("Select option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i == 0) {
                                    onUserClickedListener.OnUserClicked(userIDs.get(position));
                                }

                                if (i == 1) {

                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("userID", userIDs.get(position));
                                    chatIntent.putExtra("userName", friend.getName());
                                    chatIntent.putExtra("userThumbImage", friend.getThumb_image());
                                    chatIntent.putExtra("userLastSeen", friend.getLastSeen());
                                    startActivity(chatIntent);

                                }


                            }
                        });

                        builder.show();


                    }
                });

            }

            @Override
            public int getItemCount() {
                return users.size();
            }
        };

        friendsList.setAdapter(friendsViewHolderAdapter);

        return mainView;
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameTV = (TextView) mView.findViewById(R.id.user_single_name);
            userNameTV.setText(name);
        }

        public void setDate(String date) {
            TextView userStatusTV = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusTV.setText(date);
        }

        public void setUserImage(String thumb_image, android.content.Context context) {
            CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Glide.with(context).load(thumb_image).into(userProfileImageIV);
        }

        public void setOnlineIcon(boolean userOnlineStatus) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_iconIV);

            if (userOnlineStatus) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }
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
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    public void cleanListeners () {
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
}
