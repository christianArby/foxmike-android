package com.example.chris.kungsbrostrand;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainHostActivity extends AppCompatActivity implements OnSessionClickedListener, UserAccountFragment.OnUserAccountFragmentInteractionListener, UserProfileFragment.OnUserProfileFragmentInteractionListener, UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener, HostSessionsFragment.OnCreateSessionClickedListener{

    private FragmentManager fragmentManager;
    private UserAccountFragment hostUserAccountFragment;
    private MapsFragment hostMapsFragment;
    private DisplaySessionFragment hostDisplaySessionFragment;
    private InboxFragment hostInboxFragment;
    private HostSessionsFragment hostSessionsFragment;
    private UserProfileFragment hostUserProfileFragment;
    private UserProfilePublicFragment hostUserProfilePublicFragment;
    private UserProfilePublicEditFragment hostUserProfilePublicEditFragment;
    private BottomBar bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host);

        bottomNavigation = findViewById(R.id.bottom_navigation_host);
        fragmentManager = getSupportFragmentManager();

        if (hostUserAccountFragment ==null) {
            hostUserAccountFragment = UserAccountFragment.newInstance();
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

        if(hostUserProfileFragment == null) {
            hostUserProfileFragment = UserProfileFragment.newInstance();
        }

        if (hostUserProfilePublicEditFragment == null) {
            hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("hostUserAccountFragment")) {
            transaction.add(R.id.container_main_host, hostUserAccountFragment,"hostUserAccountFragment");
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

        if (null == fragmentManager.findFragmentByTag("hostUserProfileFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfileFragment,"hostUserProfileFragment");
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
                        break;
                    case R.id.menuProfile:
                        cleanMainActivityAndSwitch(hostUserAccountFragment);
                        break;
                }
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
        transaction.hide(hostUserAccountFragment);
        transaction.hide(hostUserProfileFragment);
        if (hostUserProfilePublicFragment!=null) {
            transaction.hide(hostUserProfilePublicFragment);
        }
        transaction.hide(hostUserProfilePublicEditFragment);
        transaction.hide(hostSessionsFragment);
        transaction.commit();
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
        if (hostUserAccountFragment.isVisible()) {
            transaction.hide(hostUserAccountFragment).addToBackStack("hostUserAccountFragment");
        }
        if (hostUserProfileFragment.isVisible()) {
            transaction.hide(hostUserProfileFragment).addToBackStack("hostUserProfileFragment");
        }
        if (hostUserProfileFragment!=null) {
            if (hostUserProfilePublicFragment.isVisible()) {
                transaction.hide(hostUserProfilePublicFragment).addToBackStack("hostUserProfilePublicFragment");
            }
        }
        if (hostUserProfilePublicEditFragment.isVisible()) {
            transaction.hide(hostUserProfilePublicEditFragment);
        }
        if (hostSessionsFragment.isVisible()) {
            transaction.hide(hostSessionsFragment).addToBackStack("hostSessionsFragment");
        }

        transaction.show(fragment).addToBackStack("fragment");
        transaction.commit();
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
    public void OnUserAccountFragmentInteraction() {
        cleanMainActivityAndSwitch(hostUserProfileFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void onUserProfileFragmentInteraction() {
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
            if (hostMapsFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            // TODO Add Newsfeed fragment here later when exist
            if (!hostUserAccountFragment.isVisible()&&!hostSessionsFragment.isVisible()&&!hostInboxFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void OnCreateSessionClicked() {
        cleanMainActivityAndSwitch(hostMapsFragment);
        bottomNavigation.setVisibility(View.GONE);
    }


}
