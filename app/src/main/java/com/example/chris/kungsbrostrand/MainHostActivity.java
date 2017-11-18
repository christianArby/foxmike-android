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

public class MainHostActivity extends AppCompatActivity implements OnSessionClickedListener{

    private FragmentManager fragmentManager;
    private UserProfileFragment userProfileFragment;
    private MapsFragment mapsFragment;
    private HostSessionsFragment hostSessionsFragment;
    private BottomNavigationView bottomNavigation;
    private Button createSessionBtn;
    private TextView createSessionMapText;
    private DisplaySessionFragment displaySessionFragment;
    private InboxFragment hostInboxFragment;

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

        if (hostInboxFragment==null) {
            hostInboxFragment = InboxFragment.newInstance();
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

        if (null == fragmentManager.findFragmentByTag("mapsFragmentHost")) {
            transaction.add(R.id.container_main_host, mapsFragment,"mapsFragmentHost");
        }

        if (null == fragmentManager.findFragmentByTag("hostInboxFragment")) {
            transaction.add(R.id.container_main_host, hostInboxFragment,"hostInboxFragment");
        }

        transaction.commit();

        fragmentManager.executePendingTransactions();

        BottomNavigationViewHelper.disableShiftMode(bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int id = item.getItemId();
                cleanMainActivity();

                switch (id){
                    case R.id.menuNewsFeed:
                        cleanMainActivity();

                        break;
                    case R.id.menuInbox:
                        cleanMainActivity();
                        FragmentTransaction transaction5 = fragmentManager.beginTransaction();
                        transaction5.show(hostInboxFragment);
                        transaction5.commit();

                        break;
                    case R.id.menuHostSessions:
                        cleanMainActivity();
                        createSessionBtn.setVisibility(View.VISIBLE);
                        FragmentTransaction transaction6 = fragmentManager.beginTransaction();
                        transaction6.show(hostSessionsFragment);
                        transaction6.commit();
                        break;
                    case R.id.menuProfile:
                        cleanMainActivity();
                        FragmentTransaction transaction8 = fragmentManager.beginTransaction();
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
                cleanMainActivity();
                transaction.show(mapsFragment);
                transaction.commit();
                createSessionMapText.setVisibility(View.VISIBLE);

            }
        });
    }

    private void cleanMainActivity() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (displaySessionFragment!=null) {
            transaction.remove(displaySessionFragment);
        }
        transaction.hide(hostInboxFragment);
        transaction.hide(mapsFragment);
        transaction.hide(userProfileFragment);
        transaction.hide(hostSessionsFragment);
        transaction.commit();
        createSessionMapText.setVisibility(View.GONE);
        createSessionBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
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
}
