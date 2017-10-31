package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.rd.PageIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements  OnWeekdayChangedListener, OnWeekdayButtonClickedListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    UserProfileFragment userProfileFragment;
    WeekdayFilterFragment weekdayFilterFragment;
    //WeekdayFilterFragmentB weekdayFilterFragmentB;
    ListSessionsFragment listSessionsFragment;
    MapsFragment mapsFragment;
    WrapContentViewPager weekdayViewpager;
    MyFirebaseDatabase myFirebaseDatabase;

    public HashMap<String,Boolean> firstWeekdayHashMap;
    public HashMap<String,Boolean> secondWeekdayHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myFirebaseDatabase= new MyFirebaseDatabase();

        firstWeekdayHashMap = new HashMap<String,Boolean>();
        secondWeekdayHashMap = new HashMap<String,Boolean>();
        Session dummySession = new Session();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar todayCal = Calendar.getInstance();
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



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()== null){
                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        bottomNavigation = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        // Initiate SessionContainer Fragment

        if (userProfileFragment==null) {
            userProfileFragment = userProfileFragment.newInstance();
        }

        if (listSessionsFragment==null) {
            listSessionsFragment = ListSessionsFragment.newInstance();
        }

        if (mapsFragment==null) {
            mapsFragment = MapsFragment.newInstance();
        }


        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("userProfileFragment")) {
            transaction.add(R.id.main_container, userProfileFragment,"userProfileFragment");
        }

        if (null == fragmentManager.findFragmentByTag("mapsFragment")) {
            transaction.add(R.id.main_container, mapsFragment,"mapsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("ListSessionsFragment")) {
            transaction.add(R.id.main_container, listSessionsFragment,"ListSessionsFragment");
        }

        transaction.commit();

        weekdayViewpager = (WrapContentViewPager) findViewById(R.id.weekdayPager);
        weekdayViewpager.setAdapter(new weekdayViewpagerAdapter(fragmentManager));

        //Bind the title indicator to the adapter
        PageIndicatorView pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(weekdayViewpager);


        int selectedColor = Color.parseColor("#303F9F");
        int unSelectedColor = Color.parseColor("#E0E0E0");

        pageIndicatorView.setSelectedColor(selectedColor);
        pageIndicatorView.setUnselectedColor(unSelectedColor);

        fragmentManager.executePendingTransactions();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                RelativeLayout weekdayFilterContainer = (RelativeLayout) findViewById(R.id.weekdayFilterFragmentContainer);

                int id = item.getItemId();
                switch (id){
                    case R.id.menuMap:
                        FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                        transaction1.hide(listSessionsFragment);
                        transaction1.hide(userProfileFragment);
                        weekdayFilterContainer.setVisibility(View.VISIBLE);
                        transaction1.show(mapsFragment);
                        transaction1.commit();
                        getSupportActionBar().setTitle("Map");
                        break;
                    case R.id.menuList:
                        FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                        transaction2.hide(mapsFragment);
                        transaction2.hide(userProfileFragment);
                        weekdayFilterContainer.setVisibility(View.VISIBLE);
                        //transaction2.attach(weekdayFilterFragment);
                        transaction2.show(listSessionsFragment);
                        transaction2.commit();
                        getSupportActionBar().setTitle("Sessions");
                        break;
                    case R.id.menuProfile:
                        FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                        weekdayFilterContainer.setVisibility(View.GONE);
                        transaction3.hide(mapsFragment);
                        transaction3.hide(listSessionsFragment);
                        transaction3.show(userProfileFragment);
                        transaction3.commit();
                        getSupportActionBar().setTitle("Profile");
                        break;
                }
                return true;
            }
        });

        myFirebaseDatabase.filterSessions(new OnSessionsFilteredListener() {
            @Override
            public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {
                MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
                mapsFragment.addMarkersToMap(sessions,location);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
                listSessionsFragment.FilterSessions(sessions,location);
            }
        }, firstWeekdayHashMap, secondWeekdayHashMap, this);

        bottomNavigation.setSelectedItemId(R.id.menuMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()== R.id.action_logout){

            logout();

        }

        if(item.getItemId()== R.id.action_clear_db){

            /*mDeleteMarkerDbRef.removeValue();
            mMarkerDbRef.removeValue();
            mDMarkerDbRef.removeValue();
            mDeleteMarkerDbRef.setValue(null);
            mMarkerDbRef.setValue(null);
            mDMarkerDbRef.setValue(null);*/

        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
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
                    listSessionsFragment.FilterSessions(sessions,location);
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
                    listSessionsFragment.FilterSessions(sessions,location);
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
