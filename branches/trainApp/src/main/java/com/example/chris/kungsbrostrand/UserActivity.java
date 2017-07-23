package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    LinearLayout list;
    ArrayList<String> titles;
    ArrayList<String> description;
    int imgs = R.drawable.twitter;
    ArrayList<String> sessionNameArray;
    ArrayList<Session> sessionArray;
    //public MyAdapter adapter;
    public LatLng sessionLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Resources res = getResources();

        titles =  new ArrayList<>();
        description = new ArrayList<>();
        //sessionNameArray= new ArrayList<>();
        sessionArray= new ArrayList<>();

        list = (LinearLayout) findViewById(R.id.list1);
        //adapter = new MyAdapter(this, titles,imgs,description);

        // Set profile layout
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View profile = inflater.inflate(R.layout.user_profile_info,list,false);
        ImageView profileImage = (ImageView) profile.findViewById(R.id.profileIV);
        profileImage.setImageResource(imgs);
        TextView userName = (TextView) profile.findViewById(R.id.profileTV) ;
        userName.setText(currentFirebaseUser.getEmail());
        list.addView(profile);

        UserActivityContent userActivityContent = new UserActivityContent();

        userActivityContent.getUserActivityContent(new OnUserActivityContentListener() {
            @Override
            public void OnUserActivityContent(ArrayList<Session> sessionsAttending, ArrayList<Session> sessionsHosting) {

                // Heading sessionAttending
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View sessionsAttendingHeadingView = inflater.inflate(R.layout.your_sessions_heading,list,false);
                TextView sessionsAttendingHeading = (TextView) sessionsAttendingHeadingView.findViewById(R.id.yourSessionsHeadingTV) ;
                sessionsAttendingHeading.setText("Sessions Attending");
                list.addView(sessionsAttendingHeadingView);
                populateList(sessionsAttending);


                // Heading sessionsHosting
                View yourSessionsHeadingView = inflater.inflate(R.layout.your_sessions_heading,list,false);
                TextView sessionsHostingHeading = (TextView) yourSessionsHeadingView.findViewById(R.id.yourSessionsHeadingTV) ;
                sessionsHostingHeading.setText("Sessions Hosting");
                list.addView(yourSessionsHeadingView);
                populateList(sessionsHosting);

            }
        });
    }

    public void populateList(final ArrayList<Session> sessionArray) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        for (int i=0; i < sessionArray.size(); i++) {
            View row  = inflater.inflate(R.layout.row, list, false);
            ImageView images = (ImageView) row.findViewById(R.id.icon);
            TextView myTitle =(TextView) row.findViewById(R.id.text1);
            TextView myDescription = (TextView) row.findViewById(R.id.text2);
            images.setImageResource(imgs);
            myTitle.setText(sessionArray.get(i).getSessionName());
            myDescription.setText(sessionArray.get(i).getSessionType());
            // set item content in view
            list.addView(row);
            final int t = i;

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sessionLatLng = new LatLng(sessionArray.get(t).latitude, sessionArray.get(t).longitude);
                    joinSession(sessionLatLng);
                }
            });
        }
    }

    public void joinSession(LatLng markerLatLng) {
        Intent intent = new Intent(this, JoinSessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }
}