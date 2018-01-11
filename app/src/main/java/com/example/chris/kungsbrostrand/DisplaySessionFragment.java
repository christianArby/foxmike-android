package com.example.chris.kungsbrostrand;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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


public class DisplaySessionFragment extends DialogFragment implements OnMapReadyCallback {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerMap;
    private TextView mDateAndTime;
    private TextView mParticipants;
    private TextView mSessionName;
    private Button mDisplaySessionBtn;
    private CircleImageView mHostImage;
    private TextView mHost;
    private TextView mAddressAndSessionType;
    private String sessionID;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    private static final String SESSION_LATITUDE = "sessionLatitude";
    private static final String SESSION_LONGITUDE = "sessionLongitude";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private ChildEventListener sessionChildEventListener;
    private Session session;

    private GoogleMap mMap;
    private RecyclerView postList;

    private RecyclerView.Adapter<PostsViewHolder> postsViewHolderAdapter;
    User user;

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
        mAddressAndSessionType = displaySession.findViewById(R.id.addressAndSessionTypeTW);


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
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(sessionID).removeValue();
                    Intent mainIntent = new Intent(getActivity(), MainPlayerActivity.class);
                    startActivity(mainIntent);
                }

                else {

                    /**
                     Else (button will show join session) add the user id to the session participant list and
                     the user sessions attending list when button is clicked.
                     */

                    final DatabaseReference sessionIDref = mSessionDbRef.child(sessionID);
                    sessionIDref.child("participants").child(currentFirebaseUser.getUid()).setValue(true);
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").child(sessionID).setValue(true);
                    Intent mainIntent = new Intent(getActivity(), MainPlayerActivity.class);
                    startActivity(mainIntent);
                }

            }
        });

        // ---------------- TEMPORARY

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserDbRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                postsViewHolderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // -----------------------------------

        postList = (RecyclerView) view.findViewById(R.id.post_list);
        postList.setLayoutManager(new LinearLayoutManager(getContext()));

        postsViewHolderAdapter = new RecyclerView.Adapter<PostsViewHolder>() {
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_post_single_layout, parent, false);
                return new PostsViewHolder(view);
            }

            @Override
            public void onBindViewHolder(PostsViewHolder holder, int position) {
                if (user!=null) {
                    if (user.getName()!=null) {
                        holder.setHeading("Erik öhrn");
                        holder.setTime("4 juli 2017 kl. 07:36");
                        holder.setUserImage(user.getThumb_image(), getContext());
                        holder.setMessage("Amanda och jag får antagligen besök av kompisar sth, men skulle det bli ändrade planer kommer vi!");
                    }

                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

        postList.setAdapter(postsViewHolderAdapter);

        // Map
        GoogleMapOptions options = new GoogleMapOptions();
        options.liteMode(true);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, mapFragment).commit();

        mapFragment.getMapAsync(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);
        mMap.addMarker(new MarkerOptions().position(markerLatLng).title(session.getSessionType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_on_black_24dp)).
                snippet(session.textTime()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public PostsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setHeading(String heading) {
            TextView headingTV = (TextView) mView.findViewById(R.id.session_post_name);
            headingTV.setText(heading);
        }

        public void setTime(String text) {
            TextView messageView = (TextView) mView.findViewById(R.id.session_post_time);
            messageView.setText(text);
        }

        public void setMessage(String text) {
            TextView messageView = (TextView) mView.findViewById(R.id.session_post_message);
            messageView.setText(text);
        }

        public void setUserImage(String thumb_image, android.content.Context context) {
            CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.session_post_image);
            Glide.with(context).load(thumb_image).into(userProfileImageIV);
        }
    }

    /**
     Find the session with the argument latitude value in firebase under the child sessions and fill view with session details
     */

    private void findSessionAndFillInUI(final Double latitude, final Double longitude){

        // Get the session information
        sessionChildEventListener = mSessionDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                session = dataSnapshot.getValue(Session.class);

                long countParticipants;

                if (dataSnapshot.hasChild("participants")) {
                    countParticipants = dataSnapshot.child("participants").getChildrenCount();
                } else {
                    countParticipants = 0;
                }


                if(session.getLongitude()==longitude) {

                    childEventListenerMap.put(dataSnapshot.getRef(), sessionChildEventListener);


                    String sessionDateAndTime = session.getSessionDate().textFullDay() + " " + session.getSessionDate().day + " " + session.getSessionDate().textMonth() + " " + session.textTime();
                    sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                    mDateAndTime.setText(sessionDateAndTime);



                    // Set the session information in UI
                    mParticipants.setText("Participants: " + countParticipants +"/" + session.getMaxParticipants());
                    sessionID = dataSnapshot.getRef().getKey();
                    mSessionName.setText(session.getSessionName());

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
                    setImage(session.getImageUrl(), sessionImage);

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
