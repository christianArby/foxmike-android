package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.data.Freezable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private OnUserClickedListener onUserClickedListener;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        myFriendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = myFriendsDbRef;

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();


        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(final FriendsViewHolder holder, int position, Friends model) {

                holder.setDate(model.getDate());

                final String friendUserID = getRef(position).getKey();

                usersDatabase.child(friendUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User friend = dataSnapshot.getValue(User.class);

                        holder.setName(friend.getName());
                        holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                        holder.setOnlineIcon(friend.isOnline());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserClickedListener.OnUserClicked(friendUserID);
                    }
                });

            }
        };

        friendsList.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();

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
    }
}
