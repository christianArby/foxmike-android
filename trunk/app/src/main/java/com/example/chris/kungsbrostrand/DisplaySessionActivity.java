/*
package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisplaySessionActivity extends AppCompatActivity {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private TextView mDateAndTime;
    private TextView mParticipants;
    private TextView mSessionName;
    private Button mDisplaySessionBtn;
    private CircleImageView mHostImage;
    private TextView mHost;
    private TextView mDescription;
    private TextView mAddressAndSessionType;
    private String sessionID;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private Long participantsCount;
    private ImageView mSessionMapImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_session);

        LinearLayout displaySessionContainer;
        View displaySession;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        displaySessionContainer = findViewById(R.id.display_session_container);
        displaySession = inflater.inflate(R.layout.display_session,displaySessionContainer,false);

        mDateAndTime = displaySession.findViewById(R.id.dateAndTimeTW);
        mParticipants = displaySession.findViewById(R.id.participantsTW);
        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mSessionName = displaySession.findViewById(R.id.sessionName);
        mDescription = displaySession.findViewById(R.id.descriptionTW);
        mAddressAndSessionType = displaySession.findViewById(R.id.addressAndSessionTypeTW);
        mSessionMapImage = displaySession.findViewById(R.id.session_map_image);


        */
/**
        Get latitude and longitude of session from previous activity.
        Use latitiude and longitude in method findSessionAndFillInUI to fill view
        with session details.
         *//*

        LatLng markerLatLng = getIntent().getExtras().getParcelable("LatLng");
        if (markerLatLng!=null) {
            findSessionAndFillInUI(markerLatLng.latitude, markerLatLng.longitude);
        } else {
            Toast toast = Toast.makeText(this,"Session not found, please try again later...",Toast.LENGTH_LONG);
            toast.show();
        }

        mDisplaySessionBtn = displaySession.findViewById(R.id.displaySessionBtn);
        displaySessionContainer.addView(displaySession);

        mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference sessionIDref = mSessionDbRef.child(sessionID);

                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Session session = dataSnapshot.getValue(Session.class);

                        */
/**
                        If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                        and send the session key to that activity as bundle.
                        *//*


                        if (session.getHost().equals(currentFirebaseUser.getUid())) {
                            Intent createSessionIntent = new Intent(DisplaySessionActivity.this, CreateOrEditSessionActivity.class);


                            Bundle sessionIdBundle = new Bundle();
                            sessionIdBundle.putString("key",sessionID);
                            createSessionIntent.putExtras(sessionIdBundle);
                            createSessionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(createSessionIntent);
                        }

                        */
/**
                        Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                        remove the current user from that session participant list and go back to main activity.
                        *//*


                        else if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                            mSessionDbRef.child(sessionID).child("participants").child(currentFirebaseUser.getUid()).removeValue();
                            mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(dataSnapshot.getKey()).removeValue();
                            countParticipants();
                            Intent mainIntent = new Intent(DisplaySessionActivity.this, MainPlayerActivity.class);
                            startActivity(mainIntent);
                        }

                        else {

                            */
/**
                            Else (button will show join session) add the user id to the session participant list and
                            the user sessions attending list when button is clicked.
                            After user is added, count participants of the current session.
                            *//*


                            final DatabaseReference sessionIDref = mSessionDbRef.child(sessionID);
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
                            Intent mainIntent = new Intent(DisplaySessionActivity.this, MainPlayerActivity.class);
                            startActivity(mainIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    */
/**
    Count participants of the current session with sessionID found in method findSessionAndFillInUI
     and set the countParticipants value in the database.
    *//*


    private void countParticipants() {
        mSessionDbRef.child(sessionID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("participants")) {
                    participantsCount = dataSnapshot.child("participants").getChildrenCount();
                    mSessionDbRef.child(dataSnapshot.getKey()).child("countParticipants").setValue(participantsCount);
                }
                else {
                    mSessionDbRef.child(dataSnapshot.getKey()).child("countParticipants").setValue(0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    */
/**
    Find the session with the argument latitude value in firebase under the child sessions and fill view with session details
    *//*


    private void findSessionAndFillInUI(Double latitude, final Double longitude){
        //
        mSessionDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Session session = dataSnapshot.getValue(Session.class);
                if(session.getLongitude()==longitude) {

                    String sessionTime = String.format("%02d:%02d", session.getSessionDate().hour, session.getSessionDate().minute);
                    String sessionDateAndTime = session.textFullDay(session.getSessionDate()) + " " + session.getSessionDate().day + " " + session.textMonth(session.getSessionDate()) + " " + sessionTime;
                    sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                    mDateAndTime.setText(sessionDateAndTime);

                    mParticipants.setText("Participants: " + session.getCountParticipants() +"/" + session.getMaxParticipants());
                    sessionID = dataSnapshot.getRef().getKey();
                    mSessionName.setText(session.getSessionName());
                    mDescription.setText(session.getDescription());

                    String address = getAddress(session.getLatitude(),session.getLongitude());
                    mAddressAndSessionType.setText(address + "  |  " + session.getSessionType());


                    */
/**
                    Get the host image from the database (found under users with the userID=session.host)
                    *//*

                    mUserDbRef.child(session.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            setImage(user.getImage(), mHostImage);
                            String hostText = user.getName() + getString(R.string.is_you_trainer);
                            mHost.setText(hostText);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    String sessionLat = Double.toString(session.getLatitude());
                    String sessionLong = Double.toString(session.getLongitude());
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + sessionLat + "," + sessionLong + "&zoom=15&size=400x400&sensor=false";
                    setImage(url,mSessionMapImage);

                    mSessionMapImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

                    */
/**
                    If participants are more than zero, see if the current user is one of the participants and if so
                    change the button text to "Cancel booking"
                    *//*

                    if (session.getParticipants() != null) {
                        if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                            mDisplaySessionBtn.setText(R.string.cancel_booking);
                        }
                    }

                    */
/**
                    If the current user is the session host change the button text to "Edit session"
                    *//*

                    if (session.getHost().equals(currentFirebaseUser.getUid())) {

                        mDisplaySessionBtn.setText("Edit session");
                    }

                    ImageView sessionImage = findViewById(R.id.displaySessionImage);

                    setImage(session.getImageUri(), sessionImage);

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
        Glide.with(this).load(image).into(imageView);
    }

    private String getAddress(double latitude, double longitude) {
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
}*/
