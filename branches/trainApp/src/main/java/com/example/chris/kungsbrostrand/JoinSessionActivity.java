package com.example.chris.kungsbrostrand;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JoinSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("markers");
    private TextView mSessionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);

        mSessionType = (TextView) findViewById(R.id.sessionTypeTW);
        LatLng markerLatLng = getIntent().getExtras().getParcelable("LatLng");
        findSession(markerLatLng.latitude, markerLatLng.longitude);

    }


    protected void findSession(Double latitude, final Double longitude){
        // find latitude value in child in realtime database and fit in imageview
        mMarkerDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Session markerResult = dataSnapshot.getValue(Session.class);
                if(markerResult.longitude==longitude) {
                    mSessionType.setText(markerResult.sessionType);
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
