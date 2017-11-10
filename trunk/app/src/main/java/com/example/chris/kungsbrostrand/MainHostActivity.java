package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainHostActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private UserProfileFragment userProfileFragment;
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;
    private HostSessionsFragment hostSessionsFragment;
    private MyFirebaseDatabase myFirebaseDatabase;
    private DatabaseReference mDatabase;
    private HashMap<String,Boolean> firstWeekdayHashMap;
    private HashMap<String,Boolean> secondWeekdayHashMap;
    private BottomNavigationView bottomNavigation;
    boolean locationPermission;
    private Button mapOrListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host);

        if (ContextCompat.checkSelfPermission(MainHostActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission=true;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermission = checkLocationPermission();
        }

        if (locationPermission) {
            FirebaseAuth.AuthStateListener mAuthListener;
            myFirebaseDatabase= new MyFirebaseDatabase();

            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    if(firebaseAuth.getCurrentUser()== null){
                        Intent loginIntent = new Intent(MainHostActivity.this,LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                    }
                }
            };
            mAuth.addAuthStateListener(mAuthListener);
            myFirebaseDatabase.checkUserExist(mAuth, mDatabase, this);
            if (mAuth.getCurrentUser()==null) {
                Intent loginIntent = new Intent(MainHostActivity.this,LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            } else {
                updateUI();
            }
        }
    }

    public void updateUI() {
        bottomNavigation = findViewById(R.id.bottom_navigation_host);
        fragmentManager = getSupportFragmentManager();

        if (userProfileFragment==null) {
            userProfileFragment = UserProfileFragment.newInstance();
        }
        if (hostSessionsFragment==null) {
            hostSessionsFragment = HostSessionsFragment.newInstance();
        }
        if (mapsFragment==null) {
            Bundle bundle = new Bundle();
            bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
            mapsFragment = MapsFragment.newInstance();
            mapsFragment.setArguments(bundle);
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("userProfileFragment")) {
            transaction.add(R.id.container_main_host, userProfileFragment,"userProfileFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostSessionsFragment")) {
            transaction.add(R.id.container_main_host, hostSessionsFragment,"hostSessionsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("mapsFragment")) {
            transaction.add(R.id.container_main_host, mapsFragment,"mapsFragment");
        }

        transaction.commit();

        fragmentManager.executePendingTransactions();

        BottomNavigationViewHelper.disableShiftMode(bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int id = item.getItemId();

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(mapsFragment);
                transaction.hide(userProfileFragment);
                transaction.hide(hostSessionsFragment);
                transaction.commit();

                switch (id){
                    case R.id.menuNewsFeed:

                        break;
                    case R.id.menuInbox:

                        break;
                    case R.id.menuHostSessions:
                        FragmentTransaction transaction6 = fragmentManager.beginTransaction();
                        transaction6.show(hostSessionsFragment);
                        transaction6.commit();
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

        bottomNavigation.setSelectedItemId(R.id.menuNewsFeed);
    }



    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(MainHostActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainHostActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(MainHostActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainHostActivity.this,
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
                    if (ContextCompat.checkSelfPermission(MainHostActivity.this,
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
