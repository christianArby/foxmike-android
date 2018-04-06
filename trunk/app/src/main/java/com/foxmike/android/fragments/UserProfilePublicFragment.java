package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.Intent;
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
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * This Fragment shows a user's profile and includes functionality to add/remove the user as friend by sending requests and messages
 */
public class UserProfilePublicFragment extends Fragment {

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference friendReqDbRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
    private DatabaseReference friendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends");
    private  DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout list;
    private View profile;
    private String otherUserID;
    private Button sendRequestBtn;
    private Button declineBtn;
    private Button sendMessageBtn;
    private int areFriends;
    private User otherUser;
    private OnChatClickedListener onChatClickedListener;


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

        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_public_info */
        list = view.findViewById(R.id.list_user_profile_public);
        profile = inflater.inflate(R.layout.user_profile_public_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        sendRequestBtn = view.findViewById(R.id.send_request_btn);
        declineBtn = view.findViewById(R.id.decline_request_btn);
        sendMessageBtn = view.findViewById(R.id.send_message_btn);
        sendMessageBtn.setVisibility(View.GONE);

        // Set initial visibility of decline button
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);

        // areFriends states:
        // 0: not friends, no requests
        // 1: not friends, user has sent a request
        // 2: not friends, user has received a request
        // 3: friends

        // set the initial state, if the relationship is otherwise it will change further down in the code
        areFriends=0;
        // Set initial visibility of decline button
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);

        // get data of the otherUserID clicked in previous activity
        usersDbRef.child(otherUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Fill the user profile page with the info from otherUser
                otherUser = dataSnapshot.getValue(User.class);
                userNameTV.setText(otherUser.getName());
                setCircleImage(otherUser.image,(CircleImageView) profile.findViewById(R.id.profilePublicIV));

                // ------------ FRIENDS LIST / REQUEST FEATURE ------------
                // Find out if there are any requests sent or recieved from the other user in the database/"friend_requests"/currentUserID
                friendReqDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(otherUserID)) {
                            String req_type = dataSnapshot.child(otherUserID).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                areFriends = 2;     // If there are a recieved friend request
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

                            // Find out if the user clicked is one of the current users friends by looking in database/"friends"/currentUserID
                                    friendsDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(otherUserID)) {
                                                areFriends = 3;
                                                sendRequestBtn.setText("Unfriend this person");

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);
                                                sendMessageBtn.setVisibility(View.VISIBLE);
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

        // -------------- SEND FRIEND REQUEST BUTTON -------------------
        // Set the on click listener for the send friend request button
        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, disable it until results from database so that multiple quires are not possible
                sendRequestBtn.setEnabled(false);

                // -------------- NOT FRIENDS STATE ----------------
                // If not friends and button is clicked, send friend request by creating structure in database
                if (areFriends==0) {
                    DatabaseReference newNotificationRef = rootDbRef.child("notifications").child(otherUserID).push();
                    String newNotificationID = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", currentFirebaseUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("friend_requests/" + currentFirebaseUser.getUid() + "/" + otherUserID + "/request_type", "sent");
                    requestMap.put("friend_requests/" + otherUserID + "/" + currentFirebaseUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + otherUserID + "/" + newNotificationID, notificationData);

                    rootDbRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(getActivity(), "There was an error when sending request", Toast.LENGTH_SHORT).show();
                                sendRequestBtn.setEnabled(true);
                            } else {
                                areFriends = 1;
                                sendRequestBtn.setText("Cancel friend Request");
                                sendRequestBtn.setEnabled(true);

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }
                    });

                }

                // -------------- CANCEL REQUEST STATE ----------------
                // If friend request has been sent and button is clicked, cancel friend request by removing structure in database

                if (areFriends==1) {

                    friendReqDbRef.child(currentFirebaseUser.getUid()).child(otherUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            friendReqDbRef.child(otherUserID).child(currentFirebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/" + currentFirebaseUser.getUid() + "/" + otherUserID + "/date", currentDate);
                    friendsMap.put("friends/" + otherUserID + "/" + currentFirebaseUser.getUid() + "/date", currentDate);

                    friendsMap.put("friend_requests/" + currentFirebaseUser.getUid() + "/" + otherUserID, null);
                    friendsMap.put("friend_requests/" + otherUserID + "/" + currentFirebaseUser.getUid(), null);

                    rootDbRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                areFriends = 3;
                                sendRequestBtn.setText("Unfriend this person");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                            }

                            sendRequestBtn.setEnabled(true);
                        }
                    });
                }

                // -------- UNFRIENDS ---------------
                // If already friends, and button is clicked, unfriend the person by removing value in database

                if (areFriends==3) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + currentFirebaseUser.getUid() + "/" + otherUserID, null);
                    unfriendMap.put("friends/" + otherUserID + "/" + currentFirebaseUser.getUid(), null);

                    rootDbRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                areFriends = 0;
                                sendRequestBtn.setText("Send friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                            }

                            sendRequestBtn.setEnabled(true);
                        }
                    });
                }
            }
        });

        // -------------- DECLINE BUTTON -------------------
        // Set the on click listener for the decline request button
        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                declineBtn.setEnabled(false);
                // -------- DECLINE ---------------
                // If already friends, and button is clicked, decline the friend request by removing value in database

                if (areFriends==2) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friend_requests/" + currentFirebaseUser.getUid() + "/" + otherUserID, null);
                    unfriendMap.put("friend_requests/" + otherUserID + "/" + currentFirebaseUser.getUid(), null);

                    rootDbRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                areFriends = 0;
                                sendRequestBtn.setText("Send friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                            }

                            declineBtn.setEnabled(true);
                        }
                    });
                }
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otherUser!=null) {
                    onChatClickedListener.OnChatClicked(otherUserID,otherUser.getName(),otherUser.getThumb_image(),null);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatClickedListener) {
            onChatClickedListener = (OnChatClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onChatClickedListener = null;
    }
}
