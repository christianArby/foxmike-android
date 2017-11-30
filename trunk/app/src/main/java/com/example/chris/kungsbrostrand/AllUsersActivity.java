package com.example.chris.kungsbrostrand;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.core.Context;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView allUsersList;
    private DatabaseReference mUsersDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        allUsersList = (RecyclerView) findViewById(R.id.allUsersList);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
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

            }
        };


        /*
        // TODO update gradle with latest FirebaseUI which has different override methods than below
        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(
                User.class,
                R.layout.users_list_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, User user, int position) {

                usersViewHolder.setName(user.getName());

            }
        }; */

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

        public void setUserImage(String name) {
            CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.user_single_image);
        }
    }
    // Method to set and scale an image into an imageView
    public void setImage(String image, ImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }
}
