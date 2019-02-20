package com.foxmike.android.fragments;
//Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * This fragment lists all users in database with a firebase recycler adapter and query
 */

public class AllUsersFragment extends Fragment {

    private EditText searchFieldET;
    private RecyclerView allUsersList;
    private DatabaseReference mUsersDatabase;
    private LinearLayoutManager linearLayoutManager;
    public OnUserClickedListener onUserClickedListener;
    private FirebaseRecyclerAdapter<User,UsersViewHolder> firebaseRecyclerAdapter;
    private Toolbar searchToolbar;
    private View view;
    private FirebaseAuth mAuth;
    private String currentUserID;
    PublishProcessor<String> pp;
    private long mLastClickTime = 0;



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
        view = inflater.inflate(R.layout.fragment_all_users, container, false);

        searchToolbar = (Toolbar)  view.findViewById(R.id.search_users_bar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(searchToolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        View action_bar_view = inflater.inflate(R.layout.search_bar, null);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // make sure the whole action bar is filled with the custom view
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);


        searchFieldET = view.findViewById(R.id.searchField);
        allUsersList = (RecyclerView) view.findViewById(R.id.allUsersList);
        allUsersList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getActivity());
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

        return view;
    }

    private void userSearch(final String searchText) {
        // Only run function if searchtext is not empty.
        if (!searchText.equals("")) {
            // If searchText starts with @ we will search for users with username in seachtext
            if (searchText.substring(0,1).equals("@")) {
                getActivity().runOnUiThread(new Runnable() {
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
                                holder.setUserImage(model.getThumb_image(), getActivity().getApplicationContext());
                                final String userId = getRef(position).getKey();
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                            return;
                                        }
                                        mLastClickTime = SystemClock.elapsedRealtime();
                                        if (userId.equals(currentUserID)) {
                                            Toast.makeText(getActivity(), "This is you", Toast.LENGTH_SHORT).show();
                                        } else {
                                            onUserClickedListener.OnUserClicked(userId);
                                        }
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
                getActivity().runOnUiThread(new Runnable() {
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
                                holder.setText(model.getFullName(),true);
                                holder.setUserImage(model.getThumb_image(), getActivity().getApplicationContext());

                                final String userId = getRef(position).getKey();

                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                            return;
                                        }
                                        mLastClickTime = SystemClock.elapsedRealtime();
                                        if (userId.equals(currentUserID)) {
                                            Toast.makeText(getActivity(), "This is you", Toast.LENGTH_SHORT).show();
                                        } else {
                                            onUserClickedListener.OnUserClicked(userId);
                                        }

                                    }
                                });
                            }
                        };

                        allUsersList.setAdapter(firebaseRecyclerAdapter);
                        firebaseRecyclerAdapter.startListening();

                    }
                });

            }
            // TEMPORARY TODO DELETE
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (firebaseRecyclerAdapter!=null) {
                        firebaseRecyclerAdapter.stopListening();
                    }

                    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("usersPublic");
                    Query query = mUsersDatabase.orderByChild("fullName").startAt(searchText).endAt(searchText + "\uf8ff").limitToFirst(100);

                    FirebaseRecyclerOptions<User> options =
                            new FirebaseRecyclerOptions.Builder<User>()
                                    .setQuery(query, User.class)
                                    .build();

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
                            holder.setUserImage(model.getThumb_image(), getActivity().getApplicationContext());

                            final String userId = getRef(position).getKey();

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                        return;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    if (userId.equals(currentUserID)) {
                                        Toast.makeText(getActivity(), "This is you", Toast.LENGTH_SHORT).show();
                                    } else {
                                        onUserClickedListener.OnUserClicked(userId);
                                    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
