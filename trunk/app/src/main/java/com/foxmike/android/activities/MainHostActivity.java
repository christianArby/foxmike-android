package com.foxmike.android.activities;
//Checked

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.ChatFragment;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.fragments.HostSessionsFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.NotificationsFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserAccountHostFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.foxmike.android.fragments.WriteReviewsFragment;
import com.foxmike.android.interfaces.AdvertisementListener;
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
import java.util.Locale;

import static com.foxmike.android.activities.MainPlayerActivity.CANCEL_ADVERTISEMENT_REQUEST;

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
        AdvertisementListener,
        NotificationsFragment.OnNotificationClickedListener,
        AlertOccasionCancelledListener{

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

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

        // Create instances of fragments

        // Add fragments to container and hide them

        Locale current = getResources().getConfiguration().locale;
        DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(current.toString());
        sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sessionTypeDictionary = new HashMap<>();
                for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                    sessionTypeDictionary.put(sessionTypeSnap.getKey(), sessionTypeSnap.getValue().toString());
                }

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
                        if (!wasSelected)
                            mainPager.setCurrentItem(position, false);
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
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (snapshot.getValue()==null) {
                                return;
                            }
                            Long currentTimestamp = System.currentTimeMillis();
                            Long reviewTimestamp = (Long)snapshot.getValue();
                            if (reviewTimestamp<currentTimestamp) {
                                presentReview(snapshot.getKey());
                            } else {
                                if (!countDownTimerHashMap.containsValue(snapshot.getKey())) {


                                    CountDownTimer reviewPending = new CountDownTimer(reviewTimestamp-currentTimestamp, reviewTimestamp-currentTimestamp) {
                                        @Override
                                        public void onTick(long l) {

                                        }
                                        @Override
                                        public void onFinish() {
                                            presentReview(countDownTimerHashMap.get(this));
                                            countDownTimerHashMap.remove(this);
                                        }
                                    }.start();
                                    countDownTimerHashMap.put(reviewPending,snapshot.getKey());
                                }
                            }
                        }
                    }
                });
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

    private void presentReview(String advertisementId) {
        WriteReviewsFragment writeReviewsFragment = WriteReviewsFragment.newInstance(advertisementId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        writeReviewsFragment.show(transaction,advertisementId);
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
            transaction.replace(R.id.container_fullscreen_main_host, fragment).commit();
        }
    }

    @Override
    public void OnSessionClicked(String sessionId) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(sessionId, sessionTypeDictionary);
        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true, "");
    }

    @Override
    public void OnSessionClicked(String sessionId, Long representingAdTimestamp) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(sessionId, representingAdTimestamp, sessionTypeDictionary);
        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
    }

    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            UserProfileFragment hostUserProfileFragment = UserProfileFragment.newInstance();
            cleanMainFullscreenActivityAndSwitch(hostUserProfileFragment,true,"");
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
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        startActivity(createOrEditSession);
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
    public void addAdvertisements(String sessionID) {
        Intent createOrEditSession = new Intent(this, CreateOrEditSessionActivity.class);
        createOrEditSession.putExtra("sessionID", sessionID);
        createOrEditSession.putExtra("addAdvertisements", true);
        startActivity(createOrEditSession);
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
    public void OnCancelBookedSession(Long bookingTimestamp, Long advertisementTimestamp, String advertisementId, String participantId, int adPrice, String hostId) {
        // Not applicable in Host environment
    }

    @Override
    public void OnBookSession(String advertisementId, Long advertisementTimestamp, String hostId, int amount, boolean dontShowBookingText, int advertisementDurationInMin) {
        // Not applicable in Host environment
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
    public void OnCancelAdvertisement(String advertisementName, String advertisementId, String imageUrl, String sessionId, Long advertisementTimestamp, HashMap<String, Long> participantsTimestamps, String accountId) {
        Intent cancelAdIntent = new Intent(MainHostActivity.this, CancelAdvertisementActivity.class);
        cancelAdIntent.putExtra("sessionName", advertisementName);
        cancelAdIntent.putExtra("advertisementId", advertisementId);
        cancelAdIntent.putExtra("imageUrl", imageUrl);
        cancelAdIntent.putExtra("sessionId", sessionId);
        cancelAdIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
        cancelAdIntent.putExtra("participantsTimestamps", participantsTimestamps);
        cancelAdIntent.putExtra("accountId",accountId);
        startActivityForResult(cancelAdIntent, CANCEL_ADVERTISEMENT_REQUEST);

    }

    @Override
    public void OnNotificationClicked(FoxmikeNotification foxmikeNotification) {
        if (foxmikeNotification.getType().equals("sessionPost")) {
            DisplaySessionFragment hostDisplaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP1(), sessionTypeDictionary);
            cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("sessionPostComment")) {
            DisplaySessionFragment hostDisplaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary);
            cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("participantNew")) {
            DisplaySessionFragment hostDisplaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary);
            cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("participantCancellation")) {
            DisplaySessionFragment hostDisplaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary);
            cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("friendRequestAccepted")) {
            InboxFragment inboxFragment = (InboxFragment) bottomNavigationAdapter.getRegisteredFragment(0);
            inboxFragment.setPage(1);
        }
        if (foxmikeNotification.getType().equals("sessionCancellation") || foxmikeNotification.getType().equals("freeSessionCancellation")) {
            Toast.makeText(MainHostActivity.this, R.string.not_possible_in_trainer_mode, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void AlertOccasionCancelled(boolean free, String sessionId) {

    }
}