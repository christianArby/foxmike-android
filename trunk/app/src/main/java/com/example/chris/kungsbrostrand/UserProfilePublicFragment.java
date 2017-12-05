package com.example.chris.kungsbrostrand;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfilePublicFragment extends Fragment {

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference friendReqDbRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
    private DatabaseReference friendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;
    private LinearLayout list;
    private View profile;
    private String userID;
    private Button sendRequestBtn;
    private Button declineBtn;
    private int areFriends;


    public UserProfilePublicFragment() {
        // Required empty public constructor
    }

    public static UserProfilePublicFragment newInstance() {
        UserProfilePublicFragment fragment = new UserProfilePublicFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            userID = bundle.getString("otherUserID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_public, container, false);

        // Get Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_public_info */
        list = view.findViewById(R.id.list_user_profile_public);
        profile = inflater.inflate(R.layout.user_profile_public_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        sendRequestBtn = view.findViewById(R.id.send_request_btn);
        declineBtn = view.findViewById(R.id.decline_request_btn);

        // Set default visibility of decline button
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);

        // friend states:
        // 0: not friends, no requests
        // 1: not friends, user has sent a request
        // 2: not friends, user has received a request
        // 3: friends

        areFriends=0;

        // get data of the userID clicked in previous activity
        usersDbRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userDb = dataSnapshot.getValue(User.class);
                userNameTV.setText(userDb.getName());
                setCircleImage(userDb.image,(CircleImageView) profile.findViewById(R.id.profilePublicIV));

                // ------------ FRIENDS LIST / REQUEST FEATURE ------------
                // Find out of there are any requests sent to the user or recieved from the user
                friendReqDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(userID)) {
                            String req_type = dataSnapshot.child(userID).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                areFriends = 2;
                                sendRequestBtn.setText("Accept friend Request");

                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                areFriends = 1;
                                sendRequestBtn.setText("Cancel friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        } else {

                            // Find out if the user clicked is one of the current users friends
                            friendsDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userID)) {
                                        areFriends = 3;
                                        sendRequestBtn.setText("Unfriend this person");

                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);
                                    }
                                    // Progressbar dismiss
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Progressbar dismiss
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // ONCLICKLISTENER ON BUTTON STARTS HERE
        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, disable it until results from database so that multiple quires are not possible
                sendRequestBtn.setEnabled(false);

                // -------------- NOT FRIENDS STATE ----------------
                // If not friends and button is clicked, send friend request by creating structure in database
                if (areFriends==0) {
                    friendReqDbRef.child(currentFirebaseUser.getUid()).child(userID).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                friendReqDbRef.child(userID).child(currentFirebaseUser.getUid()).child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {


                                                areFriends = 1;
                                                sendRequestBtn.setText("Cancel friend Request");

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);

                                                //Toast.makeText(getActivity(),"Request sent successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(getActivity(),"Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                            sendRequestBtn.setEnabled(true);
                        }
                    });

                }

                // -------------- CANCEL REQUEST STATE ----------------
                // If friend request has been sent and button is clicked, cancel friend request by removing structure in database

                if (areFriends==1) {

                    friendReqDbRef.child(currentFirebaseUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            friendReqDbRef.child(userID).child(currentFirebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    sendRequestBtn.setEnabled(true);
                                    areFriends = 0;
                                    sendRequestBtn.setText("Send friend Request");

                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                // -------- REQUEST RECIEVED STATE ---------------
                // If friend request has been recieved and button is clicked, accept friend request by adding friend in DB and removing friend request structure in DB
                if (areFriends==2) {

                    final String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    friendsDbRef.child(currentFirebaseUser.getUid()).child(userID).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            friendsDbRef.child(userID).child(currentFirebaseUser.getUid()).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    friendReqDbRef.child(currentFirebaseUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            friendReqDbRef.child(userID).child(currentFirebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    sendRequestBtn.setEnabled(true);
                                                    areFriends = 3;
                                                    sendRequestBtn.setText("Unfriend this person");

                                                    declineBtn.setVisibility(View.INVISIBLE);
                                                    declineBtn.setEnabled(false);

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                // -------- FRIEND STATE ---------------
                // If already friends, and button is clicked, unfriend the person by removing value in database
                if (areFriends==3) {
                    friendsDbRef.child(currentFirebaseUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            friendsDbRef.child(userID).child(currentFirebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    sendRequestBtn.setEnabled(true);
                                    areFriends = 0;
                                    sendRequestBtn.setText("Send friend Request");
                                }
                            });
                        }
                    });
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }
}
