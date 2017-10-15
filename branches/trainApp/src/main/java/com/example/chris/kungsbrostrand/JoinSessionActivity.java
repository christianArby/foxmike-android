package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class JoinSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");

    TextView mDateAndTime;
    TextView mParticipants;
    TextView mSessionName;
    Button mJoinSessionBtn;
    ImageView mHostImage;
    TextView mHost;
    TextView mDescription;
    TextView mAddressAndSessionType;
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

        mDateAndTime = (TextView) joinSession.findViewById(R.id.dateAndTimeTW);
        mParticipants = (TextView) joinSession.findViewById(R.id.participantsTW);
        mHostImage = (ImageView) joinSession.findViewById(R.id.JoinSessionHostImage);
        mHost = (TextView) joinSession.findViewById(R.id.hostName);
        mSessionName = (TextView) joinSession.findViewById(R.id.sessionName);
        mDescription = (TextView) joinSession.findViewById(R.id.descriptionTW);
        mAddressAndSessionType = (TextView) joinSession.findViewById(R.id.addressAndSessionTypeTW);
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
                            Intent mainIntent = new Intent(JoinSessionActivity.this, MainActivity.class);
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
                Session session = dataSnapshot.getValue(Session.class);
                if(session.longitude==longitude) {
                    String sessionTime = String.format("%02d:%02d", session.sessionDate.hour, session.sessionDate.minute);
                    String sessionDateAndTime = session.textFullDay(session.sessionDate) + " " + session.sessionDate.day + " " + session.textMonth(session.sessionDate) + " " + sessionTime;
                    sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                    mDateAndTime.setText(sessionDateAndTime);
                    mParticipants.setText("Participants: " + session.countParticipants +"/" + session.maxParticipants);
                    sessionID = dataSnapshot.getRef().getKey();
                    mSessionName.setText(session.sessionName);
                    mDescription.setText(session.description);
                    String address = getAddress(session.latitude,session.longitude);
                    mAddressAndSessionType.setText(address + "  |  " + session.sessionType);


                    mUserDbRef.child(session.host).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            setImage(user.image, mHostImage);
                            mHost.setText("Host: " + user.name);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if (session.participants != null) {
                        if (session.participants.containsKey(currentFirebaseUser.getUid())) {
                            mJoinSessionBtn.setText("Cancel booking");
                        }
                    }

                    if (session.host.equals(currentFirebaseUser.getUid())) {

                        mJoinSessionBtn.setText("Edit session");
                    }

                    ImageView sessionImage = (ImageView) findViewById(R.id.joinSessionImage);

                    setImage(session.imageUri, sessionImage);

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

    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String street = addresses.get(0).getThoroughfare();// Only if available else return NULL

            if (street != null) {

                if (!street.equals(knownName)) {
                    returnAddress = street + " " + knownName;
                } else {
                    returnAddress = street;
                }
            } else {
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {

            returnAddress = "failed";
        }

        return returnAddress;

    }


}
