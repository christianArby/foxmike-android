package com.foxmike.android.activities;
//Checked
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.foxmike.android.R;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.ChatFragment;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.CreateOrEditSessionFragment;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.fragments.DisplayStudioFragment;
import com.foxmike.android.fragments.ListSessionsFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.MapsFragment;
import com.foxmike.android.fragments.UserAccountHostFragment;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnHostSessionChangedListener;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.Studio;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.interfaces.OnNearSessionsFoundListener;
import com.foxmike.android.interfaces.OnNewMessageListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnSessionsFilteredListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.interfaces.OnWeekdayButtonClickedListener;
import com.foxmike.android.interfaces.OnWeekdayChangedListener;
import com.foxmike.android.fragments.PlayerSessionsFragment;
import com.foxmike.android.models.Session;
import com.foxmike.android.fragments.SortAndFilterFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.foxmike.android.fragments.WeekdayFilterFragment;
import com.foxmike.android.utils.WrapContentViewPager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.rd.PageIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.BehaviorSubject;
import static android.content.ContentValues.TAG;

public class MainPlayerActivity extends AppCompatActivity

        implements
        OnWeekdayChangedListener,
        OnWeekdayButtonClickedListener,
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserAccountHostFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        OnNewMessageListener,
        ListSessionsFragment.OnRefreshSessionsListener,
        OnUserClickedListener,
        ListSessionsFragment.OnListSessionsScrollListener,
        SortAndFilterFragment.OnListSessionsFilterListener,
        SortAndFilterFragment.OnListSessionsSortListener,
        DisplaySessionFragment.OnEditSessionListener,
        DisplaySessionFragment.OnBookSessionListener,
        DisplaySessionFragment.OnCancelBookedSessionListener,
        OnHostSessionChangedListener,
        OnSessionBranchClickedListener,
        OnChatClickedListener,
        OnCommentClickedListener,
        InboxFragment.OnSearchClickedListener, DisplayStudioFragment.OnStudioInteractionListener, MapsFragment.OnLocationPickedListener{

    private FragmentManager fragmentManager;
    private UserAccountFragment userAccountFragment;
    private UserProfileFragment userProfileFragment;
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;
    private PlayerSessionsFragment playerSessionsFragment;
    private DisplaySessionFragment displaySessionFragment;
    private InboxFragment inboxFragment;
    private UserProfilePublicFragment userProfilePublicFragment;
    private UserProfilePublicEditFragment userProfilePublicEditFragment;
    private AllUsersFragment allUsersFragment;
    private CreateOrEditSessionFragment createOrEditSessionFragment;
    private MyFirebaseDatabase myFirebaseDatabase;
    public HashMap<String,Boolean> firstWeekdayHashMap;
    public HashMap<String,Boolean> secondWeekdayHashMap;
    private AHBottomNavigation bottomNavigation;
    private FloatingActionButton mapOrListBtn;
    private FloatingActionButton sortAndFilterFAB;
    private FloatingActionButton myLocationBtn;
    private RelativeLayout weekdayFilterContainer;
    private String fromUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private ArrayList<Session> sessionListArrayList = new ArrayList<>();
    private Location locationClosetoSessions;
    private String sortType;
    private int distanceRadius;
    Boolean started = false;
    private boolean resumed = false;
    private FirebaseFunctions mFunctions;
    private View mainView;
    static final int BOOK_SESSION_REQUEST = 8;
    static final int CANCEL_SESSION_REQUEST = 16;
    private ArrayList<ArrayList<Session>> mainNearSessionsArrays = new ArrayList<>();
    private float mapOrListBtnStartX;
    private float mapOrListBtnStartY;

    public final BehaviorSubject<HashMap> subject = BehaviorSubject.create();
    public HashMap  getStripeDefaultSource()          { return subject.getValue(); }
    public void setStripeDefaultSource(HashMap value) { subject.onNext(value);     }

    private SortAndFilterFragment sortAndFilterFragment;

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
        mainView = findViewById(R.id.activity_main_player);
        mFunctions = FirebaseFunctions.getInstance();

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        sortType = "date";
        distanceRadius = this.getResources().getInteger(R.integer.distanceMax);

        // get views
        weekdayFilterContainer = findViewById(R.id.weekdayFilterFragmentContainer);
        bottomNavigation = findViewById(R.id.bottom_navigation_player);
        mapOrListBtn = findViewById(R.id.map_or_list_button);
        sortAndFilterFAB = findViewById(R.id.sort_button);
        myLocationBtn = findViewById(R.id.my_location_button);
        WrapContentViewPager weekdayViewpager = findViewById(R.id.weekdayPager);
        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);

        /*mapOrListBtn.post(new Runnable() {
            @Override
            public void run() {
                mapOrListBtnStartX = mapOrListBtn.getX();
                mapOrListBtnStartY = mapOrListBtn.getY();
            }
        });*/



        // get Firebase instances and references
        mAuth = FirebaseAuth.getInstance();
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        /** If friend request is sent by notification, get it through intentExtra. */
        fromUserID = getIntent().getStringExtra("notificationRequest");

        /** Setup bottom navigation */
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_player_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.foxmikePrimaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        /** Setup weekdayHashmaps*/
        firstWeekdayHashMap = new HashMap<String,Boolean>();
        secondWeekdayHashMap = new HashMap<String,Boolean>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar cal = Calendar.getInstance();
        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            firstWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }
        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            secondWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }

        /** Setup fragments */
        userAccountFragment = new UserAccountFragment();
        playerSessionsFragment = PlayerSessionsFragment.newInstance();
        listSessionsFragment = ListSessionsFragment.newInstance();
        inboxFragment = InboxFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        mapsFragment = MapsFragment.newInstance();
        mapsFragment.setArguments(bundle);
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("xMainUserAccountFragment")) {
            transaction.add(R.id.container_main_player, userAccountFragment,"xMainUserAccountFragment");
            transaction.hide(userAccountFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment")) {
            transaction.add(R.id.container_main_player, playerSessionsFragment,"xMainPlayerSessionsFragment");
            transaction.hide(playerSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainMapsFragment")) {
            transaction.add(R.id.container_main_player, mapsFragment,"xMainMapsFragment");
            transaction.hide(mapsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainListSessionsFragment")) {
            transaction.add(R.id.container_main_player, listSessionsFragment,"xMainListSessionsFragment");
            transaction.hide(listSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainInboxFragment")) {
            transaction.add(R.id.container_main_player, inboxFragment,"xMainInboxFragment");
            transaction.hide(inboxFragment);
        }
        transaction.commitNow();

        // Setup weekdaypager
        weekdayViewpager.setAdapter(new weekdayViewpagerAdapter(fragmentManager));
        pageIndicatorView.setViewPager(weekdayViewpager);

        fragmentManager.executePendingTransactions();

        /** Check if activity has been started due to notification, if so get from user ID and open up profile*/
        if (fromUserID!=null) {
            Bundle fromUserbundle = new Bundle();
            bundle.putString("otherUserID", fromUserID);
            userProfilePublicFragment = UserProfilePublicFragment.newInstance();
            userProfilePublicFragment.setArguments(fromUserbundle);
            cleanMainFullscreenActivityAndSwitch(userProfilePublicFragment, true);
        } else {
            cleanMainActivityAndSwitch(listSessionsFragment);
            weekdayFilterContainer.setVisibility(View.VISIBLE);
            mapOrListBtn.setVisibility(View.VISIBLE);
            mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
            sortAndFilterFAB.setVisibility(View.VISIBLE);
        }

        /** Setup List and Map with sessions*/
        setupListAndMapWithSessions();

        /** Setup Bottom navigation */
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        if (!wasSelected | resumed) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainListSessionsFragment"));
                            weekdayFilterContainer.setVisibility(View.VISIBLE);
                            mapOrListBtn.setVisibility(View.VISIBLE);
                            mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
                            sortAndFilterFAB.setVisibility(View.VISIBLE);
                            resumed = false;
                            return true;
                        }
                    case 1:
                        if (!wasSelected | resumed) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment"));
                            resumed = false;
                            return true;
                        }
                    case 2:
                        if (!wasSelected | resumed) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainInboxFragment"));
                            resumed = false;
                            return true;
                        }
                    case 3:
                        if (!wasSelected | resumed) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainUserAccountFragment"));
                            resumed = false;
                            return true;
                        }
                }
                return false;
            }
        });

        // --------------------------  LISTEN TO STRIPE CUSTOMER -------------------------------------
        ValueEventListener stripeCustomerListener = rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("stripeCustomer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue()==null) {
                    return;
                }

                String stripeCustomerId = dataSnapshot.child("id").getValue().toString();

                // Retrieve Stripe Account
                retrieveStripeCustomer(stripeCustomerId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                        // If not succesful, show error and return from function, will trigger if account ID does not exist
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            // [START_EXCLUDE]
                            Log.w(TAG, "retrieve:onFailure", e);
                            showSnackbar("An error occurred." + e.getMessage());
                            return;
                            // [END_EXCLUDE]
                        }
                        // If successful, extract
                        HashMap<String, Object> result = task.getResult();
                        if (result.get("resultType").toString().equals("customer")) {
                            HashMap<String, Object> sources = (HashMap<String, Object>) result.get("sources");
                            ArrayList<HashMap<String,Object>> sourcesDataList = (ArrayList<HashMap<String,Object>>) sources.get("data");
                            String defaultSource;
                            if (result.get("default_source")!= null) {
                                defaultSource = result.get("default_source").toString();
                            } else {
                                defaultSource = null;
                                setStripeDefaultSource(new HashMap());
                            }
                            for(int i=0; i<sourcesDataList.size(); i++){
                                if (sourcesDataList.get(i).get("id").toString().equals(defaultSource)) {
                                    setStripeDefaultSource(sourcesDataList.get(i));
                                }
                            }
                        } else {
                            HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                            showSnackbar(error.get("message").toString());
                        }
                        // [END_EXCLUDE]
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listenerMap.put(rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("stripeCustomer"), stripeCustomerListener);

        // --------------------------  LISTEN TO CHATS -------------------------------------
        // Check if there are unread chatmessages and if so set notifications to the bottom navigation bar
        ValueEventListener chatsListener = rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    int nrOfUnreadChats = 0;
                    for (DataSnapshot chatID: dataSnapshot.getChildren()) {
                        Boolean read = (Boolean) chatID.getValue();
                        if (!read) {
                            nrOfUnreadChats++;
                        }

                        if (nrOfUnreadChats>0) {
                            bottomNavigation.setNotification(Integer.toString(nrOfUnreadChats),2);
                        } else {
                            bottomNavigation.setNotification("",2);
                        }

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listenerMap.put(rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats"), chatsListener);

        /** Setup mapOrList FAB*/
        mapOrListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                if (mapsFragment.isVisible()) {
                    FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                    transaction1.hide(mapsFragment);
                    transaction1.show(listSessionsFragment);
                    transaction1.commit();
                    switchMapOrListUI(false);
                } else if (listSessionsFragment.isVisible()) {
                    FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                    transaction2.hide(listSessionsFragment);
                    transaction2.show(mapsFragment);
                    transaction2.commit();
                    switchMapOrListUI(true);
                } else {
                    FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                    transaction3.hide(mapsFragment);
                    transaction3.show(listSessionsFragment);
                    transaction3.commit();
                    switchMapOrListUI(false);
                }
            }
        });

        /** Setup sortAndFilter FAB*/
        sortAndFilterFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (sortAndFilterFragment!=null) {
                    transaction.remove(sortAndFilterFragment);
                }
                sortAndFilterFragment = SortAndFilterFragment.newInstance(sortType, distanceRadius);
                sortAndFilterFragment.show(transaction,"sortAndFilterFragment");
            }
        });

        // Setup my location button
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                mapsFragment.goToMyLocation();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (userAccountFragment.isVisible() | playerSessionsFragment.isVisible() | listSessionsFragment.isVisible() | mapsFragment.isVisible() | inboxFragment.isVisible()){
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
                if (listSessionsFragment.isVisible()| mapsFragment.isVisible()) {
                    weekdayFilterContainer.setVisibility(View.VISIBLE);
                    mapOrListBtn.setVisibility(View.VISIBLE);
                    sortAndFilterFAB.setVisibility(View.VISIBLE);
                }
                if(mapsFragment.isVisible()) {
                    myLocationBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void switchMapOrListUI(boolean mapIsVisible) {

        int width = mainView.getRight();
        int height = mainView.getBottom();
        float fabDiameter = convertDpToPx(MainPlayerActivity.this, 56);

        mapOrListBtnStartX = width/2 - fabDiameter/2;
        mapOrListBtnStartY = height -  convertDpToPx(MainPlayerActivity.this, 80) - fabDiameter;
        float Xcontrol2 = width - convertDpToPx(MainPlayerActivity.this,72);
        float Ycontrol2 = sortAndFilterFAB.getY() + convertDpToPx(MainPlayerActivity.this, 144);


        if (mapIsVisible) {
            mapsFragment.showRecylerView(true);
            mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_list_black_24dp));
            myLocationBtn.show();

            Path path = new Path();
            path.moveTo(mapOrListBtnStartX, mapOrListBtnStartY);
            path.quadTo(mapOrListBtnStartX, Ycontrol2, Xcontrol2, Ycontrol2);
            ObjectAnimator objectAnimator1 = new ObjectAnimator().ofFloat(mapOrListBtn, "x", "y", path);
            objectAnimator1.setDuration(500);
            objectAnimator1.setInterpolator(new LinearOutSlowInInterpolator());
            objectAnimator1.start();
        } else {
            mapsFragment.showRecylerView(false);
            mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
            myLocationBtn.hide();

            Path path = new Path();
            path.moveTo(Xcontrol2, Ycontrol2);
            path.quadTo(Ycontrol2, mapOrListBtnStartY, mapOrListBtnStartX, mapOrListBtnStartY);
            ObjectAnimator objectAnimator1 = new ObjectAnimator().ofFloat(mapOrListBtn, "x", "y", path);
            objectAnimator1.setDuration(500);
            objectAnimator1.setInterpolator(new FastOutLinearInInterpolator());
            objectAnimator1.start();
        }
    }

    private void setupListAndMapWithSessions() {
        myFirebaseDatabase= new MyFirebaseDatabase();
        // TODO if new filtersessions int is smaller than previous this function should only filter and not download
        myFirebaseDatabase.getNearSessions(this, distanceRadius, new OnNearSessionsFoundListener() {

            @Override
            public void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location, ArrayList<ArrayList<Session>> nearSessionsArrays) {
                sessionListArrayList.clear();
                mainNearSessionsArrays.clear();
                mainNearSessionsArrays = nearSessionsArrays;
                sessionListArrayList = nearSessions;
                locationClosetoSessions = location;

                myFirebaseDatabase.filterSessions(nearSessions, nearSessionsArrays,firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
                    @Override
                    public void OnSessionsFiltered(ArrayList<Session> sessions, ArrayList<ArrayList<Session>> nearSessionsArrays) {
                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                        listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
                        listSessionsFragment.stopSwipeRefreshingSymbol();

                        MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                        mapsFragment.addMarkersToMap(nearSessionsArrays);

                    }


                });

            }

            @Override
            public void OnLocationNotFound() {
                Toast.makeText(MainPlayerActivity.this, R.string.location_not_found, Toast.LENGTH_LONG).show();
                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                listSessionsFragment.emptyListView();
                listSessionsFragment.stopSwipeRefreshingSymbol();

            }
        });
    }

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /** FUNCTION to clean whole activity and switch fragment*/
    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag()!=null && frag.getTag().length()>5) {
                if (frag.getTag().substring(0,5).equals("xMain")) {
                    if (frag.isVisible()) {
                        transaction.hide(frag);
                    }
                }
            }
        }
        transaction.show(fragment);
        transaction.commit();
        weekdayFilterContainer.setVisibility(View.GONE);
        mapOrListBtn.setVisibility(View.GONE);
        mapsFragment.showRecylerView(false);
        sortAndFilterFAB.setVisibility(View.GONE);
        myLocationBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);

        if (mapOrListBtn.getX()!=0 && mapOrListBtnStartX!=0) {
            if (mapOrListBtn.getX()!= mapOrListBtnStartX) {
                switchMapOrListUI(false);
            }

        }
    }
    /* Method to hide all fragments in main container and fill the other container with fullscreen fragment */
    private void cleanMainFullscreenActivityAndSwitch(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        if (addToBackStack) {
            transaction.replace(R.id.container_fullscreen_main_player, fragment).addToBackStack(null).commit();
        } else {
            transaction.replace(R.id.container_fullscreen_main_player, fragment).commit();
        }
    }



    /** INTERFACE to change weekday hashmaps based on inputs */
    @Override
    public void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity) {
        if (week==1) {
            firstWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
        if (week==2) {
            secondWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
    }

    /** INTERFACE to refilter sessions in List and Map based on weekday hashmaps */
    @Override
    public void OnWeekdayButtonClicked(int week, int button, HashMap<Integer, Boolean> toggleHashMap) {
        HashMap<Integer, Boolean> toggleMap1;
        HashMap<Integer, Boolean> toggleMap2;
        final FragmentManager fragmentManager = getSupportFragmentManager();
        WeekdayFilterFragment weekdayFilterFragment = (WeekdayFilterFragment) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,0));
        WeekdayFilterFragment weekdayFilterFragmentB = (WeekdayFilterFragment) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,1));

        toggleMap1 = weekdayFilterFragment.getToggleMap1();
        toggleMap2 = weekdayFilterFragmentB.getToggleMap2();

        weekdayFilterFragment.changeToggleMap(week,button,toggleMap1,toggleMap2);
        weekdayFilterFragmentB.changeToggleMap(week,button,toggleMap1,toggleMap2);

        toggleMap1 = weekdayFilterFragment.getAndUpdateToggleMap1();
        weekdayFilterFragmentB.setToggleMap1(toggleMap1);
        toggleMap2 = weekdayFilterFragmentB.getAndUpdateToggleMap2();
        weekdayFilterFragment.setToggleMap2(toggleMap2);

        myFirebaseDatabase.filterSessions(sessionListArrayList, mainNearSessionsArrays,firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions, ArrayList<ArrayList<Session>> nearSessionsArrays) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                mapsFragment.addMarkersToMap(nearSessionsArrays);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);

            }
        });
    }

    @Override
    public void OnSessionClicked(String sessionId) {
        displaySessionFragment = DisplaySessionFragment.newInstance(sessionId);
        cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true);
    }

    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            userProfileFragment = UserProfileFragment.newInstance();
            cleanMainFullscreenActivityAndSwitch(userProfileFragment, true);
        }
    }
    /* Listener, when edit "button" in user profile is clicked show edit user profile */
    @Override
    public void onUserProfileFragmentInteraction() {
        userProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(userProfilePublicEditFragment,true);
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
        userProfilePublicFragment = UserProfilePublicFragment.newInstance();
        userProfilePublicFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(userProfilePublicFragment, true);
    }

    @Override
    public void OnNewMessage() {
        // TODO check if this should be used
    }

    /** INTERFACE triggered when list is scrolled setting behaviour of buttons */
    @Override
    public void OnListSessionsScroll(int dy) {
        if (dy > 0 && !started) {
            started = true;
            sortAndFilterFAB.hide();
            mapOrListBtn.animate()
                    .translationY(400)
                    .withLayer()
                    .start();

        } else if (dy < 0 && started) {
            started = false;
            sortAndFilterFAB.hide();
            sortAndFilterFAB.show();
            mapOrListBtn
                    .animate()
                    .translationY(0)
                    .withLayer()
                    .start();
        }
    }

    /** INTERFACE triggered when list is scrolled REFRESHED, downloads all sessions based on input distance radius*/
    @Override
    public void OnRefreshSessions() {
        setupListAndMapWithSessions();
    }

    /** INTERFACE triggered when sort buttons are clicked, SORTS sessions*/
    @Override
    public void OnListSessionsSort(String sortType) {
        this.sortType = sortType;
        myFirebaseDatabase.filterSessions(sessionListArrayList, mainNearSessionsArrays,firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions, ArrayList<ArrayList<Session>> nearSessionsArrays) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                mapsFragment.addMarkersToMap(nearSessionsArrays);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
        });
    }

    /** INTERFACE triggered when filter buttons are clicked, FILTERS sessions*/
    @Override
    public void OnListSessionsFilter(int filterDistance) {
        distanceRadius = filterDistance;
        setupListAndMapWithSessions();
    }

    @Override
    public void OnEditSession(String sessionID) {
        // Not possible in player environment
    }

    @Override
    public void OnEditSession(String sessionID, Session session) {
        // Not possible in player environment
    }

    @Override
    public void OnHostSessionChanged() {
        // Not possible in player environment
    }

    @Override
    public void OnBookSession(String sessionId, String hostId, String stripeCustomerId, int amount, String currency, boolean dontShowBookingText) {

        if (!dontShowBookingText) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
            // Get the layout inflater
            LayoutInflater inflater = MainPlayerActivity.this.getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_booking_dialog,null);
            AppCompatCheckBox showAgainCheckbox = view.findViewById(R.id.doNotShowAgainCheckbox);
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.book_session, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (showAgainCheckbox.isChecked()) {
                                rootDbRef.child("users").child(userId).child("dontShowBookingText").setValue(true);
                            }

                            Intent bookIntent = new Intent(MainPlayerActivity.this,BookingActivity.class);
                            bookIntent.putExtra("sessionId", sessionId);
                            bookIntent.putExtra("hostId", hostId);
                            bookIntent.putExtra("stripeCustomerId", stripeCustomerId);
                            bookIntent.putExtra("amount",amount);
                            bookIntent.putExtra("currency",currency);
                            startActivityForResult(bookIntent, BOOK_SESSION_REQUEST);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).setMessage(R.string.booking_text_policy).setTitle(R.string.confirm_booking);
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        Intent bookIntent = new Intent(MainPlayerActivity.this,BookingActivity.class);
        bookIntent.putExtra("sessionId", sessionId);
        bookIntent.putExtra("hostId", hostId);
        bookIntent.putExtra("stripeCustomerId", stripeCustomerId);
        bookIntent.putExtra("amount",amount);
        bookIntent.putExtra("currency",currency);
        startActivityForResult(bookIntent, BOOK_SESSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CANCEL_SESSION_REQUEST) {
                if (data!=null) {
                    if (data.getBooleanExtra("cancel", false)) {
                        String sessionID = data.getStringExtra("sessionID");
                        Map requestMap = new HashMap<>();
                        requestMap.put("sessions/" + sessionID + "/participants/" + mAuth.getCurrentUser().getUid(), null);
                        requestMap.put("users/" + mAuth.getCurrentUser().getUid() + "/sessionsAttending/" + sessionID, null);
                        rootDbRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment")!=null) {
                                    PlayerSessionsFragment ps = (PlayerSessionsFragment) fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment");
                                    ps.loadPages(true);
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
                                builder.setMessage(R.string.your_booking_has_been_cancelled);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });

                    }
                } else {
                    if (fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment")!=null) {
                        PlayerSessionsFragment ps = (PlayerSessionsFragment) fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment");
                        ps.loadPages(true);
                    }
                }
            }
            if (requestCode == BOOK_SESSION_REQUEST) {
                if (fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment")!=null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayerActivity.this);
                    builder.setMessage(R.string.booking_confirmed);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    PlayerSessionsFragment ps = (PlayerSessionsFragment) fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment");
                    ps.loadPages(true);
                }
            }
        }
    }

    @Override
    public void OnCancelBookedSession(Long bookingTimestamp, Long sessionTimestamp, String sessionID, String participantId, String chargeId, String accountId) {

        Intent cancelIntent = new Intent(MainPlayerActivity.this,CancelBookingActivity.class);
        cancelIntent.putExtra("bookingTimestamp", bookingTimestamp);
        cancelIntent.putExtra("sessionTimestamp", sessionTimestamp);
        cancelIntent.putExtra("sessionID", sessionID);
        cancelIntent.putExtra("participantId",participantId);
        cancelIntent.putExtra("chargeId",chargeId);
        cancelIntent.putExtra("accountId",accountId);
        startActivityForResult(cancelIntent, CANCEL_SESSION_REQUEST);

    }

    @Override
    public void OnSessionBranchClicked(SessionBranch sessionBranch, String request) {
        if (request.equals("displaySession")) {
            displaySessionFragment = DisplaySessionFragment.newInstance(sessionBranch.getSessionID());
            cleanMainFullscreenActivityAndSwitch(displaySessionFragment, true);
        }
    }

    @Override
    public void OnChatClicked(String userID, String userName, String userThumbImage, String chatID) {
        ChatFragment chatFragment = ChatFragment.newInstance(userID,userName,userThumbImage,chatID);
        cleanMainFullscreenActivityAndSwitch(chatFragment,true);
    }

    @Override
    public void OnCommentClicked(String postID, String heading, String time, String message, String thumb_image) {
        CommentFragment commentFragment = CommentFragment.newInstance(postID, heading, time, message, thumb_image);
        cleanMainFullscreenActivityAndSwitch(commentFragment,true);
    }

    @Override
    public void OnSearchClicked() {
        AllUsersFragment allUsersFragment = AllUsersFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(allUsersFragment,true);
    }

    @Override
    public void OnEditStudio(String studioID, Studio studio) {
        // Not possible in player environment
    }

    @Override
    public void OnPreviewStudio(String studioID, Studio studio) {
        // Not possible in player environment
    }

    @Override
    public void OnAdvertiseStudio(String studioID, Studio studio) {
        // Not possible in player environment
    }

    @Override
    public void OnLocationPicked(LatLng latLng, String requestType, String studioId, Studio studio) {
        // Not possible in player environment
    }

    // Sets up weekday pager
    class weekdayViewpagerAdapter extends FragmentPagerAdapter {
        public weekdayViewpagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = WeekdayFilterFragment.newInstance(1);
            }
            if (position == 1) {
                fragment = WeekdayFilterFragment.newInstance(2);
            }
            return fragment;
        }
        @Override
        public int getCount() {
            return 2;
        }
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
            // /super.onBackPressed();
            //additional code
        } else {
            // TODO Add Newsfeed fragment here later when exist
            getSupportFragmentManager().popBackStack();
            if (!listSessionsFragment.isVisible()&&!mapsFragment.isVisible()&&!playerSessionsFragment.isVisible()&&!userAccountFragment.isVisible()&&!inboxFragment.isVisible()){

            }
        }
        hideKeyboard(MainPlayerActivity.this);
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

    }
    // When activity is destroyed, remove all listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }

        if (getSupportFragmentManager().findFragmentByTag("xMainInboxFragment") !=null) {
            InboxFragment inboxFragment = (InboxFragment) getSupportFragmentManager().findFragmentByTag("xMainInboxFragment");
            inboxFragment.cleanInboxListeners();
        }

        if (getSupportFragmentManager().findFragmentByTag("displaySessionFragment")!=null) {
            DisplaySessionFragment displaySessionFragment = (DisplaySessionFragment) getSupportFragmentManager().findFragmentByTag("displaySessionFragment");
            displaySessionFragment.cleanListeners();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            hideKeyboard(MainPlayerActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** Setup fragments */
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("xMainUserAccountFragment")) {
            userAccountFragment = new UserAccountFragment();
            transaction.add(R.id.container_main_player, userAccountFragment,"xMainUserAccountFragment");
            transaction.hide(userAccountFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainPlayerSessionsFragment")) {
            playerSessionsFragment = PlayerSessionsFragment.newInstance();
            transaction.add(R.id.container_main_player, playerSessionsFragment,"xMainPlayerSessionsFragment");
            transaction.hide(playerSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainMapsFragment")) {
            Bundle bundle = new Bundle();
            bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
            mapsFragment = MapsFragment.newInstance();
            mapsFragment.setArguments(bundle);
            transaction.add(R.id.container_main_player, mapsFragment,"xMainMapsFragment");
            transaction.hide(mapsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainListSessionsFragment")) {
            listSessionsFragment = ListSessionsFragment.newInstance();
            transaction.add(R.id.container_main_player, listSessionsFragment,"xMainListSessionsFragment");
            transaction.hide(listSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainInboxFragment")) {
            inboxFragment = InboxFragment.newInstance();
            transaction.add(R.id.container_main_player, inboxFragment,"xMainInboxFragment");
            transaction.hide(inboxFragment);
        }

        resumed=true;
        bottomNavigation.setCurrentItem(bottomNavigation.getCurrentItem());
        transaction.commitNow();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private Task<HashMap<String, Object>> retrieveStripeCustomer(String customerID) {

        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("retrieveCustomer")
                .call(customerID)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }
}
