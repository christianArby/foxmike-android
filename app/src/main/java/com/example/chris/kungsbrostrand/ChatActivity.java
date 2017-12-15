package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser;
    private String chatUserName;
    private String chatThumbImage;
    private Toolbar chatToolbar;
    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView profileImage;

    private DatabaseReference rootRefDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRefDb = FirebaseDatabase.getInstance().getReference();

        chatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);

        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        chatUser = getIntent().getStringExtra("userID");
        chatUserName = getIntent().getStringExtra("userName");
        chatThumbImage = getIntent().getStringExtra("userThumbImage");
        //getSupportActionBar().setTitle(chatUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        titleView = (TextView) findViewById(R.id.custom_bar_name);
        lastSeenView = (TextView) findViewById(R.id.custom_bar_lastSeen);
        profileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        titleView.setText(chatUserName);
        Glide.with(this).load(chatThumbImage).into(profileImage);

        rootRefDb.child("users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User friend = dataSnapshot.getValue(User.class);

                if (friend.isOnline()) {
                    lastSeenView.setText("Online");

                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    String lastSeenText = getTimeAgo.getTimeAgo(friend.getLastSeen(), getApplicationContext());
                    lastSeenView.setText(lastSeenText);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
