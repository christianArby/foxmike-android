package com.foxmike.android.fragments;
// Checked
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.HashMap;
/**
 * This dialog fragment creates a list of participants in session
 */
public class ParticipantsFragment extends DialogFragment {

    private HashMap<String,Boolean> participants;
    private RecyclerView participantsList;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;
    private ArrayList<String> userIDs = new ArrayList<String>();
    private OnUserClickedListener onUserClickedListener;
    private RecyclerView.Adapter<UsersViewHolder> friendsViewHolderAdapter;
    private ArrayList<User> users = new ArrayList<>();

    public ParticipantsFragment() {
        // Required empty public constructor
    }
    // Get hashmap of userIDs (participants) from previous activity/fragment
    public static ParticipantsFragment newInstance(HashMap<String,Boolean> participants) {
        ParticipantsFragment fragment = new ParticipantsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hashmap",participants);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.participants = new HashMap<String,Boolean>();
        Bundle b = this.getArguments();
        if(b.getSerializable("hashmap") != null) {
            this.participants = (HashMap<String,Boolean>)b.getSerializable("hashmap");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d!=null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_participants, container, false);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        // Clear usersIDs and users since they will exist when onCreateView is recreated
        userIDs.clear();
        users.clear();
        userIDs.addAll(participants.keySet());
        participantsList = (RecyclerView) mainView.findViewById(R.id.participants_listRV);
        participantsList.setHasFixedSize(true);
        participantsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Loopa alla userIDs och ladda ner deras user objekt, samla dessa i users på samma position som i userIDs
        for (String key : participants.keySet()) {
            usersDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    // hitta positionen i userIDs
                    users.add(user);
                    if (users.size()==userIDs.size()) {
                        Collections.sort(users);
                        friendsViewHolderAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

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
        participantsList.setAdapter(friendsViewHolderAdapter);
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
    }
}
