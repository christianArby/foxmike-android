package com.foxmike.android.activities;
//Checked

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.ChatFragment;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.fragments.ExploreFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.ListSessionsFragment;
import com.foxmike.android.fragments.NotificationsFragment;
import com.foxmike.android.fragments.PlayerSessionsFragment;
import com.foxmike.android.fragments.SortAndFilterFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserAccountHostFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.foxmike.android.fragments.WriteReviewsFlagTrainerFragment;
import com.foxmike.android.fragments.WriteReviewsFlagTrainerWriteReportFragment;
import com.foxmike.android.fragments.WriteReviewsFragment;
import com.foxmike.android.interfaces.AdvertisementListener;
import com.foxmike.android.interfaces.AlertOccasionCancelledListener;
import com.foxmike.android.interfaces.OnAdvertisementsFoundListener;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnNewMessageListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.interfaces.SessionListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.FoxmikeNotification;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionTypeDictionary;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.Price;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import io.reactivex.subjects.BehaviorSubject;

import static android.content.ContentValues.TAG;

public class MainPlayerActivity extends AppCompatActivity

        implements
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserAccountHostFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        OnNewMessageListener,
        OnUserClickedListener,
        SessionListener,
        OnChatClickedListener,
        OnCommentClickedListener,
        InboxFragment.OnSearchClickedListener,
        OnAdvertisementsFoundListener,
        AdvertisementListener,
        ListSessionsFragment.OnRefreshSessionsListener,
        ListSessionsFragment.OnListSessionsScrollListener,
        NotificationsFragment.OnNotificationClickedListener,
        SortAndFilterFragment.OnFilterChangedListener,
        AlertOccasionCancelledListener,
        ListSessionsFragment.OnDimCurrentDayListener, ListSessionsFragment.OnFragmentAttachedNeedNewDataListener,
        WriteReviewsFragment.OnWriteReviewsFragmentInteractionListener, WriteReviewsFlagTrainerFragment.OnWriteReviewsFlagTrainerFragmentInteractionListener, WriteReviewsFlagTrainerWriteReportFragment.OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener {

    private FragmentManager fragmentManager;

    private String fromUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private boolean resumed = false;
    private FirebaseFunctions mFunctions;
    private View mainView;
    static final int BOOK_SESSION_REQUEST = 8;
    static final int CANCEL_BOOKING_REQUEST = 16;
    static final int CANCEL_ADVERTISEMENT_REQUEST = 24;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9;
    public static final int BOOKING_CANCELED = 900;
    public static final int CANCEL_ADVERTISEMENT_OK = 901;
    private String stripeCustomerId;
    private AHBottomNavigation bottomNavigation;
    private AHBottomNavigationViewPager mainPager;
    private BottomNavigationAdapter bottomNavigationAdapter;
    private int unreadChats = 0;
    private int unreadNotifications = 0;
    private int unreadFriendRequests = 0;
    // variable to track event time
    private long mLastClickTime = 0;


    private HashMap<CountDownTimer, String> countDownTimerHashMap = new HashMap<>();
    private DataSnapshot reviewsToWrite;
    private String stripeDefaultPaymentMethodId;

    // rxJava
    public final BehaviorSubject<HashMap> subject = BehaviorSubject.create();
    public void setStripeDefaultSource(HashMap value) { subject.onNext(value);     }
    public HashMap  getStripeDefaultSource()          { return subject.getValue(); }

    // rxJava
    public final BehaviorSubject<HashMap> paymentMethodSubject = BehaviorSubject.create();
    public void setPaymentMethod(HashMap paymentMethodMap) { paymentMethodSubject.onNext(paymentMethodMap);     }
    public HashMap  getPaymentMethod()          { return paymentMethodSubject.getValue(); }


    private int prevTab;
    private HashMap<String, String> sessionTypeDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        setContentView(R.layout.activity_main_player);
        ButterKnife.bind(this);

        setStripeDefaultSource(new HashMap());
        setPaymentMethod(new HashMap());
        mainView = findViewById(R.id.activity_main_player);
        mFunctions = FirebaseFunctions.getInstance();

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        fragmentManager = getSupportFragmentManager();

        // get views
        bottomNavigation = findViewById(R.id.bottom_navigation_player);

        // get Firebase instances and references
        mAuth = FirebaseAuth.getInstance();

        /** If friend request is sent by notification, get it through intentExtra. */
        fromUserID = getIntent().getStringExtra("notificationRequest");

        /** Setup bottom navigation */
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_player_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.primaryTextColor));
        bottomNavigation.setAccentColor(getResources().getColor(R.color.foxmikePrimaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        mainPager = findViewById(R.id.mainPager);
        mainPager.setPagingEnabled(false);
        mainPager.setOffscreenPageLimit(4);
        bottomNavigationAdapter = new BottomNavigationAdapter(fragmentManager);

        /** Setup fragments */

        sessionTypeDictionary = new HashMap<>();

        // --------------------------  SETUP LISTENER TO STRIPE CUSTOMER -------------------------------------
        FirebaseDatabaseViewModel stripeDefaultPaymentMethodViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDefaultPaymentMethodLiveData = stripeDefaultPaymentMethodViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
        stripeDefaultPaymentMethodLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
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

        /*rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    stripeDefaultPaymentMethodId = dataSnapshot.getValue().toString();
                    updateStripeCustomerInfo();
                }

                FirebaseDatabaseViewModel stripeLastChangeViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
                LiveData<DataSnapshot> StripeLastChangeLiveData = stripeLastChangeViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
                StripeLastChangeLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
                    @Override
                    public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                        if (stripeDefaultPaymentMethodId==null) {

                            FirebaseDatabaseViewModel stripeCustomerViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
                            LiveData<DataSnapshot> stripeCustomerLiveData = stripeCustomerViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
                            stripeCustomerLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
                                @Override
                                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()!=null) {
                                        stripeDefaultPaymentMethodId = dataSnapshot.getValue().toString();
                                        updateStripeCustomerInfo();
                                    }

                                }
                            });
                        } else {
                            updateStripeCustomerInfo();
                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        // --------------------------  LISTEN TO CHATS -------------------------------------
        // Check if there are unread chatmessages and if so set inboxNotifications to the bottom navigation bar

        FirebaseDatabaseViewModel userChatsUserIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> liveData = userChatsUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("userChats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        liveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
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
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),2);
                } else {
                    bottomNavigation.setNotification("",2);
                }
            }
        });



        FirebaseDatabaseViewModel unreadNotificationsUserIdViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> unreadNotificationsliveData = unreadNotificationsUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        unreadNotificationsliveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                unreadNotifications = 0;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        unreadNotifications++;
                    }
                }
                if ((unreadChats + unreadNotifications + unreadFriendRequests) >0) {
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),2);
                } else {
                    bottomNavigation.setNotification("",2);
                }

            }
        });

        FirebaseDatabaseViewModel firebaseDatabaseFriendRequestViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseFriendRequestLiveData = firebaseDatabaseFriendRequestViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("friend_requests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        firebaseDatabaseFriendRequestLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
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
                    bottomNavigation.setNotification(Integer.toString(unreadChats + unreadNotifications + unreadFriendRequests),2);
                } else {
                    bottomNavigation.setNotification("",2);
                }
            }
        });

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

        // check if maintenance
        MaintenanceViewModel maintenanceViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(MaintenanceViewModel.class);
        LiveData<DataSnapshot> maintenanceLiveData = maintenanceViewModel.getDataSnapshotLiveData();
        maintenanceLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if ((boolean) dataSnapshot.getValue()) {
                        Intent welcomeIntent = new Intent(MainPlayerActivity.this,WelcomeActivity.class);
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
        ExploreFragment exploreFragment = ExploreFragment.newInstance(sessionTypeDictionary);
        bottomNavigationAdapter.addFragments(exploreFragment);

        PlayerSessionsFragment playerSessionsFragment = PlayerSessionsFragment.newInstance(sessionTypeDictionary);
        bottomNavigationAdapter.addFragments(playerSessionsFragment);

        InboxFragment inboxFragment = InboxFragment.newInstance();
        bottomNavigationAdapter.addFragments(inboxFragment);

        UserAccountFragment userAccountFragment = new UserAccountFragment();
        bottomNavigationAdapter.addFragments(userAccountFragment);

        mainPager.setAdapter(bottomNavigationAdapter);

        /** Check if activity has been started due to notification, if so get from user ID and open up profile*/
        if (fromUserID!=null) {
            Bundle fromUserbundle = new Bundle();
            fromUserbundle.putString("otherUserID", fromUserID);
            UserProfilePublicFragment userProfilePublicFragment = UserProfilePublicFragment.newInstance();
            userProfilePublicFragment.setArguments(fromUserbundle);
            cleanMainFullscreenActivityAndSwitch(userProfilePublicFragment, true,"");
        } else {
            // Start normally
        }

        prevTab = 0;

        /** Setup Bottom navigation */
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                checkReviews();
                if (!wasSelected) {
                    mainPager.setCurrentItem(position, false);
                    if (position==0) {
                        // Refresh sessions?
                    }
                    if (position==2) {
                        bottomNavigation.setNotification("", 2);
                    }
                    if (position==2) {
                        bottomNavigation.setNotification("", 2);
                    }
                    if (position==2) {
                        FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
                    }
                }
                prevTab = position;
                return true;
            }
        });


        FirebaseDatabaseViewModel reviewsToWriteUserIdViewModel = ViewModelProviders.of(MainPlayerActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> reviewsToWriteLiveData = reviewsToWriteUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        reviewsToWriteLiveData.observe(MainPlayerActivity.this, new Observer<DataSnapshot>() {
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
                                WriteReviewsFragment writeReviewsFragment = WriteReviewsFragment.newInstance(advertisement);
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                writeReviewsFragment.show(transaction,advertisementId);
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

    private void updateStripeCustomerInfo() {

        if (stripeDefaultPaymentMethodId==null) {
            setPaymentMethod(new HashMap());
        }

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

    /* Method to hide all fragments in main container and fill the other container with fullscreen fragment */
    private void cleanMainFullscreenActivityAndSwitch(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        if (addToBackStack) {
            if (!tag.equals("")) {
                transaction.replace(R.id.container_fullscreen_main_player, fragment).addToBackStack(tag).commit();
            } else {
                transaction.replace(R.id.container_fullscreen_main_player, fragment).addToBackStack(null).commit();
            }
        } else {
            if (!tag.equals("")) {
                transaction.replace(R.id.container_fullscreen_main_player, fragment, tag).commit();
            } else {
                transaction.replace(R.id.container_fullscreen_main_player, fragment).commit();
            }
        }
    }

    @Override
    public void OnSessionClicked(String sessionId) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(sessionId, sessionTypeDictionary, false);
        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,DisplaySessionFragment.TAG);
    }

    @Override
    public void OnSessionClicked(String sessionId, Long representingAdTimestamp) {
     if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(sessionId, representingAdTimestamp, sessionTypeDictionary, false);
        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
    }

    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            UserProfileFragment userProfileFragment = (UserProfileFragment) fragmentManager.findFragmentByTag(UserProfileFragment.TAG);
            if (userProfileFragment==null) {
                userProfileFragment = UserProfileFragment.newInstance();
            }
            cleanMainFullscreenActivityAndSwitch(userProfileFragment, true,UserProfileFragment.TAG);
        }
    }
    /* Listener, when edit "button" in user profile is clicked show edit user profile */
    @Override
    public void onUserProfileFragmentInteraction() {
        UserProfilePublicEditFragment userProfilePublicEditFragment = (UserProfilePublicEditFragment) fragmentManager.findFragmentByTag(UserProfilePublicEditFragment.TAG);
        if (userProfilePublicEditFragment==null) {
            userProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        }
        cleanMainFullscreenActivityAndSwitch(userProfilePublicEditFragment,true,UserProfilePublicEditFragment.TAG);
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
        UserProfilePublicFragment userProfilePublicFragment = (UserProfilePublicFragment) fragmentManager.findFragmentByTag(UserProfilePublicFragment.TAG);
        if (userProfilePublicFragment==null) {
            userProfilePublicFragment = UserProfilePublicFragment.newInstance();
        }
        userProfilePublicFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(userProfilePublicFragment, true,"");
    }

    @Override
    public void OnNewMessage() {
        // TODO check if this should be used
    }

    @Override
    public void OnEditSession(String sessionID) {
        // Not possible in player environment
    }

    @Override
    public void OnEditSession(String sessionID, String type) {
        // Not possible in player environment

    }

    @Override
    public void addAdvertisements(String sessionID) {
        // Not possible in player environment
    }

    @Override
    public void OnEditSession(String sessionID, Session session) {
        // Not possible in player environment
    }

    @Override
    public void OnEditSession(String sessionID, Session session, String type) {
        // Not possible in player environment

    }

    @Override
    public void OnCancelBookedSession(Long bookingTimestamp, Long advertisementTimestamp, String advertisementId, String participantId, int adPrice, String hostId, boolean superHosted) {
        Intent cancelIntent = new Intent(MainPlayerActivity.this,CancelBookingActivity.class);
        cancelIntent.putExtra("bookingTimestamp", bookingTimestamp);
        cancelIntent.putExtra("advertisementTimestamp", advertisementTimestamp);
        cancelIntent.putExtra("advertisementId", advertisementId);
        cancelIntent.putExtra("participantId",participantId);
        cancelIntent.putExtra("adPrice", adPrice);
        cancelIntent.putExtra("hostId",hostId);
        cancelIntent.putExtra("superHosted",superHosted);
        startActivityForResult(cancelIntent, CANCEL_BOOKING_REQUEST);
    }

    @Override
    public void OnBookSession(String advertisementId, Long advertisementTimestamp, String hostId, int amount, boolean dontShowBookingText, int advertisementDurationInMin, String sessionType, int currentNrOfParticipants, boolean superHosted) {
        // If the users dontShowBookingText is false we should show the booking textin a dialog, a warning text explaining the payment policy
        if (!dontShowBookingText) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
            LayoutInflater inflater = MainPlayerActivity.this.getLayoutInflater();
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
                            Intent bookIntent = new Intent(MainPlayerActivity.this,BookingActivity.class);
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
        Intent bookIntent = new Intent(MainPlayerActivity.this,BookingActivity.class);
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

    @Override
    public void OnDismissDisplaySession() {
        getSupportFragmentManager().popBackStack();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                fragmentManager.popBackStack("ad",FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
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

    @Override
    public void OnChatClicked(String userID, String userName, String userThumbImage, String chatID) {
        ChatFragment chatFragment = ChatFragment.newInstance(userID,userName,userThumbImage,chatID);
        cleanMainFullscreenActivityAndSwitch(chatFragment,true,ChatFragment.TAG);
    }

    @Override
    public void OnCommentClicked(String sourceID, String postID, String heading, String time, String message, String thumb_image, String wallType) {
        CommentFragment commentFragment = CommentFragment.newInstance(sourceID, postID, heading, time, message, thumb_image, wallType);
        cleanMainFullscreenActivityAndSwitch(commentFragment,true, CommentFragment.TAG);
    }

    @Override
    public void OnSearchClicked() {
        AllUsersFragment allUsersFragment = AllUsersFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(allUsersFragment,true, AllUsersFragment.TAG);
    }

    @Override
    public void OnAdvertisementsFound(ArrayList<Advertisement> advertisements) {

    }

    @Override
    public void OnCancelAdvertisement(String advertisementName, String advertisementId, String imageUrl, String sessionId, Long advertisementTimestamp, HashMap<String, Long> participantsTimestamps, String accountId, int price) {
        Intent cancelAdIntent = new Intent(MainPlayerActivity.this,CancelAdvertisementActivity.class);
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

    // Makes a fragemnt name to fragments created by pager
    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }
    /* When back button is pressed while in main host activity override function and replace with following */
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
        } else {
            getSupportFragmentManager().popBackStack();
        }
        hideKeyboard();
    }

    // Before starting activity, make sure user is signed-in, otherwise start login activity
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null) {
            //User is signed out
            Intent welcomeIntent = new Intent(MainPlayerActivity.this,WelcomeActivity.class);
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
    protected void onStop() {
        super.onStop();
        for (CountDownTimer countDownTimer: countDownTimerHashMap.keySet()) {
            countDownTimer.cancel();
        }


    }

    // When activity is destroyed, remove all listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mainPager.setAdapter(null);
        bottomNavigationAdapter = null;
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
    protected void onResume() {
        super.onResume();
        resumed=true;
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

    private Task<HashMap<String, Object>> retrieveStripeCustomer(String customerID) {
        return mFunctions
                .getHttpsCallable("retrieveCustomer")
                .call(customerID)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
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

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void OnRefreshSessions(int weekday) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnRefreshSessions(weekday);
    }

    @Override
    public void OnListSessionsScroll(int dy) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnListSessionsScroll(dy);
    }

    @Override
    public void OnNotificationClicked(FoxmikeNotification foxmikeNotification) {
        if (foxmikeNotification.getType().equals("sessionPost")) {
            DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP1(), sessionTypeDictionary, false);
            cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("sessionPostComment")) {
            DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary, false);
            cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("participantNew")) {
            DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary, false);
            cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("participantCancellation")) {
            DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(foxmikeNotification.getP2(), sessionTypeDictionary, false);
            cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true,"");
        }
        if (foxmikeNotification.getType().equals("friendRequestAccepted")) {
            InboxFragment inboxFragment = (InboxFragment) bottomNavigationAdapter.getRegisteredFragment(2);
            inboxFragment.setPage(2);
        }
        if (foxmikeNotification.getType().equals("sessionCancellation") || foxmikeNotification.getType().equals("freeSessionCancellation")) {
            bottomNavigation.setCurrentItem(1);
            PlayerSessionsFragment playerSessionsFragment = (PlayerSessionsFragment) bottomNavigationAdapter.getRegisteredFragment(1);
            playerSessionsFragment.setPage(0);
        }
    }

    @Override
    public void OnDistanceFilterChanged(int filterDistance) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnFilterByDistance(filterDistance);

    }

    @Override
    public void OnMinPriceChanged(int minPrice) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnMinPriceChanged(minPrice, "SE");
    }

    @Override
    public void OnMaxPriceChanged(int maxPrice) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnMaxPriceChanged(maxPrice, "SE");
    }

    @Override
    public void OnTimeRangeChanged(int minHour, int minMinute, int maxHour, int maxMinute) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnTimeRangeChanged(minHour, minMinute, maxHour, maxMinute);
    }

    @Override
    public void OnSessionTypeChanged(HashMap<String, Boolean> sessionTypeChosen) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        exploreFragment.OnSessionTypeChanged(sessionTypeChosen);
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
    public void AlertOccasionCancelled(boolean free, String sessionId) {
        String message = getResources().getString(R.string.occasion_cancelled_text);
        if (!free) {
            message = getResources().getString(R.string.occasion_cancelled_text) + getResources().getString(R.string.you_will_be_refunded);
        }
        alertDialogPositiveOrNegative(getResources().getString(R.string.occasion_cancelled), message,
                getResources().getString(R.string.ok), getResources().getString(R.string.show_availability), new OnPositiveOrNegativeButtonPressedListener() {
                    @Override
                    public void OnPositivePressed() {

                    }

                    @Override
                    public void OnNegativePressed() {
                        DisplaySessionFragment displaySessionFragment = DisplaySessionFragment.newInstance(sessionId, sessionTypeDictionary, false);
                        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true, "");
                    }
                });
    }

    @Override
    public void OnDimCurrentDay(boolean dim) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        if (exploreFragment!=null) {
            exploreFragment.dimCurrentDay(dim);
        }
    }

    @Override
    public void OnFragmentAttachedNeedNewData(int week) {
        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
        if (exploreFragment!=null) {
            exploreFragment.updateWeekFragmentWithData(week);
        }
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

    public interface OnOkPressedListener {
        void OnOkPressed();
    }

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (bottomNavigationAdapter!=null) {
                        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
                        exploreFragment.locationPermissionChanged(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (bottomNavigationAdapter!=null) {
                        ExploreFragment exploreFragment = (ExploreFragment) bottomNavigationAdapter.getRegisteredFragment(0);
                        exploreFragment.locationPermissionChanged(false);
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}