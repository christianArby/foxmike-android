package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rd.PageIndicatorView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPlayerActivity extends AppCompatActivity

        implements
        OnWeekdayChangedListener,
        OnWeekdayButtonClickedListener,
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        OnNewMessageListener,
        ListSessionsFragment.OnRefreshSessionsListener,
        OnUserClickedListener,
        ListSessionsFragment.OnListSessionsScrollListener,
        SortAndFilterFragment.OnListSessionsFilterListener,
        SortAndFilterFragment.OnListSessionsSortListener{

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
    private String sortType = "date";
    private int distanceRadius;
    Boolean started = false;

    private SortAndFilterFragment sortAndFilterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);

        distanceRadius = this.getResources().getInteger(R.integer.distanceMax);

        // get views
        weekdayFilterContainer = findViewById(R.id.weekdayFilterFragmentContainer);
        bottomNavigation = findViewById(R.id.bottom_navigation_player);
        mapOrListBtn = findViewById(R.id.map_or_list_button);
        sortAndFilterFAB = findViewById(R.id.sort_button);
        myLocationBtn = findViewById(R.id.my_location_button);
        WrapContentViewPager weekdayViewpager = findViewById(R.id.weekdayPager);
        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);

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
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        userAccountFragment = new UserAccountFragment();
        playerSessionsFragment = PlayerSessionsFragment.newInstance();
        listSessionsFragment = ListSessionsFragment.newInstance();
        inboxFragment = InboxFragment.newInstance();
        userProfileFragment = UserProfileFragment.newInstance();
        userProfilePublicEditFragment = userProfilePublicEditFragment.newInstance();
        allUsersFragment = allUsersFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        mapsFragment = MapsFragment.newInstance();
        mapsFragment.setArguments(bundle);

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
        if (null == fragmentManager.findFragmentByTag("xMainUserProfileFragment")) {
            transaction.add(R.id.container_main_player, userProfileFragment,"xMainUserProfileFragment");
            transaction.hide(userProfileFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainUserProfilePublicEditFragment")) {
            transaction.add(R.id.container_main_player, userProfilePublicEditFragment,"xMainUserProfilePublicEditFragment");
            transaction.hide(userProfilePublicEditFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainAllUsersFragment")) {
            transaction.add(R.id.container_main_player, allUsersFragment,"xMainAllUsersFragment");
            transaction.hide(allUsersFragment);
        }
        transaction.commitNow();

        // Setup weekdaypager
        weekdayViewpager.setAdapter(new weekdayViewpagerAdapter(fragmentManager));
        pageIndicatorView.setViewPager(weekdayViewpager);

        fragmentManager.executePendingTransactions();

        /** Check if activity has been started due to notification, if so get from user ID and open up profile*/
        if (fromUserID!=null) {
            allUsersFragment.onUserClickedListener.OnUserClicked(fromUserID);
        } else {
            cleanMainActivityAndSwitch(allUsersFragment);
        }

        /** Setup List and Map with sessions*/
        myFirebaseDatabase= new MyFirebaseDatabase();
        myFirebaseDatabase.getNearSessions(this, distanceRadius, new OnNearSessionsFoundListener() {
            @Override
            public void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location) {
                sessionListArrayList.clear();
                sessionListArrayList = nearSessions;
                locationClosetoSessions = location;
                myFirebaseDatabase.filterSessions(nearSessions, firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
                    @Override
                    public void OnSessionsFiltered(ArrayList<Session> sessions) {
                        MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                        mapsFragment.addMarkersToMap(sessions,locationClosetoSessions);

                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                        listSessionsFragment.generateSessionListView(sessions,locationClosetoSessions);
                    }
                });
            }
        });

        /** Setup Bottom navigation */
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        if (!wasSelected) {
                            if (fromUserID!=null) {
                                allUsersFragment.onUserClickedListener.OnUserClicked(fromUserID);
                            } else {
                                cleanMainActivityAndSwitch(allUsersFragment);
                            }
                            return true;
                        }
                    case 1:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(listSessionsFragment);
                            weekdayFilterContainer.setVisibility(View.VISIBLE);
                            mapOrListBtn.setVisibility(View.VISIBLE);
                            mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
                            sortAndFilterFAB.setVisibility(View.VISIBLE);
                            return true;
                        }
                    case 2:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(playerSessionsFragment);
                            return true;
                        }
                    case 3:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(inboxFragment);
                            return true;
                        }
                    case 4:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(userAccountFragment);
                            return true;
                        }
                }
                return false;
            }
        });
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.secondaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

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
                            bottomNavigation.setNotification(Integer.toString(nrOfUnreadChats),3);
                        } else {
                            bottomNavigation.setNotification("",3);
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
                if (mapsFragment.isVisible()) {
                    FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                    transaction1.hide(mapsFragment);
                    transaction1.show(listSessionsFragment);
                    transaction1.commit();
                    mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
                    myLocationBtn.setVisibility(View.GONE);
                } else if (listSessionsFragment.isVisible()) {
                    FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                    transaction2.hide(listSessionsFragment);
                    transaction2.show(mapsFragment);
                    transaction2.commit();
                    mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_list_black_24dp));
                    myLocationBtn.setVisibility(View.VISIBLE);

                    // Ska detta finnas?
                } else {
                    weekdayFilterContainer.setVisibility(View.VISIBLE);
                    FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                    transaction3.hide(mapsFragment);
                    transaction3.show(listSessionsFragment);
                    transaction3.commit();
                    mapOrListBtn.setVisibility(View.VISIBLE);
                    mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
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
    }

    /** FUNCTION to clean whole activity and switch fragment*/
    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag().substring(0,5).equals("xMain")) {
                if (frag.isVisible()) {
                    transaction.hide(frag);
                }
            }
        }
        transaction.show(fragment).addToBackStack("fragment");
        transaction.commit();
        weekdayFilterContainer.setVisibility(View.GONE);
        mapOrListBtn.setVisibility(View.GONE);
        sortAndFilterFAB.setVisibility(View.GONE);
        myLocationBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    /** FUNCTION to override onBackPressed*/
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {
            if (userProfileFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            // TODO Add Newsfeed fragment here later when exist
            if (!listSessionsFragment.isVisible()&&!mapsFragment.isVisible()&&!playerSessionsFragment.isVisible()&&!userAccountFragment.isVisible()&&!inboxFragment.isVisible()&&!allUsersFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
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

        myFirebaseDatabase.filterSessions(sessionListArrayList, firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                mapsFragment.addMarkersToMap(sessions,locationClosetoSessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
            }
        });
    }

    /** INTERFACE to listen to events which should trigger display session */
    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (displaySessionFragment!=null) {
            transaction.remove(displaySessionFragment);
        }
        displaySessionFragment = DisplaySessionFragment.newInstance(sessionLatitude,sessionLongitude);
        displaySessionFragment.show(transaction,"displaySessionFragment");
    }

    /** INTERFACES to listen to events which should trigger userProfileFragment */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            cleanMainActivityAndSwitch(userProfileFragment);
            bottomNavigation.setVisibility(View.GONE);
        }
    }
    @Override
    public void onUserProfileFragmentInteraction() {
        cleanMainActivityAndSwitch(userProfilePublicEditFragment);
        bottomNavigation.setVisibility(View.GONE);
    }
    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**CHECK THIS */
    @Override
    public void OnUserClicked(String otherUserID) {

        if (fragmentManager.findFragmentByTag("xMainUserProfilePublicFragment") != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragmentManager.findFragmentByTag("xMainUserProfilePublicFragment"));
            transaction.commitNow();

            Bundle bundle = new Bundle();
            bundle.putString("otherUserID", otherUserID);
            userProfilePublicFragment = UserProfilePublicFragment.newInstance();
            userProfilePublicFragment.setArguments(bundle);

            FragmentTransaction transaction2 = fragmentManager.beginTransaction();
            transaction2.add(R.id.container_main_player, userProfilePublicFragment,"xMainUserProfilePublicFragment");
            transaction2.hide(userProfilePublicFragment);
            transaction2.commitNow();

            cleanMainActivityAndSwitch(userProfilePublicFragment);
        } else {

            Bundle bundle = new Bundle();
            bundle.putString("otherUserID", otherUserID);
            userProfilePublicFragment = UserProfilePublicFragment.newInstance();
            userProfilePublicFragment.setArguments(bundle);

            FragmentTransaction transaction3 = fragmentManager.beginTransaction();
            transaction3.add(R.id.container_main_player, userProfilePublicFragment,"xMainUserProfilePublicFragment");
            transaction3.hide(userProfilePublicFragment);
            transaction3.commitNow();

            cleanMainActivityAndSwitch(userProfilePublicFragment);
        }
    }
    @Override
    public void OnNewMessage() {

    }

    /** INTERFACE triggered when list is scrolled */
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
            sortAndFilterFAB.show();
            mapOrListBtn
                    .animate()
                    .translationY(0)
                    .withLayer()
                    .start();
            //mapOrListBtn.show();
        }
    }

    /** INTERFACE triggered when list is scrolled REFRESHED, downloads all sessions based on input*/
    @Override
    public void OnRefreshSessions() {

        myFirebaseDatabase= new MyFirebaseDatabase();
        myFirebaseDatabase.getNearSessions(this, distanceRadius, new OnNearSessionsFoundListener() {
            @Override
            public void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location) {
                sessionListArrayList.clear();
                sessionListArrayList = nearSessions;
                locationClosetoSessions = location;
                myFirebaseDatabase.filterSessions(nearSessions, firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
                    @Override
                    public void OnSessionsFiltered(ArrayList<Session> sessions) {
                        MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                        mapsFragment.addMarkersToMap(sessions,locationClosetoSessions);

                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                        listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
                        listSessionsFragment.stopSwipeRefreshingSymbol();
                    }
                });
            }
        });
    }

    /** INTERFACE triggered sort buttons are clicked, SORTS sessions*/
    @Override
    public void OnListSessionsSort(String sortType) {
        this.sortType = sortType;
        myFirebaseDatabase.filterSessions(sessionListArrayList, firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                mapsFragment.addMarkersToMap(sessions,locationClosetoSessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
        });
    }

    /** INTERFACE triggered filter buttons are clicked, FILTERS sessions*/
    @Override
    public void OnListSessionsFilter(int filterDistance) {

        distanceRadius = filterDistance;
        myFirebaseDatabase= new MyFirebaseDatabase();
        // TODO if new filtersessions int is smaller than previous this funtion should only filter and not download
        myFirebaseDatabase.getNearSessions(this, distanceRadius, new OnNearSessionsFoundListener() {
            @Override
            public void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location) {

                sessionListArrayList.clear();
                sessionListArrayList = nearSessions;
                locationClosetoSessions = location;

                myFirebaseDatabase.filterSessions(nearSessions, firstWeekdayHashMap, secondWeekdayHashMap, sortType, new OnSessionsFilteredListener() {
                    @Override
                    public void OnSessionsFiltered(ArrayList<Session> sessions) {
                        MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("xMainMapsFragment");
                        mapsFragment.addMarkersToMap(sessions,locationClosetoSessions);

                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("xMainListSessionsFragment");
                        listSessionsFragment.updateSessionListView(sessions,locationClosetoSessions);
                    }
                });
            }
        });
    }

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

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            //User is signed out
            Intent loginIntent = new Intent(MainPlayerActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }

    }

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

    /*public void listenToInbox() {

        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid());

        mMessageDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for dataSnapshot.getChildren()


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

        messageChildEventListener = lastMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String data = dataSnapshot.child("message").getValue().toString();
                holder.setMessage(data, model.isSeen());
                if (!model.isSeen()) {
                    onNewMessageListener.OnNewMessage();
                }

                childEventListenerMap.put(dataSnapshot.getRef(), messageChildEventListener);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
    }*/
}
