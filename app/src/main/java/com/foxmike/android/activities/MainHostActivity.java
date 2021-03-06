package com.foxmike.android.activities;
//Checked

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.crashlytics.android.Crashlytics;
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.ChatFragment;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.HostSessionsFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.NotificationsFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserAccountHostFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.foxmike.android.fragments.WriteReviewsFlagTrainerFragment;
import com.foxmike.android.fragments.WriteReviewsFlagTrainerWriteReportFragment;
import com.foxmike.android.fragments.WriteReviewsFragment;
import com.foxmike.android.interfaces.AlertOccasionCancelledListener;
import com.foxmike.android.interfaces.OnAdvertisementsFoundListener;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnCreateSessionClickedListener;
import com.foxmike.android.interfaces.OnNewMessageListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.FoxmikeNotification;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionTypeDictionary;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainHostActivity extends AppCompatActivity implements
        OnSessionClickedListener,
        UserAccountHostFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        OnUserClickedListener,
        OnNewMessageListener,
        SessionListener,
        OnChatClickedListener,
        OnCommentClickedListener,
        InboxFragment.OnSearchClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        OnCreateSessionClickedListener,
        OnAdvertisementsFoundListener,
        NotificationsFragment.OnNotificationClickedListener,
        AlertOccasionCancelledListener, WriteReviewsFragment.OnWriteReviewsFragmentInteractionListener, WriteReviewsFlagTrainerFragment.OnWriteReviewsFlagTrainerFragmentInteractionListener, WriteReviewsFlagTrainerWriteReportFragment.OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener {

    private FragmentManager fragmentManager;
    private AHBottomNavigation bottomNavigation;
    private DatabaseReference userDbRef;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private Session editedSession;
    private String editedSessionID;
    private boolean resumed = false;
    private AHBottomNavigationViewPager mainPager;
    private BottomNavigationAdapter bottomNavigationAdapter;
    private int unreadChats = 0;
    private int unreadNotifications = 0;
    private int unreadFriendRequests = 0;
    private long mLastClickTime = 0;
    private String currentUserId;
    private HashMap<CountDownTimer, String> countDownTimerHashMap = new HashMap<>();
    private HashMap<String, String> sessionTypeDictionary;
    private DataSnapshot reviewsToWrite;
    private boolean hasDeposition;
    public static final int CANCEL_ADVERTISEMENT_OK = 901;
    private int prevTab;
    private HashMap<String, Boolean> reviewsPresented = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().getUid());

        setContentView(R.layout.activity_main_host);

        bottomNavigation = findViewById(R.id.bottom_navigation_host);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        fragmentManager = getSupportFragmentManager();

        // Setup bottom navigation
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_host_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.primaryTextColor));
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.foxmikePrimaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        mainPager = findViewById(R.id.mainPager);
        mainPager.setPagingEnabled(false);
        mainPager.setOffscreenPageLimit(3);
        bottomNavigationAdapter = new BottomNavigationAdapter(fragmentManager);

        FirebaseDatabaseViewModel stripeDepositionPaymentIntentIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDepositionPaymentIntentIdLiveData = stripeDepositionPaymentIntentIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDepositionPaymentIntentId"));
        stripeDepositionPaymentIntentIdLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                hasDeposition = dataSnapshot.getValue() != null;
            }
        });

        // Create instances of fragments

        // Add fragments to container and hide them

        String language = getResources().getConfiguration().locale.getLanguage();
        DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(language);
        sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sessionTypeDictionary = new SessionTypeDictionary(getResources().getString(R.string.other));
                if (dataSnapshot.getValue()!=null) {
                    for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                        sessionTypeDictionary.put(sessionTypeSnap.getKey(), sessionTypeSnap.getValue().toString());
                    }
                    setupUI();
                } else {
                    DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child("en");
                    sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                                sessionTypeDictionary.put(sessionTypeSnap.getKey(), sessionTypeSnap.getValue().toString());
                            }
                            setupUI();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        // --------------------------  LISTEN TO CHATS -------------------------------------
        // Check if there are unread chatmessages and if so set inboxNotifications to the bottom navigation bar

        FirebaseDatabaseViewModel userChatsUserIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> liveData = userChatsUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("userChats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        liveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                unreadChats = 0;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot chatID: dataSnapshot.getChildren()) {
                        Boolean read = (Boolean) chatID.getValue();
                        if (!read) {
                            unreadChats++;
                        }
                    }
                }
                if ((unreadChats + unreadNotifications + unreadFriendRequests) >0) {
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),0);
                } else {
                    bottomNavigation.setNotification("",0);
                }
            }
        });

        FirebaseDatabaseViewModel unreadNotificationsUserIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> unreadNotificationsliveData = unreadNotificationsUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        unreadNotificationsliveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                unreadNotifications = 0;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        unreadNotifications++;
                    }
                }
                if ((unreadChats + unreadNotifications + unreadFriendRequests) >0) {
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),0);
                } else {
                    bottomNavigation.setNotification("",0);
                }
            }
        });

        FirebaseDatabaseViewModel firebaseDatabaseFriendRequestViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseFriendRequestLiveData = firebaseDatabaseFriendRequestViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("friend_requests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        firebaseDatabaseFriendRequestLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                unreadFriendRequests = 0;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        if (!child.child("request_type").getValue().toString().equals("sent")) {
                            unreadFriendRequests++;
                        }
                    }
                }
                if ((unreadChats + unreadNotifications + unreadFriendRequests) >0) {
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),0);
                } else {
                    bottomNavigation.setNotification("",0);
                }
            }
        });



        // check if maintenance
        MaintenanceViewModel maintenanceViewModel = ViewModelProviders.of(this).get(MaintenanceViewModel.class);
        LiveData<DataSnapshot> maintenanceLiveData = maintenanceViewModel.getDataSnapshotLiveData();
        maintenanceLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if ((boolean) dataSnapshot.getValue()) {
                        Intent welcomeIntent = new Intent(MainHostActivity.this,WelcomeActivity.class);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(welcomeIntent);
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            }
        });
    }

    private void setupUI() {
        InboxFragment hostInboxFragment = InboxFragment.newInstance();
        bottomNavigationAdapter.addFragments(hostInboxFragment);

        HostSessionsFragment hostSessionsFragment = HostSessionsFragment.newInstance(sessionTypeDictionary);
        bottomNavigationAdapter.addFragments(hostSessionsFragment);

        UserAccountHostFragment hostUserAccountFragment = UserAccountHostFragment.newInstance();
        bottomNavigationAdapter.addFragments(hostUserAccountFragment);

        mainPager.setAdapter(bottomNavigationAdapter);

        // Listen to bottom navigation and switch to corresponding fragment
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                checkReviews();
                if (!wasSelected)
                    mainPager.setCurrentItem(position, false);
                if (position==0) {
                    FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
                }
                prevTab = position;
                return true;
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (hostUserAccountFragment.isVisible() | hostSessionsFragment.isVisible() | hostInboxFragment.isVisible()){
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            }
        });

        FirebaseDatabaseViewModel reviewsToWriteUserIdViewModel = ViewModelProviders.of(MainHostActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> reviewsToWriteLiveData = reviewsToWriteUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        reviewsToWriteLiveData.observe(MainHostActivity.this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    return;
                }
                reviewsToWrite = dataSnapshot;
            }
        });
    }

    private void checkReviews() {
        if (reviewsToWrite!=null) {
            for (DataSnapshot snapshot: reviewsToWrite.getChildren()) {
                if (snapshot.getValue()==null) {
                    return;
                }
                Long currentTimestamp = System.currentTimeMillis();
                Long reviewTimestamp = (Long)snapshot.getValue();
                if (reviewTimestamp<currentTimestamp) {
                    presentReview(snapshot.getKey());
                }
            }
        }
    }

    private void presentReview(String advertisementId) {

        if (advertisementId!=null) {
            FirebaseDatabaseViewModel reviewsToWriteUserIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> reviewsToWriteLiveData = reviewsToWriteUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(advertisementId));
            reviewsToWriteLiveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }

                    rootDbRef.child("advertisements").child(advertisementId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue()==null) {
                                return;
                            }
                            Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                            if (advertisement.getStatus().equals("cancelled")) {
                                FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(advertisement.getAdvertisementId()).removeValue();
                            } else {
                                if (!reviewsPresented.containsKey(advertisementId)) {
                                    reviewsPresented.put(advertisementId, true);
                                    WriteReviewsFragment writeReviewsFragment = WriteReviewsFragment.newInstance(advertisement);
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                    writeReviewsFragment.show(transaction,advertisementId);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }

    /* Method to hide all fragments in main container and fill the other container with fullscreen fragment */
    private void cleanMainFullscreenActivityAndSwitch(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        if (addToBackStack) {
            if (!tag.equals("")) {
                transaction.replace(R.id.container_fullscreen_main_host, fragment).addToBackStack(tag).commit();
            } else {
                transaction.replace(R.id.container_fullscreen_main_host, fragment).addToBackStack(null).commit();
            }
        } else {
            if (!tag.equals("")) {
                transaction.replace(R.id.container_fullscreen_main_host, fragment, tag).commit();
            } else {
                transaction.replace(R.id.container_fullscreen_main_host, fragment).commit();
            }
        }
    }

    @Override
    public void OnSessionClicked(String sessionId) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
        displaySession.putExtra("sessionID", sessionId);
        displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
        displaySession.putExtra("trainerMode",true);
        startActivity(displaySession);
    }

    @Override
    public void OnSessionClicked(String sessionId, Long representingAdTimestamp) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
        displaySession.putExtra("sessionID", sessionId);
        displaySession.putExtra("representingAdTimestamp", representingAdTimestamp);
        displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
        displaySession.putExtra("trainerMode",true);
        startActivity(displaySession);
    }

    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            UserProfileFragment hostUserProfileFragment = UserProfileFragment.newInstance();
            cleanMainFullscreenActivityAndSwitch(hostUserProfileFragment,true,"");
        }
        if (type.equals("DEPOSITION")) {
            Intent depositionIntent = new Intent(this, DepositionActivity.class);
            startActivity(depositionIntent);
        }
    }

    /* Listener, when edit "button" in user profile is clicked show edit user profile */
    @Override
    public void onUserProfileFragmentInteraction() {
        UserProfilePublicEditFragment hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicEditFragment,true,"");
    }
    /* Listener, when finished editing restart this activity */
    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        getSupportFragmentManager().popBackStack();
    }
    /* Listener, when a user is clicked, get the others user ID and start User profile fragment */
    @Override
    public void OnUserClicked(String otherUserID) {
        Bundle bundle = new Bundle();
        bundle.putString("otherUserID", otherUserID);
        UserProfilePublicFragment hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
        hostUserProfilePublicFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicFragment, true,"");
    }

    @Override
    public void OnNewMessage() {
        // TODO should this be used?
    }

    @Override
    public void OnCreateSessionClicked() {
        if (hasDeposition) {
            Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
            startActivity(createOrEditSession);
        } else {
            alertDialogPositiveOrNegative(getResources().getString(R.string.deposition), getResources().getString(R.string.deposition_needed), getResources().getString(R.string.go_to_deposition), getResources().getString(R.string.cancel), new OnPositiveOrNegativeButtonPressedListener() {
                @Override
                public void OnPositivePressed() {
                    Intent depositionIntent = new Intent(MainHostActivity.this, DepositionActivity.class);
                    startActivity(depositionIntent);
                }

                @Override
                public void OnNegativePressed() {

                }
            });
        }

    }


    @Override
    public void OnEditSession(String sessionID) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("sessionID", sessionID);
        startActivity(createOrEditSession);
    }

    @Override
    public void OnEditSession(String sessionID, String type) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("sessionID", sessionID);
        createOrEditSession.putExtra("type", type);
        startActivity(createOrEditSession);
    }

    @Override
    public void OnEditSession(String sessionID, Session session) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("session", session);
        createOrEditSession.putExtra("sessionID", sessionID);
        startActivity(createOrEditSession);
    }

    @Override
    public void OnEditSession(String sessionID, Session session, String type) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("session", session);
        createOrEditSession.putExtra("sessionID", sessionID);
        createOrEditSession.putExtra("type", type);
        startActivity(createOrEditSession);
    }

    @Override
    public void OnCancelBookedSession(Long bookingTimestamp, Long advertisementTimestamp, String advertisementId, String participantId, int adPrice, String hostId, boolean superHosted) {
        // Not applicable in Host environment

    }

    @Override
    public void OnBookSession(String advertisementId, Long advertisementTimestamp, String hostId, int amount, boolean dontShowBookingText, int advertisementDurationInMin, String sessionType, int currentNrOfParticipants, boolean superHosted) {
        // Not applicable in Host environment

    }

    @Override
    public void addAdvertisements(String sessionID) {
        if (hasDeposition) {
            Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
            createOrEditSession.putExtra("sessionID", sessionID);
            createOrEditSession.putExtra("addAdvertisements", true);
            startActivity(createOrEditSession);
        } else {
            alertDialogPositiveOrNegative(getResources().getString(R.string.deposition), getResources().getString(R.string.deposition_needed), getResources().getString(R.string.go_to_deposition), getResources().getString(R.string.cancel), new OnPositiveOrNegativeButtonPressedListener() {
                @Override
                public void OnPositivePressed() {
                    Intent depositionIntent = new Intent(MainHostActivity.this, DepositionActivity.class);
                    startActivity(depositionIntent);
                }

                @Override
                public void OnNegativePressed() {

                }
            });
        }
    }
    /* When back button is pressed while in main host activity override function and replace with following */
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {

            getSupportFragmentManager().popBackStack();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (CountDownTimer countDownTimer: countDownTimerHashMap.keySet()) {
            countDownTimer.cancel();
        }
    }

    // Before starting activity, make sure user is signed-in, otherwise start login activity
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null) {
            //User is signed out
            Intent welcomeIntent = new Intent(MainHostActivity.this, WelcomeActivity.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(welcomeIntent);
            finish();
        }
        for (CountDownTimer countDownTimer: countDownTimerHashMap.keySet()) {
            countDownTimer.start();
        }
    }

    @Override
    public void OnDismissDisplaySession() {
        getSupportFragmentManager().popBackStack();
    }


    @Override
    public void OnChatClicked(String userID, String userName, String userThumbImage, String chatID) {
        ChatFragment chatFragment = ChatFragment.newInstance(userID,userName,userThumbImage,chatID);
        cleanMainFullscreenActivityAndSwitch(chatFragment,true,"");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            hideKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnCommentClicked(String sourceID, String postID, String heading, String time, String message, String thumb_image, String wallType) {
        CommentFragment commentFragment = CommentFragment.newInstance(sourceID, postID, heading, time, message, thumb_image, wallType);
        cleanMainFullscreenActivityAndSwitch(commentFragment,true,"");
    }

    @Override
    public void OnSearchClicked() {
        AllUsersFragment allUsersFragment = AllUsersFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(allUsersFragment,true,"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        checkReviews();
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void OnAdvertisementsFound(ArrayList<Advertisement> advertisements) {

    }

    @Override
    public void OnNotificationClicked(FoxmikeNotification foxmikeNotification) {
        if (foxmikeNotification.getType().equals("sessionPost")) {
            Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
            displaySession.putExtra("sessionID", foxmikeNotification.getP1());
            displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
            displaySession.putExtra("trainerMode",true);
            startActivity(displaySession);
        }
        if (foxmikeNotification.getType().equals("sessionPostComment")) {
            Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
            displaySession.putExtra("sessionID", foxmikeNotification.getP2());
            displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
            displaySession.putExtra("trainerMode",true);
            startActivity(displaySession);
        }
        if (foxmikeNotification.getType().equals("participantNew")) {
            Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
            displaySession.putExtra("sessionID", foxmikeNotification.getP2());
            displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
            displaySession.putExtra("trainerMode",true);
            startActivity(displaySession);
        }
        if (foxmikeNotification.getType().equals("participantCancellation")) {
            Intent displaySession = new Intent(MainHostActivity.this,DisplaySessionActivity.class);
            displaySession.putExtra("sessionID", foxmikeNotification.getP2());
            displaySession.putExtra("sessionTypeDictionary", sessionTypeDictionary);
            displaySession.putExtra("trainerMode",true);
            startActivity(displaySession);
        }
        if (foxmikeNotification.getType().equals("friendRequestAccepted")) {
            InboxFragment inboxFragment = (InboxFragment) bottomNavigationAdapter.getRegisteredFragment(0);
            inboxFragment.setPage(2);
        }
        if (foxmikeNotification.getType().equals("sessionCancellation") || foxmikeNotification.getType().equals("freeSessionCancellation")) {
            Toast.makeText(MainHostActivity.this, R.string.not_possible_in_trainer_mode, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void AlertOccasionCancelled(boolean free, String sessionId) {

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

    @Override
    public void onReportTrainer(Session session, Advertisement advertisement, UserPublic host, String ratingAndReviewId) {
        WriteReviewsFlagTrainerFragment writeReviewsFlagTrainerFragment = WriteReviewsFlagTrainerFragment.newInstance(session, advertisement, host, ratingAndReviewId);
        cleanMainFullscreenActivityAndSwitch(writeReviewsFlagTrainerFragment, false, WriteReviewsFlagTrainerFragment.TAG);
    }

    @Override
    public void onFinishedWriteReviewsFlagTrainer() {
        Fragment fragment = fragmentManager.findFragmentByTag(WriteReviewsFlagTrainerFragment.TAG);
        if (fragment!=null) {
            FragmentTransaction removeTransaction = fragmentManager.beginTransaction();
            removeTransaction.remove(fragment).commit();
        }
    }

    @Override
    public void onWriteCustomTextReason(Session session, Advertisement advertisement, UserPublic host, String ratingAndReviewId) {
        WriteReviewsFlagTrainerWriteReportFragment writeReviewsFlagTrainerWriteReportFragment = WriteReviewsFlagTrainerWriteReportFragment.newInstance(session, advertisement, host, ratingAndReviewId);
        cleanMainFullscreenActivityAndSwitch(writeReviewsFlagTrainerWriteReportFragment, false, WriteReviewsFlagTrainerWriteReportFragment.TAG);
    }

    @Override
    public void onWriteReportFinished() {
        Fragment fragment = fragmentManager.findFragmentByTag(WriteReviewsFlagTrainerWriteReportFragment.TAG);
        if (fragment!=null) {
            FragmentTransaction removeTransaction = fragmentManager.beginTransaction();
            removeTransaction.remove(fragment).commit();
        }
    }

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CANCEL_ADVERTISEMENT_OK) {
            if (data!=null) {
                String date = data.getStringExtra("date");
                alertDialogOk(getResources().getString(R.string.cancellation_confirmed),
                        getResources().getString(R.string.the_session_on) + " " + date + " " + getResources().getString(R.string.has_now_been_cancelled), true);
            }
        }
    }

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();
    }
}