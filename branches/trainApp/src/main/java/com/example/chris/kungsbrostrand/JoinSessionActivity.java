package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");

    TextView mDay;
    TextView mMonth;
    TextView mSessionType;
    TextView mLevel;
    TextView mParticipants;
    Button mJoinSessionBtn;
    String sessionID;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    Long participantsCount;
    String test;
    int sessionHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);

        mDay = (TextView) findViewById(R.id.dayTW);
        mMonth = (TextView) findViewById(R.id.monthTW);
        mSessionType = (TextView) findViewById(R.id.sessionTypeTW);
        mLevel = (TextView) findViewById(R.id.levelTW);
        mParticipants = (TextView) findViewById(R.id.participantsTW);
        LatLng markerLatLng = getIntent().getExtras().getParcelable("LatLng");
        findSession(markerLatLng.latitude, markerLatLng.longitude);

        mJoinSessionBtn = (Button) findViewById(R.id.joinSessionBtn);

        mJoinSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference sessionIDref = mMarkerDbRef.child(sessionID);

                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("host").getValue().equals(currentFirebaseUser.getUid())) {
                            Intent createSessionIntent = new Intent(JoinSessionActivity.this, TrainingSessionActivity.class);


                            Bundle sessionIdBundle = new Bundle();
                            sessionIdBundle.putString("key",sessionID);
                            createSessionIntent.putExtras(sessionIdBundle);
                            createSessionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(createSessionIntent);
                        }

                        else if (dataSnapshot.child("participants").hasChild(currentFirebaseUser.getUid())) {
                            mMarkerDbRef.child(sessionID).child("participants").child(currentFirebaseUser.getUid()).removeValue();
                            mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(dataSnapshot.getKey()).removeValue();
                            countParticipants();

                            Intent mainIntent = new Intent(JoinSessionActivity.this, MapsActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        }

                        else {

                            final DatabaseReference sessionIDref = mMarkerDbRef.child(sessionID);
                            sessionIDref.child("participants").child(currentFirebaseUser.getUid()).setValue(true);
                            mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(sessionID).setValue(true);

                            sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    countParticipants();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    private void countParticipants() {
        mMarkerDbRef.child(sessionID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("participants")) {
                    participantsCount = dataSnapshot.child("participants").getChildrenCount();
                    mMarkerDbRef.child(dataSnapshot.getKey()).child("countParticipants").setValue(participantsCount);
                }
                else {
                    mMarkerDbRef.child(dataSnapshot.getKey()).child("countParticipants").setValue(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void findSession(Double latitude, final Double longitude){
        // find latitude value in child in realtime database and fit in imageview
        mMarkerDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Session markerResult = dataSnapshot.getValue(Session.class);
                if(markerResult.longitude==longitude) {
                    String monthName = markerResult.textMonth(markerResult.sessionDate);
                    mDay.setText(String.valueOf(markerResult.sessionDate.day));
                    mMonth.setText(monthName);
                    mSessionType.setText(markerResult.sessionType);
                    mLevel.setText("Level: " + markerResult.level);
                    mParticipants.setText("Participants: " + markerResult.countParticipants +"/" + markerResult.maxParticipants);
                    sessionID = dataSnapshot.getRef().getKey();

                    if (markerResult.participants != null) {
                        for (String participant:markerResult.participants.keySet()){
                            if (participant.equals(currentFirebaseUser.getUid())) {
                                Toast.makeText(getApplicationContext(),participant,Toast.LENGTH_SHORT).show();
                            }
                        };
                    }

                    if (markerResult.participants != null) {
                        if (markerResult.participants.containsKey(currentFirebaseUser.getUid())) {
                            mJoinSessionBtn.setText("Cancel booking");
                        }
                    }

                    if (markerResult.host.equals(currentFirebaseUser.getUid())) {

                        mJoinSessionBtn.setText("Edit session");
                    }

                    setImage(markerResult.imageUri);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setImage(String image) {


        ImageView sessionImage = (ImageView) findViewById(R.id.joinSessionImage);
        //Picasso.with(this).load(image).resize(3900,2000).centerCrop().into(sessionImage);
        Glide.with(this).load(image).into(sessionImage);


    }

}
