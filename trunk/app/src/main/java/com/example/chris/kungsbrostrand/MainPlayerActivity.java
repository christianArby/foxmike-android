package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rd.PageIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.Inflater;

public class MainPlayerActivity extends AppCompatActivity implements  OnWeekdayChangedListener, OnWeekdayButtonClickedListener, OnSessionClickedListener{
    private FragmentManager fragmentManager;
    private UserProfileFragment userProfileFragment;
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;
    private PlayerSessionsFragment playerSessionsFragment;
    private DisplaySessionFragment displaySessionFragment;
    private MyFirebaseDatabase myFirebaseDatabase;
    private HashMap<String,Boolean> firstWeekdayHashMap;
    private HashMap<String,Boolean> secondWeekdayHashMap;
    private BottomNavigationView bottomNavigation;
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
            userProfileFragment = UserProfileFragment.newInstance();
        }
        if (playerSessionsFragment==null) {
            playerSessionsFragment = PlayerSessionsFragment.newInstance();
        }
        if (listSessionsFragment==null) {
            listSessionsFragment = ListSessionsFragment.newInstance();
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

        transaction.commit();

        WrapContentViewPager weekdayViewpager = findViewById(R.id.weekdayPager);
        weekdayViewpager.setAdapter(new weekdayViewpagerAdapter(fragmentManager));

        //Bind the title indicator to the adapter
        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(weekdayViewpager);

        int selectedColor = Color.parseColor("#303F9F");
        int unSelectedColor = Color.parseColor("#E0E0E0");
        pageIndicatorView.setSelectedColor(selectedColor);
        pageIndicatorView.setUnselectedColor(unSelectedColor);

        fragmentManager.executePendingTransactions();

        BottomNavigationViewHelper.disableShiftMode(bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                weekdayFilterContainer = findViewById(R.id.weekdayFilterFragmentContainer);
                final int id = item.getItemId();

                cleanMainActivity();

                mapOrListBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (id==R.id.menuListOrMap) {

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

                    }
                });

                switch (id){
                    case R.id.menuNewsFeed:
                        mapOrListBtn.setVisibility(View.VISIBLE);
                        mapOrListBtn.setText("NEWSFEED WILL BE HERE...");
                        break;
                    case R.id.menuListOrMap:
                        weekdayFilterContainer.setVisibility(View.VISIBLE);
                        FragmentTransaction transaction5 = fragmentManager.beginTransaction();
                        transaction5.show(listSessionsFragment);
                        transaction5.commit();
                        mapOrListBtn.setVisibility(View.VISIBLE);
                        mapOrListBtn.setText("Map");
                        break;
                    case R.id.menuPlayerSessions:
                        FragmentTransaction transaction6 = fragmentManager.beginTransaction();
                        transaction6.show(playerSessionsFragment);
                        transaction6.commit();
                        break;
                    case R.id.menuInbox:
                        FragmentTransaction transaction7 = fragmentManager.beginTransaction();
                        transaction7.commit();
                        break;
                    case R.id.menuProfile:
                        FragmentTransaction transaction8 = fragmentManager.beginTransaction();
                        transaction8.show(userProfileFragment);
                        transaction8.commit();
                        break;
                }
                return true;
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

        bottomNavigation.setSelectedItemId(R.id.menuNewsFeed);
    }

    private void cleanMainActivity() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (displaySessionFragment!=null) {
            transaction.remove(displaySessionFragment);
        }
        transaction.hide(mapsFragment);
        transaction.hide(listSessionsFragment);
        transaction.hide(userProfileFragment);
        transaction.hide(playerSessionsFragment);
        transaction.commit();
        weekdayFilterContainer.setVisibility(View.GONE);
        mapOrListBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            //getFragmentManager().popBackStack();
        }

    }

    @Override
    public void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity) {
        if (week==1) {
            firstWeekdayHashMap.put(weekdayKey,weekdayBoolean);
            myFirebaseDatabase.filterSessions(new OnSessionsFilteredListener() {
                @Override
                public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {
                    MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
                    mapsFragment.addMarkersToMap(sessions,location);

                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
                    listSessionsFragment.generateSessionListView(sessions,location);
                }
            }, firstWeekdayHashMap, secondWeekdayHashMap, activity);
        }
        if (week==2) {
            secondWeekdayHashMap.put(weekdayKey,weekdayBoolean);
            myFirebaseDatabase.filterSessions(new OnSessionsFilteredListener() {
                @Override
                public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {
                    MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
                    mapsFragment.addMarkersToMap(sessions,location);

                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
                    listSessionsFragment.generateSessionListView(sessions,location);
                }
            }, firstWeekdayHashMap, secondWeekdayHashMap, activity);
        }
    }

    @Override
    public void OnWeekdayButtonClicked(int week, int button, HashMap<Integer, Boolean> toggleHashMap) {
        HashMap<Integer, Boolean> toggleMap1;
        HashMap<Integer, Boolean> toggleMap2;
        FragmentManager fragmentManager = getSupportFragmentManager();
        WeekdayFilterFragment weekdayFilterFragment = (WeekdayFilterFragment) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,0));
        WeekdayFilterFragmentB weekdayFilterFragmentB = (WeekdayFilterFragmentB) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,1));

        toggleMap1 = weekdayFilterFragment.getToggleMap1();
        toggleMap2 = weekdayFilterFragmentB.getToggleMap2();

        weekdayFilterFragment.changeToggleMap(week,button,toggleMap1,toggleMap2);
        weekdayFilterFragmentB.changeToggleMap(week,button,toggleMap1,toggleMap2);

        toggleMap1 = weekdayFilterFragment.getAndUpdateToggleMap1();
        weekdayFilterFragmentB.setToggleMap1(toggleMap1);
        toggleMap2 = weekdayFilterFragmentB.getAndUpdateToggleMap2();
        weekdayFilterFragment.setToggleMap2(toggleMap2);

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

    class weekdayViewpagerAdapter extends FragmentPagerAdapter {


        public weekdayViewpagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            if (position == 0) {
                fragment = WeekdayFilterFragment.newInstance();
            }

            if (position == 1) {
                fragment = WeekdayFilterFragmentB.newInstance();
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