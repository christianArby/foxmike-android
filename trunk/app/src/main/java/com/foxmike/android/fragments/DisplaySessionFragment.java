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
import com.foxmike.android.interfaces.OnAdvertisementClickedListener;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.SessionDateAndTimeClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionDateAndTime;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.SessionDateAndTimeViewHolder;
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
    private FirebaseRecyclerAdapter<Advertisement, SessionDateAndTimeViewHolder> fbAdDateAndTimeAdapter;
    private ImageView sessionImage;
    private int asyncTasksFinished = 0;
    private int rowIndex = -1;
    private int itemHeight = 0;
    private boolean adSelectedReady;
    private Long representingAdTimestamp;
    private Advertisement adSelected;
    private LinearLayout noUpcomingAds;
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
                onTaskFinished();
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
                        onTaskFinished();
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

    private void getSessionHost() {
        /*
            Get the host image from the database (found under users with the userID=session.host)
            */
        mUserDbRef.child(session.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
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
        if (!listenerMap.containsKey(mSessionDbRef.child(sessionID).child("posts"))) {
            ValueEventListener postsListener = mSessionDbRef.child(sessionID).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Session session = new Session();
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
                                    if (postBranchArrayList.size()==session.getPosts().size()) {
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
                                                if (nrOfComments.size()==session.getPosts().size()) {
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
            listenerMap.put(mSessionDbRef.child(sessionID).child("posts"), postsListener);
        }
    }

    private void setupAds() {
        Long currentTimestamp = System.currentTimeMillis();
        Query keyQuery = rootDbRef.child("sessions").child(session.getSessionId()).child("advertisements").orderByValue().startAt(currentTimestamp);
        keyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // no ads
                    if (representingAdTimestamp!=0) {
                        repAdCancelled = true;
                    }
                    noUpcomingAds.setVisibility(View.VISIBLE);
                    showMoreTV.setText(getResources().getString(R.string.end_of_list));
                    paymentMethodAdSelectedAndViewUsed = false;
                    adSelectedReady = true;
                    onTaskFinished();
                } else {
                    HashMap<String,Long> adTimes = (HashMap<String,Long>) dataSnapshot.getValue();
                    if (!adTimes.containsValue(representingAdTimestamp)) {
                        repAdCancelled = true;
                        adSelected = null;
                        adSelectedReady = true;
                        paymentMethodAdSelectedAndViewUsed = false;
                        onTaskFinished();
                    }
                    Long twoWeekTimestamp = new DateTime(currentTimestamp).plusWeeks(2).getMillis();
                    int twoWeekssize = 0;
                    // Ifall aktiviteten har öppnats från kartan så är repAd null och då sätter vi repAdTimestamp till den som kommer först
                    // Vi tar dessutom reda på hur hög vår lista ska vara genom att räkna hur många som är med första två veckorna.
                    Long firstTimestamp = 0L;
                    for (Long adTime: adTimes.values()) {
                        if (adTime<twoWeekTimestamp) {
                            twoWeekssize++;
                        }
                    }
                    if (representingAdTimestamp==0) {
                        repAdCancelled = false;
                        adSelected = null;
                        adSelectedReady = true;
                        paymentMethodAdSelectedAndViewUsed = false;
                        onTaskFinished();
                    }

                    currentHeightInNr = twoWeekssize;
                    if (twoWeekssize<5) {
                        if (adTimes.size()<5) {
                            currentHeightInNr = adTimes.size();
                        } else {
                            currentHeightInNr = 4;
                        }
                    }
                    ViewGroup.LayoutParams params =fbRVContainer.getLayoutParams();
                    params.height= 167*currentHeightInNr;
                    fbRVContainer.setLayoutParams(params);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query allKeysQuery = rootDbRef.child("sessions").child(session.getSessionId()).child("advertisements").orderByValue().startAt(currentTimestamp);
        DatabaseReference adDbRef = rootDbRef.child("advertisements");
        FirebaseRecyclerOptions<Advertisement> options = new FirebaseRecyclerOptions.Builder<Advertisement>()
                .setIndexedQuery(allKeysQuery, adDbRef, Advertisement.class)
                .build();
        fbAdDateAndTimeAdapter = new FirebaseRecyclerAdapter<Advertisement, SessionDateAndTimeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SessionDateAndTimeViewHolder holder, int position, @NonNull Advertisement model) {

                // -----------  set the number of participants ------------
                long countParticipants;
                if (model.getParticipantsIds()!=null) {
                    countParticipants = model.getParticipantsIds().size();
                } else {
                    countParticipants = 0;
                }
                holder.setParticipantsTV(countParticipants +"/" + model.getMaxParticipants());
                holder.sessionDateAndTimeText.setText(TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp()));
                holder.setSessionDateAndTimeClickedListener(new SessionDateAndTimeClickedListener() {
                    @Override
                    public void OnSessionDateAndTimeClicked(View view, int position) {
                        rowIndex = position; // set row index to selected position
                        adSelected = fbAdDateAndTimeAdapter.getItem(position);
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onTaskFinished();
                        notifyDataSetChanged(); // Made effect on Recycler Views adapter
                    }
                });

                setAdListItemDefault(holder);
                if (rowIndex==-1) {
                    if (representingAdTimestamp==model.getAdvertisementTimestamp()) {
                        rowIndex = position;
                    }
                }
                if (rowIndex == position) {
                    adSelected=model;
                    paymentMethodAdSelectedAndViewUsed = false;
                    adSelectedReady = true;
                    setAdListItemSelected(holder);
                    onTaskFinished();
                }
                if (model.getParticipantsIds().size()!=0) {
                    if (model.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                        setAdListItemBooked(holder);
                    }
                }
            }
            @NonNull
            @Override
            public SessionDateAndTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_date_time_single_layout, parent, false);
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(upcomingSessionsRV.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                itemHeight = view.getMeasuredHeight();
                return new SessionDateAndTimeViewHolder(view);
            }
        };
        upcomingSessionsRV.setAdapter(fbAdDateAndTimeAdapter);
        fbAdDateAndTimeAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateListViews();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (rowIndex==positionStart) {
                    if (fbAdDateAndTimeAdapter.getItemCount()==0) {
                        adSelected = null;
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onTaskFinished();
                    } else {
                        adSelected = fbAdDateAndTimeAdapter.getItem(0);
                        //setAdListItemSelected((SessionDateAndTimeViewHolder) upcomingSessionsRV.findViewHolderForAdapterPosition(0));
                        rowIndex = 0;
                        fbAdDateAndTimeAdapter.notifyDataSetChanged();
                        paymentMethodAdSelectedAndViewUsed = false;
                        adSelectedReady = true;
                        onTaskFinished();
                    }
                }
                updateListViews();
            }
        });

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
        noUpcomingAds = displaySession.findViewById(R.id.noUpcomingAdsText);
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
        onTaskFinished();
    }

    private void onTaskFinished() {

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
            mDuration.setText(session.getDuration());
        }

        // -------- VIEW -------- PAYMENT ----- ADSELECTED ---
        if (getView()!=null && paymentMethodLoaded && adSelectedReady && !paymentMethodAdSelectedAndViewUsed) {
            paymentMethodAdSelectedAndViewUsed = true;

            // ------------------ Setup snackbar ----------------------------------

            if (adSelected==null) {

                snackNoUpcomingAds.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appBarLayout.setExpanded(false);
                        displaySessionSV.smoothScrollTo(0, upcomingSessionsRV.getRootView().getBottom());
                    }
                });
                if (repAdCancelled) {
                    noSnackAdTV.setText(R.string.representing_occasion_cancelled);
                } else {
                    if (representingAdTimestamp==0) {
                        noSnackAdTV.setVisibility(View.GONE);
                    } else {
                        noSnackAdTV.setText(getResources().getString(R.string.no_upcoming_sessions));
                    }
                }
                //set default
                snackBarDateAndTimeTV.setVisibility(View.GONE);
                priceTV.setVisibility(View.GONE);
                paymentMethodProgressBar.setVisibility(View.GONE);
                paymentMethodTV.setVisibility(View.GONE);
                addPaymentMethodTV.setVisibility(View.GONE);
                mDisplaySessionBtn.setVisibility(View.GONE);
                snackNoUpcomingAds.setVisibility(View.VISIBLE);
            } else {
                // ---------- Set date and price text ---------------
                snackBarDateAndTimeTV.setText(TextTimestamp.textDateAndTime(adSelected.getAdvertisementTimestamp()));
                setPriceText();
                // -------------------- HOST -----------------------------
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
                    snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                    priceTV.setVisibility(View.VISIBLE);
                    paymentMethodProgressBar.setVisibility(View.GONE);
                    mDisplaySessionBtn.setVisibility(View.VISIBLE);
                    snackNoUpcomingAds.setVisibility(View.GONE);

                    mDisplaySessionBtn.setText(getString(R.string.book_session));
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
            // Setup Booking, Cancelling and Editing Button
            mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*
                If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                and send the session key to that activity as bundle.
                */
                    if (session.getHost().equals(currentFirebaseUser.getUid())) {
                        //onEditSessionListener.OnEditSession(sessionID);
                        onAdvertisementClickedListener.OnAdvertisementClicked(adSelected.getAdvertisementId());
                    }
                /*
                 Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                remove the current user from that session participant list and go back to main activity.
                */
                    else {
                        if (adSelected==null) {
                            // session will display no upcoming sessions.
                            return;
                        }
                        if (adSelected.getParticipantsIds()!=null) {
                            if (adSelected.getParticipantsIds().containsKey(currentFirebaseUser.getUid())) {
                                onAdvertisementClickedListener.OnAdvertisementClicked(adSelected.getAdvertisementId());
                                //sessionListener.OnCancelBookedSession(adSelected.getParticipantsTimestamps().get(currentFirebaseUser.getUid()),adSelected.getAdvertisementTimestamp(),adSelected.getAdvertisementId(),currentFirebaseUser.getUid(),adSelected.getParticipantsIds().get(currentFirebaseUser.getUid()),session.getStripeAccountId());
                                return;
                            }
                        }
                        if (!hasPaymentSystem) {

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
                        /*
                        Else (button will show join session) add the user id to the session participant list and
                        the user sessions attending list when button is clicked.
                        */
                        sessionListener.OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), session.getHost(), defaultSourceMap.get("customer").toString(), session.getPrice(), session.getCurrency(), currentUser.isDontShowBookingText());
                    }
                }
            });
        }
    }

    private void updateListViews() {
        // Om det inte finns några som inte är kancellerade sätt till noll
        if (fbAdDateAndTimeAdapter.getItemCount()==0) {
            showMoreTV.setText(getResources().getString(R.string.end_of_list));
            noUpcomingAds.setVisibility(View.VISIBLE);
            fbRVContainer.setVisibility(View.GONE);
        } else {
            noUpcomingAds.setVisibility(View.GONE);
            fbRVContainer.setVisibility(View.VISIBLE);
        }
        // Om det finns fler i adaptern än nuvarande höjd visa visa mer knapp
        if (fbAdDateAndTimeAdapter.getItemCount() > currentHeightInNr) {
            showMoreTV.setText(getResources().getString(R.string.show_more));
        } else {
            showMoreTV.setText(getResources().getString(R.string.end_of_list));
        }
    }

    private void setAdListItemDefault(@NonNull SessionDateAndTimeViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        holder.sessionDateAndTimeText.setTextColor(getResources().getColor(R.color.primaryTextColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.primaryTextColor));
    }

    private void setAdListItemBooked(@NonNull SessionDateAndTimeViewHolder holder) {
        holder.itemView.setBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
        holder.sessionDateAndTimeText.setTextColor(getResources().getColor(R.color.secondaryTextColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.secondaryTextColor));
    }

    private void setAdListItemSelected(@NonNull SessionDateAndTimeViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#F8F8FA"));
        holder.sessionDateAndTimeText.setTextColor(getResources().getColor(R.color.foxmikePrimaryColor));
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
            priceText = "Free";
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