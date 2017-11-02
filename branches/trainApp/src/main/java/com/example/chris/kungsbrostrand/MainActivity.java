package com.example.chris.kungsbrostrand;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class MainActivity extends AppCompatActivity implements  OnWeekdayChangedListener, OnWeekdayButtonClickedListener{
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private UserProfileFragment userProfileFragment;
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;
    private MyFirebaseDatabase myFirebaseDatabase;
    private DatabaseReference mDatabase;
    private HashMap<String,Boolean> firstWeekdayHashMap;
    private HashMap<String,Boolean> secondWeekdayHashMap;
    boolean locationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission=true;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        if (locationPermission) {
            BottomNavigationView bottomNavigation;
            FirebaseAuth.AuthStateListener mAuthListener;

            myFirebaseDatabase= new MyFirebaseDatabase();

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

            checkUserExist();

            bottomNavigation = findViewById(R.id.bottom_navigation);
            fragmentManager = getSupportFragmentManager();

            // Initiate SessionContainer Fragment

            if (userProfileFragment==null) {
                userProfileFragment = UserProfileFragment.newInstance();
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
                transaction.add(R.id.main_container, userProfileFragment,"userProfileFragment");
            }

            if (null == fragmentManager.findFragmentByTag("mapsFragment")) {
                transaction.add(R.id.main_container, mapsFragment,"mapsFragment");
            }

            if (null == fragmentManager.findFragmentByTag("ListSessionsFragment")) {
                transaction.add(R.id.main_container, listSessionsFragment,"ListSessionsFragment");
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

            bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    RelativeLayout weekdayFilterContainer = findViewById(R.id.weekdayFilterFragmentContainer);

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

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabase.child("users").addValueEventListener(new ValueEventListener() { ////// TA BORT signingUp FIXA SÅ ATT LISTENER PAUSAS UNDER REG https://stackoverflow.com/questions/44435763/firebase-value-event-listener-firing-even-after-activity-is-finished
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(user_id)){

                        Intent setupIntent = new Intent(MainActivity.this,SetupAccountActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationPermission=true;
                        recreate();
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    this.finishAffinity();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

}
