package com.foxmike.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;

public class GetUserActivity extends AppCompatActivity {

    private EditText searchFieldET;
    private RecyclerView allUsersList;
    private DatabaseReference mUsersDatabase;
    private LinearLayoutManager linearLayoutManager;
    public OnUserClickedListener onUserClickedListener;
    private FirebaseRecyclerAdapter<User,UsersViewHolder> firebaseRecyclerAdapter;
    private Toolbar searchToolbar;
    private FirebaseAuth mAuth;
    private String currentUserID;
    PublishProcessor<String> pp;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user);

        searchToolbar = (Toolbar)  findViewById(R.id.search_users_bar);
        setSupportActionBar(searchToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        View action_bar_view = getLayoutInflater().inflate(R.layout.search_bar, null);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // make sure the whole action bar is filled with the custom view
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);


        searchFieldET = findViewById(R.id.searchField);
        allUsersList = (RecyclerView) findViewById(R.id.allUsersList);
        allUsersList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        allUsersList.setLayoutManager(linearLayoutManager);

        searchFieldET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pp.onNext(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        pp = PublishProcessor.create();
        pp.debounce(500, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        userSearch(s);
                    }
                });

    }

    private void userSearch(final String searchText) {
        // Only run function if searchtext is not empty.
        if (!searchText.equals("")) {
            // If searchText starts with @ we will search for users with username in seachtext
            if (searchText.substring(0,1).equals("@")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (firebaseRecyclerAdapter!=null) {
                            firebaseRecyclerAdapter.stopListening();
                        }
                        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("usersPublic");
                        // Find usernames which starts with our searchtext and ends with our searchtext and any other text (any other text = "\uf8ff")
                        Query query = mUsersDatabase.orderByChild("userName").startAt(searchText).endAt(searchText + "\uf8ff").limitToFirst(100);

                        FirebaseRecyclerOptions<User> options =
                                new FirebaseRecyclerOptions.Builder<User>()
                                        .setQuery(query, User.class)
                                        .build();
                        // Setup our RecyclerView
                        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
                            @Override
                            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.users_list_single_layout, parent, false);
                                return new UsersViewHolder(view);
                            }
                            @Override
                            protected void onBindViewHolder(UsersViewHolder holder, int position, User model) {
                                holder.setHeading(model.getFullName());
                                holder.setText(model.getUserName(),true);
                                holder.setUserImage(model.getThumb_image(), GetUserActivity.this);
                                final String userId = getRef(position).getKey();
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                            return;
                                        }
                                        mLastClickTime = SystemClock.elapsedRealtime();

                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("secondaryHostId", userId);
                                        resultIntent.putExtra("secondaryHostFullName", model.getFirstName() + " " + model.getLastName());
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                });
                            }
                        };
                        allUsersList.setAdapter(firebaseRecyclerAdapter);
                        firebaseRecyclerAdapter.startListening();
                    }
                });

            } else {
                // If searchtext does not start with @ we will search for users with the fullName in the searchtext
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (firebaseRecyclerAdapter!=null) {
                            firebaseRecyclerAdapter.stopListening();
                        }
                        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("usersPublic");
                        // Find fullnames which starts with our searchtext and ends with our searchtext and any other text (any other text = "\uf8ff")
                        Query query = mUsersDatabase.orderByChild("fullName").startAt(searchText).endAt(searchText + "\uf8ff").limitToFirst(100);

                        FirebaseRecyclerOptions<User> options =
                                new FirebaseRecyclerOptions.Builder<User>()
                                        .setQuery(query, User.class)
                                        .build();
                        // Setup our RecyclerView
                        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
                            @Override
                            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.users_list_single_layout, parent, false);
                                return new UsersViewHolder(view);
                            }
                            @Override
                            protected void onBindViewHolder(UsersViewHolder holder, int position, User model) {
                                holder.setHeading(model.getFullName());
                                holder.setText(model.getUserName(),true);
                                holder.setUserImage(model.getThumb_image(), GetUserActivity.this);

                                final String userId = getRef(position).getKey();

                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                            return;
                                        }
                                        mLastClickTime = SystemClock.elapsedRealtime();
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("secondaryHostId", userId);
                                        resultIntent.putExtra("secondaryHostFullName", model.getFirstName() + " " + model.getLastName());
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();

                                    }
                                });
                            }
                        };

                        allUsersList.setAdapter(firebaseRecyclerAdapter);
                        firebaseRecyclerAdapter.startListening();
                    }
                });
            }
        }
    }
}
