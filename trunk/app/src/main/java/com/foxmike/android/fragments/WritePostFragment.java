package com.foxmike.android.fragments;


import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class WritePostFragment extends DialogFragment {

    DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User user;

    EditText postTextET;
    ImageView postProfileImage;
    TextView sendTW;
    TextView postName;
    String postText;
    Boolean sendable = false;
    private static final String SESSION_ID = "sessionID";
    private String sessionID;
    private String postID;
    private Toolbar postToolbar;


    public WritePostFragment() {
        // Required empty public constructor
    }

    public static WritePostFragment newInstance(String sessionID) {
        WritePostFragment fragment = new WritePostFragment();
        Bundle args = new Bundle();
        args.putString(SESSION_ID, sessionID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreenDialog);

        rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_write_post, container, false);


        postToolbar = (Toolbar)  view.findViewById(R.id.post_app_bar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(postToolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        sessionID = getArguments().getString(SESSION_ID);

        View action_bar_view = inflater.inflate(R.layout.write_post_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        sendTW = view.findViewById(R.id.post_custom_bar_send);
        postTextET = view.findViewById(R.id.postText);
        postProfileImage = view.findViewById(R.id.post_profile_image);
        postName = view.findViewById(R.id.post_user_name);
        sendTW.setTextColor(Color.GRAY);

        rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                postName.setText(user.getName());
                Glide.with(getActivity()).load(user.getThumb_image()).into(postProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        postTextET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (postTextET.getText().length()>0) {
                    sendTW.setTextColor(Color.WHITE);
                    sendable=true;
                } else  {
                    sendTW.setTextColor(Color.GRAY);
                    sendable=false;
                }

            }
        });

        sendTW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sendable) {
                    postID = rootDbRef.child("posts").push().getKey();

                    rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            Post post = new Post(mAuth.getCurrentUser().getUid(), postTextET.getText().toString(), user.getName(), user.getThumb_image());

                            rootDbRef.child("posts").child(postID).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    rootDbRef.child("sessions").child(sessionID).child("posts").child(postID).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            dismiss();
                                        }
                                    });
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

        return view;
    }

}
