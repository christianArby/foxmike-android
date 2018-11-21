package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;
import com.foxmike.android.interfaces.OnAdvertisementClickedListener;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionDateAndTime;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.AdvertisementRowViewHolder;
import com.foxmike.android.utils.TextTimestamp;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;

/**
 * This fragment takes a longitude and latitude and displays the corresponding session with that longitude and latitude.
 */
public class DisplaySessionFragment extends Fragment implements OnMapReadyCallback {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<Query, ChildEventListener> childEventListenerMap;
    private ConstraintLayout sessionImageCardView;
    private TextView mDuration;
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
    private TextView mSendMessageToHost;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    private LinearLayout writePostLayout;
    private LinearLayout commentLayout;
    private static final String SESSION_ID = "sessionID";
    private static final String REPRESSENTING_AD_TIMESTAMP = "representingAdTimestamp";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private String sessionID="";
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
    private OnCommentClickedListener onCommentClickedListener;
    private UserAccountFragment.OnUserAccountFragmentInteractionListener onUserAccountFragmentInteractionListener;
    private OnAdvertisementClickedListener onAdvertisementClickedListener;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private User host;
    private boolean currentUserLoaded;
    private boolean sessionLoaded;
    private boolean hostLoaded;
    private boolean postsLoaded;
    private boolean postCommentsLoaded;
    private TextView paymentMethodTV;
    private DotProgressBar paymentMethodProgressBar;
    private boolean hasPaymentSystem;
    private TextView addPaymentMethodTV;
    private LinearLayout paymentFrame;
    private HashMap defaultSourceMap;
    private boolean mapReady;
    BitmapDescriptor selectedIcon;
    private TextView editSession;
    private TextView snackBarDateAndTimeTV;
    private LinearLayout snackNoUpcomingAds;
    private boolean currentUserAndViewUsed;
    private boolean sessionUsed;
    private boolean currentUserAndSessionAndViewAndMapUsed;
    private boolean hostAndViewUsed;
    private boolean postsUsed;
    private boolean postCommentsUsed;
    private boolean paymentMethodLoaded;
    private boolean sessionAndPaymentAndViewUsed;
    private RecyclerView upcomingSessionsRV;
    private List<SessionDateAndTime> sessionDateAndTimeList = new ArrayList<>();
    private LinearLayoutManager sessionDateAndTimeLLManager;
    private boolean adSetupLoaded;
    private boolean sessionAndViewUsed;
    private boolean sessionAndPaymentAndViewAndAdSelectedUsed;
    private FrameLayout fbRVContainer;
    private LinearLayout showMore;
    private TextView showMoreTV;
    private int currentHeightInNr;
    private SessionListener sessionListener;
    private FirebaseRecyclerAdapter<Advertisement, AdvertisementRowViewHolder> fbAdDateAndTimeAdapter;
    private ImageView sessionImage;
    private int asyncTasksFinished = 0;
    private int rowIndex = -1;
    private int itemHeight = 0;
    private boolean adSelectedReady;
    private Long representingAdTimestamp;
    private Advertisement adSelected;
    private boolean repAdCancelled = false;
    private boolean paymentMethodAdSelectedAndViewUsed;
    private OnChatClickedListener onChatClickedListener;
    private NestedScrollView displaySessionSV;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout rootLayout;
    private TextView noSnackAdTV;

    public DisplaySessionFragment() {
        // Required empty public constructor
    }

    public static DisplaySessionFragment newInstance(String sessionID) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putString(SESSION_ID, sessionID);
        fragment.setArguments(args);
        return fragment;
    }

    public static DisplaySessionFragment newInstance(String sessionID, Long representingAdTimestamp) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putString(SESSION_ID, sessionID);
        args.putLong(REPRESSENTING_AD_TIMESTAMP, representingAdTimestamp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup static map with session location
        if (null == getChildFragmentManager().findFragmentByTag("xDisplaySessionMapsFragment")) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.liteMode(true);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.child_fragment_container, mapFragment,"xDisplaySessionMapsFragment").commit();
            mapFragment.getMapAsync(this);
        }

        postBranchArrayList = new ArrayList<>();
        childEventListenerMap = new HashMap<>();

        if (getArguments() != null) {
            sessionID = getArguments().getString(SESSION_ID);
            representingAdTimestamp = getArguments().getLong(REPRESSENTING_AD_TIMESTAMP);
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
                onAsyncTaskFinished();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listenerMap.put(mUserDbRef.child(currentFirebaseUser.getUid()),currentUserListener);

        // GET SESSION FROM DATABASE
        if (!sessionID.equals("")) {
            if (!listenerMap.containsKey(mSessionDbRef.child(sessionID))) {
                fbSessionListener = mSessionDbRef.child(sessionID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        session = dataSnapshot.getValue(Session.class);
                        sessionLongitude = session.getLongitude();
                        sessionLatitude = session.getLatitude();
                        sessionID = dataSnapshot.getRef().getKey();
                        currentUserAndSessionAndViewAndMapUsed = false;
                        sessionAndPaymentAndViewUsed = false;
                        sessionUsed = false;
                        sessionLoaded = true;
                        onAsyncTaskFinished();
                        getPosts();
                        getSessionHost();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                listenerMap.put(mSessionDbRef.child(sessionID),fbSessionListener);
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
                        onAsyncTaskFinished();
                    } else {
                        hasPaymentSystem = false;
                        defaultSourceMap = new HashMap();
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onAsyncTaskFinished();
                    }
                }
            });
            return;
        } catch (RuntimeException e){
            defaultSourceMap = new HashMap();
            sessionAndPaymentAndViewUsed = false;
            paymentMethodLoaded = true;
            onAsyncTaskFinished();
        }
    }

    private void getSessionHost() {
        /*
            Get the host image from the database (found under users with the userID=session.host)
            */
        mUserDbRef.child(session.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                host = dataSnapshot.getValue(User.class);
                hostLoaded = true;
                onAsyncTaskFinished();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getPosts() {
        if (!listenerMap.containsKey(mSessionDbRef.child(sessionID).child("posts"))) {
            ValueEventListener postsListener = mSessionDbRef.child(sessionID).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Session session = new Session();
                    postBranchArrayList.clear();
                    session.setPosts((HashMap<String,Long>)dataSnapshot.getValue());
                    if (dataSnapshot.getChildrenCount()>0) {
                        for (final String postID : session.getPosts().keySet()) {
                            rootDbRef.child("sessionPosts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Post post = dataSnapshot.getValue(Post.class);
                                    PostBranch postBranch = new PostBranch(dataSnapshot.getKey(),post);
                                    postBranchArrayList.add(postBranch);
                                    if (postBranchArrayList.size()==session.getPosts().size()) {
                                        Collections.sort(postBranchArrayList);
                                        postsLoaded = true;
                                        onAsyncTaskFinished();
                                    }
                                    // Number of comments listener
                                    if (!listenerMap.containsKey(rootDbRef.child("sessionPostComments").child(postID))) {

                                        ValueEventListener postCommentsListener = rootDbRef.child("sessionPostComments").child(postID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                nrOfComments.put(dataSnapshot.getKey(), dataSnapshot.getChildrenCount());
                                                if (nrOfComments.size()==session.getPosts().size()) {
                                                    postCommentsUsed = false;
                                                    postCommentsLoaded = true;
                                                    onAsyncTaskFinished();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        listenerMap.put(rootDbRef.child("sessionPostComments").child(postID), postCommentsListener);
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
            listenerMap.put(mSessionDbRef.child(sessionID).child("posts"), postsListener);
        }
    }

    private void setupAds() {

        // --------- GET ALL THE AD TIMESTAMPS SAVED UNDER SESSION/ADVERTISEMENTS ---------
        // Current time as timestamp
        Long currentTimestamp = System.currentTimeMillis();
        // Create query to get all the advertisement keys from the current session
        Query keyQuery = rootDbRef.child("sessions").child(session.getSessionId()).child("advertisements").orderByValue().startAt(currentTimestamp);
        // Use the query to get all the advertisement keys from the current session
        keyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // If data snapshot is null but representingAdTimestamp is not null it means that the ad has been cancelled during the time the user opened the session from the session representing
                    // the cancelled ad, set the boolean repAdCancelled to true
                    if (representingAdTimestamp!=0) {
                        repAdCancelled = true;
                    }
                    // display text "no upcoming sessions"
                    showMoreTV.setText(getResources().getString(R.string.no_upcoming_sessions));
                    // run method which updates snackbar
                    paymentMethodAdSelectedAndViewUsed = false;
                    adSelectedReady = true;
                    onAsyncTaskFinished();
                } else {
                    // if there are advertisements collect all the timestamps in a hashmap
                    HashMap<String,Long> adTimes = (HashMap<String,Long>) dataSnapshot.getValue();
                    // If activity has been opened from map representingAdTimestamp will be 0, update snackbar.
                    if (representingAdTimestamp==0) {
                        repAdCancelled = false;
                        // run method which updates snackbar
                        adSelected = null;
                        adSelectedReady = true;
                        paymentMethodAdSelectedAndViewUsed = false;
                        onAsyncTaskFinished();
                    }
                    if (!adTimes.containsValue(representingAdTimestamp)) {
                        // if this sessions representing timestamp is not part of the advertisment timestamps it means that the ad has been cancelled during the time the user opened the session from the session representing
                        // the cancelled ad, set the boolean repAdCancelled to true
                        repAdCancelled = true;
                        // run method which updates snackbar
                        adSelected = null;
                        adSelectedReady = true;
                        paymentMethodAdSelectedAndViewUsed = false;
                        onAsyncTaskFinished();
                    }
                    // create timestamp which is two weeks ahead of today
                    Long twoWeekTimestamp = new DateTime(currentTimestamp).plusWeeks(2).getMillis();
                    int twoWeekssize = 0;
                    // The list height will be based on how many ads exists within the two weeks
                    // count how many ads exists within the first two weeks
                    for (Long adTime: adTimes.values()) {
                        if (adTime<twoWeekTimestamp) {
                            twoWeekssize++;
                        }
                    }

                    // set the variable currentHeightInNr which later sets the height of the list to the number of ads existing within two weeks
                    currentHeightInNr = twoWeekssize;
                    // if number of ads within two weeks are less than 5 check if that is all the future ads, if so set currentHeightInNr to that number
                    // if not: display 4 ads regardless if they are within two weeks or not (4 items are minimum if there are more than 4 ads in the future)
                    if (twoWeekssize<5) {
                        if (adTimes.size()<5) {
                            currentHeightInNr = adTimes.size();
                        } else {
                            currentHeightInNr = 4;
                        }
                    }
                    // set the height of the list to the height of one row times number of ads to be displayed (currentHeightInNr)
                    ViewGroup.LayoutParams params =fbRVContainer.getLayoutParams();
                    params.height= 167*currentHeightInNr;
                    fbRVContainer.setLayoutParams(params);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // --------- POPULATE THE RECYCLERVIEW WITH THE ADVERTISEMENTS ---------
        // Create the key query which will get all the advertisement keys for ads with a date in the future.
        Query allKeysQuery = rootDbRef.child("sessions").child(session.getSessionId()).child("advertisements").orderByValue().startAt(currentTimestamp);
        DatabaseReference adDbRef = rootDbRef.child("advertisements");
        // Create the firebase recycler adapter which will fill the list with those advertisements specified by the above query
        FirebaseRecyclerOptions<Advertisement> options = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(allKeysQuery, adDbRef, Advertisement.class)
                .build();
        fbAdDateAndTimeAdapter = new FirebaseRecyclerAdapter<Advertisement, AdvertisementRowViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AdvertisementRowViewHolder holder, int position, @NonNull Advertisement model) {
                // load the views in each row in the recyclerview (list) with data from each advertisement (found in model)

                // -----------  set the number of participants ------------
                long countParticipants;
                if (model.getParticipantsIds()!=null) {
                    countParticipants = model.getParticipantsIds().size() + 1;
                } else {
                    countParticipants = 1;
                }
                // set the text of each row in the list of advertisements
                int maxParticipants = Integer.parseInt(model.getMaxParticipants()) + 1;
                holder.setParticipantsTV(countParticipants +"/" + maxParticipants);
                holder.advertisementRowDateAndTimeText.setText(TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp()));
                // set the click listener on each row
                holder.setAdvertisementRowClickedListener(new AdvertisementRowClickedListener() {
                    @Override
                    public void OnAdvertisementRowClicked(View view, int position) {
                        // Save the clicked position by setting the variable rowIndex = position
                        rowIndex = position;
                        // update snackbar
                        adSelected = fbAdDateAndTimeAdapter.getItem(position);
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onAsyncTaskFinished();
                        notifyDataSetChanged(); // Made effect on Recycler Views adapter
                    }
                });
                // set the row appearance to default (no selection or booked color)
                setAdListItemDefaultAppearance(holder);
                // if rowIndex==-1 it means that no item has been selected in the list, make the first item (this item) selected
                if (rowIndex==-1) {
                    if (representingAdTimestamp==model.getAdvertisementTimestamp()) {
                        rowIndex = position;
                    }
                }
                // If this item has been selected set advertisement adSelected to this ad and update snackbar
                if (rowIndex == position) {
                    adSelected=model;
                    paymentMethodAdSelectedAndViewUsed = false;
                    adSelectedReady = true;
                    setAdListItemSelectedAppearance(holder);
                    onAsyncTaskFinished();
                }
                // If the ad has participants, check if the current user is one of them and if so turn the appearance of the row to booked (filled dark green)
                if (model.getParticipantsIds().size()!=0) {
                    if (model.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                        setAdListItemBookedAppearance(holder);
                    }
                }
            }
            @NonNull
            @Override
            public AdvertisementRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_date_time_single_layout, parent, false);
                // When view is created measure the height of one row (item)
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(upcomingSessionsRV.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                itemHeight = view.getMeasuredHeight();
                return new AdvertisementRowViewHolder(view);
            }
        };
        // set the adapter to the recyclerview.
        upcomingSessionsRV.setAdapter(fbAdDateAndTimeAdapter);
        // --------- LISTEN TO CHANGES IN THE DATABASE) ---------
        fbAdDateAndTimeAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // If an item has been inserted, call the function updateListViews() which updates the showMore text
                updateListViews();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                // If an item has been removed that was selected and if the list only contained that item set the adSelected to null and update snackbar
                // else set the adSelected to the first item in the list instead and update snackbar.
                // TODO maybe better to set the snackbar to show availability
                if (rowIndex==positionStart) {
                    if (fbAdDateAndTimeAdapter.getItemCount()==0) {
                        adSelected = null;
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onAsyncTaskFinished();
                    } else {
                        adSelected = fbAdDateAndTimeAdapter.getItem(0);
                        rowIndex = 0;
                        fbAdDateAndTimeAdapter.notifyDataSetChanged();
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onAsyncTaskFinished();
                    }
                }
                // If an item has been removed, call the function updateListViews() which updates the showMore text
                updateListViews();
            }
        });
        // start listening to changes in the database
        fbAdDateAndTimeAdapter.startListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_session, container, false);

        setRetainInstance(true);

        LinearLayout displaySessionContainer;
        View displaySession;
        displaySessionContainer = view.findViewById(R.id.display_session_container);
        displaySession = inflater.inflate(R.layout.display_session,displaySessionContainer,false);

        mDuration = view.findViewById(R.id.durationTV);
        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mHostAboutTV = displaySession.findViewById(R.id.hostAbout);
        mAddressAndSessionType = view.findViewById(R.id.addressTV);
        writePostLayout = displaySession.findViewById(R.id.write_post_layout);
        mWhatTW = displaySession.findViewById(R.id.whatTW);
        mWhoTW = displaySession.findViewById(R.id.whoTW);
        mWhereTW = displaySession.findViewById(R.id.whereTW);
        mCurrentUserPostImage = displaySession.findViewById(R.id.session_post_current_user_image);
        sessionImageCardView = view.findViewById(R.id.sessionImageCardView);
        mSessionType = view.findViewById(R.id.sessionDateHeading);
        mSendMessageToHost = displaySession.findViewById(R.id.sendMessageToHost);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        toolbar = view.findViewById(R.id.toolbar);
        priceTV = view.findViewById(R.id.priceTV);
        mDisplaySessionBtn = view.findViewById(R.id.displaySessionBtn);
        displaySessionContainer.addView(displaySession);
        // Set the session image
        sessionImage = view.findViewById(R.id.displaySessionImage);
        postList = (RecyclerView) view.findViewById(R.id.post_list);
        postList.setVisibility(View.GONE);
        paymentMethodProgressBar = view.findViewById(R.id.paymentMethodProgressBar);
        addPaymentMethodTV = view.findViewById(R.id.addPaymentMethodTV);
        paymentFrame = view.findViewById(R.id.framePayment);
        paymentMethodTV = view.findViewById(R.id.paymentMethod);
        snackBarDateAndTimeTV = view.findViewById(R.id.snackBarDateAndTimeTV);
        upcomingSessionsRV = displaySession.findViewById(R.id.upcomingSessionsList);
        showMore = displaySession.findViewById(R.id.showMoreText);
        snackNoUpcomingAds = view.findViewById(R.id.snackNoUpcomingAds);
        editSession = displaySession.findViewById(R.id.editSession);
        fbRVContainer = displaySession.findViewById(R.id.firebaseRVContainer);
        sessionDateAndTimeLLManager = new LinearLayoutManager(getContext());
        upcomingSessionsRV.setHasFixedSize(true);
        upcomingSessionsRV.setLayoutManager(sessionDateAndTimeLLManager);
        ((SimpleItemAnimator) upcomingSessionsRV.getItemAnimator()).setSupportsChangeAnimations(false);
        upcomingSessionsRV.setNestedScrollingEnabled(false);
        showMoreTV = displaySession.findViewById(R.id.showMoreTV);
        displaySessionSV = view.findViewById(R.id.displaySessionSV);
        rootLayout = view.findViewById(R.id.rootLayout);
        noSnackAdTV = view.findViewById(R.id.noSnackAdTV);

        //set default
        snackBarDateAndTimeTV.setVisibility(View.GONE);
        priceTV.setVisibility(View.GONE);
        mDisplaySessionBtn.setVisibility(View.GONE);
        snackNoUpcomingAds.setVisibility(View.VISIBLE);

        paymentFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        appBarLayout = view.findViewById(R.id.displaySessionAppBar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset!=0) {
                    mSessionType.setVisibility(View.GONE);
                } else {
                    mSessionType.setVisibility(View.VISIBLE);
                }
            }
        });

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

        // --------- Set on click listener to showMore text --------
        // If number of items in the list (adapter) is more than current height + 4 set it to current height + 4
        // else set the height to the number of items in the adapter.

        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fbAdDateAndTimeAdapter.getItemCount()>(currentHeightInNr+4)) {
                    showMoreTV.setText(getResources().getString(R.string.show_more));
                    ViewGroup.LayoutParams params =fbRVContainer.getLayoutParams();
                    params.height= 167*(currentHeightInNr+4);
                    fbRVContainer.setLayoutParams(params);
                    currentHeightInNr = currentHeightInNr +4;
                } else {
                    showMoreTV.setText(getResources().getString(R.string.end_of_list));
                    ViewGroup.LayoutParams params =fbRVContainer.getLayoutParams();
                    params.height= 167*(fbAdDateAndTimeAdapter.getItemCount());
                    fbRVContainer.setLayoutParams(params);
                    currentHeightInNr = fbAdDateAndTimeAdapter.getItemCount();
                }
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
        onAsyncTaskFinished();
    }
    private void onAsyncTaskFinished() {

        // ---------------- CURRENTUSER && VIEW-----------------
        if (currentUserLoaded && getView()!=null && !currentUserAndViewUsed) {
            currentUserAndViewUsed=true;
            // Set the users profile image to the "write post" layout
            setImage(currentUser.getThumb_image(), mCurrentUserPostImage);
        }

        // ---------------- SESSION && VIEW-----------------
        if (sessionLoaded && getView()!=null && !sessionAndViewUsed) {
            sessionAndViewUsed = true;

            setupAds();

            if (session.getHost().equals(currentFirebaseUser.getUid())) {

                // -------------------- HOST -----------------------------
                editSession.setVisibility(View.VISIBLE);
                mSendMessageToHost.setText(R.string.show_and_edit_profile_text);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserAccountFragmentInteractionListener.OnUserAccountFragmentInteraction("edit");
                    }
                });

            } else {

                // -------------------- PLAYER -----------------------------
                editSession.setVisibility(View.GONE);
                mSendMessageToHost.setText(R.string.send_message_text);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onChatClickedListener.OnChatClicked(session.getHost(),host.getFirstName(),host.getThumb_image(),null);
                    }
                });
            }
        }

        // ---------------- HOST && VIEW-----------------
        if (hostLoaded && getView()!=null && !hostAndViewUsed) {
            hostAndViewUsed=true;
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

        // ------------------------------ SESSION --- PAYMENT METHOD -----------LOADED ------------------
        if (sessionLoaded && getView()!=null && paymentMethodLoaded && !sessionAndPaymentAndViewUsed) {
            paymentMethodProgressBar.setVisibility(View.GONE);
            sessionAndPaymentAndViewUsed = true;

            postList.setVisibility(View.VISIBLE);
            writePostLayout.setVisibility(View.VISIBLE);
            writePostLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WritePostFragment writePostFragment = WritePostFragment.newInstance("sessions", sessionID, session.getSessionName());
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (null == fragmentManager.findFragmentByTag("writePostFragment")) {
                        transaction.add(R.id.container_fullscreen_display_session, writePostFragment,"writePostFragment").addToBackStack(null);
                        transaction.commit();
                    }
                }
            });

            // set the image
            setImage(session.getImageUrl(), sessionImage);
            sessionImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);

            // -----------  Set the session information in UI from session object --------------
            collapsingToolbarLayout.setTitle(session.getSessionName());
            String address = getAddress(session.getLatitude(),session.getLongitude());
            mAddressAndSessionType.setText(address);
            mWhatTW.setText(session.getWhat());
            mWhoTW.setText(session.getWho());
            mWhereTW.setText(session.getWhereAt());
            mSessionType.setText(session.getSessionType());
            mDuration.setText(session.getDurationInMin() + getString(R.string.minutes_append));
        }

        // -------- VIEW -------- PAYMENT ----- ADSELECTED ---
        if (getView()!=null && paymentMethodLoaded && adSelectedReady && !paymentMethodAdSelectedAndViewUsed) {
            paymentMethodAdSelectedAndViewUsed = true;
            // When view has been loaded, payment source has been checked and which ad has been selected in the ad list has been saved in the variable adSelected the following
            // method will run which updates all the views in the snackbar (if it hasn't already been executed with the current variables)
            // if adSelected is null snackbar will show the text no upcoming ads and when the text is clicked it will scroll the view down to the list
            if (adSelected==null) {
                // Setup the default views of the snackbar
                snackBarDateAndTimeTV.setVisibility(View.GONE);
                priceTV.setVisibility(View.GONE);
                paymentMethodProgressBar.setVisibility(View.GONE);
                paymentMethodTV.setVisibility(View.GONE);
                addPaymentMethodTV.setVisibility(View.GONE);
                mDisplaySessionBtn.setVisibility(View.GONE);
                snackNoUpcomingAds.setVisibility(View.VISIBLE);
                snackNoUpcomingAds.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appBarLayout.setExpanded(false);
                        displaySessionSV.smoothScrollTo(0, upcomingSessionsRV.getRootView().getBottom());
                    }
                });
                // If the the current user has clicked a session in the sessionlist which represented an ad which has been cancelled it will show
                // "This occasion has been cancelled, please choose another occasion." otherwise it will show "no upcoming sessions"
                if (repAdCancelled) {
                    noSnackAdTV.setText(R.string.show_availability);
                } else {
                    if (representingAdTimestamp==0) {
                        noSnackAdTV.setVisibility(View.GONE);
                    } else {
                        noSnackAdTV.setText(getResources().getString(R.string.no_upcoming_sessions));
                    }
                }
            } else {
                // If the session has upcoming advertisements, set the snackbars text and price to the date and price of the selected ad
                // Setup all the views accordingly
                // ---------- Set date and price text ---------------
                snackBarDateAndTimeTV.setText(TextTimestamp.textDateAndTime(adSelected.getAdvertisementTimestamp()));
                setPriceText();
                // -------------------- HOST -----------------------------
                // If the current user is the session host display "show occasion" as the text of the button.
                if (session.getHost().equals(currentFirebaseUser.getUid())) {
                    snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                    priceTV.setVisibility(View.VISIBLE);
                    mDisplaySessionBtn.setVisibility(View.VISIBLE);
                    snackNoUpcomingAds.setVisibility(View.GONE);
                    mDisplaySessionBtn.setEnabled(true);
                    mDisplaySessionBtn.setText(R.string.show_occasion);
                    mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));

                } else {
                    // -------------------- PLAYER -----------------------------
                    // If the current user is the player, display "book session" or "show booking" depending on if the user has booked the session or not
                    snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                    priceTV.setVisibility(View.VISIBLE);
                    paymentMethodProgressBar.setVisibility(View.GONE);
                    mDisplaySessionBtn.setVisibility(View.VISIBLE);
                    snackNoUpcomingAds.setVisibility(View.GONE);
                    mDisplaySessionBtn.setText(getString(R.string.book_session));
                    // If the ad selected is free do not show payment method
                    if (adSelected.getPrice()==0) {
                        mDisplaySessionBtn.setEnabled(true);
                        paymentMethodTV.setVisibility(View.GONE);
                        addPaymentMethodTV.setVisibility(View.GONE);
                        mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));
                    } else {
                        // If the ad costs money find out if the current user has a payment source and if so show that payment method
                        // else display add payment method link to PaymentPreferencesActivity
                        if (defaultSourceMap.get("brand")!=null) {
                            String last4 = defaultSourceMap.get("last4").toString();
                            paymentMethodTV.setText("**** " + last4);
                            String cardBrand = defaultSourceMap.get("brand").toString();
                            int resourceId = BRAND_CARD_RESOURCE_MAP.get(cardBrand);
                            paymentMethodTV.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);

                            paymentMethodTV.setVisibility(View.VISIBLE);
                            addPaymentMethodTV.setVisibility(View.GONE);

                            mDisplaySessionBtn.setEnabled(true);
                            mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));
                        } else {
                            hasPaymentSystem = false;
                            mDisplaySessionBtn.setEnabled(true);
                            paymentMethodTV.setVisibility(View.GONE);
                            addPaymentMethodTV.setVisibility(View.VISIBLE);
                            mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_gray));
                        }
                    }
                    // If the ad selected has participants and of the current user is one of them, display show booking
                    if (adSelected.getParticipantsIds() != null) {
                        if (adSelected.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                            mDisplaySessionBtn.setEnabled(true);
                            snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                            priceTV.setVisibility(View.VISIBLE);
                            paymentMethodProgressBar.setVisibility(View.GONE);
                            paymentMethodTV.setVisibility(View.GONE);
                            addPaymentMethodTV.setVisibility(View.GONE);
                            mDisplaySessionBtn.setVisibility(View.VISIBLE);
                            snackNoUpcomingAds.setVisibility(View.GONE);
                            mDisplaySessionBtn.setText(R.string.show_booking);
                        }
                    }
                }

            }
        }

        // ---------------- CURRENTUSER && SESSION && VIEW && MAP-----------------
        if (currentUserLoaded && sessionLoaded && mapReady && getView()!=null && !currentUserAndSessionAndViewAndMapUsed) {
            currentUserAndSessionAndViewAndMapUsed =true;

            editSession.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.OnEditSession(sessionID,session);
                    }

                }
            });

            // SETUP MAP
            LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);

            Drawable locationDrawable = getResources().getDrawable(R.mipmap.baseline_location_on_black_36);
            Drawable selectedLocationDrawable = locationDrawable.mutate();
            selectedLocationDrawable.setColorFilter(getResources().getColor(R.color.foxmikePrimaryColor), PorterDuff.Mode.SRC_ATOP);
            selectedIcon = getMarkerIconFromDrawable(selectedLocationDrawable);

            mMap.addMarker(new MarkerOptions().position(markerLatLng).title(session.getSessionType()).icon(selectedIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
            // ----- Setup snackbar button click listener --------
            mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adSelected==null) {
                        // if no ad is selected, snackbar will display no upcoming sessions, return from click.
                        return;
                    }
                    // If the current user isn´t the host of the session
                    if (!session.getHost().equals(currentFirebaseUser.getUid())) {
                        // If the current user is a participant and already booked this session, button will display show booking and click will send user to advertisement
                        if (adSelected.getParticipantsIds()!=null) {
                            if (adSelected.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                                onAdvertisementClickedListener.OnAdvertisementClicked(adSelected.getAdvertisementId());
                                return;
                            }
                        }
                        // If the current user is not a participant, the session is not free and the user does not have a payment method, button will be gray,
                        // click will show dialog saying you need to have a payment method to book
                        if (!hasPaymentSystem && adSelected.getPrice()!=0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.you_need_a_payment_method_in_order_to_book_this_session).setTitle(R.string.booking_failed);
                            builder.setPositiveButton(R.string.add_payment_method, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                                    startActivity(paymentPreferencesIntent);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        // Payment method has been checked above (method return if no payment method)
                        // Now user will book session if button is pressed, if free send parameters to book session with blank customerId and price 0 and dont show booking "warning" text
                        if (adSelected.getPrice()==0) {
                            sessionListener.OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), session.getHost(), "", adSelected.getPrice(), adSelected.getCurrency(), true);
                        } else {
                            // session costs money, send customerId, price and if user has not clicked dont want to see booking text show the warning text
                            sessionListener.OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), session.getHost(), defaultSourceMap.get("customer").toString(), adSelected.getPrice(), adSelected.getCurrency(), currentUser.isDontShowBookingText());
                        }
                    }
                    // If the current user is the session host, send the user to the currently selected advertisement
                    if (session.getHost().equals(currentFirebaseUser.getUid())) {
                        onAdvertisementClickedListener.OnAdvertisementClicked(adSelected.getAdvertisementId());
                    }
                }
            });
        }
    }

    private void updateListViews() {
        // Om det inte finns några som inte är kancellerade sätt till noll
        if (fbAdDateAndTimeAdapter.getItemCount()==0) {
            showMoreTV.setText(getResources().getString(R.string.no_upcoming_sessions));
            fbRVContainer.setVisibility(View.GONE);
        } else {
            showMoreTV.setText(getResources().getString(R.string.show_more));
            fbRVContainer.setVisibility(View.VISIBLE);
        }
        // Om det finns fler i adaptern än nuvarande höjd visa visa mer knapp
        if (fbAdDateAndTimeAdapter.getItemCount() > currentHeightInNr) {
            showMoreTV.setText(getResources().getString(R.string.show_more));
        } else {
            showMoreTV.setText(getResources().getString(R.string.end_of_list));
        }
    }

    private void setAdListItemDefaultAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        holder.advertisementRowDateAndTimeText.setTextColor(getResources().getColor(R.color.primaryTextColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.primaryTextColor));
    }

    private void setAdListItemBookedAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
        holder.advertisementRowDateAndTimeText.setTextColor(getResources().getColor(R.color.secondaryTextColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.secondaryTextColor));
    }

    private void setAdListItemSelectedAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#F8F8FA"));
        holder.advertisementRowDateAndTimeText.setTextColor(getResources().getColor(R.color.foxmikePrimaryColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.foxmikePrimaryColor));
    }

    private void setPriceText() {
        String currencyString = "?";
        if (adSelected.getCurrency()==null) {
            currencyString = "";
        } else {
            currencyString = "kr";
        }
        String priceText;
        if (adSelected.getPrice()== 0) {
            priceText = getString(R.string.free);
        } else {
            priceText = adSelected.getPrice() + " " + currencyString + " " + "per person";
        }
        priceTV.setText(priceText);
    }

    // Setup static map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        currentUserAndSessionAndViewAndMapUsed = false;
        mapReady = true;
        onAsyncTaskFinished();
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
        CommentFragment commentFragment = CommentFragment.newInstance(postID, heading, time, message, thumb_image, "session");
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

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
        sessionAndViewUsed = false;
        currentUserAndViewUsed = false;
        currentUserAndSessionAndViewAndMapUsed = false;
        hostAndViewUsed = false;
        sessionUsed = false;
        sessionAndPaymentAndViewUsed = false;
        postsUsed = false;
        postCommentsUsed = false;
        adSetupLoaded = false;
        paymentMethodAdSelectedAndViewUsed = false;
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
                    + " must implement OnEditSessionListener");
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
        if (context instanceof OnAdvertisementClickedListener) {
            onAdvertisementClickedListener = (OnAdvertisementClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdvertisementClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onAdvertisementClickedListener = null;
        onCommentClickedListener = null;
        onChatClickedListener = null;
        onUserAccountFragmentInteractionListener = null;
        sessionListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fbAdDateAndTimeAdapter!=null) {
            fbAdDateAndTimeAdapter.stopListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fbAdDateAndTimeAdapter!=null) {
            fbAdDateAndTimeAdapter.startListening();
        }
    }
}