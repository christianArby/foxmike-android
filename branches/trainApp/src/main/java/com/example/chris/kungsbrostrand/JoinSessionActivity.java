package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    TextView mSessionName;
    Button mJoinSessionBtn;
    ImageView mHostImage;
    TextView mHost;
    TextView mDescription;
    String sessionID;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    Long participantsCount;
    String test;
    LinearLayout joinSessionContainer;
    View joinSession;
    int sessionHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);

        joinSessionContainer = (LinearLayout) findViewById(R.id.join_session_container);
        // Set create session layout
        joinSession = inflater.inflate(R.layout.join_session,joinSessionContainer,false);

        mDay = (TextView) joinSession.findViewById(R.id.dayTW);
        mMonth = (TextView) joinSession.findViewById(R.id.monthTW);
        mSessionType = (TextView) joinSession.findViewById(R.id.sessionTypeTW);
        mLevel = (TextView) joinSession.findViewById(R.id.levelTW);
        mParticipants = (TextView) joinSession.findViewById(R.id.participantsTW);
        mHostImage = (ImageView) joinSession.findViewById(R.id.JoinSessionHostImage);
        mHost = (TextView) joinSession.findViewById(R.id.hostName);
        mSessionName = (TextView) joinSession.findViewById(R.id.sessionName);
        mDescription = (TextView) joinSession.findViewById(R.id.descriptionTW);
        LatLng markerLatLng = getIntent().getExtras().getParcelable("LatLng");
        findSession(markerLatLng.latitude, markerLatLng.longitude);

        mJoinSessionBtn = (Button) joinSession.findViewById(R.id.joinSessionBtn);

        joinSessionContainer.addView(joinSession);

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
                    mSessionName.setText(markerResult.sessionName);
                    mDescription.setText(markerResult.description);


                    mUserDbRef.child(markerResult.host).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            setImage(user.image, mHostImage);
                            mHost.setText(user.name);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if (markerResult.participants != null) {
                        if (markerResult.participants.containsKey(currentFirebaseUser.getUid())) {
                            mJoinSessionBtn.setText("Cancel booking");
                        }
                    }

                    if (markerResult.host.equals(currentFirebaseUser.getUid())) {

                        mJoinSessionBtn.setText("Edit session");
                    }

                    ImageView sessionImage = (ImageView) findViewById(R.id.joinSessionImage);

                    setImage(markerResult.imageUri, sessionImage);

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

    private void setImage(String image, ImageView imageView) {



        //Picasso.with(this).load(image).resize(3900,2000).centerCrop().into(sessionImage);
        Glide.with(this).load(image).into(imageView);


    }

}
