package com.foxmike.android.fragments;
// Checked

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.UsersViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
/**
 * This dialog fragment creates a list of participants in session
 */
public class ParticipantsFragment extends DialogFragment {

    public static final String TAG = ParticipantsFragment.class.getSimpleName();

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    private OnUserClickedListener onUserClickedListener;
    private FirebaseRecyclerAdapter<UserPublic, UsersViewHolder> participantsFirebaseRecyclerAdapter;
    private RecyclerView participantsList;
    private RecyclerView.Adapter<UsersViewHolder> friendsViewHolderAdapter;
    private long mLastClickTime = 0;
    private String advertisementId;
    private String heading;
    private TextView headingTV;
    private ImageView closeButton;

    public ParticipantsFragment() {
        // Required empty public constructor
    }
    // Get hashmap of userIDs (participants) from previous activity/fragment
    public static ParticipantsFragment newInstance(String advertisementId, String heading) {
        ParticipantsFragment fragment = new ParticipantsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("advertisementId", advertisementId);
        bundle.putString("heading", heading);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = this.getArguments();
        if(b.getString("advertisementId") != null) {
            this.advertisementId = b.getString("advertisementId");
        }
        if(b.getString("heading") != null) {
            this.heading = b.getString("heading");
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
        participantsFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_participants, container, false);

        mAuth = FirebaseAuth.getInstance();

        headingTV = mainView.findViewById(R.id.participantsHeading);
        headingTV.setText(heading);

        closeButton = mainView.findViewById(R.id.closeImageButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        participantsList = (RecyclerView) mainView.findViewById(R.id.participants_listRV);
        participantsList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        Query participantsQuery = rootDbRef.child("advertisements").child(advertisementId).child("participantsTimestamps").orderByValue();
        DatabaseReference adDbRef = rootDbRef.child("usersPublic");
        // Create the firebase recycler adapter which will fill the list with those advertisements specified by the above query
        FirebaseRecyclerOptions<UserPublic> options = new FirebaseRecyclerOptions.Builder<UserPublic>()
                .setIndexedQuery(participantsQuery, adDbRef, UserPublic.class)
                .build();

        participantsFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserPublic, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull UserPublic model) {
                holder.setHeading(model.getFirstName() + " " + model.getLastName());
                holder.setText(model.getUserName(),true);
                holder.setUserImage(model.getThumb_image(), getActivity().getApplicationContext());
                String userId = getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        if (userId.equals(mAuth.getCurrentUser().getUid())) {
                            Toast.makeText(getActivity().getApplicationContext(), "This is you", Toast.LENGTH_SHORT).show();
                        } else {
                            onUserClickedListener.OnUserClicked(userId);
                        }
                    }
                });

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_single_layout, parent, false);
                return new UsersViewHolder(view);
            }
        };

        participantsList.setAdapter(participantsFirebaseRecyclerAdapter);

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

    @Override
    public void onStop() {
        super.onStop();
        participantsFirebaseRecyclerAdapter.stopListening();
    }
}
