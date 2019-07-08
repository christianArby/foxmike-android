package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.SparseArray;
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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListFriendsFirebaseAdapter;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.UserBranch;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.UsersViewHolder;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This fragment lists all the current user's friend requests and friends
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    private RecyclerView friendsList;
    private DatabaseReference myFriendsDbRef;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View mainView;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    //private HashMap<DatabaseReference, ValueEventListener> presenceListenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private DatabaseReference rootDbRef;
    private ArrayList<String> friendUserIDs = new ArrayList<String>();
    private ArrayList<String> requestsUserIDs = new ArrayList<String>();
    private HashMap<String, String> requests = new HashMap<String, String>();
    private ArrayList<UserBranch> userBranches = new ArrayList<>();
    private SparseArray<String> userPosition = new SparseArray<String>();
    //private HashMap<Integer, Presence> presenceHashMap = new HashMap<Integer, Presence>();
    private OnUserClickedListener onUserClickedListener;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> friendsViewHolderAdapter;
    private RecyclerView requestsList;
    private HashMap<Integer, UserPublic> requestUsers = new HashMap<Integer, UserPublic>();
    private RecyclerView.Adapter<UsersViewHolder> requestsViewHolderAdapter;
    private TextView requestsHeading;
    private TextView friendsHeading;
    private TextView noContent;
    private TextView noFriends;
    private long mLastClickTime = 0;
    private HashMap<String, Boolean> contentMap = new HashMap<>();
    private ListFriendsFirebaseAdapter listFriendsFirebaseAdapter;
    private DotProgressBar firstLoadProgressBar;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listFriendsFirebaseAdapter!=null) {
            listFriendsFirebaseAdapter.stopListening();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getUserVisibleHint()) {
            if (listFriendsFirebaseAdapter!=null) {
                listFriendsFirebaseAdapter.startListening();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentMap.put("requests", false);
        contentMap.put("friends", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootDbRef = FirebaseDatabase.getInstance().getReference();
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("usersPublic");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        requestsHeading = mainView.findViewById(R.id.friendRequestsHeadingTV);
        friendsHeading = mainView.findViewById(R.id.friendsHeadingTV);
        noFriends = mainView.findViewById(R.id.noFriends);
        noContent = mainView.findViewById(R.id.noContent);
        firstLoadProgressBar = mainView.findViewById(R.id.firstLoadProgressBar);

        noContent.setVisibility(View.VISIBLE);
        requestsHeading.setVisibility(View.GONE);
        friendsHeading.setVisibility(View.GONE);
        noFriends.setVisibility(View.GONE);

        // -------------------------- REQUEST LIST -------------------------
        requestsList = (RecyclerView) mainView.findViewById(R.id.requests_list);
        requestsList.setHasFixedSize(true);
        requestsList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        // Listen to changes at the current users request database reference
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("friend_requests").child(currentUserID));
        firebaseDatabaseLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                requestsHeading.setVisibility(View.GONE);
                friendsHeading.setVisibility(View.GONE);

                // if any change under requests to or from current user clear the arrays
                requestsUserIDs.clear();
                requestUsers.clear();
                cleanPresenceListeners();
                if (dataSnapshot.hasChildren()) {
                    // if there are requests, save them in array
                    for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                        requestsUserIDs.add(requestSnapshot.getKey());
                        String requestType = requestSnapshot.child("request_type").getValue().toString();
                        // TODO Check so that IOS version has "sent" and "received" as strings
                        if (requestType.equals("sent")) {
                            requests.put(requestSnapshot.getKey(), getString(R.string.friend_request_sent));
                        } else if (requestType.equals("received")) {
                            requests.put(requestSnapshot.getKey(), getString(R.string.friend_request_received));
                        }

                    }
                    contentMap.put("requests", true);
                    contentChange();
                    // if no requests, notify the recycler view to load empty view
                } else {
                    requestsViewHolderAdapter.notifyDataSetChanged();
                    contentMap.put("requests", false);
                    contentChange();
                }

                // Loop through the user IDs the current user has requests sent to or received from
                for (String requestUserID : requestsUserIDs) {
                    rootDbRef.child("usersPublic").child(requestUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()==null) {
                                return;
                            }
                            UserPublic userPublic = dataSnapshot.getValue(UserPublic.class);

                            // Save the user to the array of users and notify recycler view on data set changed
                            int pos = requestsUserIDs.indexOf(dataSnapshot.getKey());
                            requestUsers.put(pos,userPublic);
                            if (requestUsers.size()==requestsUserIDs.size()) {
                                requestsViewHolderAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

            }
        });

        // Setup the request list with a recycler adapter
        requestsViewHolderAdapter = new RecyclerView.Adapter<UsersViewHolder>() {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout_mini, parent, false);
                return new UsersViewHolder(view);
            }
            @Override
            public void onBindViewHolder(UsersViewHolder holder, final int position) {
                holder.setText(requests.get(requestsUserIDs.get(position)), true);
                UserPublic friend = requestUsers.get(position);
                holder.setHeading(friend.getFirstName()+ " " + friend.getLastName());
                holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                holder.setOnlineIcon(false);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        onUserClickedListener.OnUserClicked(requestsUserIDs.get(position));
                    }
                });
            }
            @Override
            public int getItemCount() {
                return requestUsers.size();
            }
        };
        requestsList.setAdapter(requestsViewHolderAdapter);

        // -------------------------- FRIENDS LIST -------------------------
        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        //friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        // Listen to changes at the current users friends database reference

        Query friendsQuery = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID).orderByValue();
        FirebaseRecyclerOptions<UserPublic> friendsOptions = new FirebaseRecyclerOptions.Builder<UserPublic>()
                .setIndexedQuery(friendsQuery, usersDatabase, UserPublic.class)
                .build();

        listFriendsFirebaseAdapter = new ListFriendsFirebaseAdapter(friendsOptions, getActivity().getApplicationContext(), onUserClickedListener, new OnFbAdapterChangedListener() {
            @Override
            public void OnDataHasChanged() {
                firstLoadProgressBar.setVisibility(View.GONE);
            }
        });

        friendsList.setAdapter(listFriendsFirebaseAdapter);

        myFriendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        FirebaseDatabaseViewModel friendFirebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> friendFirebaseDatabaseLiveData = friendFirebaseDatabaseViewModel.getDataSnapshotLiveData(myFriendsDbRef);
        friendFirebaseDatabaseLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                friendUserIDs.clear();
                userBranches.clear();
                // save all the users IDs in an array
                if (dataSnapshot.hasChildren()) {
                    contentMap.put("friends", true);
                    contentChange();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        friendUserIDs.add(child.getKey());
                    }
                } else {
                    // if current user does not have any friends, notify recyclerview and load empty list
                    requestsViewHolderAdapter.notifyDataSetChanged();
                    contentMap.put("friends", false);
                    contentChange();
                }

            }
        });

        return mainView;
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
        cleanPresenceListeners();
    }

    public void cleanPresenceListeners () {
        /*for (Map.Entry<DatabaseReference, ValueEventListener> entry : presenceListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }*/
    }

    public void contentChange() {

        if (!contentMap.get("requests") && !contentMap.get("friends")) {
            noContent.setVisibility(View.VISIBLE);
            firstLoadProgressBar.setVisibility(View.GONE);
            return;
        } else {
            noContent.setVisibility(View.GONE);
        }

        if (contentMap.get("requests") && contentMap.get("friends")) {
            requestsHeading.setVisibility(View.VISIBLE);
            friendsHeading.setVisibility(View.VISIBLE);
            noFriends.setVisibility(View.GONE);
            return;
        }

        if (contentMap.get("requests") && !contentMap.get("friends")) {
            requestsHeading.setVisibility(View.VISIBLE);
            friendsHeading.setVisibility(View.GONE);
            firstLoadProgressBar.setVisibility(View.GONE);
            if (!requests.containsValue(getString(R.string.friend_request_sent))) {
                noFriends.setVisibility(View.VISIBLE);
            } else {
                noFriends.setVisibility(View.GONE);
            }

        }

        if (!contentMap.get("requests") && contentMap.get("friends")) {
            requestsHeading.setVisibility(View.GONE);
            friendsHeading.setVisibility(View.GONE);
            noFriends.setVisibility(View.GONE);
        }

    }

    public interface OnFbAdapterChangedListener {
        void OnDataHasChanged();
    }
}