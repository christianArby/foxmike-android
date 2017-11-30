package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainHostActivity extends AppCompatActivity implements OnSessionClickedListener, UserProfileFragment.OnUserProfileFragmentInteractionListener, UserProfilePublicFragment.OnUserProfilePublicFragmentInteractionListener, UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener{

    private FragmentManager fragmentManager;
    private UserProfileFragment hostUserProfileFragment;
    private MapsFragment hostMapsFragment;
    private DisplaySessionFragment hostDisplaySessionFragment;
    private InboxFragment hostInboxFragment;
    private HostSessionsFragment hostSessionsFragment;
    private UserProfilePublicFragment hostUserProfilePublicFragment;
    private UserProfilePublicEditFragment hostUserProfilePublicEditFragment;
    private BottomBar bottomNavigation;
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

        if (hostUserProfileFragment==null) {
            hostUserProfileFragment = UserProfileFragment.newInstance();
        }
        if (hostSessionsFragment==null) {
            hostSessionsFragment = HostSessionsFragment.newInstance();
        }

        if (hostInboxFragment==null) {
            hostInboxFragment = InboxFragment.newInstance();
        }

        if (hostMapsFragment==null) {
            Bundle bundle = new Bundle();
            bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
            hostMapsFragment = MapsFragment.newInstance();
            hostMapsFragment.setArguments(bundle);
        }

        if(hostUserProfilePublicFragment == null) {
            hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
        }

        if (hostUserProfilePublicEditFragment == null) {
            hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("hostUserProfileFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfileFragment,"hostUserProfileFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostSessionsFragment")) {
            transaction.add(R.id.container_main_host, hostSessionsFragment,"hostSessionsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostMapsFragment")) {
            transaction.add(R.id.container_main_host, hostMapsFragment,"hostMapsFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostInboxFragment")) {
            transaction.add(R.id.container_main_host, hostInboxFragment,"hostInboxFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostUserProfilePublicFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfilePublicFragment,"hostUserProfilePublicFragment");
        }

        if (null == fragmentManager.findFragmentByTag("hostUserProfilePublicEditFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfilePublicEditFragment,"hostUserProfilePublicEditFragment");
        }

        transaction.commit();

        fragmentManager.executePendingTransactions();

        bottomNavigation.setAnimation(null);




        bottomNavigation.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId){
                    case R.id.menuNewsFeed:
                        cleanMainActivity();

                        break;
                    case R.id.menuInbox:
                        cleanMainActivityAndSwitch(hostInboxFragment);
                        break;
                    case R.id.menuHostSessions:
                        cleanMainActivityAndSwitch(hostSessionsFragment);
                        createSessionBtn.setVisibility(View.VISIBLE);
                        break;
                    case R.id.menuProfile:
                        cleanMainActivityAndSwitch(hostUserProfileFragment);
                        break;
                }
            }
        });

        createSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanMainActivityAndSwitch(hostMapsFragment);
                createSessionMapText.setVisibility(View.VISIBLE);
            }
        });
    }

    // TODO cleanMainActivity is probably useless once Newsfeed fragment has been created, delete this functionality then
    private void cleanMainActivity() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (hostDisplaySessionFragment!=null) {
            transaction.remove(hostDisplaySessionFragment);
        }
        transaction.hide(hostInboxFragment);
        transaction.hide(hostMapsFragment);
        transaction.hide(hostUserProfileFragment);
        transaction.hide(hostUserProfilePublicFragment);
        transaction.hide(hostUserProfilePublicEditFragment);
        transaction.hide(hostSessionsFragment);
        transaction.commit();
        createSessionMapText.setVisibility(View.GONE);
        createSessionBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (hostDisplaySessionFragment!=null) {
            if (hostDisplaySessionFragment.isVisible()) {
                transaction.hide(hostDisplaySessionFragment).addToBackStack("hostDisplaySessionFragment");
            }
        }
        if (hostInboxFragment.isVisible()) {
            transaction.hide(hostInboxFragment).addToBackStack("hostInboxFragment");
        }
        if (hostMapsFragment.isVisible()) {
            transaction.hide(hostMapsFragment).addToBackStack("hostMapsFragment");
        }
        if (hostUserProfileFragment.isVisible()) {
            transaction.hide(hostUserProfileFragment).addToBackStack("hostUserProfileFragment");
        }
        if (hostUserProfilePublicFragment.isVisible()) {
            transaction.hide(hostUserProfilePublicFragment).addToBackStack("hostUserProfilePublicFragment");
        }
        if (hostUserProfilePublicEditFragment.isVisible()) {
            transaction.hide(hostUserProfilePublicEditFragment);
        }
        if (hostSessionsFragment.isVisible()) {
            transaction.hide(hostSessionsFragment).addToBackStack("hostSessionsFragment");
        }

        transaction.show(fragment).addToBackStack("fragment");
        transaction.commit();
        createSessionMapText.setVisibility(View.GONE);
        createSessionBtn.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (hostDisplaySessionFragment!=null) {
            transaction.remove(hostDisplaySessionFragment);
        }

        hostDisplaySessionFragment = DisplaySessionFragment.newInstance(sessionLatitude,sessionLongitude);
        hostDisplaySessionFragment.show(transaction,"hostDisplaySessionFragment");
    }

    @Override
    public void OnUserProfileFragmentInteraction() {
        cleanMainActivityAndSwitch(hostUserProfilePublicFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void OnUserProfilePublicFragmentInteraction() {
        cleanMainActivityAndSwitch(hostUserProfilePublicEditFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {
            if (hostUserProfilePublicFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            // TODO Add Newsfeed fragment here later when exist
            if (!hostUserProfileFragment.isVisible()&&!hostSessionsFragment.isVisible()&&!hostInboxFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
        }
    }
}
