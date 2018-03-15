package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.Presence;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * This fragment lists all the current user's friends
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;
    private DatabaseReference myFriendsDbRef;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View mainView;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private DatabaseReference rootDbRef;
    private ArrayList<String> userIDs = new ArrayList<String>();
    private HashMap<Integer, User> users = new HashMap<Integer, User>();
    private HashMap<Integer, Presence> presenceHashMap = new HashMap<Integer, Presence>();
    private OnUserClickedListener onUserClickedListener;
    private RecyclerView.Adapter<UsersViewHolder> friendsViewHolderAdapter;

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
        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Listen to current users's friends userIds
        ValueEventListener friendsListener = myFriendsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //nollställ alla IDs
                userIDs.clear();
                // Samla alla userIDs som användaren är vän med
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userIDs.add(child.getKey());
                }
                // Loopa alla userIDs och ladda ner deras user objekt, samla dessa i users på samma position som i userIDs
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
                            // Lägg till en lyssnare på vännens "presence" status
                            if (!listenerMap.containsKey(rootDbRef.child("presence").child(dataSnapshot.getKey()))) {
                                ValueEventListener onlineListener = rootDbRef.child("presence").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int pos = userIDs.indexOf(dataSnapshot.getKey());
                                        Presence presence = dataSnapshot.getValue(Presence.class);
                                        presenceHashMap.put(pos,presence);
                                        friendsViewHolderAdapter.notifyItemChanged(pos);
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

        // Lista alla användare sparade i users i en lista
        friendsViewHolderAdapter = new RecyclerView.Adapter<UsersViewHolder>() {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            public void onBindViewHolder(UsersViewHolder holder, final int position) {
                holder.setText("nothing", true);
                final User friend = users.get(position);
                holder.setHeading(friend.getName());
                holder.setUserImage(friend.getThumb_image(), getActivity().getApplicationContext());
                if (presenceHashMap.get(position) == null) {
                    Presence noPresence = new Presence();
                    presenceHashMap.put(position,noPresence);
                }

                holder.setOnlineIcon(presenceHashMap.get(position).isOnline());
                final long lastSeen = presenceHashMap.get(position).getLastOnline();

                // Vid klick på en user skicka dess user ID genom lyssnaren OnUserClickedListener
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
    }

    public void cleanListeners () {
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
}
