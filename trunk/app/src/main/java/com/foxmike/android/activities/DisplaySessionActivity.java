package com.foxmike.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListHistoryAdvertisementsAdapter;
import com.foxmike.android.adapters.SessionImagesAdapter;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.ParticipantsFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionDateAndTime;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.AdvertisementRowViewHolder;
import com.foxmike.android.utils.CirclePagerIndicatorDecoration;
import com.foxmike.android.utils.FixAppBarLayoutBehavior;
import com.foxmike.android.utils.Price;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.stripe.android.model.PaymentMethod;

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

import static android.content.ContentValues.TAG;
import static com.foxmike.android.activities.BookingActivity.BOOKING_CANCELED;
import static com.foxmike.android.activities.CancelAdvertisementActivity.CANCEL_ADVERTISEMENT_OK;
import static com.foxmike.android.activities.MainPlayerActivity.BOOK_SESSION_REQUEST;
import static com.foxmike.android.activities.MainPlayerActivity.CANCEL_ADVERTISEMENT_REQUEST;
import static com.foxmike.android.activities.MainPlayerActivity.CANCEL_BOOKING_REQUEST;
import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;
import static com.foxmike.android.utils.Price.PRICES_STRINGS;

public class DisplaySessionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private ConstraintLayout sessionImageCardView;
    private TextView mDisplaySessionBtn;
    private CircleImageView mHostImage;
    private CircleImageView mCurrentUserPostImage;
    private TextView mHostAboutTV;
    private TextView mHost;
    private ConstraintLayout whoContainer;
    private ConstraintLayout whereContainer;
    private View whoDivider;
    private View infoDivider;
    private TextView mWhatTW;
    private TextView mWhoTW;
    private TextView mWhereTW;
    private TextView mSendMessageToHost;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
    private androidx.appcompat.widget.Toolbar toolbar;
    private UserPublic host;
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
    //private HashMap defaultSourceMap;
    private boolean mapReady;
    BitmapDescriptor selectedIcon;
    private TextView addDates;
    private TextView snackBarDateAndTimeTV;
    private LinearLayout snackNoUpcomingAds;
    private boolean currentUserAndViewUsed;
    private boolean sessionUsed;
    private boolean currentUserAndSessionAndViewAndMapUsed;
    private boolean hostAndSessionAndViewUsed;
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
    private TextView showMoreTV2;
    private int currentHeightInNr;
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
    private TextView mAddress;
    private long mLastClickTime = 0;
    private boolean showAvClicked = false;
    private long lastScrollTime = 0;
    private boolean firstLoadOfPosts = true;
    private boolean firstLoadOfComments;
    private ProgressBar postProgressBar;
    private ImageView editTop;
    private ArrayList<ImageView> editTVArrayList=new ArrayList<ImageView>();
    private boolean isHost = false;
    private ImageView editWhat;
    private ImageView editWho;
    private ImageView editWhere;
    private ImageView editAvailability;
    private AppCompatRatingBar ratingBar;
    private TextView ratingsAndReviewsText;
    private TextView showAllReviews;
    private Disposable subscription;
    private TextView showAvailAbility;
    private HashMap<String, Long> adTimes = new HashMap<>();
    private String stripeCustomerId;
    private String foxmikeUid;
    private AppCompatTextView whatHeadingTW;
    private HashMap<String, String> sessionTypeDictionary;
    private FrameLayout dotProgressBarContainer;
    private ConstraintLayout snackBar;
    private ImageView shareIcon;
    private PaymentMethod mPaymentMethod;
    private boolean trainerMode;
    private RecyclerView historySessionsRV;
    private Long numberInHistory = 0L;
    private int sizeOfHistoryStart = 1;
    private int sizeOfHistoryAdded = 4;
    private Query allHistoryKeysQuery;
    private Long currentLastTimestamp = 0L;
    private ListHistoryAdvertisementsAdapter listHistoryAdvertisementsAdapter;
    private boolean firstLoaded;
    private VideoView videoView;
    private boolean isContinuously = false;

    private RecyclerView imagesRV;
    private LinearLayout plusContainer;
    private FirebaseFunctions mFunctions;
    private String stripeDefaultPaymentMethodId;
    private boolean hasDeposition;

    private View mainView;
    private ProgressBar videoProgressBar;

    private ImageView playVideoIcon;
    private ConstraintLayout videoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_session);


        sessionID = getIntent().getStringExtra("sessionID");
        sessionTypeDictionary = (HashMap<String, String>)getIntent().getSerializableExtra("sessionTypeDictionary");
        trainerMode = getIntent().getBooleanExtra("trainerMode", false);
        representingAdTimestamp = getIntent().getLongExtra(REPRESSENTING_AD_TIMESTAMP, 0L);
        hasDeposition = getIntent().getBooleanExtra("hasDeposition", false);

        mainView = findViewById(R.id.activity_main_player);

        mFunctions = FirebaseFunctions.getInstance();



        LinearLayout displaySessionContainer;
        View displaySession;
        displaySessionContainer = findViewById(R.id.display_session_container);
        displaySession = getLayoutInflater().inflate(R.layout.display_session,displaySessionContainer,false);

        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mHostAboutTV = displaySession.findViewById(R.id.hostAbout);
        writePostLayout = displaySession.findViewById(R.id.write_post_layout);
        whatHeadingTW = displaySession.findViewById(R.id.whatHeadingTW);
        mWhatTW = displaySession.findViewById(R.id.whatTW);
        mWhoTW = displaySession.findViewById(R.id.whoTW);
        mWhereTW = displaySession.findViewById(R.id.whereTW);
        whoContainer = displaySession.findViewById(R.id.whoContainer);
        whereContainer = displaySession.findViewById(R.id.whereContainer);
        whoDivider = displaySession.findViewById(R.id.whoDivider);
        infoDivider = displaySession.findViewById(R.id.infoDivider);
        playVideoIcon = displaySession.findViewById(R.id.playVideoIcon);

        plusContainer = displaySession.findViewById(R.id.plusContainer);
        videoView = displaySession.findViewById(R.id.video);


        mCurrentUserPostImage = displaySession.findViewById(R.id.session_post_current_user_image);
        sessionImageCardView = findViewById(R.id.sessionImageCardView);

        mSendMessageToHost = displaySession.findViewById(R.id.sendMessageToHost);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        priceTV = findViewById(R.id.priceTV);
        mDisplaySessionBtn = findViewById(R.id.displaySessionBtn);
        displaySessionContainer.addView(displaySession);
        // Set the mSession image
        sessionImage = findViewById(R.id.displaySessionImage);
        postList = (RecyclerView) findViewById(R.id.post_list);
        postList.setVisibility(View.GONE);
        paymentMethodProgressBar = findViewById(R.id.paymentMethodProgressBar);
        addPaymentMethodTV = findViewById(R.id.addPaymentMethodTV);
        paymentFrame = findViewById(R.id.framePayment);
        paymentMethodTV = findViewById(R.id.paymentMethod);
        snackBarDateAndTimeTV = findViewById(R.id.snackBarDateAndTimeTV);
        upcomingSessionsRV = displaySession.findViewById(R.id.upcomingSessionsList);
        showMore = displaySession.findViewById(R.id.showMoreText);
        snackNoUpcomingAds = findViewById(R.id.snackNoUpcomingAds);
        showAvailAbility = findViewById(R.id.showAvailAbility);
        addDates = displaySession.findViewById(R.id.editSession);
        fbRVContainer = displaySession.findViewById(R.id.firebaseRVContainer);
        sessionDateAndTimeLLManager = new LinearLayoutManager(this);
        upcomingSessionsRV.setHasFixedSize(true);
        upcomingSessionsRV.setLayoutManager(sessionDateAndTimeLLManager);
        ((SimpleItemAnimator) upcomingSessionsRV.getItemAnimator()).setSupportsChangeAnimations(false);
        upcomingSessionsRV.setNestedScrollingEnabled(false);
        historySessionsRV = displaySession.findViewById(R.id.historySessionsList);
        LinearLayoutManager historyLayoutManager = new LinearLayoutManager(this);
        historyLayoutManager.setStackFromEnd(true);
        historySessionsRV.setLayoutManager(historyLayoutManager);
        historySessionsRV.setNestedScrollingEnabled(false);
        ((SimpleItemAnimator) historySessionsRV.getItemAnimator()).setSupportsChangeAnimations(false);
        showMoreTV2 = displaySession.findViewById(R.id.showMoreTV2);
        showMoreTV = displaySession.findViewById(R.id.showMoreTV);
        displaySessionSV = findViewById(R.id.displaySessionSV);
        snackNoAdTV = findViewById(R.id.snackNoAdTV);
        mAddress = displaySession.findViewById(R.id.addressTV);
        postProgressBar = displaySession.findViewById(R.id.postProgressBar);
        ratingBar = displaySession.findViewById(R.id.ratingBar);
        ratingsAndReviewsText = displaySession.findViewById(R.id.ratingsAndReviewsText);
        showAllReviews = displaySession.findViewById(R.id.showAllReviews);
        dotProgressBarContainer = findViewById(R.id.dotProgressBarContainer);
        snackBar = findViewById(R.id.snackBar);

        imagesRV = findViewById(R.id.sessionImagesRV);

        videoContainer = displaySession.findViewById(R.id.videoContainer);
        //set default
        snackBarDateAndTimeTV.setVisibility(View.GONE);
        priceTV.setVisibility(View.GONE);
        //mDisplaySessionBtn.setVisibility(View.GONE);
        snackNoUpcomingAds.setVisibility(View.VISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        View adRow = findViewById(R.id.adRowDummyToMeasure);
        adRow.measure(display.getWidth(), display.getHeight());

        itemHeight = adRow.getMeasuredHeight(); //view height*/

        paymentFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent paymentPreferencesIntent = new Intent(DisplaySessionActivity.this, PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

        // Store refs to editTV

        editWhat = displaySession.findViewById(R.id.editWhat);
        editWho = displaySession.findViewById(R.id.editWho);
        editWhere = displaySession.findViewById(R.id.editWhere);
        editAvailability = displaySession.findViewById(R.id.editAvailability);
        editTVArrayList.add(editWhat);
        editTVArrayList.add(editWho);
        editTVArrayList.add(editWhere);
        editTVArrayList.add(editAvailability);

        videoProgressBar = displaySession.findViewById(R.id.videoProgressBar);

        View spaceView0 = findViewById(R.id.spaceView0);

        // Setup toolbar
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //View action_bar_view = getLayoutInflater().inflate(R.layout.display_session_custom_bar, null);

        shareIcon = findViewById(R.id.shareIcon);
        editTop = (ImageView) (findViewById(R.id.editTop));

        // make sure the whole action bar is filled with the custom view
        /*ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);*/


        appBarLayout = findViewById(R.id.displaySessionAppBar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new FixAppBarLayoutBehavior());
        onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset)>Math.abs(appBarLayout.getTotalScrollRange()/2)) {
                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.primaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    shareIcon.setColorFilter(getResources().getColor(R.color.primaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    editTop.setColorFilter(getResources().getColor(R.color.primaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    spaceView0.setVisibility(View.VISIBLE);
                } else {
                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    shareIcon.setColorFilter(getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    editTop.setColorFilter(getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);
                    spaceView0.setVisibility(View.GONE);
                }
            }
        };
        appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener);

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
        postList.setLayoutManager(new LinearLayoutManager(this));
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
                            holder.setUserImage(userPublicHashMap.get(post.getAuthorId()).getThumb_image(), DisplaySessionActivity.this);
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
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        postList.setNestedScrollingEnabled(false);



        // Setup static map with mSession location
        if (null == getSupportFragmentManager().findFragmentByTag("xDisplaySessionMapsFragment")) {
            GoogleMapOptions options = new GoogleMapOptions();
            options.liteMode(true);
            options.mapToolbarEnabled(true);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.child_fragment_container, mapFragment,"xDisplaySessionMapsFragment").commit();
            mapFragment.getMapAsync(this);
        }

        postBranchArrayList = new ArrayList<>();

        FirebaseDatabaseViewModel stripeDepositionPaymentIntentIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDepositionPaymentIntentIdLiveData = stripeDepositionPaymentIntentIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDepositionPaymentIntentId"));
        stripeDepositionPaymentIntentIdLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                hasDeposition = dataSnapshot.getValue() != null;
            }
        });


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
            FirebaseDatabaseViewModel sessionViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> sessionLiveData = sessionViewModel.getDataSnapshotLiveData(rootDbRef.child("sessions").child(sessionID));
            sessionLiveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        Session session = dataSnapshot.getValue(Session.class);
                        if (session.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            isHost = true;
                        }
                        mSession = session;
                        sessionLongitude = session.getLongitude();
                        sessionLatitude = session.getLatitude();
                        sessionID = session.getSessionId();
                        currentUserAndSessionAndViewAndMapUsed = false;
                        hostAndSessionAndViewUsed = false;
                        sessionAndPaymentAndViewUsed = false;
                        sessionUsed = false;
                        sessionLoaded = true;
                        onAsyncTaskFinished();
                        getPosts();
                        getSessionHost();

                    }
                }
            });


            Long currentTimestamp = System.currentTimeMillis();
            // Create query to get all the advertisement keys from the current mSession
            Query keyQuery = rootDbRef.child("sessionAdvertisements").child(sessionID).orderByValue().startAt(currentTimestamp);
            // Use the query to get all the advertisement keys from the current mSession
            FirebaseDatabaseViewModel adsViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> adsLiveData = adsViewModel.getDataSnapshotLiveData(keyQuery);
            adsLiveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(DataSnapshot dataSnapshot) {
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
            });


            // listen to history
            Query allHistoryKeysQuery = rootDbRef.child("sessionAdvertisements").child(sessionID).orderByValue().startAt(1).endAt(currentTimestamp);
            FirebaseDatabaseViewModel historyViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> historyLiveData = historyViewModel.getDataSnapshotLiveData(allHistoryKeysQuery);
            historyLiveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    numberInHistory = dataSnapshot.getChildrenCount();
                    if (dataSnapshot.getChildrenCount()<2) {
                        showMoreTV2.setText(getResources().getString(R.string.end_of_list));
                        showMoreTV2.setClickable(false);
                    } else {
                        if (listHistoryAdvertisementsAdapter!=null) {
                            if (listHistoryAdvertisementsAdapter.getItemCount()<dataSnapshot.getChildrenCount()) {
                                showMoreTV2.setText(getResources().getString(R.string.show_more));
                                showMoreTV2.setClickable(true);
                            } else {
                                showMoreTV2.setText(getResources().getString(R.string.end_of_list));
                                showMoreTV2.setClickable(false);
                            }
                        } else {
                            showMoreTV2.setText(getResources().getString(R.string.show_more));
                            showMoreTV2.setClickable(true);
                        }
                    }

                }
            });

            showMoreTV2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    populateHistoryList(currentLastTimestamp, false);
                }
            });

        } else {
            Toast toast = Toast.makeText(this, R.string.Session_not_found_please_try_again_later,Toast.LENGTH_LONG);
            toast.show();
        }


        FirebaseDatabaseViewModel stripeDefaultPaymentMethodViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDefaultPaymentMethodLiveData = stripeDefaultPaymentMethodViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
        stripeDefaultPaymentMethodLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    stripeDefaultPaymentMethodId = dataSnapshot.getValue().toString();
                    updateStripeCustomerInfo();
                } else {
                    setPaymentMethod(new HashMap());
                }

            }
        });






    }

    private void updateStripeCustomerInfo() {

        if (stripeDefaultPaymentMethodId==null) {
            setPaymentMethod(new HashMap());
        } else {

            retrievePaymentMethod(stripeDefaultPaymentMethodId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                    // If not succesful, show error and return from function, will trigger if account ID does not exist
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        // [START_EXCLUDE]
                        Log.w(TAG, "retrieve:onFailure", e);
                        showSnackbar(getString(R.string.bad_internet));
                        setPaymentMethod(new HashMap());
                        return;
                        // [END_EXCLUDE]
                    }
                    // If successful, extract
                    HashMap<String, Object> result = task.getResult();
                    if (result.get("resultType").toString().equals("paymentMethod")) {
                        setPaymentMethod((HashMap) result.get("paymentMethod"));
                    } else {
                        setPaymentMethod(new HashMap());
                        HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                        showSnackbar(error.get("message").toString());
                    }

                }
            });
        }


    }

    private void setPaymentMethod(HashMap hashMap) {
        if (hashMap.get("card")!=null) {
            Gson gson = new Gson();
            String json = gson.toJson(hashMap);
            PaymentMethod paymentMethod = PaymentMethod.fromString(json);
            mPaymentMethod = paymentMethod;
            hasPaymentSystem = true;
            sessionAndPaymentAndViewUsed = false;
            paymentMethodAdSelectedAndViewUsed = false;
            paymentMethodLoaded = true;
            onAsyncTaskFinished();
        } else {
            hasPaymentSystem = false;
            sessionAndPaymentAndViewUsed = false;
            paymentMethodAdSelectedAndViewUsed = false;
            paymentMethodLoaded = true;
            onAsyncTaskFinished();
        }
    }

    private Task<HashMap<String, Object>> retrievePaymentMethod(String paymentMethodId) {
        return mFunctions
                .getHttpsCallable("retrievePaymentMethod")
                .call(paymentMethodId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
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
                host = dataSnapshot.getValue(UserPublic.class);
                hostLoaded = true;
                hostAndSessionAndViewUsed = false;
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
        firebaseDatabaseLiveData.observe(DisplaySessionActivity.this, new Observer<DataSnapshot>() {
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
                    FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(DisplaySessionActivity.this).get(FirebaseDatabaseViewModel.class);
                    LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("sessionPostComments").child(sessionID).child(postID));
                    firebaseDatabaseLiveData.observe(DisplaySessionActivity.this, new Observer<DataSnapshot>() {
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

    private void setupAds() {

        // --------- GET ALL THE AD TIMESTAMPS SAVED UNDER SESSION/ADVERTISEMENTS ---------
        // Current time as timestamp

        DatabaseReference adDbRef = rootDbRef.child("advertisements");

        populateHistoryList(currentLastTimestamp, true);


        // --------- POPULATE THE RECYCLERVIEW WITH THE ADVERTISEMENTS ---------
        // Create the key query which will get all the advertisement keys for ads with a date in the future.
        Query allKeysQuery = rootDbRef.child("sessionAdvertisements").child(mSession.getSessionId()).orderByValue().startAt(new DateTime(System.currentTimeMillis()).minusMinutes(75).getMillis());

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


                Long endTimestamp = model.getAdvertisementTimestamp() + (model.getDurationInMin()*1000*60);

                holder.advertisementRowDateText.setText(TextTimestamp.textSessionDate(model.getAdvertisementTimestamp()));
                holder.advertisementRowTimeText.setText(TextTimestamp.textTime(model.getAdvertisementTimestamp()) + "-" + TextTimestamp.textTime(endTimestamp));
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
                                ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(model.getAdvertisementId(), getResources().getString(R.string.participants_on) + " " + TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp()));
                                FragmentManager fragmentManager = getSupportFragmentManager();
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


                // START WITH DEFAULT APPEARANCE
                setAdListItemDefaultAppearance(holder);

                // if rowIndex==-1 it means that no item has been selected in the list, make the first item (this item) selected
                if (rowIndex==-1) {
                    if (representingAdTimestamp==model.getAdvertisementTimestamp()) {
                        rowIndex = position;
                    }
                }
                // SELECTED
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

                // set the row appearance to default (no selection or booked color)
                // SUPERADMIN OR HOST
                if (currentUser.isSuperAdmin() ||  mSession.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.toggleParticipantsLeftTV(false);
                    holder.toggleParticipantsTV(true);

                    // If the ad has participants, check if the current user is one of them and if so turn the appearance of the row to booked (filled dark green)
                    if (model.getParticipantsTimestamps().size()!=0) {
                        if (model.getParticipantsTimestamps().containsKey(FirebaseAuth.getInstance().getUid())) {
                            setAdListItemBookedAppearance(holder);
                        } else {
                            if (model.getParticipantsTimestamps().size()>=model.getMaxParticipants()) {
                                setAdListItemFullyBookedAppearance(holder);
                            }
                        }
                    }

                } else {
                    // SUPERHOSTED
                    if (mSession.isSuperHosted()) {
                        holder.toggleParticipantsTV(false);
                        holder.toggleParticipantsLeftTV(false);
                        holder.toggleFullyBooked(false);
                        holder.toggleBooked(false);
                    } else {
                        // NORMAL
                        if (countParticipants >= (model.getMaxParticipants()-3) && countParticipants!=model.getMaxParticipants()) {
                            if ((model.getMaxParticipants()-countParticipants)==1) {
                                holder.setParticipantsLeftTV(Long.toString(model.getMaxParticipants()-countParticipants) + getResources().getString(R.string.slot_left));
                            } else {
                                holder.setParticipantsLeftTV(Long.toString(model.getMaxParticipants()-countParticipants) + getResources().getString(R.string.slots_left));
                            }

                            holder.toggleParticipantsLeftTV(true);
                            holder.toggleParticipantsTV(false);
                        } else {
                            holder.toggleParticipantsLeftTV(false);
                            holder.toggleParticipantsTV(false);
                        }

                        // If the ad has participants, check if the current user is one of them and if so turn the appearance of the row to booked (filled dark green)
                        if (model.getParticipantsTimestamps().size()!=0) {
                            if (model.getParticipantsTimestamps().containsKey(FirebaseAuth.getInstance().getUid())) {
                                setAdListItemBookedAppearance(holder);
                                holder.toggleParticipantsLeftTV(false);
                                holder.toggleParticipantsTV(true);
                            } else {
                                if (model.getParticipantsTimestamps().size()>=model.getMaxParticipants()) {
                                    setAdListItemFullyBookedAppearance(holder);
                                    holder.toggleParticipantsLeftTV(false);
                                    holder.toggleParticipantsTV(false);
                                }
                            }
                        }
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
        // If an item has been inserted, call the function updateListViews() which updates the showMore text
// If an item has been removed that was selected and if the list only contained that item set the adSelected to null and update snackbar
// else set the adSelected to the first item in the list instead and update snackbar.
// TODO maybe better to set the snackbar to show availability
// If an item has been removed, call the function updateListViews() which updates the showMore text

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

    private void populateHistoryList(Long lastTimestamp, boolean isFirstLoad) {

        if (isFirstLoad && firstLoaded) {
            return;
        }

        if (isFirstLoad) {
            firstLoaded = true;
            // POPULATE THE HISTORY RECYCLERVIEW //

            currentLastTimestamp = new DateTime(System.currentTimeMillis()).minusMinutes(75).getMillis();
            lastTimestamp = currentLastTimestamp;

            if (listHistoryAdvertisementsAdapter!=null) {
                listHistoryAdvertisementsAdapter.clear();
            }

            listHistoryAdvertisementsAdapter = new ListHistoryAdvertisementsAdapter(new ArrayList<>(), mSession.isSuperHosted(), currentUser.isSuperAdmin(), isHost, new OnHistoryAdClickedListener() {
                @Override
                public void OnHistoryAdClicked(Advertisement adClicked) {
                    if (adClicked.getParticipantsTimestamps().size()>0) {
                        ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(adClicked.getAdvertisementId(), getResources().getString(R.string.participants_on) + " " + TextTimestamp.textSessionDateAndTime(adClicked.getAdvertisementTimestamp()));
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        participantsFragment.show(transaction,"participantsFragment");
                    }

                }
            });
            historySessionsRV.setAdapter(listHistoryAdvertisementsAdapter);
            allHistoryKeysQuery = rootDbRef.child("sessionAdvertisements").child(mSession.getSessionId()).orderByValue().startAt(1).endAt(lastTimestamp).limitToLast(sizeOfHistoryStart);
        } else {
            allHistoryKeysQuery = rootDbRef.child("sessionAdvertisements").child(mSession.getSessionId()).orderByValue().startAt(1).endAt(lastTimestamp-1).limitToLast(sizeOfHistoryAdded);
        }

        // Listener to keep data in cache in sync with database
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(DisplaySessionActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(allHistoryKeysQuery);
        firebaseDatabaseLiveData.observe(DisplaySessionActivity.this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                // Dummy listener
            }
        });


        allHistoryKeysQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue()==null) {
                    showMoreTV2.setText(getResources().getString(R.string.end_of_list));
                    showMoreTV2.setClickable(false);
                    return;
                }

                HashMap<String,Long> adKeys = (HashMap<String,Long>) dataSnapshot.getValue();

                ArrayList<Task<?>> tasks = new ArrayList<>();

                for (String key: adKeys.keySet()) {
                    if (currentLastTimestamp.equals(0L)) {
                        currentLastTimestamp = adKeys.get(key);
                    } else {
                        if (currentLastTimestamp>=adKeys.get(key)) {
                            currentLastTimestamp = adKeys.get(key);
                        }
                    }
                    TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
                    Task dbTask = dbSource.getTask();
                    DatabaseReference ref = rootDbRef.child("advertisements").child(key);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dbSource.setResult(dataSnapshot);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dbSource.setException(databaseError.toException());
                        }
                    });
                    tasks.add(dbTask);
                }
                Tasks.whenAll(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Advertisement> advertisements = new ArrayList<>();
                            for (Task finishedTask: tasks) {
                                DataSnapshot dataSnapshot = (DataSnapshot) finishedTask.getResult();
                                Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                                advertisements.add(advertisement);
                            }
                            Collections.sort(advertisements, Collections.reverseOrder());
                            listHistoryAdvertisementsAdapter.addData(advertisements);
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void onAsyncTaskFinished() {

        // ---------------- CURRENTUSER && VIEW-----------------
        if (currentUserLoaded && !currentUserAndViewUsed) {
            currentUserAndViewUsed=true;
            // Set the users profile image to the "write post" layout
            setImage(currentUser.getThumb_image(), mCurrentUserPostImage);
        }

        // ---------------- SESSION && VIEW-----------------
        if (sessionLoaded && !sessionAndViewUsed) {
            sessionAndViewUsed = true;

            /*MediaController mediacontroller = new MediaController(DisplaySessionActivity.this);
            mediacontroller.setAnchorView(videoView);*/

            if (mSession.getVideoUrl()==null) {
                videoContainer.setVisibility(View.GONE);
            } else {
                String uriPath = mSession.getVideoUrl(); //update package name
                Uri uri = Uri.parse(uriPath);

                /*videoView.setMediaController(mediacontroller);*/
                videoView.setVideoURI(uri);
                playVideoIcon.setVisibility(View.GONE);
                videoView.start();

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        videoView.start();
                    }
                });

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        videoProgressBar.setVisibility(View.GONE);
                    }
                });

            }




            if (mSession.isPlus()) {
                plusContainer.setVisibility(View.VISIBLE);
            } else {
                plusContainer.setVisibility(View.GONE);
            }

            shareIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(DisplaySessionActivity.this);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "session");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mSession.getSessionId());
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "https://foxmike.app/explore/" + mSession.getSessionId());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share)));
                }
            });

            String address = getAddress(mSession.getLatitude(), mSession.getLongitude());
            mAddress.setText(address);

            boolean hasRating = false;
            String ratingTextString = "";

            ratingBar.setRating(mSession.getRating());
            String ratingTextFormatted = String.format("%.1f", mSession.getRating());
            if (mSession.getNrOfRatings()==0) {
                ratingsAndReviewsText.setText(R.string.new_session_no_reviews_yet);
                showAllReviews.setVisibility(View.GONE);
            } else if (mSession.getNrOfRatings()==1) {
                showAllReviews.setVisibility(View.VISIBLE);
                hasRating = true;
                String rating = String.format("%.1f", mSession.getRating());
                ratingTextString = rating + " (" + mSession.getNrOfRatings() + ")";
                ratingsAndReviewsText.setText(ratingTextFormatted + getString(R.string.based_on_nr_ratings_text_1) + mSession.getNrOfRatings() + getString(R.string.based_on_nr_ratings_text_2_single));
            } else {
                showAllReviews.setVisibility(View.VISIBLE);
                String rating = String.format("%.1f", mSession.getRating());
                hasRating = true;
                ratingTextString = rating + " (" + mSession.getNrOfRatings() + ")";
                ratingsAndReviewsText.setText(ratingTextFormatted + getString(R.string.based_on_nr_ratings_text_1) + mSession.getNrOfRatings() + getString(R.string.based_on_nr_ratings_text_2));
            }
            showAllReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ratingAndReviewsIntet = new Intent(DisplaySessionActivity.this, RatingsAndReviewsActivity.class);
                    ratingAndReviewsIntet.putExtra("sessionId", sessionID);
                    ratingAndReviewsIntet.putExtra("sessionName", mSession.getSessionName());
                    startActivity(ratingAndReviewsIntet);

                }
            });

            String sessionTypeString = "";

            if (!sessionTypeDictionary.containsKey(mSession.getSessionType())) {
                sessionTypeString = getResources().getString(R.string.other);
            } else {
                sessionTypeString = sessionTypeDictionary.get(mSession.getSessionType());
            }

            ArrayList<String> imagesKeys = new ArrayList<>(mSession.getImages().keySet());
            Collections.sort(imagesKeys);
            ArrayList<String> imagesURLs = new ArrayList<>();
            for (String key: imagesKeys) {
                imagesURLs.add(mSession.getImages().get(key));
            }

            if (imagesURLs.size()==0) {
                imagesURLs.add(mSession.getImageUrl());
            }

            // set the images
            // -----------  Set the mSession information in UI from mSession object --------------
            SessionImagesAdapter sessionImagesAdapter = new SessionImagesAdapter(this, imagesURLs, mSession.getSessionName(), sessionTypeString, address, hasRating, ratingTextString);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            imagesRV.setLayoutManager(linearLayoutManager);
            imagesRV.setAdapter(sessionImagesAdapter);

            if (imagesURLs.size()>1) {
                imagesRV.setOnFlingListener(null);
                PagerSnapHelper snapHelper = new PagerSnapHelper();
                snapHelper.attachToRecyclerView(imagesRV);
                imagesRV.addItemDecoration(new CirclePagerIndicatorDecoration());
            }

            // -------------------

            postList.setVisibility(View.VISIBLE);
            writePostLayout.setVisibility(View.VISIBLE);
            writePostLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent writePostIntent = new Intent(DisplaySessionActivity.this, WritePostActivity.class);
                    writePostIntent.putExtra("dbParent", "sessions");
                    writePostIntent.putExtra("sourceID", sessionID);
                    writePostIntent.putExtra("title", mSession.getSessionName());
                    startActivity(writePostIntent);
                }
            });

            mWhatTW.setText(mSession.getWhat());
            mWhoTW.setText(mSession.getWho());
            mWhereTW.setText(mSession.getWhereAt());

            // Hide if superhosted
            if (mSession.isSuperHosted()) {
                mWhoTW.setVisibility(View.GONE);
                mWhereTW.setVisibility(View.GONE);
                whoContainer.setVisibility(View.GONE);
                whereContainer.setVisibility(View.GONE);
                whoDivider.setVisibility(View.GONE);
                infoDivider.setVisibility(View.GONE);
            }

            if (mSession.getHost().equals(currentFirebaseUser.getUid())) {

                // -------------------- HOST -----------------------------
                editTop.setVisibility(View.VISIBLE);
                for (ImageView editTV: editTVArrayList) {
                    editTV.setVisibility(View.VISIBLE);
                }
                addDates.setVisibility(View.VISIBLE);

                if (mSession.getSecondaryHostId()!=null) {
                    if (mSession.getSecondaryHostId().equals(currentFirebaseUser.getUid())) {
                        // -------------------- HOST AND SECONDARY HOST -----------------------------
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
                        // -------------------- HOST BUT IS NOT SECONDARY HOST -----------------------------
                        mSendMessageToHost.setText(R.string.show_profile);
                        mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();
                                onUserClickedListener.OnUserClicked(mSession.getSecondaryHostId());
                            }
                        });
                    }

                } else {
                    // -------------------- HOST -----------------------------
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

            } else {

                // -------------------- PLAYER -----------------------------
                editTop.setVisibility(View.GONE);
                for (ImageView editTV: editTVArrayList) {
                    editTV.setVisibility(View.GONE);
                }
                addDates.setVisibility(View.GONE);

                if (mSession.getSecondaryHostId()!=null) {
                    if (mSession.getSecondaryHostId().equals(currentFirebaseUser.getUid())) {
                        // -------------------- PLAYER AND IS SECONDARY HOST -----------------------------
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
                        // -------------------- PLAYER BUT IS NOT SECONDARY HOST -----------------------------
                        mSendMessageToHost.setText(R.string.show_profile);
                        mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();
                                onUserClickedListener.OnUserClicked(mSession.getSecondaryHostId());
                            }
                        });
                    }

                } else {
                    // -------------------- PLAYER -----------------------------
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


        }

        // ---------------- HOST && VIEW-----------------
        if (hostLoaded && sessionLoaded && !hostAndSessionAndViewUsed) {
            hostAndSessionAndViewUsed =true;
            setImage(host.getThumb_image(), mHostImage);
            String hostText = getString(R.string.hosted_by_text) + " " + host.getFirstName();
            mHost.setText(hostText);
            mHostAboutTV.setText(host.getAboutMe());

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupAds();

                }
            }, 400);

            if (host.isSuperAdmin()) {
                if (mSession.getSecondaryHostId()!=null) {
                    FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(DisplaySessionActivity.this).get(FirebaseDatabaseViewModel.class);
                    LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("usersPublic").child(mSession.getSecondaryHostId()));
                    firebaseDatabaseLiveData.observe(DisplaySessionActivity.this, new Observer<DataSnapshot>() {
                        @Override
                        public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()==null) {
                                return;
                            }
                            UserPublic secondaryHost = dataSnapshot.getValue(UserPublic.class);
                            setImage(secondaryHost.getThumb_image(), mHostImage);
                            String hostText = getString(R.string.hosted_by_text) + " " + secondaryHost.getFirstName();
                            mHost.setText(hostText);
                            mHostAboutTV.setText(secondaryHost.getAboutMe());
                        }
                    });
                } else {
                    whatHeadingTW.setText(R.string.information);
                    mHost.setText(host.getFirstName() + " " + getResources().getString(R.string.foxmike_tip));
                    mHostAboutTV.setText(R.string.foxmike_tip_text);
                }
            }
        }

        if (postsLoaded && !postsUsed) {
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

        if (postCommentsLoaded && !postCommentsUsed) {
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
        if (sessionLoaded && paymentMethodLoaded && !sessionAndPaymentAndViewUsed) {


            sessionAndPaymentAndViewUsed = true;




        }

        // -------- VIEW -------- PAYMENT ----- ADSELECTED ---
        if (paymentMethodLoaded && adSelectedReady && !paymentMethodAdSelectedAndViewUsed) {
            paymentMethodProgressBar.setVisibility(View.GONE);
            // (session will also be loaded)
            paymentMethodAdSelectedAndViewUsed = true;

            dotProgressBarContainer.setVisibility(View.GONE);
            snackBar.setVisibility(View.VISIBLE);


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
                snackBarDateAndTimeTV.setText(TextTimestamp.textSessionDateAndTime(adSelected.getAdvertisementTimestamp()));
                setPriceText();
                // -------------------- HOST -----------------------------
                // If the current user is the mSession host display "show occasion" as the text of the button.
                if (mSession.getHost().equals(currentFirebaseUser.getUid())) {
                    Log.w("CRICKE", "setting");
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
                    if (mSession.isSuperHosted()) {
                        mDisplaySessionBtn.setText(getResources().getString(R.string.add_to_my_bookings));
                    }
                    // If the ad selected is free do not show payment method
                    if (adSelected.getPrice()==0) {
                        mDisplaySessionBtn.setEnabled(true);
                        paymentMethodTV.setVisibility(View.GONE);
                        addPaymentMethodTV.setVisibility(View.GONE);
                        mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));
                    } else {
                        // If the ad costs money find out if the current user has a payment source and if so show that payment method
                        // else display add payment method link to PaymentPreferencesActivity
                        if (mPaymentMethod!=null) {
                            String last4 = mPaymentMethod.card.last4;
                            paymentMethodTV.setText("**** " + last4);
                            String cardBrand = mPaymentMethod.card.brand;
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
                    // If the ad selected has participants and of the current user is one of them, display cancel booking
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
                            if (mSession.isSuperHosted()) {
                                mDisplaySessionBtn.setText(getResources().getString(R.string.remove_from_my_bookings));
                            }
                        }
                    }
                }
            }
        }

        // ---------------- CURRENTUSER && SESSION && VIEW && MAP-----------------
        if (currentUserLoaded && sessionLoaded && mapReady && !currentUserAndSessionAndViewAndMapUsed) {
            currentUserAndSessionAndViewAndMapUsed =true;

            addDates.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        addAdvertisements(sessionID);

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
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        addAdvertisements(sessionID);
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
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        OnEditSession(sessionID, mSession);
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
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        OnEditSession(sessionID, mSession, "what");
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
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        OnEditSession(sessionID, mSession, "who");
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
                    if (!trainerMode) {
                        Toast.makeText(DisplaySessionActivity.this, R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                    } else {
                        OnEditSession(sessionID, mSession, "where");
                    }

                }
            });


            // SETUP MAP
            LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);

            Drawable locationDrawable = getResources().getDrawable(R.mipmap.baseline_location_on_black_36);
            Drawable selectedLocationDrawable = locationDrawable.mutate();
            selectedLocationDrawable.setColorFilter(getResources().getColor(R.color.foxmikePrimaryColor), PorterDuff.Mode.SRC_ATOP);
            selectedIcon = getMarkerIconFromDrawable(selectedLocationDrawable);


            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(markerLatLng).title(mSession.getSessionName()).icon(selectedIcon));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
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
                                OnCancelBookedSession(adSelected.getParticipantsTimestamps().get(currentFirebaseUser.getUid()),adSelected.getAdvertisementTimestamp(),adSelected.getAdvertisementId(),currentFirebaseUser.getUid(), adSelected.getPrice(), mSession.getHost(), mSession.isSuperHosted());
                                return;
                            }
                        }

                        if (!mSession.isSuperHosted()) {
                            if (adSelected.getMaxParticipants()<=adSelected.getParticipantsTimestamps().size()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
                                builder.setMessage(R.string.max_number_of_participants_reached).setTitle(R.string.session_is_full);
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return;
                            }
                        }


                        // If the current user is not a participant, the mSession is not free and the user does not have a payment method, button will be gray,
                        // click will show dialog saying you need to have a payment method to book
                        if (!hasPaymentSystem && adSelected.getPrice()!=0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
                            builder.setMessage(R.string.you_need_a_payment_method_in_order_to_book_this_session).setTitle(R.string.booking_failed);
                            builder.setPositiveButton(R.string.add_payment_method, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent paymentPreferencesIntent = new Intent(DisplaySessionActivity.this,PaymentPreferencesActivity.class);
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

                            OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), mSession.getHost(), adSelected.getPrice(), true, mSession.getDurationInMin(), mSession.getSessionType(), adSelected.getParticipantsTimestamps().size(), mSession.isSuperHosted());
                        } else {
                            // mSession costs money, send customerId, price and if user has not clicked dont want to see booking text show the warning text
                            OnBookSession(adSelected.getAdvertisementId(), adSelected.getAdvertisementTimestamp(), mSession.getHost(), adSelected.getPrice(), currentUser.isDontShowBookingText(), mSession.getDurationInMin(), mSession.getSessionType(), adSelected.getParticipantsTimestamps().size(), mSession.isSuperHosted());
                        }
                    }
                    // If the current user is the mSession host, button will show cancel mSession, if clicked start cancellation process
                    if (mSession.getHost().equals(currentFirebaseUser.getUid())) {
                        OnCancelAdvertisement(mSession.getSessionName(), adSelected.getAdvertisementId(), mSession.getImageUrl(), adSelected.getSessionId(), adSelected.getAdvertisementTimestamp(), adSelected.getParticipantsTimestamps(), currentUser.getStripeAccountId(), adSelected.getPrice());
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
        holder.itemView.setBackgroundColor(getResources().getColor(R.color.color_background_light));
        holder.toggleBooked(false);
        holder.toggleFullyBooked(false);
        holder.toggleParticipantsTV(false);
        holder.toggleParticipantsLeftTV(false);
    }

    private void setAdListItemBookedAppearance(@NonNull AdvertisementRowViewHolder holder) {
        //holder.itemView.setBackgroundColor(getResources().getColor(R.color.color_background_light));
        holder.toggleBooked(true);
        holder.toggleFullyBooked(false);
        holder.participantsTV.setTextColor(getResources().getColor(R.color.foxmikePrimaryColor));
    }

    private void setAdListItemFullyBookedAppearance(@NonNull AdvertisementRowViewHolder holder) {
        //holder.itemView.setBackgroundColor(getResources().getColor(R.color.color_background_light));
        holder.toggleBooked(false);
        holder.toggleFullyBooked(true);
    }

    private void setAdListItemSelectedAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#F8F8FA"));
    }

    private void setAdListItemSelectedNoParticipantsAppearance(@NonNull AdvertisementRowViewHolder holder) {
        holder.itemView.setBackgroundColor(Color.parseColor("#F8F8FA"));
    }

    private void setPriceText() {
        String priceText;

        if (adSelected.getPrice()== 0) {
            priceText = getString(R.string.free);
        } else {
            priceText = PRICES_STRINGS.get(adSelected.getCurrency()).get(adSelected.getPrice())  + " " + getResources().getString(R.string.per_person);
        }
        priceTV.setText(priceText);
    }



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
                    CommentFragment commentFragment = CommentFragment.newInstance(sessionID, postID, heading, time, message, thumb_image, "mSession");
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                    transaction.replace(R.id.container_fullscreen_display_session, commentFragment).addToBackStack(CommentFragment.TAG).commit();
                }
            });
            nrOfCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommentFragment commentFragment = CommentFragment.newInstance(sessionID, postID, heading, time, message, thumb_image, "mSession");
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                    transaction.replace(R.id.container_fullscreen_display_session, commentFragment).addToBackStack(CommentFragment.TAG).commit();
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

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }

    private void setImage(String image, ImageView imageView) {
        Glide.with(DisplaySessionActivity.this).load(image).into(imageView);
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(DisplaySessionActivity.this, Locale.getDefault());

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

    public void alertDialogPositiveOrNegative(String title, String message, String positiveButton, String negativeButton, OnPositiveOrNegativeButtonPressedListener onPositiveOrNegativeButtonPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                onPositiveOrNegativeButtonPressedListener.OnPositivePressed();
            }
        });
        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onPositiveOrNegativeButtonPressedListener.OnNegativePressed();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }

    public interface OnOkPressedListener {
        void OnOkPressed();
    }

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside, OnOkPressedListener onOkPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onOkPressedListener.OnOkPressed();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BOOKING_CANCELED) {
            alertDialogOk(getResources().getString(R.string.payment_cancelled),
                    getResources().getString(R.string.booking_did_not_succeed), true,
                    new OnOkPressedListener() {
                        @Override
                        public void OnOkPressed() {

                        }
                    });

        }

        if (resultCode == CANCEL_ADVERTISEMENT_OK) {
            if (data!=null) {
                String date = data.getStringExtra("date");
                alertDialogOk(getResources().getString(R.string.cancellation_confirmed),
                        getResources().getString(R.string.the_session_on) + " " + date + " " + getResources().getString(R.string.has_now_been_cancelled), true,
                        new OnOkPressedListener() {
                            @Override
                            public void OnOkPressed() {

                            }
                        });
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == CANCEL_ADVERTISEMENT_REQUEST) {

            }
            if (requestCode == CANCEL_BOOKING_REQUEST) {
                getSupportFragmentManager().popBackStack("ad",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if (data!=null) {

                    if(data.getStringExtra("operationType")!=null) {
                        if (data.getStringExtra("operationType").equals("REFUND")) {
                            String refundAmount = data.getStringExtra("refundAmount");
                            String currency = data.getStringExtra("currency");
                            alertDialogOk(getString(R.string.cancellation_confirmation),
                                    getString(R.string.your_cancellation_has_been_confirmed) + refundAmount + " " + Price.CURRENCIES.get(currency) + getString(R.string.will_be_refunded_to_your_account), true,
                                    new OnOkPressedListener() {
                                        @Override
                                        public void OnOkPressed() {

                                        }
                                    });
                            return;
                        }
                    }
                    if (data.getBooleanExtra("superHosted", false)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
                        builder.setMessage(getResources().getString(R.string.session_removed_from_bookings));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    }


                }
                alertDialogOk(getString(R.string.cancellation_confirmation), getString(R.string.cancelled_free_session),
                        true,new OnOkPressedListener() {
                            @Override
                            public void OnOkPressed() {

                            }
                        });

            }
            if (requestCode == BOOK_SESSION_REQUEST) {
                if (data!=null) {
                    if (data.getBooleanExtra("superHosted", false)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
                        builder.setMessage(getResources().getString(R.string.session_added_to_your_bookings));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
                builder.setMessage(R.string.booking_confirmed);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private void addAdvertisements(String sessionID) {
        if (hasDeposition) {
            Intent createOrEditSession = new Intent(DisplaySessionActivity.this, CreateOrEditSessionActivity.class);
            createOrEditSession.putExtra("sessionID", sessionID);
            createOrEditSession.putExtra("addAdvertisements", true);
            startActivity(createOrEditSession);
        } else {
            alertDialogPositiveOrNegative(getResources().getString(R.string.deposition), getResources().getString(R.string.deposition_needed), getResources().getString(R.string.go_to_deposition), getResources().getString(R.string.cancel), new OnPositiveOrNegativeButtonPressedListener() {
                @Override
                public void OnPositivePressed() {
                    Intent depositionIntent = new Intent(DisplaySessionActivity.this, DepositionActivity.class);
                    startActivity(depositionIntent);
                }

                @Override
                public void OnNegativePressed() {

                }
            });
        }
    }

    public void OnEditSession(String sessionID) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("sessionID", sessionID);
        startActivity(createOrEditSession);
    }

    public void OnEditSession(String sessionID, String type) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("sessionID", sessionID);
        createOrEditSession.putExtra("type", type);
        startActivity(createOrEditSession);
    }

    public void OnEditSession(String sessionID, Session session) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("session", session);
        createOrEditSession.putExtra("sessionID", sessionID);
        startActivity(createOrEditSession);
    }

    public void OnEditSession(String sessionID, Session session, String type) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("session", session);
        createOrEditSession.putExtra("sessionID", sessionID);
        createOrEditSession.putExtra("type", type);
        startActivity(createOrEditSession);
    }

    public void OnCancelBookedSession(Long bookingTimestamp, Long advertisementTimestamp, String advertisementId, String participantId, int adPrice, String hostId, boolean superHosted) {
        Intent cancelIntent = new Intent(DisplaySessionActivity.this,CancelBookingActivity.class);
        cancelIntent.putExtra("bookingTimestamp", bookingTimestamp);
        cancelIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
        cancelIntent.putExtra("advertisementId", advertisementId);
        cancelIntent.putExtra("participantId",participantId);
        cancelIntent.putExtra("adPrice", adPrice);
        cancelIntent.putExtra("hostId",hostId);
        cancelIntent.putExtra("superHosted",superHosted);
        startActivityForResult(cancelIntent, CANCEL_BOOKING_REQUEST);
    }

    public void OnBookSession(String advertisementId, Long advertisementTimestamp, String hostId, int amount, boolean dontShowBookingText, int advertisementDurationInMin, String sessionType, int currentNrOfParticipants, boolean superHosted) {
        // If the users dontShowBookingText is false we should show the booking textin a dialog, a warning text explaining the payment policy
        if (!dontShowBookingText) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            AlertDialog.Builder builder = new AlertDialog.Builder(DisplaySessionActivity.this);
            LayoutInflater inflater = DisplaySessionActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_booking_dialog,null);
            AppCompatCheckBox showAgainCheckbox = view.findViewById(R.id.doNotShowAgainCheckbox);
            builder.setView(view)
                    // When the button "Book session" in the dialog is pressed
                    .setPositiveButton(R.string.book_session, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // If the user does not want to see the booking text again, save this to the database
                            if (showAgainCheckbox.isChecked()) {
                                rootDbRef.child("users").child(userId).child("dontShowBookingText").setValue(true);
                            }
                            // send all the info to the booking activity and start that activity and pick up the result in onActivityResult (another method further down)
                            Intent bookIntent = new Intent(DisplaySessionActivity.this,BookingActivity.class);
                            bookIntent.putExtra("advertisementId", advertisementId);
                            bookIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
                            bookIntent.putExtra("hostId", hostId);
                            bookIntent.putExtra("amount",amount);
                            bookIntent.putExtra("advertisementDurationInMin",advertisementDurationInMin);
                            bookIntent.putExtra("sessionType",sessionType);
                            bookIntent.putExtra("currentNrOfParticipants",currentNrOfParticipants);
                            bookIntent.putExtra("superHosted",superHosted);
                            startActivityForResult(bookIntent, BOOK_SESSION_REQUEST);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // cancels the action
                }
            }).setMessage(R.string.booking_text_policy).setTitle(R.string.confirm_booking);
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        // send all the info to the booking activity and start that activity and pick up the result in onActivityResult (another method further down)
        Intent bookIntent = new Intent(DisplaySessionActivity.this,BookingActivity.class);
        bookIntent.putExtra("advertisementId", advertisementId);
        bookIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
        bookIntent.putExtra("hostId", hostId);
        bookIntent.putExtra("amount",amount);
        bookIntent.putExtra("advertisementDurationInMin",advertisementDurationInMin);
        bookIntent.putExtra("sessionType",sessionType);
        bookIntent.putExtra("currentNrOfParticipants",currentNrOfParticipants);
        bookIntent.putExtra("superHosted",superHosted);
        startActivityForResult(bookIntent, BOOK_SESSION_REQUEST);

    }

    public void OnCancelAdvertisement(String advertisementName, String advertisementId, String imageUrl, String sessionId, Long advertisementTimestamp, HashMap<String, Long> participantsTimestamps, String accountId, int price) {
        Intent cancelAdIntent = new Intent(DisplaySessionActivity.this,CancelAdvertisementActivity.class);
        cancelAdIntent.putExtra("sessionName", advertisementName);
        cancelAdIntent.putExtra("advertisementId", advertisementId);
        cancelAdIntent.putExtra("imageUrl", imageUrl);
        cancelAdIntent.putExtra("sessionId", sessionId);
        cancelAdIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
        cancelAdIntent.putExtra("participantsTimestamps", participantsTimestamps);
        cancelAdIntent.putExtra("accountId",accountId);
        cancelAdIntent.putExtra("price",price);
        startActivityForResult(cancelAdIntent, CANCEL_ADVERTISEMENT_REQUEST);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        playVideoIcon.setVisibility(View.VISIBLE);
        playVideoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideoIcon.setVisibility(View.GONE);
                videoView.start();
            }
        });
    }

    public interface OnHistoryAdClickedListener {
        void OnHistoryAdClicked(Advertisement adClicked);
    }
}
