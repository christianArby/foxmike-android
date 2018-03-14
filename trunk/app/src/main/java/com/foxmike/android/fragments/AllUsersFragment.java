package com.foxmike.android.fragments;
//Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * This fragment lists all users in database with a firebase recycler adapter and query
 */

public class AllUsersFragment extends Fragment {

    private RecyclerView allUsersList;
    private DatabaseReference mUsersDatabase;
    public OnUserClickedListener onUserClickedListener;

    public AllUsersFragment() {
        // Required empty public constructor
    }

    public static AllUsersFragment newInstance() {
        AllUsersFragment fragment = new AllUsersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_users, container, false);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        allUsersList = (RecyclerView) view.findViewById(R.id.allUsersList);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mUsersDatabase;

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();


        FirebaseRecyclerAdapter<User,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(UsersViewHolder holder, int position, User model) {
                holder.setName(model.getName());
                holder.setStatus(model.getName());
                holder.setUserImage(model.getThumb_image(), getActivity().getApplicationContext());

                final String userId = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserClickedListener.OnUserClicked(userId);
                    }
                });

            }
        };

        allUsersList.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameTV = (TextView) mView.findViewById(R.id.user_single_name);
            userNameTV.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusTV = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusTV.setText(status);
        }

        public void setUserImage(String thumb_image, android.content.Context context) {
            CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Glide.with(context).load(thumb_image).into(userProfileImageIV);
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
