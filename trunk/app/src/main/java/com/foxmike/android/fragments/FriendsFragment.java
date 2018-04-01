package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.Presence;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserBranch;
import com.foxmike.android.utils.HeaderViewHolder;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This fragment lists all the current user's friend requests and friends
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;
    private DatabaseReference myFriendsDbRef;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View mainView;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private HashMap<DatabaseReference, ValueEventListener> presenceListenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private DatabaseReference rootDbRef;
    private ArrayList<String> friendUserIDs = new ArrayList<String>();
    private ArrayList<String> requestsUserIDs = new ArrayList<String>();
    private HashMap<String, String> requests = new HashMap<String, String>();
    private ArrayList<UserBranch> userBranches = new ArrayList<>();
    private SparseArray<String> userPosition = new SparseArray<String>();
    private HashMap<Integer, Presence> presenceHashMap = new HashMap<Integer, Presence>();
    private OnUserClickedListener onUserClickedListener;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> friendsViewHolderAdapter;
    private RecyclerView requestsList;
    private HashMap<Integer, User> requestUsers = new HashMap<Integer, User>();
    private RecyclerView.Adapter<UsersViewHolder> requestsViewHolderAdapter;
    private TextView requestsHeading;
    private TextView friendsHeading;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootDbRef = FirebaseDatabase.getInstance().getReference();
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        myFriendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);
        requestsHeading = mainView.findViewById(R.id.friendRequestsHeadingTV);
        friendsHeading = mainView.findViewById(R.id.friendsHeadingTV);


        // -------------------------- REQUEST LIST -------------------------
        requestsList = (RecyclerView) mainView.findViewById(R.id.requests_list);
        requestsList.setHasFixedSize(true);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Listen to changes at the current users request database reference
        ValueEventListener friendRequestsListener = rootDbRef.child("friend_requests").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // if any change under requests to or from current user clear the arrays
                requestsUserIDs.clear();
                requestUsers.clear();
                cleanPresenceListeners();
                if (dataSnapshot.hasChildren()) {
                    // if there are requests, save them in array
                    for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                        requestsUserIDs.add(requestSnapshot.getKey());
                        requests.put(requestSnapshot.getKey(), requestSnapshot.child("request_type").getValue().toString());
                    }
                    // if no requests, notify the recycler view to load empty view
                } else {
                    requestsHeading.setVisibility(View.GONE);
                    friendsHeading.setVisibility(View.GONE);
                    requestsViewHolderAdapter.notifyDataSetChanged();
                }

                // Loop through the user IDs the current user has requests sent to or received from
                for (String requestUserID : requestsUserIDs) {
                    rootDbRef.child("users").child(requestUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            // Save the user to the array of users and notify recycler view on data set changed
                            int pos = requestsUserIDs.indexOf(dataSnapshot.getKey());
                            requestUsers.put(pos,user);
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
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        // save the listener to a map in order to later detach the listener
        listenerMap.put(rootDbRef.child("friend_requests").child(currentUserID), friendRequestsListener);
        // Setup the request list with a recycler adapter
        requestsViewHolderAdapter = new RecyclerView.Adapter<UsersViewHolder>() {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }
            @Override
            public void onBindViewHolder(UsersViewHolder holder, final int position) {
                holder.setText(requests.get(requestsUserIDs.get(position)), true);
                final User friend = requestUsers.get(position);
                holder.setHeading(friend.getName());
                holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                holder.setOnlineIcon(false);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Listen to changes at the current users friends database reference
        ValueEventListener friendsListener = myFriendsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendUserIDs.clear();
                userBranches.clear();
                // save all the users IDs in an array
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        friendUserIDs.add(child.getKey());
                    }
                } else {
                    // if current user does not have any friends, notify recyclerview and load empty list
                    requestsViewHolderAdapter.notifyDataSetChanged();
                }

                // Loop all the current users friends userIDs and download their user objects
                for (String key : friendUserIDs) {
                    usersDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            // save the userID and the user object in a UserBranch list
                            userBranches.add(new UserBranch(dataSnapshot.getKey(),user));
                            // When this condition has been met all listeners have been triggered and all the user objects have been saved in userBranches
                            if (userBranches.size()==friendUserIDs.size()) {
                                // Sort the userBranches based on user names
                                Collections.sort(userBranches);
                                // Write the first letter heading
                                if (userBranches.size()>0) {
                                    User dummyUser = new User();
                                    dummyUser.setAboutMe(userBranches.get(0).getUser().getName().substring(0,1));
                                    UserBranch userBranch = new UserBranch("header",dummyUser);
                                    userBranches.add(0,userBranch);
                                }
                                // Input letter headings into userBranches as dummy objects with the userID set to string header and object user.aboutMe set to the substring first letter
                                int n = 1;
                                while (n < userBranches.size()) {
                                    if (!userBranches.get(n-1).getUserID().equals("header")) {
                                        if (n>0 && !userBranches.get(n).getUser().getName().substring(0,1).equals(userBranches.get(n-1).getUser().getName().substring(0,1))) {
                                            User dummyUser = new User();
                                            dummyUser.setAboutMe(userBranches.get(n).getUser().getName().substring(0,1));
                                            UserBranch userBranch = new UserBranch("header",dummyUser);
                                            userBranches.add(n,userBranch);
                                        }
                                    }
                                    n++;
                                }
                                friendsViewHolderAdapter.notifyDataSetChanged();
                            }
                            // Set listeners to the friends presence in order to set their online status
                            if (!presenceListenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // TODO check so that pos cant be null
                                        int pos = userPosition.indexOfValue(dataSnapshot.getKey());
                                        if (pos>-1) {
                                            Presence presence = dataSnapshot.getValue(Presence.class);
                                            presenceHashMap.put(pos,presence);
                                            friendsViewHolderAdapter.notifyItemChanged(pos);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                                presenceListenerMap.put(rootDbRef.child("users").child(dataSnapshot.getKey()).child("online"),onlineListener);
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
        // Setup the friends list with a recycler adapter alternating between two layouts, one for heading and one for a user object
        friendsViewHolderAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType==1) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.friends_letter_heading, parent, false);
                    return new HeaderViewHolder(view);
                } else {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.users_list_single_layout, parent, false);
                    return new UsersViewHolder(view);
                }
            }
            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final int posClicked = position;
                userPosition.append(position,userBranches.get(position).getUserID());
                if (userBranches.get(position).getUserID().equals("header")) {
                    ((HeaderViewHolder) holder).setHeading(userBranches.get(position).getUser().getAboutMe());
                } else {
                    ((UsersViewHolder) holder).setText("nothing", true);
                    final User friend = userBranches.get(position).getUser();
                    ((UsersViewHolder) holder).setHeading(friend.getName());
                    ((UsersViewHolder) holder).setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                    if (presenceHashMap.get(position) == null) {
                        Presence noPresence = new Presence();
                        presenceHashMap.put(position,noPresence);
                    }
                    ((UsersViewHolder) holder).setOnlineIcon(presenceHashMap.get(position).isOnline());
                    final long lastSeen = presenceHashMap.get(position).getLastOnline();
                    // Vid klick på en user skicka dess user ID genom lyssnaren OnUserClickedListener
                    ((UsersViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onUserClickedListener.OnUserClicked(userBranches.get(posClicked).getUserID());
                        }
                    });
                }
            }
            @Override
            public int getItemCount() {
                return userBranches.size();
            }
            @Override
            public int getItemViewType(int position) {
                if (userBranches.get(position).getUserID().equals("header")) {
                    return 1;
                }
                return 0;
            }
        };
        friendsList.setAdapter(friendsViewHolderAdapter);
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
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : presenceListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
}