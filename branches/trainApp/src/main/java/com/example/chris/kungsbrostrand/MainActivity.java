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

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  WeekdayFilterFragment.OnSessionsFilteredListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String tag;

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
        tag = "SessionContainer";
        fragment = SessionContainerFragment.newInstance("ListSessionsFragment");

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag(tag)) {
            transaction.add(R.id.main_container, fragment,tag);
        } else {
            transaction.show(fragmentManager.findFragmentByTag(tag));
        }
        transaction.commit();

        fragmentManager.executePendingTransactions();

        final SessionContainerFragment sessionContainerFragment = (SessionContainerFragment) fragmentManager.findFragmentByTag(tag);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.action_favorites:
                        sessionContainerFragment.chooseSessionFragmentType("mapsFragment");
                        break;
                    case R.id.action_schedules:
                        sessionContainerFragment.chooseSessionFragmentType("ListSessionsFragment");
                        break;
                    case R.id.action_music:
                        sessionContainerFragment.chooseSessionFragmentType("ListSessionsFragment");
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()== R.id.action_my_page){

            startActivity(new Intent(MainActivity.this, UserActivity.class));

        }

        if(item.getItemId()== R.id.action_list){


        }

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
