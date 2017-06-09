package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TrainingSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("markers");
    EditText mSessionType;
    EditText mDate;
    EditText mTime;
    EditText mLevel;
    EditText mNrOfParticipants;
    private Button mCreateSessionBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        mCreateSessionBtn =(Button) findViewById(R.id.createSessionBtn);

        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionType = (EditText)findViewById(R.id.sessionTypeET);
                mDate = (EditText)findViewById(R.id.dateET);
                mTime = (EditText) findViewById(R.id.timeET);
                mLevel = (EditText) findViewById(R.id.levelET);
                mNrOfParticipants = (EditText) findViewById(R.id.nrOfParticipantsET);

                Session session = new Session();
                LatLng clickedLatLng = getIntent().getExtras().getParcelable("LatLng");

                session.sessionType = mSessionType.getText().toString();
                session.date = mDate.getText().toString();
                session.time = mTime.getText().toString();
                session.level = mLevel.getText().toString();
                session.nrOfParticipants = mNrOfParticipants.getText().toString();
                session.longitude = clickedLatLng.longitude;
                session.latitude = clickedLatLng.latitude;

                mMarkerDbRef.push().setValue(session);
                finish();
            }
        });

    }
}
