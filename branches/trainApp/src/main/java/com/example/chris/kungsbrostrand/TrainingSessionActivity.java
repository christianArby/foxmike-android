package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.EditText;

public class TrainingSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("markers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);
        LatLng clickedLatLng =getIntent().getExtras().getParcelable("LatLng");
        FirebaseMarker marker = new FirebaseMarker();
        marker.sessionType="Running";
        marker.longitude = clickedLatLng.longitude;
        marker.latitude = clickedLatLng.latitude;
        mMarkerDbRef.push().setValue(marker);
    }
}
