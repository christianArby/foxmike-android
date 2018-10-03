package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.interfaces.AdvertisementListener;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.User;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;

/**
 * This fragment takes a longitude and latitude and displays the corresponding session with that longitude and latitude.
 */
public class DisplayAdvertisementFragment extends Fragment implements OnMapReadyCallback {

    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<Query, ChildEventListener> childEventListenerMap;
    private ConstraintLayout sessionImageCardView;
    private TextView dateAndTimeHeadingTV;
    private TextView mAdvertisementName;
    private TextView gotToSession;
    private TextView mParticipants;
    private LinearLayout mManageBooking;
    private TextView mManageBookingTV;
    private CircleImageView mHostImage;
    private CircleImageView mCurrentUserPostImage;
    private TextView mHostAboutTV;
    private TextView mHost;
    private TextView mWhatTW;
    private TextView mWhoTW;
    private TextView mWhereTW;
    private TextView mAddress;
    private TextView mSendMessageToHost;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    private LinearLayout writePostLsyout;
    private LinearLayout commentLayout;
    private static final String ADVERTISEMENT_ID = "advertisementId";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private String advertisementId ="";
    private ChildEventListener sessionChildEventListener;
    private ValueEventListener fbSessionListener;
    private Session session;
    private TextView priceTV;
    private LinearLayoutManager linearLayoutManager;
    private Map<Long, String> postIDs = new HashMap<Long, String>();
    private ArrayList<PostBranch> postBranchArrayList;
    private Map<String, Long> nrOfComments = new HashMap<String, Long>();
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private GoogleMap mMap;
    private RecyclerView postList;
    private RecyclerView.Adapter<PostsViewHolder> postsViewHolderAdapter;
    private User currentUser;
    private SessionListener sessionListener;
    private OnCommentClickedListener onCommentClickedListener;
    private UserAccountFragment.OnUserAccountFragmentInteractionListener onUserAccountFragmentInteractionListener;
    private OnSessionClickedListener onSessionClickedListener;
    private AdvertisementListener advertisementListener;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private User host;
    private boolean currentUserLoaded;
    private boolean sessionLoaded;
    private boolean hostLoaded;
    private boolean postsLoaded;
    private boolean postCommentsLoaded;
    private boolean hasPaymentSystem;
    private HashMap defaultSourceMap;
    private boolean mapReady;
    private boolean advertisementLoaded;
    private boolean advertisementUsed;
    private Advertisement advertisement;
    private boolean currentUserAndViewUsed;
    private boolean sessionUsed;
    private boolean currentUserAndSessionAndViewAndMapUsed;
    private boolean hostAndViewUsed;
    private boolean postsUsed;
    private boolean postCommentsUsed;
    private boolean paymentMethodLoaded;
    private boolean sessionAndPaymentAndViewUsed;
    private boolean advertisementAndViewUsed;
    @BindView(R.id.manageBooking) LinearLayout manageBooking;
    private LinearLayout price;
    private ImageView advertisementImage;
    private int asyncTasksFinished = 0;
    private TextView cancelledTV;
    @BindView(R.id.participants_layout) LinearLayout participants;
    private OnChatClickedListener onChatClickedListener;
    public DisplayAdvertisementFragment() {
        // Required empty public constructor
    }

    public static DisplayAdvertisementFragment newInstance(String advertisementId) {
        DisplayAdvertisementFragment fragment = new DisplayAdvertisementFragment();
        Bundle args = new Bundle();
        args.putString(ADVERTISEMENT_ID, advertisementId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup static map with session location
        if (null == getChildFragmentManager().findFragmentByTag("xDisplayAdvertisementMapsFragment")) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.liteMode(true);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.child_fragment_container, mapFragment,"xDisplayAdvertisementMapsFragment").commit();
            mapFragment.getMapAsync(this);
        }

        postBranchArrayList = new ArrayList<>();
        childEventListenerMap = new HashMap<>();

        if (getArguments() != null) {
            advertisementId = getArguments().getString(ADVERTISEMENT_ID);
        }

        // GET THE PAYMENT INFO FROM CURRENT USER
        getDefaultSourceMap();

        // GET CURRENT USER FROM DATABASE

        ValueEventListener currentUserListener = mUserDbRef.child(currentFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserAndViewUsed = false;
                currentUserAndSessionAndViewAndMapUsed = false;
                currentUser = dataSnapshot.getValue(User.class);
                currentUserLoaded =true;
                onTaskFinished();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listenerMap.put(mUserDbRef.child(currentFirebaseUser.getUid()),currentUserListener);

        // GET SESSION FROM DATABASE
        if (!advertisementId.equals("")) {
            // FINDS SESSION AND FILLS UI
            // Get the session information
            if (!listenerMap.containsKey(rootDbRef.child("advertisements").child(advertisementId))) {
                fbSessionListener = rootDbRef.child("advertisements").child(advertisementId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        advertisement = dataSnapshot.getValue(Advertisement.class);
                        advertisementId = dataSnapshot.getRef().getKey();
                        advertisementAndViewUsed = false;
                        advertisementUsed = false;
                        advertisementLoaded = true;
                        onTaskFinished();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                listenerMap.put(rootDbRef.child("advertisements").child(advertisementId), fbSessionListener);
            }
        } else {
            Toast toast = Toast.makeText(getActivity(), R.string.Session_not_found_please_try_again_later,Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void getDefaultSourceMap () {
        try {
            MainPlayerActivity mainPlayerActivity = (MainPlayerActivity) getActivity();
            mainPlayerActivity.subject.subscribe(new Consumer<HashMap>() {
                @Override
                public void accept(HashMap hashMap) throws Exception {
                    if (hashMap.get("brand")!=null) {
                        defaultSourceMap = hashMap;
                        hasPaymentSystem = true;
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onTaskFinished();

                    } else {
                        hasPaymentSystem = false;
                        defaultSourceMap = new HashMap();
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onTaskFinished();
                    }
                }
            });
            return;
        } catch (RuntimeException e){
            defaultSourceMap = new HashMap();
            sessionAndPaymentAndViewUsed = false;
            paymentMethodLoaded = true;
            onTaskFinished();
        }
    }

    private void getAdvertisementHost() {
        /*
            Get the host image from the database (found under users with the userID=session.host)
            */
        mUserDbRef.child(advertisement.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                host = dataSnapshot.getValue(User.class);
                hostLoaded = true;
                onTaskFinished();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getPosts() {
        if (!listenerMap.containsKey(rootDbRef.child("advertisements").child(advertisementId).child("posts"))) {
            ValueEventListener postsListener = rootDbRef.child("advertisements").child(advertisementId).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Advertisement tempAd = new Advertisement();
                    postBranchArrayList.clear();
                    tempAd.setPosts((HashMap<String,Boolean>)dataSnapshot.getValue());
                    if (dataSnapshot.getChildrenCount()>0) {
                        for (final String postID : tempAd.getPosts().keySet()) {
                            rootDbRef.child("posts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Post post = dataSnapshot.getValue(Post.class);
                                    PostBranch postBranch = new PostBranch(dataSnapshot.getKey(),post);
                                    postBranchArrayList.add(postBranch);
                                    if (postBranchArrayList.size()==tempAd.getPosts().size()) {
                                        Collections.sort(postBranchArrayList);
                                        postsLoaded = true;
                                        onTaskFinished();
                                    }
                                    // Number of comments listener
                                    if (!listenerMap.containsKey(rootDbRef.child("postMessages").child(postID))) {
                                        ValueEventListener postMessagesListener = rootDbRef.child("postMessages").child(postID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                nrOfComments.put(dataSnapshot.getKey(), dataSnapshot.getChildrenCount());
                                                if (nrOfComments.size()==tempAd.getPosts().size()) {
                                                    postCommentsUsed = false;
                                                    postCommentsLoaded = true;
                                                    onTaskFinished();
                                                }
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
            listenerMap.put(rootDbRef.child("advertisements").child(advertisementId).child("posts"), postsListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_advertisement, container, false);

        setRetainInstance(true);

        LinearLayout displayAdvertisementContainer;
        View displayAdvertisement;
        displayAdvertisementContainer = view.findViewById(R.id.display_advertisement_container);
        displayAdvertisement = inflater.inflate(R.layout.display_advertisement,displayAdvertisementContainer,false);
        ButterKnife.bind(this, displayAdvertisement);

        dateAndTimeHeadingTV = displayAdvertisement.findViewById(R.id.sessionDateHeading);
        mAdvertisementName = displayAdvertisement.findViewById(R.id.adName);
        mParticipants = displayAdvertisement.findViewById(R.id.participantsTW);
        mHostImage = displayAdvertisement.findViewById(R.id.displaySessionHostImage);
        mHost = displayAdvertisement.findViewById(R.id.hostName);
        mHostAboutTV = displayAdvertisement.findViewById(R.id.hostAbout);
        mAddress = displayAdvertisement.findViewById(R.id.addressTV);
        writePostLsyout = displayAdvertisement.findViewById(R.id.write_post_layout);
        mWhatTW = displayAdvertisement.findViewById(R.id.whatTW);
        mWhoTW = displayAdvertisement.findViewById(R.id.whoTW);
        mWhereTW = displayAdvertisement.findViewById(R.id.whereTW);
        mCurrentUserPostImage = displayAdvertisement.findViewById(R.id.session_post_current_user_image);
        sessionImageCardView = view.findViewById(R.id.sessionImageCardView);
        mSendMessageToHost = displayAdvertisement.findViewById(R.id.sendMessageToHost);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        toolbar = view.findViewById(R.id.toolbar);
        priceTV = displayAdvertisement.findViewById(R.id.priceTV);
        mManageBooking = displayAdvertisement.findViewById(R.id.manageBooking);
        mManageBookingTV = displayAdvertisement.findViewById(R.id.manageBookingTV);
        displayAdvertisementContainer.addView(displayAdvertisement);
        gotToSession = displayAdvertisement.findViewById(R.id.go_to_session);
        // Set the session image
        advertisementImage = view.findViewById(R.id.displaySessionImage);
        postList = (RecyclerView) view.findViewById(R.id.post_list);
        cancelledTV = displayAdvertisement.findViewById(R.id.cancelledText);

        price = displayAdvertisement.findViewById(R.id.price);

        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.displaySessionAppBar);

        // Setup standard aspect ratio of session image
        sessionImageCardView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) sessionImageCardView.getLayoutParams();
                mParams.height = sessionImageCardView.getWidth()*getResources().getInteger(R.integer.heightOfSessionImageNumerator)/getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                sessionImageCardView.setLayoutParams(mParams);
                sessionImageCardView.postInvalidate();

                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    boolean isShow = true;
                    int scrollRange = -1;

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();
                        }
                        if (scrollRange + verticalOffset == 0) {
                            if (advertisement!=null) {
                                collapsingToolbarLayout.setTitle(advertisement.getAdvertisementName());
                            } else {
                                collapsingToolbarLayout.setTitle(" ");
                            }

                            isShow = true;
                        } else if(isShow) {
                            collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                            isShow = false;
                        }
                    }
                });
            }
        });

        // Setup wall, Posts are displayed in a RecyclerView
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
                    holder.setCommentClickListener(postBranchArrayList.get(position).getPostID(),postBranchArrayList.get(position).getPost().getSenderName(),timeText, postBranchArrayList.get(position).getPost().getMessage(),postBranchArrayList.get(position).getPost().getSenderThumbImage());
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
        postList.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onTaskFinished();
    }

    private void onTaskFinished() {

        // ---------------- ADVERTISEMENT-----------------
        if (advertisementLoaded && !advertisementUsed) {
            advertisementUsed =true;
            getPosts();
            getAdvertisementHost();
            loadSession();
        }

        // ---------------- ADVERTISEMENT && VIEW-----------------
        if (advertisementLoaded && getView()!=null && !advertisementAndViewUsed) {
            advertisementAndViewUsed = true;

            // -----------  Set the session information in UI from session object --------------
            String address = getAddress(advertisement.getLatitude(),advertisement.getLongitude());
            mAddress.setText(address);
            mWhatTW.setText(advertisement.getWhat());
            mWhoTW.setText(advertisement.getWho());
            mWhereTW.setText(advertisement.getWhereAt());

            // -----------  set the number of participants ------------
            long countParticipants;
            if (advertisement.getParticipantsIds().size()>0) {
                countParticipants = advertisement.getParticipantsIds().size();
            } else {
                countParticipants = 0;
            }
            mParticipants.setText(countParticipants +"/" + advertisement.getMaxParticipants());

            gotToSession.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(advertisement.getSessionId());
                }
            });

            if (advertisement.getStatus().equals("cancelled")) {
                cancelledTV.setVisibility(View.VISIBLE);
                price.setVisibility(View.GONE);
                manageBooking.setVisibility(View.GONE);
                participants.setVisibility(View.GONE);
                writePostLsyout.setVisibility(View.GONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.occasion_cancelled_text).setTitle(R.string.occasion_cancelled);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                cancelledTV.setVisibility(View.GONE);
                dateAndTimeHeadingTV.setText(TextTimestamp.textSessionDateAndTime(advertisement.getAdvertisementTimestamp()));
            }
            mAdvertisementName.setText(advertisement.getAdvertisementName());
            // set the image
            setImage(advertisement.getImageUrl(), advertisementImage);
            //advertisementImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
            //collapsingToolbarLayout.setTitle(advertisement.getAdvertisementName());

            // Set an onclicklistener to number of participants and start dialog fragment listing participants if clicked
            mParticipants.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(advertisement.getParticipantsIds());
                    FragmentManager fragmentManager = getChildFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (participantsFragment!=null) {
                        transaction.remove(participantsFragment);
                    }
                    participantsFragment.show(transaction,"participantsFragment");
                }
            });

            // ----------------- Set price text ---------------------------------------------------------------

            String currencyString = "?";
            if (advertisement.getCurrency()==null) {
                currencyString = "";
            } else {
                currencyString = getString(R.string.sek);
            }
            String priceText;
            if (advertisement.getPrice()== 0) {
                priceText = getString(R.string.free);
            } else {
                if (advertisement.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    priceText = getString(R.string.price_colon) + " " + advertisement.getPrice() + " " + currencyString + " " + getString(R.string.per_person);

                } else {
                    priceText = getString(R.string.cost_colon) + " " + advertisement.getPrice() + " " + currencyString;


                }
            }
            priceTV.setText(priceText);

            // ----------- EDIT SESSION -----------------------------------------------------
            /**
             If the current user is the session host change the button text to "Edit session"
             */
            if (advertisement.getHost().equals(currentFirebaseUser.getUid())) {
                mManageBookingTV.setText(R.string.cancel_occasion);
                mSendMessageToHost.setText(R.string.show_and_edit_profile_text);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserAccountFragmentInteractionListener.OnUserAccountFragmentInteraction("edit");
                    }
                });
            } else {
                mManageBookingTV.setText(R.string.cancel_booking);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onChatClickedListener.OnChatClicked(advertisement.getHost(),host.getFirstName(),host.getThumb_image(),null);
                    }
                });
            }

            // -----------------------------SETUP WRITE POST------------------------------------------

            writePostLsyout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WritePostFragment writePostFragment = WritePostFragment.newInstance("advertisements", advertisementId, advertisement.getAdvertisementName());
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (null == fragmentManager.findFragmentByTag("writePostFragment")) {
                        transaction.add(R.id.container_fullscreen_display_session, writePostFragment,"writePostFragment").addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }

        // ---------------- CURRENTUSER && VIEW-----------------
        if (currentUserLoaded && getView()!=null && !currentUserAndViewUsed) {
            currentUserAndViewUsed=true;
            // Set the users profile image to the "write post" layout
            setImage(currentUser.getThumb_image(), mCurrentUserPostImage);
        }

        // ---------------- CURRENTUSER && SESSION && VIEW && MAP-----------------
        if (currentUserLoaded && sessionLoaded && mapReady && getView()!=null && !currentUserAndSessionAndViewAndMapUsed) {
            currentUserAndSessionAndViewAndMapUsed =true;
            // SETUP MAP
            LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);
            mMap.addMarker(new MarkerOptions().position(markerLatLng).title(session.getSessionType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_on_black_24dp)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
            // Setup Booking, Cancelling and Editing Button
            mManageBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*
                If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                and send the session key to that activity as bundle.
                */
                    if (session.getHost().equals(currentFirebaseUser.getUid())) {
                        advertisementListener.OnCancelAdvertisement(advertisement.getAdvertisementId(), advertisement.getSessionId(), advertisement.getAdvertisementTimestamp(), advertisement.getParticipantsIds(), session.getStripeAccountId());
                    }
                /*
                 Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                remove the current user from that session participant list and go back to main activity.
                */
                    else if (advertisement.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                        sessionListener.OnCancelBookedSession(advertisement.getParticipantsTimestamps().get(currentFirebaseUser.getUid()),advertisement.getAdvertisementTimestamp(),advertisementId,currentFirebaseUser.getUid(),advertisement.getParticipantsIds().get(currentFirebaseUser.getUid()),session.getStripeAccountId());
                    }
                }
            });
        }

        // ---------------- HOST && VIEW-----------------
        if (hostLoaded && getView()!=null && !hostAndViewUsed) {
            setImage(host.getImage(), mHostImage);
            String hostText = getString(R.string.hosted_by_text) + " " + host.getFirstName();
            mHost.setText(hostText);
            mHostAboutTV.setText(host.getAboutMe());
        }

        if (postsLoaded && getView()!=null && !postsUsed) {
            postsUsed = true;
            postsViewHolderAdapter.notifyDataSetChanged();
            postList.setAdapter(postsViewHolderAdapter);
        }

        if (postCommentsLoaded && getView()!=null && !postCommentsUsed) {
            postCommentsUsed = true;
            postsViewHolderAdapter.notifyDataSetChanged();
        }

        // ------------------------------ SESSION --- PAYMENT METHOD ----VIEW ------------------
        if (sessionLoaded && getView()!=null && paymentMethodLoaded && !sessionAndPaymentAndViewUsed) {
            sessionAndPaymentAndViewUsed = true;

            // NOT USED ANYMORE
        }
    }

    private void loadSession() {
        if (!listenerMap.containsKey(rootDbRef.child("sessions").child(advertisement.getSessionId()))) {
            fbSessionListener = rootDbRef.child("sessions").child(advertisement.getSessionId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    session = dataSnapshot.getValue(Session.class);
                    sessionLongitude = session.getLongitude();
                    sessionLatitude = session.getLatitude();
                    currentUserAndSessionAndViewAndMapUsed = false;
                    sessionAndPaymentAndViewUsed = false;
                    sessionUsed = false;
                    sessionLoaded = true;
                    onTaskFinished();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            listenerMap.put(rootDbRef.child("sessions").child(advertisement.getSessionId()),fbSessionListener);
        }
    }

    // Setup static map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        currentUserAndSessionAndViewAndMapUsed = false;
        mapReady = true;
        onTaskFinished();
    }

    // ------------   Posts viewholder for the post recyclerview ----------------------
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
        public void setCommentClickListener(String postID, String heading, String time, String message, String thumb_image) {
            TextView commentLayout = mView.findViewById(R.id.session_post_comment_text);
            TextView NrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCommentFragment(postID, heading, time, message, thumb_image);
                }
            });
            NrOfCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCommentFragment(postID, heading, time, message, thumb_image);
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

    private void startCommentFragment(String postID, String heading, String time, String message, String thumb_image) {
        CommentFragment commentFragment = CommentFragment.newInstance(postID, heading, time, message, thumb_image);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("commentFragment")) {
            transaction.add(R.id.container_fullscreen_display_session, commentFragment,"commentFragment").addToBackStack(null);
            transaction.commit();
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

            if (addresses.size()!=0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String address2 = addresses.get(0).getAddressLine(1);
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
                    if (addresses.get(0).getLocality()!=null) {
                        returnAddress = addresses.get(0).getLocality() + " " + addresses.get(0).getPremises();
                    } else {
                        returnAddress = "Unknown area";
                    }

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
    public void onDestroy() {
        super.onDestroy();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentUserAndViewUsed = false;
        currentUserAndSessionAndViewAndMapUsed = false;
        hostAndViewUsed = false;
        sessionUsed = false;
        sessionAndPaymentAndViewUsed = false;
        postsUsed = false;
        postCommentsUsed = false;
        advertisementUsed = false;
        advertisementAndViewUsed = false;
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

        if (context instanceof SessionListener) {
            sessionListener = (SessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SessionListener");
        }
        if (context instanceof OnCommentClickedListener) {
            onCommentClickedListener = (OnCommentClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCommentClickedListener");
        }
        if (context instanceof OnChatClickedListener) {
            onChatClickedListener = (OnChatClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatClickedListener");
        }
        if (context instanceof UserAccountFragment.OnUserAccountFragmentInteractionListener) {
            onUserAccountFragmentInteractionListener = (UserAccountFragment.OnUserAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserAccountFragmentInteractionListener");
        }
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
        if (context instanceof AdvertisementListener) {
            advertisementListener = (AdvertisementListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AdvertisementListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sessionListener = null;
        onCommentClickedListener = null;
        onChatClickedListener = null;
        onUserAccountFragmentInteractionListener = null;
        onSessionClickedListener = null;
        advertisementListener = null;
    }
}