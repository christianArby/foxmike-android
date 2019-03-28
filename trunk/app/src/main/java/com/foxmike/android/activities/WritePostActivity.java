package com.foxmike.android.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class WritePostActivity extends AppCompatActivity {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText postTextET;
    private ImageView postProfileImage;
    private TextView sendTW;
    private TextView postName;
    private TextView postTitle;
    private Boolean sendable = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        postToolbar = (Toolbar)  findViewById(R.id.post_app_bar);
        setSupportActionBar(postToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        sourceID = getIntent().getStringExtra(SOURCE_ID);
        dbParent = getIntent().getStringExtra(DB_PARENT);
        title = getIntent().getStringExtra(TITLE);

        View action_bar_view = getLayoutInflater().inflate(R.layout.write_post_custom_bar, null);

        // make sure the whole action bar is filled with the custom view
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);

        sendTW = findViewById(R.id.post_custom_bar_send);
        postTextET = findViewById(R.id.postText);
        postProfileImage = findViewById(R.id.post_profile_image);
        postName = findViewById(R.id.post_user_name);
        sendTW.setTextColor(Color.GRAY);
        postTitle = findViewById(R.id.post_custom_bar_name);
        postTitle.setText(title);

        showKeyboard();

        rootDbRef.child("usersPublic").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    return;
                }
                currentUserPublic = dataSnapshot.getValue(UserPublic.class);
                postName.setText(currentUserPublic.getFirstName() + " " + currentUserPublic.getLastName());
                Glide.with(WritePostActivity.this).load(currentUserPublic.getThumb_image()).into(postProfileImage);
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
                    Post post = new Post(mAuth.getCurrentUser().getUid(),postTextET.getText().toString(), sourceID);

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
                                    hideKeyboard();
                                    finish();
                                }
                            });
                        }
                    });
                }
            }
        });


    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}
