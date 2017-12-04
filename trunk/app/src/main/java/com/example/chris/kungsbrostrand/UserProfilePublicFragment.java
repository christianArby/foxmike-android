package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
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
    private LatLng sessionLatLng;
    private View profile;
    private String otherUserID;
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
            otherUserID = bundle.getString("otherUserID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_public, container, false);

        mAuth = FirebaseAuth.getInstance();

        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_info */
        list = view.findViewById(R.id.list_user_profile_public);
        profile = inflater.inflate(R.layout.user_profile_public_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        ImageView editIconIV = view.findViewById(R.id.editIconIV);
        sendRequestBtn = view.findViewById(R.id.send_request_btn);
        declineBtn = view.findViewById(R.id.decline_request_btn);

        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);

        areFriends=0;

        if (otherUserID==null) {
            userID = currentFirebaseUser.getUid();
        } else {
            userID = otherUserID;
        }

        usersDbRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userDb = dataSnapshot.getValue(User.class);
                userNameTV.setText(userDb.getName());
                setCircleImage(userDb.image,(CircleImageView) profile.findViewById(R.id.profilePublicIV));


                // FRIENDS LIST / REQUEST FEATURE -----
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


        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequestBtn.setEnabled(false);

                // -------------- NOT FRIENDS STATE ----------------

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
