package com.foxmike.android.fragments;
// Checked

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.os.Handler;
import android.os.SystemClock;
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
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.foxmike.android.activities.RatingsAndReviewsActivity;
import com.foxmike.android.activities.WritePostActivity;
import com.foxmike.android.interfaces.AdvertisementListener;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionDateAndTime;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.AdvertisementRowViewHolder;
import com.foxmike.android.utils.FixAppBarLayoutBehavior;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.foxmike.android.viewmodels.SessionViewModel;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;

/**
 * This fragment takes a longitude and latitude and displays the corresponding mSession with that longitude and latitude.
 */
public class DisplaySessionFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = DisplaySessionFragment.class.getSimpleName();

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private ConstraintLayout sessionImageCardView;
    private TextView mDuration;
    private TextView mDisplaySessionBtn;
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
    private Session mSession;
    private TextView priceTV;
    private LinearLayoutManager linearLayoutManager;
    private Map<Long, String> postIDs = new HashMap<Long, String>();
    private ArrayList<PostBranch> postBranchArrayList;
    private Map<String, Long> nrOfComments = new HashMap<String, Long>();
    private GoogleMap mMap;
    private RecyclerView postList;
    private RecyclerView.Adapter<PostsViewHolder> postsViewHolderAdapter;
    private User currentUser;
    private OnCommentClickedListener onCommentClickedListener;
    private UserAccountFragment.OnUserAccountFragmentInteractionListener onUserAccountFragmentInteractionListener;
    private OnUserClickedListener onUserClickedListener;
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
    private TextView addDates;
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
    private NestedScrollView displaySessionSV;
    private AppBarLayout appBarLayout;
    private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;
    private ActionBar actionBar;
    private TextView snackNoAdTV;
    HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();
    private long nrOfPosts;
    private AdvertisementListener advertisementListener;
    private TextView mAddress;
    private long mLastClickTime = 0;
    private boolean showAvClicked = false;
    private long lastScrollTime = 0;
    private boolean firstLoadOfPosts = true;
    private boolean firstLoadOfComments;
    private ProgressBar postProgressBar;
    private TextView editTop;
    private ArrayList<TextView> editTVArrayList=new ArrayList<TextView>();
    private boolean isHost = false;
    private TextView sessionName;
    private TextView editWhat;
    private TextView editWho;
    private TextView editWhere;
    private TextView editAvailability;
    private AppCompatRatingBar ratingBar;
    private TextView ratingsAndReviewsText;
    private TextView showAllReviews;
    private TextView ratingText;
    private ConstraintLayout ratingContainer;
    private TextView newFlag;
    private Disposable subscription;
    private TextView showAvailAbility;
    private HashMap<String, Long> adTimes = new HashMap<>();
    private String stripeCustomerId;

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

        // Setup static map with mSession location
        if (null == getChildFragmentManager().findFragmentByTag("xDisplaySessionMapsFragment")) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.liteMode(true);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.child_fragment_container, mapFragment,"xDisplaySessionMapsFragment").commit();
            mapFragment.getMapAsync(this);
        }

        postBranchArrayList = new ArrayList<>();

        if (getArguments() != null) {
            sessionID = getArguments().getString(SESSION_ID);
            representingAdTimestamp = getArguments().getLong(REPRESSENTING_AD_TIMESTAMP);
        }

        // GET THE PAYMENT INFO FROM CURRENT USER
        getDefaultSourceMap();

        // GET CURRENT USER FROM DATABASE
        FirebaseDatabaseViewModel firebaseDatabaseUserViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseUserLiveData = firebaseDatabaseUserViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        firebaseDatabaseUserLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    currentUserAndViewUsed = false;
                    currentUserAndSessionAndViewAndMapUsed = false;
                    currentUser = dataSnapshot.getValue(User.class);
                    currentUserLoaded =true;
                    stripeCustomerId = currentUser.getStripeCustomerId();
                    onAsyncTaskFinished();
                }
            }
        });

        // GET SESSION FROM DATABASE
        if(!sessionID.equals("")) {
            SessionViewModel sessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
            LiveData<Session> sessionLiveData = sessionViewModel.getSessionLiveData(sessionID);
            sessionLiveData.observe(this, new Observer<Session>() {
                @Override
                public void onChanged(@Nullable Session session) {
                    if (session!=null) {
                        if (session.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            isHost = true;
                        }
                        mSession = session;
                        sessionLongitude = session.getLongitude();
                        sessionLatitude = session.getLatitude();
                        sessionID = session.getSessionId();
                        currentUserAndSessionAndViewAndMapUsed = false;
                        sessionAndPaymentAndViewUsed = false;
                        sessionUsed = false;
                        sessionLoaded = true;
                        onAsyncTaskFinished();
                        getPosts();
                        getSessionHost();
                    }

                }
            });
        } else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.Session_not_found_please_try_again_later,Toast.LENGTH_LONG);
            toast.show();
        }

    }


    private void getDefaultSourceMap () {
        try {
            MainPlayerActivity mainPlayerActivity = (MainPlayerActivity) getActivity();
            subscription = mainPlayerActivity.subject.subscribe(new Consumer<HashMap>() {
                @Override
                public void accept(HashMap hashMap) throws Exception {

                    if (hashMap.get("brand")!=null) {
                        defaultSourceMap = hashMap;
                        hasPaymentSystem = true;
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodAdSelectedAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onAsyncTaskFinished();
                    } else {
                        hasPaymentSystem = false;
                        defaultSourceMap = new HashMap();
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodAdSelectedAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onAsyncTaskFinished();
                    }
                }
            });
            return;
        } catch (RuntimeException e){
            defaultSourceMap = new HashMap();
            sessionAndPaymentAndViewUsed = false;
            paymentMethodAdSelectedAndViewUsed = false;
            paymentMethodLoaded = true;
            onAsyncTaskFinished();
        }
    }

    private void getSessionHost() {
        /*
            Get the host image from the database (found under users with the userID=mSession.host)
            */
        rootDbRef.child("usersPublic").child(mSession.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    return;
                }
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
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("sessionPosts").child(sessionID));
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                postsUsed = false;
                nrOfPosts = dataSnapshot.getChildrenCount();
                postBranchArrayList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postID = postSnapshot.getKey();
                    Post post = postSnapshot.getValue(Post.class);
                    PostBranch postBranch = new PostBranch(postSnapshot.getKey(),post);
                    postBranchArrayList.add(postBranch);
                    // Number of comments listener
                    FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(DisplaySessionFragment.this).get(FirebaseDatabaseViewModel.class);
                    LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("sessionPostComments").child(sessionID).child(postID));
                    firebaseDatabaseLiveData.observe(DisplaySessionFragment.this, new Observer<DataSnapshot>() {
                        @Override
                        public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                            nrOfComments.put(dataSnapshot.getKey(), dataSnapshot.getChildrenCount());
                            if (nrOfComments.size()==nrOfPosts) {
                                postCommentsUsed = false;
                                postCommentsLoaded = true;
                                onAsyncTaskFinished();
                            }
                        }
                    });
                }
                Collections.sort(postBranchArrayList);
                postsLoaded = true;
                onAsyncTaskFinished();
            }
        });
    }

    private void setupAds() {

        // --------- GET ALL THE AD TIMESTAMPS SAVED UNDER SESSION/ADVERTISEMENTS ---------
        // Current time as timestamp
        Long currentTimestamp = System.currentTimeMillis();
        // Create query to get all the advertisement keys from the current mSession
        Query keyQuery = rootDbRef.child("sessionAdvertisements").child(mSession.getSessionId()).orderByValue().startAt(currentTimestamp);
        // Use the query to get all the advertisement keys from the current mSession
        keyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // If data snapshot is null but representingAdTimestamp is not null it means that the ad has been cancelled during the time the user opened the mSession from the mSession representing
                    // the cancelled ad, set the boolean repAdCancelled to true
                    if (representingAdTimestamp!=0) {
                        repAdCancelled = true;
                    }
                    addDates.setText(R.string.add_sessions);
                    // display text "no upcoming sessions"
                    showMoreTV.setText(getResources().getString(R.string.no_upcoming_sessions));
                    // run method which updates snackbar
                    paymentMethodAdSelectedAndViewUsed = false;
                    adSelectedReady = true;
                    onAsyncTaskFinished();
                } else {
                    addDates.setText(R.string.add_more_sessions);
                    // if there are advertisements collect all the timestamps in a hashmap
                    adTimes.clear();
                    adTimes = (HashMap<String,Long>) dataSnapshot.getValue();
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
                        // if this sessions representing timestamp is not part of the advertisment timestamps it means that the ad has been cancelled during the time the user opened the mSession from the mSession representing
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
                    params.height= itemHeight*currentHeightInNr;
                    fbRVContainer.setLayoutParams(params);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // --------- POPULATE THE RECYCLERVIEW WITH THE ADVERTISEMENTS ---------
        // Create the key query which will get all the advertisement keys for ads with a date in the future.
        Query allKeysQuery = rootDbRef.child("sessionAdvertisements").child(mSession.getSessionId()).orderByValue().startAt(currentTimestamp);
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
                if (model.getParticipantsTimestamps()!=null) {
                    countParticipants = model.getParticipantsTimestamps().size();
                } else {
                    countParticipants = 0;
                }
                // set the text of each row in the list of advertisements
                int maxParticipants = model.getMaxParticipants();
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

                    @Override
                    public void OnParticipantsClicked(int position) {

                        if (rowIndex==position) {
                            if (model.getParticipantsTimestamps().size()>0) {
                                ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(model.getAdvertisementId(), getActivity().getApplicationContext().getResources().getString(R.string.participants_on) + " " + TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp()));
                                FragmentManager fragmentManager = getChildFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                participantsFragment.show(transaction,"participantsFragment");
                            }
                        }

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
                    if (model.getParticipantsTimestamps().size()!=0) {
                        setAdListItemSelectedAppearance(holder);
                    } else {
                        setAdListItemSelectedNoParticipantsAppearance(holder);
                    }

                    onAsyncTaskFinished();
                }
                // If the ad has participants, check if the current user is one of them and if so turn the appearance of the row to booked (filled dark green)
                if (model.getParticipantsTimestamps().size()!=0) {
                    if (model.getParticipantsTimestamps().containsKey(currentFirebaseUser.getUid())) {
                        setAdListItemBookedAppearance(holder);
                    }
                }
            }
            @NonNull
            @Override
            public AdvertisementRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_date_time_single_layout, parent, false);
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
        // Set the mSession image
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
        showAvailAbility = view.findViewById(R.id.showAvailAbility);
        addDates = displaySession.findViewById(R.id.editSession);
        fbRVContainer = displaySession.findViewById(R.id.firebaseRVContainer);
        sessionDateAndTimeLLManager = new LinearLayoutManager(getActivity().getApplicationContext());
        upcomingSessionsRV.setHasFixedSize(true);
        upcomingSessionsRV.setLayoutManager(sessionDateAndTimeLLManager);
        ((SimpleItemAnimator) upcomingSessionsRV.getItemAnimator()).setSupportsChangeAnimations(false);
        upcomingSessionsRV.setNestedScrollingEnabled(false);
        showMoreTV = displaySession.findViewById(R.id.showMoreTV);
        displaySessionSV = view.findViewById(R.id.displaySessionSV);
        snackNoAdTV = view.findViewById(R.id.snackNoAdTV);
        mAddress = displaySession.findViewById(R.id.addressTV);
        postProgressBar = displaySession.findViewById(R.id.postProgressBar);
        sessionName = view.findViewById(R.id.sessionName);
        ratingBar = displaySession.findViewById(R.id.ratingBar);
        ratingsAndReviewsText = displaySession.findViewById(R.id.ratingsAndReviewsText);
        showAllReviews = displaySession.findViewById(R.id.showAllReviews);

        ratingText = view.findViewById(R.id.ratingsAndReviewsText);
        ratingContainer = view.findViewById(R.id.reviewContainer);
        newFlag = view.findViewById(R.id.newFlag);

        //set default
        snackBarDateAndTimeTV.setVisibility(View.GONE);
        priceTV.setVisibility(View.GONE);
        mDisplaySessionBtn.setVisibility(View.GONE);
        snackNoUpcomingAds.setVisibility(View.VISIBLE);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        View adRow = view.findViewById(R.id.adRowDummyToMeasure);
        adRow.measure(display.getWidth(), display.getHeight());

        itemHeight = adRow.getMeasuredHeight(); //view height*/

        paymentFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

        // Store refs to editTV
        editTop = (TextView)(view.findViewById(R.id.editTop));
        editWhat = displaySession.findViewById(R.id.editWhat);
        editWho = displaySession.findViewById(R.id.editWho);
        editWhere = displaySession.findViewById(R.id.editWhere);
        editAvailability = displaySession.findViewById(R.id.editAvailability);
        editTVArrayList.add(editWhat);
        editTVArrayList.add(editWho);
        editTVArrayList.add(editWhere);
        editTVArrayList.add(editAvailability);

        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        appBarLayout = view.findViewById(R.id.displaySessionAppBar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new FixAppBarLayoutBehavior());
        onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset)>Math.abs(appBarLayout.getTotalScrollRange()/2)) {
                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.primaryTextColor), PorterDuff.Mode.SRC_ATOP);
                } else {
                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);
                }
            }
        };
        appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener);

        // Setup standard aspect ratio of mSession image
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
                    params.height= itemHeight*(currentHeightInNr+4);
                    fbRVContainer.setLayoutParams(params);
                    currentHeightInNr = currentHeightInNr +4;
                } else {
                    showMoreTV.setText(getResources().getString(R.string.end_of_list));
                    ViewGroup.LayoutParams params =fbRVContainer.getLayoutParams();
                    params.height= itemHeight*(fbAdDateAndTimeAdapter.getItemCount());
                    fbRVContainer.setLayoutParams(params);
                    currentHeightInNr = fbAdDateAndTimeAdapter.getItemCount();
                }
            }
        });

        // Setup wall, Posts are displayed in a RecyclerView
        postList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
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
                    Post post = postBranchArrayList.get(position).getPost();
                    String postID = postBranchArrayList.get(position).getPostID();



                    TextTimestamp textTimestamp = new TextTimestamp((long) post.getTimestamp());
                    String timeText = textTimestamp.textDateAndTime();
                    holder.setTime(timeText);

                    populateUserPublicHashMap(post.getAuthorId(), new OnUsersLoadedListener() {
                        @Override
                        public void OnUsersLoaded() {
                            holder.setUserImage(userPublicHashMap.get(post.getAuthorId()).getThumb_image(), getActivity().getApplicationContext());
                            holder.setHeading(userPublicHashMap.get(post.getAuthorId()).getFirstName());
                            holder.setCommentClickListener(postID, userPublicHashMap.get(post.getAuthorId()).getFirstName(), timeText, post.getMessage(), userPublicHashMap.get(post.getAuthorId()).getThumb_image());
                        }
                    });
                    holder.setMessage(post.getMessage());
                    holder.setNrOfComments(nrOfComments.get(postBranchArrayList.get(position).getPostID()));
                }
            }
            @Override
            public int getItemCount() {
                return postBranchArrayList.size();
            }
        };
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
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

            String address = getAddress(mSession.getLatitude(), mSession.getLongitude());
            mAddress.setText(address);

            setupAds();

            if (mSession.getHost().equals(currentFirebaseUser.getUid())) {

                // -------------------- HOST -----------------------------
                editTop.setVisibility(View.VISIBLE);
                ratingContainer.setVisibility(View.GONE);
                newFlag.setVisibility(View.GONE);
                for (TextView editTV: editTVArrayList) {
                    editTV.setVisibility(View.VISIBLE);
                }
                addDates.setVisibility(View.VISIBLE);
                mSendMessageToHost.setText(R.string.show_and_edit_profile_text);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        onUserAccountFragmentInteractionListener.OnUserAccountFragmentInteraction("edit");
                    }
                });

            } else {

                // -------------------- PLAYER -----------------------------
                editTop.setVisibility(View.GONE);
                for (TextView editTV: editTVArrayList) {
                    editTV.setVisibility(View.GONE);
                }
                addDates.setVisibility(View.GONE);
                mSendMessageToHost.setText(R.string.show_profile);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        onUserClickedListener.OnUserClicked(mSession.getHost());
                    }
                });
            }
        }

        // ---------------- HOST && VIEW-----------------
        if (hostLoaded && getView()!=null && !hostAndViewUsed) {
            hostAndViewUsed=true;
            setImage(host.getThumb_image(), mHostImage);
            String hostText = getString(R.string.hosted_by_text) + " " + host.getFirstName();
            mHost.setText(hostText);
            mHostAboutTV.setText(host.getAboutMe());
        }

        if (postsLoaded && getView()!=null && !postsUsed) {
            postsUsed = true;
            // To make it not disturb transition animation
            if (firstLoadOfPosts) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postsViewHolderAdapter.notifyDataSetChanged();
                        postList.setAdapter(postsViewHolderAdapter);
                        firstLoadOfPosts = false;
                        postProgressBar.setVisibility(View.GONE);
                    }
                }, 400);
            } else {
                postProgressBar.setVisibility(View.GONE);
                postsViewHolderAdapter.notifyDataSetChanged();
                postList.setAdapter(postsViewHolderAdapter);
            }

        }

        if (postCommentsLoaded && getView()!=null && !postCommentsUsed) {
            // To make it not disturb transition animation
            if (firstLoadOfComments) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postCommentsUsed = true;
                        postsViewHolderAdapter.notifyDataSetChanged();
                        firstLoadOfComments = false;
                    }
                }, 400);
            } else {
                postCommentsUsed = true;
                postsViewHolderAdapter.notifyDataSetChanged();
            }
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
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent writePostIntent = new Intent(getActivity().getApplicationContext(), WritePostActivity.class);
                    writePostIntent.putExtra("dbParent", "sessions");
                    writePostIntent.putExtra("sourceID", sessionID);
                    writePostIntent.putExtra("title", mSession.getSessionName());
                    startActivity(writePostIntent);
                }
            });

            // set the image
            setImage(mSession.getImageUrl(), sessionImage);
            sessionImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);

            // -----------  Set the mSession information in UI from mSession object --------------
            sessionName.setText(mSession.getSessionName());
            String address = getAddress(mSession.getLatitude(), mSession.getLongitude());
            mAddressAndSessionType.setText(address);
            mWhatTW.setText(mSession.getWhat());
            mWhoTW.setText(mSession.getWho());
            mWhereTW.setText(mSession.getWhereAt());
            mSessionType.setText(mSession.getSessionType());
            mDuration.setText(mSession.getDurationInMin() + getString(R.string.minutes_append));
            ratingBar.setRating(mSession.getRating());
            String ratingTextFormatted = String.format("%.1f", mSession.getRating());
            if (mSession.getNrOfRatings()==0) {
                ratingsAndReviewsText.setText(R.string.new_session_no_reviews_yet);
                showAllReviews.setVisibility(View.GONE);
                ratingContainer.setVisibility(View.GONE);
                if (!isHost) {
                    newFlag.setVisibility(View.VISIBLE);
                }
            } else if (mSession.getNrOfRatings()==1) {
                showAllReviews.setVisibility(View.VISIBLE);
                ratingContainer.setVisibility(View.VISIBLE);
                newFlag.setVisibility(View.GONE);
                String rating = String.format("%.1f", mSession.getRating());
                ratingText.setText(rating + " (" + mSession.getNrOfRatings() + ")");
                ratingsAndReviewsText.setText(ratingTextFormatted + getString(R.string.based_on_nr_ratings_text_1) + mSession.getNrOfRatings() + getString(R.string.based_on_nr_ratings_text_2_single));
            } else {
                showAllReviews.setVisibility(View.VISIBLE);
                String rating = String.format("%.1f", mSession.getRating());
                ratingText.setText(rating + " (" + mSession.getNrOfRatings() + ")");
                if (!isHost) {
                    ratingContainer.setVisibility(View.VISIBLE);
                }
                newFlag.setVisibility(View.GONE);
                ratingsAndReviewsText.setText(ratingTextFormatted + getString(R.string.based_on_nr_ratings_text_1) + mSession.getNrOfRatings() + getString(R.string.based_on_nr_ratings_text_2));
            }
            showAllReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ratingAndReviewsIntet = new Intent(getActivity().getApplicationContext(), RatingsAndReviewsActivity.class);
                    ratingAndReviewsIntet.putExtra("sessionId", sessionID);
                    ratingAndReviewsIntet.putExtra("sessionName", mSession.getSessionName());
                    startActivity(ratingAndReviewsIntet);

                }
            });
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
                        lastScrollTime = SystemClock.elapsedRealtime();
                        snackNoUpcomingAds.setVisibility(View.GONE);
                        displaySessionSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                if (SystemClock.elapsedRealtime() - lastScrollTime < 1000) {
                                    return;
                                }
                                lastScrollTime = SystemClock.elapsedRealtime();
                                if (adSelected==null) {
                                    snackNoUpcomingAds.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
                // If the the current user has clicked a mSession in the sessionlist which represented an ad which has been cancelled it will show
                // "This occasion has been cancelled, please choose another occasion." otherwise it will show "no upcoming sessions"
                if (repAdCancelled) {
                    if (adTimes.size()>0) {
                        showAvailAbility.setVisibility(View.VISIBLE);
                        snackNoAdTV.setVisibility(View.GONE);
                    }
                } else {
                    if (adTimes.size()>0) {
                        showAvailAbility.setVisibility(View.VISIBLE);
                        snackNoAdTV.setVisibility(View.GONE);
                    } else {
                        showAvailAbility.setVisibility(View.GONE);
                        snackNoAdTV.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                // If the mSession has upcoming advertisements, set the snackbars text and price to the date and price of the selected ad
                // Setup all the views accordingly
                // ---------- Set date and price text ---------------
                snackBarDateAndTimeTV.setText(TextTimestamp.textDateAndTime(adSelected.getAdvertisementTimestamp()));
                setPriceText();
                // -------------------- HOST -----------------------------
                // If the current user is the mSession host display "show occasion" as the text of the button.
                if (mSession.getHost().equals(currentFirebaseUser.getUid())) {
                    snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                    priceTV.setVisibility(View.VISIBLE);
                    mDisplaySessionBtn.setVisibility(View.VISIBLE);
                    snackNoUpcomingAds.setVisibility(View.GONE);
                    mDisplaySessionBtn.setEnabled(true);
                    mDisplaySessionBtn.setText(R.string.cancel_occasion);
                    mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));

                } else {
                    // -------------------- PLAYER -----------------------------
                    // If the current user is the player, display "book mSession" or "show booking" depending on if the user has booked the mSession or not
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
                    if (adSelected.getParticipantsTimestamps() != null) {
                        if (adSelected.getParticipantsTimestamps().containsKey(currentFirebaseUser.getUid())) {
                            mDisplaySessionBtn.setEnabled(true);
                            snackBarDateAndTimeTV.setVisibility(View.VISIBLE);
                            priceTV.setVisibility(View.VISIBLE);
                            paymentMethodProgressBar.setVisibility(View.GONE);
                            paymentMethodTV.setVisibility(View.GONE);
                            addPaymentMethodTV.setVisibility(View.GONE);
                            mDisplaySessionBtn.setVisibility(View.VISIBLE);
                            snackNoUpcomingAds.setVisibility(View.GONE);
                            mDisplaySessionBtn.setText(R.string.cancel_booking);
                        }
                    }
                }
            }
        }

        // ---------------- CURRENTUSER && SESSION && VIEW && MAP-----------------
        if (currentUserLoaded && sessionLoaded && mapReady && getView()!=null && !currentUserAndSessionAndViewAndMapUsed) {
            currentUserAndSessionAndViewAndMapUsed =true;

            addDates.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.addAdvertisements(sessionID);
                    }
                }
            });
            editAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.addAdvertisements(sessionID);
                    }
                }
            });
            editTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.OnEditSession(sessionID, mSession);
                    }
                }
            });
            editWhat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.OnEditSession(sessionID, mSession, "what");
                    }

                }
            });
            editWho.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.OnEditSession(sessionID, mSession, "who");
                    }

                }
            });
            editWhere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!currentUser.isTrainerMode()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        sessionListener.OnEditSession(sessionID, mSession, "where");
                    }

                }
            });


            // SETUP MAP
            LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);

            Drawable locationDrawable = getResources().getDrawable(R.mipmap.baseline_location_on_black_36);
            Drawable selectedLocationDrawable = locationDrawable.mutate();
            selectedLocationDrawable.setColorFilter(getResources().getColor(R.color.foxmikePrimaryColor), PorterDuff.Mode.SRC_ATOP);
            selectedIcon = getMarkerIconFromDrawable(selectedLocationDrawable);

            mMap.addMarker(new MarkerOptions().position(markerLatLng).title(mSession.getSessionType()).icon(selectedIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
            // ----- Setup snackbar button click listener --------
            mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adSelected==null) {
                        // if no ad is selected, snackbar will display no upcoming sessions, return from click.
                        return;
                    }

                    // If the current user isn´t the host of the mSession
                    if (!mSession.getHost().equals(currentFirebaseUser.getUid())) {

                        Long currentTimestamp = System.currentTimeMillis();
                        Long twoWeekTimestamp = new DateTime(currentTimestamp).plusWeeks(2).getMillis();

                        if (adSelected.getAdvertisementTimestamp()>twoWeekTimestamp) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.booking_more_than_2_weeks_ahead).setTitle(R.string.booking_not_possible);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        // If the current user is a participant and already booked this mSession, button will display cancel booking and click will start cancellation method
                        if (adSelected.getParticipantsTimestamps()!=null) {
                            if (adSelected.getParticipantsTimestamps().containsKey(currentFirebaseUser.getUid())) {
                                sessionListener.OnCancelBookedSession(adSelected.getParticipantsTimestamps().get(currentFirebaseUser.getUid()),adSelected.getAdvertisementTimestamp(),adSelected.getAdvertisementId(),currentFirebaseUser.getUid(), adSelected.getPrice(), mSession.getHost());
                                return;
                            }
                        }

                        if (adSelected.getMaxParticipants()<=adSelected.getParticipantsTimestamps().size()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.max_number_of_participants_reached).setTitle(R.string.session_is_full);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        // If the current user is not a participant, the mSession is not free and the user does not have a payment method, button will be gray,
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
                        // Now user will book mSession if button is pressed, if free send parameters to book mSession with blank customerId and price 0 and dont show booking "warning" text
                        if (adSelected.getPrice()==0) {
                            sessionListener.OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), mSession.getHost(), adSelected.getPrice(), true, mSession.getDurationInMin());
                        } else {
                            // mSession costs money, send customerId, price and if user has not clicked dont want to see booking text show the warning text
                            sessionListener.OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), mSession.getHost(), adSelected.getPrice(), currentUser.isDontShowBookingText(), mSession.getDurationInMin());
                        }
                    }
                    // If the current user is the mSession host, button will show cancel mSession, if clicked start cancellation process
                    if (mSession.getHost().equals(currentFirebaseUser.getUid())) {
                        advertisementListener.OnCancelAdvertisement(mSession.getSessionName(), adSelected.getAdvertisementId(), mSession.getImageUrl(), adSelected.getSessionId(), adSelected.getAdvertisementTimestamp(), adSelected.getParticipantsTimestamps(), currentUser.getStripeAccountId());
                    }
                }
            });
        }
    }

    /*private static class LeakyHandler extends Handler {

        *//*
         * Fix number III - Use WeakReferences
         * *//*
        private WeakReference<DisplaySessionFragment> weakReference;
        public LeakyHandler(DisplaySessionFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            DisplaySessionFragment fragment = weakReference.get();
            if (fragment != null) {
                fragment.postCommentsUsed = true;
                fragment.postsViewHolderAdapter.notifyDataSetChanged();
                fragment.firstLoadOfComments = false;
            }
        }
    }

    private static final Runnable leakyRunnable = new Runnable() {
        @Override
        public void run() { *//* ... *//* }
    };*/

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

    private void setAdListItemSelectedNoParticipantsAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#F8F8FA"));
        holder.advertisementRowDateAndTimeText.setTextColor(getResources().getColor(R.color.foxmikePrimaryColor));
        holder.participantsTV.setTextColor(getResources().getColor(R.color.primaryTextColor));
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
            TextView nrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCommentClickedListener.OnCommentClicked(sessionID, postID, heading, time, message,thumb_image, "mSession");
                }
            });
            nrOfCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCommentClickedListener.OnCommentClicked(sessionID, postID, heading, time, message,thumb_image, "mSession");
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
        geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

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
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity)getActivity()).setSupportActionBar(null);
        selectedIcon=null;
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
        if (onOffsetChangedListener!=null) {
            appBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener);
        }
        appBarLayout = null;
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
        if (context instanceof UserAccountFragment.OnUserAccountFragmentInteractionListener) {
            onUserAccountFragmentInteractionListener = (UserAccountFragment.OnUserAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserAccountFragmentInteractionListener");
        }
        if (context instanceof OnUserClickedListener) {
            onUserClickedListener = (OnUserClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserClickedListener");
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
        onCommentClickedListener = null;
        onUserAccountFragmentInteractionListener = null;
        sessionListener = null;
        onUserClickedListener = null;
        advertisementListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fbAdDateAndTimeAdapter!=null) {
            fbAdDateAndTimeAdapter.stopListening();
        }
        if (mMap!=null) {
            mMap.clear();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDefaultSourceMap();
        if (fbAdDateAndTimeAdapter!=null) {
            fbAdDateAndTimeAdapter.startListening();
        }
    }

    private void populateUserPublicHashMap(String userId, OnUsersLoadedListener onUsersLoadedListener) {

        if (!userPublicHashMap.containsKey(userId)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    UserPublic userPublic = dataSnapshot.getValue(UserPublic.class);
                    userPublicHashMap.put(userId, userPublic);
                    onUsersLoadedListener.OnUsersLoaded();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            onUsersLoadedListener.OnUsersLoaded();
        }
    }

    public interface OnUsersLoadedListener{
        void OnUsersLoaded();
    }
}