package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * This Fragment shows a user's profile and includes functionality to add/remove the user as friend by sending requests and messages
 */
public class UserProfilePublicFragment extends Fragment {

    public static final String TAG = UserProfilePublicFragment.class.getSimpleName();

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("usersPublic");
    private DatabaseReference friendReqDbRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
    private DatabaseReference friendsDbRef = FirebaseDatabase.getInstance().getReference().child("friends");
    private  DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener currentUserListener;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout list;
    private View profile;
    private String otherUserID;
    private Button sendRequestBtn;
    private Button declineBtn;
    private Button sendMessageBtn;
    private int areFriends;
    private UserPublic otherUser;
    private OnChatClickedListener onChatClickedListener;
    private long mLastClickTime = 0;
    private ImageView instagramIcon;


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

        final TextView fullNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        final TextView userAboutMeTV = profile.findViewById(R.id.aboutMeProfilePublicTV);
        final TextView userNameTV = profile.findViewById(R.id.userNameProfilePublicTV);
        instagramIcon = profile.findViewById(R.id.instagramIcon);
        sendRequestBtn = profile.findViewById(R.id.send_request_btn);
        sendRequestBtn.setVisibility(View.VISIBLE);
        declineBtn = profile.findViewById(R.id.decline_request_btn);
        sendMessageBtn = profile.findViewById(R.id.send_message_btn);
        sendMessageBtn.setVisibility(View.GONE);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Set initial visibility of decline button
        declineBtn.setVisibility(View.GONE);
        declineBtn.setEnabled(false);

        // areFriends states:
        // 0: not friends, no requests
        // 1: not friends, user has sent a request
        // 2: not friends, user has received a request
        // 3: friends

        // set the initial state, if the relationship is otherwise it will change further down in the code
        areFriends=0;
        // Set initial visibility of decline button
        declineBtn.setVisibility(View.GONE);
        declineBtn.setEnabled(false);

        // get data of the otherUserID clicked in previous activity
        // GET CURRENT USER FROM DATABASE
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(usersDbRef.child(otherUserID));
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                // Fill the user profile page with the info from otherUser
                otherUser = dataSnapshot.getValue(UserPublic.class);
                fullNameTV.setText(otherUser.getFirstName() + " " + otherUser.getLastName());
                userAboutMeTV.setText(otherUser.getAboutMe());
                userNameTV.setText(otherUser.getUserName());

                setCircleImage(otherUser.getImage(), (CircleImageView) profile.findViewById(R.id.profilePublicIV));

                if (otherUser.getInstagramUrl()==null) {
                    instagramIcon.setVisibility(View.GONE);
                } else {
                    instagramIcon.setVisibility(View.VISIBLE);
                    instagramIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(newInstagramProfileIntent(getActivity().getPackageManager(), otherUser.getInstagramUrl()));
                        }
                    });
                }

                // ------------ FRIENDS LIST / REQUEST FEATURE ------------
                // Find out if there are any requests sent or recieved from the other user in the database/"friend_requests"/currentUserID
                friendReqDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(otherUserID)) {
                            String req_type = dataSnapshot.child(otherUserID).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                areFriends = 2;     // If there are a recieved friend request
                                sendRequestBtn.setText(R.string.accept_friend_request);

                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                areFriends = 1;
                                sendRequestBtn.setText(R.string.cancel_friend_request);

                                declineBtn.setVisibility(View.GONE);
                                declineBtn.setEnabled(false);
                            }
                        } else {

                            // Find out if the user clicked is one of the current users friends by looking in database/"friends"/currentUserID
                            friendsDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(otherUserID)) {
                                        areFriends = 3;
                                        sendRequestBtn.setText(R.string.unfriend_this_person);

                                        declineBtn.setVisibility(View.GONE);
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
        });

        // -------------- SEND FRIEND REQUEST BUTTON -------------------
        // Set the on click listener for the send friend request button
        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                // When button is clicked, disable it until results from database so that multiple quires are not possible
                sendRequestBtn.setEnabled(false);

                // -------------- NOT FRIENDS STATE ----------------
                // If not friends and button is clicked, send friend request by creating structure in database
                if (areFriends==0) {

                    Map requestMap = new HashMap<>();
                    requestMap.put("friend_requests/" + currentFirebaseUser.getUid() + "/" + otherUserID + "/request_type", "sent");
                    requestMap.put("friend_requests/" + otherUserID + "/" + currentFirebaseUser.getUid() + "/request_type", "received");

                    rootDbRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(getActivity(), "There was an error when sending request", Toast.LENGTH_SHORT).show();
                                sendRequestBtn.setEnabled(true);
                            } else {
                                areFriends = 1;
                                sendRequestBtn.setText(R.string.cancel_friend_request);
                                sendRequestBtn.setEnabled(true);

                                declineBtn.setVisibility(View.GONE);
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
                                    sendRequestBtn.setText(R.string.send_friend_request);

                                    declineBtn.setVisibility(View.GONE);
                                    declineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                // -------- REQUEST RECIEVED STATE ---------------
                // If friend request has been recieved and button is clicked, accept friend request by adding friend in DB and removing friend request structure in DB
                if (areFriends==2) {

                    Map friendsMap = new HashMap();
                    Long currentTimestamp = System.currentTimeMillis();
                    friendsMap.put("friends/" + currentFirebaseUser.getUid() + "/" + otherUserID, currentTimestamp);
                    friendsMap.put("friends/" + otherUserID + "/" + currentFirebaseUser.getUid(), currentTimestamp);

                    friendsMap.put("friend_requests/" + currentFirebaseUser.getUid() + "/" + otherUserID, null);
                    friendsMap.put("friend_requests/" + otherUserID + "/" + currentFirebaseUser.getUid(), null);

                    rootDbRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                areFriends = 3;
                                sendRequestBtn.setText(R.string.unfriend_this_person);

                                declineBtn.setVisibility(View.GONE);
                                declineBtn.setEnabled(false);
                                sendMessageBtn.setVisibility(View.VISIBLE);
                                sendMessageBtn.setEnabled(true);
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
                                sendRequestBtn.setText(R.string.send_friend_request);

                                declineBtn.setVisibility(View.GONE);
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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

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
                                sendRequestBtn.setText(R.string.send_friend_request);

                                declineBtn.setVisibility(View.GONE);
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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (otherUser!=null) {
                    onChatClickedListener.OnChatClicked(otherUserID,otherUser.getFirstName(),otherUser.getThumb_image(),null);
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(getActivity().getApplicationContext()).load(image).into(imageView);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity)getActivity()).setSupportActionBar(null);
    }

    public static Intent newInstagramProfileIntent(PackageManager pm, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            if (pm.getPackageInfo("com.instagram.android", 0) != null) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                final String username = url.substring(url.lastIndexOf("/") + 1);
                intent.setData(Uri.parse("http://instagram.com/_u/" + username));
                intent.setPackage("com.instagram.android");
                return intent;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (!url.contains("/")) {
            url = "https://instagram.com/" + url;
        }

        intent.setData(Uri.parse(url));
        return intent;
    }
}
