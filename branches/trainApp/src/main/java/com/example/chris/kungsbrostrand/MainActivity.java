package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  WeekdayFilterFragment.OnSessionsFilteredListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    UserProfileFragment userProfileFragment;
    WeekdayFilterFragment weekdayFilterFragment;
    ListSessionsFragment listSessionsFragment;
    MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        if (weekdayFilterFragment==null) {
            weekdayFilterFragment = WeekdayFilterFragment.newInstance();
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("userProfileFragment")) {
            transaction.add(R.id.main_container, userProfileFragment,"userProfileFragment");
            transaction.detach(userProfileFragment);
        }

        if (null == fragmentManager.findFragmentByTag("weekdayFragment")) {
            transaction.add(R.id.weekdayFilterFragmentContainer, weekdayFilterFragment,"weekdayFragment");
            transaction.detach(weekdayFilterFragment);
        }

        if (null == fragmentManager.findFragmentByTag("mapsFragment")) {
            transaction.add(R.id.main_container, mapsFragment,"mapsFragment");
            transaction.detach(mapsFragment);
        }

        if (null == fragmentManager.findFragmentByTag("ListSessionsFragment")) {
            transaction.add(R.id.main_container, listSessionsFragment,"ListSessionsFragment");
            transaction.detach(listSessionsFragment);
        }

        transaction.commit();

        fragmentManager.executePendingTransactions();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch (id){
                    case R.id.menuMap:
                        FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                        transaction1.detach(listSessionsFragment);
                        transaction1.detach(userProfileFragment);
                        transaction1.attach(weekdayFilterFragment);
                        transaction1.attach(mapsFragment);
                        transaction1.commit();
                        getSupportActionBar().setTitle("Map");
                        break;
                    case R.id.menuList:
                        FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                        transaction2.detach(mapsFragment);
                        transaction2.detach(userProfileFragment);
                        transaction2.attach(weekdayFilterFragment);
                        transaction2.attach(listSessionsFragment);
                        transaction2.commit();
                        getSupportActionBar().setTitle("Sessions");
                        break;
                    case R.id.menuProfile:
                        FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                        transaction3.detach(weekdayFilterFragment);
                        transaction3.detach(mapsFragment);
                        transaction3.detach(listSessionsFragment);
                        transaction3.attach(userProfileFragment);
                        transaction3.commit();
                        getSupportActionBar().setTitle("Profile");
                        break;
                }
                return true;
            }
        });

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
    public void OnSessionsFiltered(ArrayList<Session> sessions, Location location) {

        MapsFragment mapsFragment = (MapsFragment) fragmentManager.findFragmentByTag("mapsFragment");
        mapsFragment.addMarkersToMap(sessions,location);

        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) fragmentManager.findFragmentByTag("ListSessionsFragment");
        listSessionsFragment.FilterSessions(sessions,location);

    }
}
