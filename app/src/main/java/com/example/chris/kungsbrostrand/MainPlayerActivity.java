package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.rd.PageIndicatorView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainPlayerActivity extends AppCompatActivity implements  OnWeekdayChangedListener, OnWeekdayButtonClickedListener, OnSessionClickedListener, UserProfileFragment.OnUserProfileFragmentInteractionListener, UserProfilePublicFragment.OnUserProfilePublicFragmentInteractionListener, UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener{
    private FragmentManager fragmentManager;
    private UserProfileFragment userProfileFragment;
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;
    private PlayerSessionsFragment playerSessionsFragment;
    private DisplaySessionFragment displaySessionFragment;
    private InboxFragment inboxFragment;
    private UserProfilePublicFragment userProfilePublicFragment;
    private UserProfilePublicEditFragment userProfilePublicEditFragment;
    private MyFirebaseDatabase myFirebaseDatabase;
    public HashMap<String,Boolean> firstWeekdayHashMap;
    public HashMap<String,Boolean> secondWeekdayHashMap;
    private BottomBar bottomNavigation;
    private Button mapOrListBtn;
    private RelativeLayout weekdayFilterContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);

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

        bottomNavigation = findViewById(R.id.bottom_navigation_player);
        fragmentManager = getSupportFragmentManager();
        mapOrListBtn = findViewById(R.id.map_or_list_button);

        if (userProfileFragment==null) {
            userProfileFragment = new UserProfileFragment();
        }
        if (playerSessionsFragment==null) {
            playerSessionsFragment = PlayerSessionsFragment.newInstance();
        }
        if (listSessionsFragment==null) {
            listSessionsFragment = ListSessionsFragment.newInstance();
        }

        if (inboxFragment==null) {
            inboxFragment = InboxFragment.newInstance();
        }

        if (userProfilePublicFragment==null) {
            userProfilePublicFragment = UserProfilePublicFragment.newInstance();
        }

        if (userProfilePublicEditFragment==null) {
            userProfilePublicEditFragment = userProfilePublicEditFragment.newInstance();
        }

        if (mapsFragment==null) {
            Bundle bundle = new Bundle();
            bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
            mapsFragment = MapsFragment.newInstance();
            mapsFragment.setArguments(bundle);
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("userProfileFragment")) {
            transaction.add(R.id.container_main_player, userProfileFragment,"userProfileFragment");
        }

        if (null == fragmentManager.findFragmentByTag("playerSessionsFragment")) {
            transaction.add(R.id.container_main_player, playerSessionsFragment,"playerSessionsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("mapsFragment")) {
            transaction.add(R.id.container_main_player, mapsFragment,"mapsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("ListSessionsFragment")) {
            transaction.add(R.id.container_main_player, listSessionsFragment,"ListSessionsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("InboxFragment")) {
            transaction.add(R.id.container_main_player, inboxFragment,"InboxFragment");
        }

        if (null == fragmentManager.findFragmentByTag("userProfilePublicFragment")) {
            transaction.add(R.id.container_main_player, userProfilePublicFragment,"userProfilePublicFragment");
        }

        if (null == fragmentManager.findFragmentByTag("userProfilePublicEditFragment")) {
            transaction.add(R.id.container_main_player, userProfilePublicEditFragment,"userProfilePublicEditFragment");
        }

        transaction.commit();

        WrapContentViewPager weekdayViewpager = findViewById(R.id.weekdayPager);
        weekdayViewpager.setAdapter(new weekdayViewpagerAdapter(fragmentManager));

        //Bind the title indicator to the adapter
        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(weekdayViewpager);

        int selectedColor = Color.parseColor("#003d00");
        int unSelectedColor = Color.parseColor("#E0E0E0");
        pageIndicatorView.setSelectedColor(selectedColor);
        pageIndicatorView.setUnselectedColor(unSelectedColor);

        fragmentManager.executePendingTransactions();

        mapOrListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapsFragment.isVisible()) {
                    FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                    transaction1.hide(mapsFragment);
                    transaction1.show(listSessionsFragment);
                    transaction1.commit();
                    mapOrListBtn.setText("Map");
                } else if (listSessionsFragment.isVisible()) {
                    FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                    transaction2.hide(listSessionsFragment);
                    transaction2.show(mapsFragment);
                    transaction2.commit();
                    mapOrListBtn.setText("List");

                    // Ska detta finnas?
                } else {
                    weekdayFilterContainer.setVisibility(View.VISIBLE);
                    FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                    transaction3.hide(mapsFragment);
                    transaction3.show(listSessionsFragment);
                    transaction3.commit();
                    mapOrListBtn.setVisibility(View.VISIBLE);
                    mapOrListBtn.setText("Map");
                }
            }

        });

        bottomNavigation.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes final int tabId) {
                weekdayFilterContainer = findViewById(R.id.weekdayFilterFragmentContainer);

                cleanMainActivity();

                switch (tabId) {
                    case R.id.menuNewsFeed:
                        mapOrListBtn.setVisibility(View.VISIBLE);
                        mapOrListBtn.setText("NEWSFEED WILL BE HERE...");
                        Intent AllUsersIntent = new Intent(MainPlayerActivity.this, AllUsersActivity.class);
                        startActivity(AllUsersIntent);
                        break;
                    case R.id.menuListOrMap:
                        cleanMainActivityAndSwitch(listSessionsFragment);
                        weekdayFilterContainer.setVisibility(View.VISIBLE);
                        mapOrListBtn.setVisibility(View.VISIBLE);
                        mapOrListBtn.setText("Map");
                        break;
                    case R.id.menuPlayerSessions:
                        cleanMainActivityAndSwitch(playerSessionsFragment);
                        break;
                    case R.id.menuInbox:
                        cleanMainActivityAndSwitch(inboxFragment);
                        break;
                    case R.id.menuProfile:
                        cleanMainActivityAndSwitch(userProfileFragment);
                        break;
                }
            }
        });

        myFirebaseDatabase= new MyFirebaseDatabase();

        myFirebaseDatabase.filterSessions(new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
                mapsFragment.addMarkersToMap(sessions,location);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
                listSessionsFragment.generateSessionListView(sessions,location);
            }
        }, firstWeekdayHashMap, secondWeekdayHashMap, this);

    }

    // TODO cleanMainActivity is probably useless once Newsfeed fragment has been created, delete this functionality then
    private void cleanMainActivity() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (displaySessionFragment!=null) {
            transaction.remove(displaySessionFragment);
        }
        transaction.hide(inboxFragment);
        transaction.hide(mapsFragment);
        transaction.hide(listSessionsFragment);
        transaction.hide(userProfileFragment);
        transaction.hide(userProfilePublicFragment);
        transaction.hide(userProfilePublicEditFragment);
        transaction.hide(playerSessionsFragment);
        transaction.commit();
        weekdayFilterContainer.setVisibility(View.GONE);
        mapOrListBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (displaySessionFragment!=null) {
            if (displaySessionFragment.isVisible()) {
                transaction.hide(displaySessionFragment).addToBackStack("displaySessionFragment");
            }
        }
        if (inboxFragment.isVisible()) {
            transaction.hide(inboxFragment).addToBackStack("inboxFragment");
        }
        if (mapsFragment.isVisible()) {
            transaction.hide(mapsFragment).addToBackStack("mapsFragment");
        }
        if (listSessionsFragment.isVisible()) {
            transaction.hide(listSessionsFragment).addToBackStack("listSessionsFragment");
        }
        if (userProfileFragment.isVisible()) {
            transaction.hide(userProfileFragment).addToBackStack("userProfileFragment");
        }
        if (userProfilePublicFragment.isVisible()) {
            transaction.hide(userProfilePublicFragment).addToBackStack("userProfilePublicFragment");
        }
        if (userProfilePublicEditFragment.isVisible()) {
            transaction.hide(userProfilePublicEditFragment);
        }
        if (playerSessionsFragment.isVisible()) {
            transaction.hide(playerSessionsFragment).addToBackStack("playerSessionsFragment");
        }

        transaction.show(fragment).addToBackStack("fragment");
        transaction.commit();
        weekdayFilterContainer.setVisibility(View.GONE);
        mapOrListBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {
            if (userProfilePublicFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            // TODO Add Newsfeed fragment here later when exist
            if (!listSessionsFragment.isVisible()&&!mapsFragment.isVisible()&&!playerSessionsFragment.isVisible()&&!userProfileFragment.isVisible()&&!inboxFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
        }

    }


    @Override
    public void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity) {
        if (week==1) {
            firstWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
        if (week==2) {
            secondWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
    }

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

        myFirebaseDatabase.filterSessions(new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
                mapsFragment.addMarkersToMap(sessions,location);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
                listSessionsFragment.generateSessionListView(sessions,location);
            }
        }, firstWeekdayHashMap, secondWeekdayHashMap, this);

    }

    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (displaySessionFragment!=null) {
            transaction.remove(displaySessionFragment);
        }

        displaySessionFragment = DisplaySessionFragment.newInstance(sessionLatitude,sessionLongitude);
        displaySessionFragment.show(transaction,"displaySessionFragment");

    }

    @Override
    public void OnUserProfileFragmentInteraction() {
        cleanMainActivityAndSwitch(userProfilePublicFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void OnUserProfilePublicFragmentInteraction() {
        cleanMainActivityAndSwitch(userProfilePublicEditFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
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
}
