package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.support.v4.app.Fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * This fragment takes a longitude and latitude and displays the corresponding session with that longitude and latitude.
 */
public class DisplaySessionFragment extends Fragment implements OnMapReadyCallback {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<Query, ChildEventListener> childEventListenerMap;
    private CardView sessionImageCardView;
    private TextView mDateAndTime;
    private TextView mParticipants;
    private TextView mSessionName;
    private Button mDisplaySessionBtn;
    private CircleImageView mHostImage;
    private CircleImageView mCurrentUserPostImage;
    private TextView mHostAboutTV;
    private TextView mHost;
    private TextView mWhatTW;
    private TextView mWhoTW;
    private TextView mWhereTW;
    private TextView mSessionType;
    private TextView mAddressAndSessionType;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    private LinearLayout writePostLsyout;
    private LinearLayout commentLayout;
    private static final String SESSION_LATITUDE = "sessionLatitude";
    private static final String SESSION_LONGITUDE = "sessionLongitude";
    private static final String SESSION_ID = "sessionID";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private String sessionID="";
    private ChildEventListener sessionChildEventListener;
    private ValueEventListener sessionListener;
    private Session session;

    private LinearLayoutManager linearLayoutManager;
    private Map<Long, String> postIDs = new HashMap<Long, String>();
    private ArrayList<PostBranch> postBranchArrayList;
    private Map<String, Long> nrOfComments = new HashMap<String, Long>();
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private GoogleMap mMap;
    private RecyclerView postList;
    private RecyclerView.Adapter<PostsViewHolder> postsViewHolderAdapter;
    User user;
    private OnEditSessionListener onEditSessionListener;
    private OnBookSessionListener onBookSessionListener;
    private OnCancelBookedSessionListener onCancelBookedSessionListener;
    private OnCommentClickedListener onCommentClickedListener;
    private ProgressBar progressBar;
    private MyProgressBar myProgressBar;

    public DisplaySessionFragment() {
        // Required empty public constructor
    }

    public static DisplaySessionFragment newInstance(double sessionLatitude, double sessionLongitude, String sessionID) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putDouble(SESSION_LATITUDE, sessionLatitude);
        args.putDouble(SESSION_LONGITUDE, sessionLongitude);
        args.putString(SESSION_ID, sessionID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_session, container, false);

        postBranchArrayList = new ArrayList<>();
        childEventListenerMap = new HashMap<>();

        LinearLayout displaySessionContainer;
        View displaySession;
        displaySessionContainer = view.findViewById(R.id.display_session_container);
        displaySession = inflater.inflate(R.layout.display_session,displaySessionContainer,false);

        /*progressBar = displaySession.findViewById(R.id.progressBar_cyclic);
        myProgressBar = new MyProgressBar(progressBar, getActivity());
        myProgressBar.startProgressBar();*/

        mDateAndTime = displaySession.findViewById(R.id.dateAndTimeTW);
        mParticipants = displaySession.findViewById(R.id.participantsTW);
        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mHostAboutTV = displaySession.findViewById(R.id.hostAbout);
        mSessionName = displaySession.findViewById(R.id.sessionName);
        mAddressAndSessionType = displaySession.findViewById(R.id.addressAndSessionTypeTW);
        writePostLsyout = displaySession.findViewById(R.id.write_post_layout);
        mWhatTW = displaySession.findViewById(R.id.whatTW);
        mWhoTW = displaySession.findViewById(R.id.whoTW);
        mWhereTW = displaySession.findViewById(R.id.whereTW);
        mCurrentUserPostImage = displaySession.findViewById(R.id.session_post_current_user_image);
        sessionImageCardView = displaySession.findViewById(R.id.sessionImageCardView);
        mSessionType = displaySession.findViewById(R.id.sessionType);

        /*
         Get latitude and longitude of session from previous activity.
         Use latitiude and longitude in method findSessionAndFillInUI to fill view
         with session details.
         */
        sessionLatitude = getArguments().getDouble(SESSION_LATITUDE);
        sessionLongitude = getArguments().getDouble(SESSION_LONGITUDE);
        sessionID = getArguments().getString(SESSION_ID);

        if (sessionLatitude!=null | !sessionID.equals("")) {
            // FINDS SESSION AND FILLS UI
            findSessionAndFillInUI(sessionLatitude, sessionLongitude);
        } else {
            Toast toast = Toast.makeText(getActivity(), R.string.Session_not_found_please_try_again_later,Toast.LENGTH_LONG);
            toast.show();
        }

        // Setup standard aspect ratio of session image
        sessionImageCardView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) sessionImageCardView.getLayoutParams();
                mParams.height = sessionImageCardView.getWidth()*getResources().getInteger(R.integer.heightOfSessionImageNumerator)/getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                sessionImageCardView.setLayoutParams(mParams);
                sessionImageCardView.postInvalidate();
            }
        });

        // Setup Booking, Cancelling and Editing Button
        mDisplaySessionBtn = displaySession.findViewById(R.id.displaySessionBtn);
        displaySessionContainer.addView(displaySession);
        mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                 and send the session key to that activity as bundle.
                 */
                if (session.getHost().equals(currentFirebaseUser.getUid())) {
                    onEditSessionListener.OnEditSession(sessionID);
                }
                /*
                 Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                 remove the current user from that session participant list and go back to main activity.
                 */
                else if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                    onCancelBookedSessionListener.OnCancelBookedSession(sessionID);
                }

                else {
                    /*
                     Else (button will show join session) add the user id to the session participant list and
                     the user sessions attending list when button is clicked.
                     */
                    onBookSessionListener.OnBookSession(sessionID);
                }
            }
        });

        // When clicked on add post text a dialog fragment where posts can be written is opened
        writePostLsyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WritePostFragment writePostFragment = WritePostFragment.newInstance(sessionID);
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (writePostFragment!=null) {
                    transaction.remove(writePostFragment);
                }

                writePostFragment.show(transaction,"writePostFragment");

            }
        });

        // Posts are displayed in a RecyclerView
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
                if (postBranchArrayList.size()>0) {
                    holder.setHeading(postBranchArrayList.get(position).getPost().getSenderName());

                    TextTimestamp textTimestamp = new TextTimestamp((long) postBranchArrayList.get(position).getPost().getTimestamp());
                    String timeText = textTimestamp.textDateAndTime();
                    holder.setTime(timeText);

                    holder.setUserImage(postBranchArrayList.get(position).getPost().getSenderThumbImage(), getContext());
                    holder.setMessage(postBranchArrayList.get(position).getPost().getMessage());
                    holder.setCommentClickListener(postBranchArrayList.get(position).getPostID());
                    holder.setNrOfComments(nrOfComments.get(postBranchArrayList.get(position).getPostID()));
                }
            }
            @Override
            public int getItemCount() {
                return postBranchArrayList.size();
            }
        };
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        postList.setAdapter(postsViewHolderAdapter);

        postList.setNestedScrollingEnabled(false);

        // Setup static map with session location
        GoogleMapOptions options = new GoogleMapOptions();
        options.liteMode(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);

        // Set the users profile image to the "write post" layout
        mUserDbRef.child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                setImage(user.getThumb_image(), mCurrentUserPostImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }

    /**
     Find the session with the argument latitude value in firebase under the child sessions and fill view with session details
     */
    private void findSessionAndFillInUI(final Double latitude, final Double longitude){

        // Get the session information
        if (sessionID.equals("")) {
            if (!childEventListenerMap.containsKey(mSessionDbRef.orderByChild("latitude"))) {
                sessionChildEventListener = mSessionDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        session = dataSnapshot.getValue(Session.class);
                        if (longitude==session.getLongitude()) {
                            fillUI(dataSnapshot);
                        }
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        session = dataSnapshot.getValue(Session.class);
                        if (longitude==session.getLongitude()) {
                            fillUI(dataSnapshot);
                        }
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
                childEventListenerMap.put(mSessionDbRef.orderByChild("latitude").equalTo(latitude), sessionChildEventListener);
            }


        } else {
            if (!listenerMap.containsKey(mSessionDbRef.child(sessionID))) {
                sessionListener = mSessionDbRef.child(sessionID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        session = dataSnapshot.getValue(Session.class);
                        fillUI(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            listenerMap.put(mSessionDbRef.child(sessionID),sessionListener);

        }

    }

    private void fillUI(DataSnapshot dataSnapshot) {
        session = dataSnapshot.getValue(Session.class);
        sessionID = dataSnapshot.getRef().getKey();
        // Set default text on button
        mDisplaySessionBtn.setText("Book session");
        // Count participants
        long countParticipants;
        if (dataSnapshot.hasChild("participants")) {
            countParticipants = dataSnapshot.child("participants").getChildrenCount();
        } else {
            countParticipants = 0;
        }
        // Set the session information in UI
        TextTimestamp textTimestamp = new TextTimestamp(session.getSessionTimestamp());
        String sessionDateAndTime = textTimestamp.textSessionDateAndTime();
        sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
        mDateAndTime.setText(sessionDateAndTime);
        mParticipants.setText(countParticipants +"/" + session.getMaxParticipants());
        mSessionName.setText(session.getSessionName());
        String address = getAddress(session.getLatitude(),session.getLongitude());
        mAddressAndSessionType.setText(address);
        mWhatTW.setText(session.getWhat());
        mWhoTW.setText(session.getWho());
        mWhereTW.setText(session.getWhere());
        mSessionType.setText(session.getSessionType());
        /*
         Get the host image from the database (found under users with the userID=session.host)
         */
        mUserDbRef.child(session.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                setImage(user.getImage(), mHostImage);
                String hostText = getString(R.string.hosted_by_text) + " " + user.getFirstName();
                mHost.setText(hostText);
                mHostAboutTV.setText(user.getAboutMe());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        // Set an onclicklistener to number of participants and start dialog fragment listing participants if clicked
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
        // Set the session image
        ImageView sessionImage = view.findViewById(R.id.displaySessionImage);
        setImage(session.getImageUrl(), sessionImage);
        sessionImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
        // Listen for posts and add add them to wall
        if (!listenerMap.containsKey(mSessionDbRef.child(sessionID).child("posts"))) {
            ValueEventListener postsListener = mSessionDbRef.child(sessionID).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Session session = new Session();
                    postBranchArrayList.clear();
                    session.setPosts((HashMap<String,Boolean>)dataSnapshot.getValue());
                    if (dataSnapshot.getChildrenCount()>0) {
                        for (final String postID : session.getPosts().keySet()) {
                            rootDbRef.child("posts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Post post = dataSnapshot.getValue(Post.class);
                                    PostBranch postBranch = new PostBranch(dataSnapshot.getKey(),post);
                                    postBranchArrayList.add(postBranch);
                                    Collections.sort(postBranchArrayList);
                                    postsViewHolderAdapter.notifyDataSetChanged();
                                    // Number of comments listener
                                    if (!listenerMap.containsKey(rootDbRef.child("postMessages").child(postID))) {

                                        ValueEventListener postMessagesListener = rootDbRef.child("postMessages").child(postID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                nrOfComments.put(dataSnapshot.getKey(),dataSnapshot.getChildrenCount());
                                                for (int i = 0; i < postBranchArrayList.size(); i++) {
                                                    if (postBranchArrayList.get(i).postID.equals(dataSnapshot.getKey())) {
                                                        postsViewHolderAdapter.notifyItemChanged(i);
                                                    }
                                                }
                                                postsViewHolderAdapter.notifyDataSetChanged();
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        listenerMap.put(rootDbRef.child("postMessages").child(postID), postMessagesListener);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            listenerMap.put(mSessionDbRef.child(dataSnapshot.getKey()).child("posts"), postsListener);
        }
    }

    // Setup static map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);
        TextTimestamp textTimestamp = new TextTimestamp(session.getSessionTimestamp());
        mMap.addMarker(new MarkerOptions().position(markerLatLng).title(session.getSessionType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_on_black_24dp)).
                snippet(textTimestamp.textTime()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
    }
    // Posts viewholder for the post recyclerview
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
        public void setCommentClickListener(final String postID) {
            TextView commentLayout = mView.findViewById(R.id.session_post_comment_text);
            TextView NrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCommentClickedListener.OnCommentClicked(postID);
                }
            });
            NrOfCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCommentClickedListener.OnCommentClicked(postID);
                }
            });

        }
        public void setNrOfComments(Long nr) {
            TextView NrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            if (nr==null || nr<1) {
                NrOfCommentsLayout.setVisibility(View.GONE);
            } else if (nr<2) {
                NrOfCommentsLayout.setVisibility(View.VISIBLE);
                NrOfCommentsLayout.setText(nr+getString(R.string.comment_text));
            } else {
                NrOfCommentsLayout.setVisibility(View.VISIBLE);
                NrOfCommentsLayout.setText(nr+getString(R.string.comments_text));
            }
        }
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
        for (Map.Entry<Query, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            Query ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        childEventListenerMap.clear();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        listenerMap.clear();
    }

    public void cleanListeners () {

        for (Map.Entry<Query, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            Query ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    // Model PostBranch which is used to reflect the branch posts in the database
    public class PostBranch implements Comparable<PostBranch>{
        String postID;
        Post post;
        public PostBranch(String postID, Post post) {
            this.postID = postID;
            this.post = post;
        }

        public PostBranch() {
        }
        public String getPostID() {
            return postID;
        }

        public void setPostID(String postID) {
            this.postID = postID;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        @Override
        public int compareTo(@NonNull PostBranch postBranch) {
            return ((int) (long) this.post.getTimestamp() - (int) (long) postBranch.post.getTimestamp());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnEditSessionListener) {
            onEditSessionListener = (OnEditSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEditSessionListener");
        }
        if (context instanceof OnBookSessionListener) {
            onBookSessionListener = (OnBookSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBookSessionListener");
        }
        if (context instanceof OnCancelBookedSessionListener) {
            onCancelBookedSessionListener = (OnCancelBookedSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCancelBookedSessionListener");
        }
        if (context instanceof OnCommentClickedListener) {
            onCommentClickedListener = (OnCommentClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCommentClickedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        onEditSessionListener = null;
        onBookSessionListener = null;
        onCancelBookedSessionListener = null;
        onCommentClickedListener = null;
    }
    public interface OnEditSessionListener {
        void OnEditSession(String sessionID);
    }
    public interface OnBookSessionListener {
        void OnBookSession(String sessionID);
    }
    public interface OnCancelBookedSessionListener {
        void OnCancelBookedSession(String sessionID);
    }
}