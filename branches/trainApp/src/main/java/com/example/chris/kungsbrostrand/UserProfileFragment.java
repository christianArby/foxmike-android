package com.example.chris.kungsbrostrand;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class UserProfileFragment extends Fragment {

    DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    LinearLayout list;
    ArrayList<String> titles;
    ArrayList<String> description;
    ArrayList<String> sessionNameArray;
    ArrayList<Session> sessionArray;
    //public MyAdapter adapter;
    public LatLng sessionLatLng;
    private View profile;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        Resources res = getResources();

        titles =  new ArrayList<>();
        description = new ArrayList<>();
        //sessionNameArray= new ArrayList<>();
        sessionArray= new ArrayList<>();

        list = (LinearLayout) view.findViewById(R.id.list1);
        //adapter = new MyAdapter(this, titles,imgs,description);

        // Set profile layout
        profile = inflater.inflate(R.layout.user_profile_info,list,false);
        final TextView userNameTV = (TextView) profile.findViewById(R.id.profileTV) ;
        //TextView userName = (TextView) profile.findViewById(R.id.profileTV) ;
        //
        list.addView(profile);

        UserActivityContent userActivityContent = new UserActivityContent();

        userActivityContent.getUserActivityContent(new OnUserActivityContentListener() {
            @Override
            public void OnUserActivityContent(ArrayList<Session> sessionsAttending, ArrayList<Session> sessionsHosting, String name, String image) {

                userNameTV.setText(name);

                setImage(image, (ImageView) profile.findViewById(R.id.profileIV));

                // Heading sessionAttending
                LayoutInflater inflater = LayoutInflater.from(getActivity());
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
        // Inflate the layout for this fragment
        return view;
    }

    private void setImage(String image, ImageView imageView) {


        //ImageView profileImage = (ImageView) profile.findViewById(R.id.profileIV);
        Glide.with(this).load(image).into(imageView);


    }

    public void populateList(final ArrayList<Session> sessionArray) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i=0; i < sessionArray.size(); i++) {
            View row  = inflater.inflate(R.layout.row, list, false);
            ImageView images = (ImageView) row.findViewById(R.id.icon);
            TextView myTitle =(TextView) row.findViewById(R.id.text1);
            TextView myDescription = (TextView) row.findViewById(R.id.text2);
            //images.setImageResource(imgs);
            myTitle.setText(sessionArray.get(i).getSessionName());
            myDescription.setText(sessionArray.get(i).getSessionType());
            setImage(sessionArray.get(i).getImageUri(),images);
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
        Intent intent = new Intent(getActivity(), JoinSessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }

}
