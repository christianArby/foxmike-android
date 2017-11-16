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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainHostActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private UserProfileFragment userProfileFragment;
    private MapsFragment mapsFragment;
    private HostSessionsFragment hostSessionsFragment;
    private BottomNavigationView bottomNavigation;
    private Button createSessionBtn;
    private TextView createSessionMapText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host);

        createSessionBtn = findViewById(R.id.add_session_btn);
        createSessionBtn.setVisibility(View.GONE);
        createSessionMapText = findViewById(R.id.create_session_map_text);
        createSessionMapText.setVisibility(View.GONE);

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
                createSessionBtn.setVisibility(View.GONE);
                createSessionMapText.setVisibility(View.GONE);

                switch (id){
                    case R.id.menuNewsFeed:

                        break;
                    case R.id.menuInbox:

                        break;
                    case R.id.menuHostSessions:
                        createSessionBtn.setVisibility(View.VISIBLE);
                        createSessionMapText.setVisibility(View.GONE);
                        FragmentTransaction transaction6 = fragmentManager.beginTransaction();
                        transaction6.show(hostSessionsFragment);
                        transaction6.commit();
                        break;
                    case R.id.menuProfile:
                        FragmentTransaction transaction8 = fragmentManager.beginTransaction();
                        createSessionMapText.setVisibility(View.GONE);
                        transaction8.show(userProfileFragment);
                        transaction8.commit();
                        break;
                }
                return true;
            }
        });

        bottomNavigation.setSelectedItemId(R.id.menuNewsFeed);

        createSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(userProfileFragment);
                transaction.hide(hostSessionsFragment);
                transaction.show(mapsFragment);
                transaction.commit();
                createSessionMapText.setVisibility(View.VISIBLE);
                createSessionBtn.setVisibility(View.GONE);

            }
        });
    }
}
