package com.foxmike.android.fragments;
// Checked

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.foxmike.android.models.UserPublic;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
/**
 * This dialog fragment lets the user write a post to the session
 */
public class WritePostFragment extends DialogFragment {

    DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText postTextET;
    ImageView postProfileImage;
    TextView sendTW;
    TextView postName;
    TextView postTitle;
    Boolean sendable = false;
    private static final String SOURCE_ID = "sourceID";
    private static final String DB_PARENT = "dbParent";
    private static final String TITLE = "title";
    private String sourceID;
    private String dbParent;
    private String title;
    private String postID;
    private Toolbar postToolbar;
    private UserPublic currentUserPublic;
    private long mLastClickTime = 0;

    public WritePostFragment() {
        // Required empty public constructor
    }

    public static WritePostFragment newInstance(String dbParent, String sourceID, String title) {
        WritePostFragment fragment = new WritePostFragment();
        Bundle args = new Bundle();
        args.putString(SOURCE_ID, sourceID);
        args.putString(DB_PARENT, dbParent);
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreenDialog);
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

        sourceID = getArguments().getString(SOURCE_ID);
        dbParent = getArguments().getString(DB_PARENT);
        title = getArguments().getString(TITLE);
        View action_bar_view = inflater.inflate(R.layout.write_post_custom_bar, null);

        // make sure the whole action bar is filled with the custom view
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);

        sendTW = view.findViewById(R.id.post_custom_bar_send);
        postTextET = view.findViewById(R.id.postText);
        postProfileImage = view.findViewById(R.id.post_profile_image);
        postName = view.findViewById(R.id.post_user_name);
        sendTW.setTextColor(Color.GRAY);
        postTitle = view.findViewById(R.id.post_custom_bar_name);
        postTitle.setText(title);

        rootDbRef.child("usersPublic").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserPublic = dataSnapshot.getValue(UserPublic.class);
                postName.setText(currentUserPublic.getFirstName() + " " + currentUserPublic.getLastName());
                Glide.with(getActivity()).load(currentUserPublic.getThumb_image()).into(postProfileImage);
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
                    sendTW.setTextColor(getResources().getColor(R.color.primaryTextColor));
                    sendable=true;
                } else  {
                    sendTW.setTextColor(getResources().getColor(R.color.grayTextColor));
                    sendable=false;
                }
            }
        });

        sendTW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (sendable && currentUserPublic!=null) {
                    postID = rootDbRef.child("posts").push().getKey();
                    Post post = new Post(mAuth.getCurrentUser().getUid(), postTextET.getText().toString(), currentUserPublic.getFirstName() + " " + currentUserPublic.getLastName(), sourceID, title);

                    String postRef;

                    if (dbParent.equals("sessions")) {
                        postRef = "sessionPosts";
                    } else {
                        postRef = "advertisementPosts";
                    }

                    rootDbRef.child(postRef).child(sourceID).child(postID).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Long currentTimestamp = System.currentTimeMillis();
                            rootDbRef.child("userPosts").child(mAuth.getCurrentUser().getUid()).child(sourceID).child(postID).setValue(currentTimestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
        return view;
    }
}
