package com.example.chris.kungsbrostrand;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class JoinSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    TextView mDay;
    TextView mMonth;
    Calendar cal = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);

        mDay = (TextView) findViewById(R.id.dayTW);
        mMonth = (TextView) findViewById(R.id.monthTW);
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

                    String monthName = markerResult.textMonth(markerResult.sessionDate);
                    mDay.setText(String.valueOf(markerResult.sessionDate.day));
                    mMonth.setText(monthName);
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
