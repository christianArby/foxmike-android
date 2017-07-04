package com.example.chris.kungsbrostrand;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
    Long countParticipants;

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

                sessionIDref.child("participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        countParticipants = dataSnapshot.getChildrenCount();
                        DatabaseReference sessionIDref = mMarkerDbRef.child(sessionID);
                        sessionIDref.child("countParticipants").setValue(countParticipants);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                sessionIDref.child("participants").child(currentFirebaseUser.getUid()).setValue(true);

                DatabaseReference userIDref = mUserDbRef.child(currentFirebaseUser.getUid()).child("sessions");
                userIDref.child(sessionID).setValue(true);

                finish();

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

}
