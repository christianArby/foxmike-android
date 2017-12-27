package com.example.chris.kungsbrostrand;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class DisplaySessionFragment extends DialogFragment {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerMap;
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
    private View view;
    private static final String SESSION_LATITUDE = "sessionLatitude";
    private static final String SESSION_LONGITUDE = "sessionLongitude";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private ChildEventListener sessionChildEventListener;

    public DisplaySessionFragment() {
        // Required empty public constructor
    }


    public static DisplaySessionFragment newInstance(double sessionLatitude, double sessionLongitude) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putDouble(SESSION_LATITUDE, sessionLatitude);
        args.putDouble(SESSION_LONGITUDE, sessionLongitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreenDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d!=null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_session, container, false);

        childEventListenerMap = new HashMap<>();

        LinearLayout displaySessionContainer;
        View displaySession;
        displaySessionContainer = view.findViewById(R.id.display_session_container);
        displaySession = inflater.inflate(R.layout.display_session,displaySessionContainer,false);

        mDateAndTime = displaySession.findViewById(R.id.dateAndTimeTW);
        mParticipants = displaySession.findViewById(R.id.participantsTW);
        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mSessionName = displaySession.findViewById(R.id.sessionName);
        mDescription = displaySession.findViewById(R.id.descriptionTW);
        mAddressAndSessionType = displaySession.findViewById(R.id.addressAndSessionTypeTW);
        mSessionMapImage = displaySession.findViewById(R.id.session_map_image);


        /**
         Get latitude and longitude of session from previous activity.
         Use latitiude and longitude in method findSessionAndFillInUI to fill view
         with session details.
         */
        sessionLatitude = getArguments().getDouble(SESSION_LATITUDE);
        sessionLongitude = getArguments().getDouble(SESSION_LONGITUDE);
        if (sessionLatitude!=null) {
            findSessionAndFillInUI(sessionLatitude, sessionLongitude);
        } else {
            Toast toast = Toast.makeText(getActivity(),"Session not found, please try again later...",Toast.LENGTH_LONG);
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

                        /**
                         If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                         and send the session key to that activity as bundle.
                         */

                        if (session.getHost().equals(currentFirebaseUser.getUid())) {
                            Intent createSessionIntent = new Intent(getActivity(), CreateOrEditSessionActivity.class);


                            Bundle sessionIdBundle = new Bundle();
                            sessionIdBundle.putString("key",sessionID);
                            createSessionIntent.putExtras(sessionIdBundle);
                            createSessionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(createSessionIntent);
                        }

                        /**
                         Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                         remove the current user from that session participant list and go back to main activity.
                         */

                        else if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                            mSessionDbRef.child(sessionID).child("participants").child(currentFirebaseUser.getUid()).removeValue();
                            mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(dataSnapshot.getKey()).removeValue();
                            countParticipants();
                            Intent mainIntent = new Intent(getActivity(), MainPlayerActivity.class);
                            startActivity(mainIntent);
                        }

                        else {

                            /**
                             Else (button will show join session) add the user id to the session participant list and
                             the user sessions attending list when button is clicked.
                             After user is added, count participants of the current session.
                             */

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
                            Intent mainIntent = new Intent(getActivity(), MainPlayerActivity.class);
                            startActivity(mainIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    /**
     Count participants of the current session with sessionID found in method findSessionAndFillInUI
     and set the countParticipants value in the database.
     */

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

    /**
     Find the session with the argument latitude value in firebase under the child sessions and fill view with session details
     */

    private void findSessionAndFillInUI(final Double latitude, final Double longitude){

        sessionChildEventListener = mSessionDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Session session = dataSnapshot.getValue(Session.class);

                childEventListenerMap.put(dataSnapshot.getRef(), sessionChildEventListener);

                if(session.getLongitude()==longitude) {

                    String sessionTime = String.format("%02d:%02d", session.getSessionDate().hour, session.getSessionDate().minute);
                    String sessionDateAndTime = session.getSessionDate().textFullDay() + " " + session.getSessionDate().day + " " + session.getSessionDate().textMonth() + " " + sessionTime;
                    sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                    mDateAndTime.setText(sessionDateAndTime);

                    mParticipants.setText("Participants: " + session.getCountParticipants() +"/" + session.getMaxParticipants());
                    sessionID = dataSnapshot.getRef().getKey();
                    mSessionName.setText(session.getSessionName());
                    mDescription.setText(session.getmDescription());

                    String address = getAddress(session.getLatitude(),session.getLongitude());
                    mAddressAndSessionType.setText(address + "  |  " + session.getSessionType());


                    /**
                     Get the host image from the database (found under users with the userID=session.host)
                     */
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

                    mParticipants.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(session.getParticipants());

                            FragmentManager fragmentManager = getChildFragmentManager();

                            FragmentTransaction transaction = fragmentManager.beginTransaction();

                            if (participantsFragment!=null) {
                                transaction.remove(participantsFragment);
                            }

                            participantsFragment.show(transaction,"participantsFragment");

                        }
                    });

                    mSessionMapImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String label = session.getSessionName();
                            String uriBegin = "geo:" + sessionLatitude + "," + sessionLongitude;
                            String query = sessionLatitude + "," + sessionLongitude + "(" + label + ")";
                            String encodedQuery = Uri.encode(query);
                            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                            Uri uri = Uri.parse(uriString);
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                    /**
                     If participants are more than zero, see if the current user is one of the participants and if so
                     change the button text to "Cancel booking"
                     */
                    if (session.getParticipants() != null) {
                        if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                            mDisplaySessionBtn.setText(R.string.cancel_booking);
                        }
                    }

                    /**
                     If the current user is the session host change the button text to "Edit session"
                     */
                    if (session.getHost().equals(currentFirebaseUser.getUid())) {

                        mDisplaySessionBtn.setText("Edit session");
                    }

                    ImageView sessionImage = view.findViewById(R.id.displaySessionImage);

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
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    public void cleanListeners () {

        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
}
