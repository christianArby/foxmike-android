package com.foxmike.android.activities;
//Checked
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.foxmike.android.R;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.ChatFragment;
import com.foxmike.android.fragments.CommentFragment;
import com.foxmike.android.fragments.CreateOrEditSessionFragment;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.interfaces.OnHostSessionChangedListener;
import com.foxmike.android.interfaces.OnNewMessageListener;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.fragments.HostSessionsFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.MapsFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHostActivity extends AppCompatActivity implements
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        HostSessionsFragment.OnCreateSessionClickedListener,
        OnUserClickedListener,
        OnNewMessageListener,
        DisplaySessionFragment.OnEditSessionListener,
        DisplaySessionFragment.OnBookSessionListener,
        DisplaySessionFragment.OnCancelBookedSessionListener,
        OnHostSessionChangedListener, MapsFragment.OnCreateSessionListener,
        CreateOrEditSessionFragment.OnEditLocationListener,
        MapsFragment.OnSessionLocationChangedListener, OnSessionBranchClickedListener, OnChatClickedListener, OnCommentClickedListener, InboxFragment.OnSearchClickedListener{

    private FragmentManager fragmentManager;
    private UserAccountFragment hostUserAccountFragment;
    private MapsFragment hostMapsFragment;
    private DisplaySessionFragment hostDisplaySessionFragment;
    private InboxFragment hostInboxFragment;
    private HostSessionsFragment hostSessionsFragment;
    private UserProfileFragment hostUserProfileFragment;
    private UserProfilePublicFragment hostUserProfilePublicFragment;
    private UserProfilePublicEditFragment hostUserProfilePublicEditFragment;
    private CreateOrEditSessionFragment createOrEditSessionFragment;
    private AHBottomNavigation bottomNavigation;
    private DatabaseReference userDbRef;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private Session editedSession;
    private String editedSessionID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host);

        bottomNavigation = findViewById(R.id.bottom_navigation_host);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mAuth = FirebaseAuth.getInstance();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        fragmentManager = getSupportFragmentManager();

        // Setup bottom navigation
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_host_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.foxmikePrimaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        // Create instances of fragments
        hostUserAccountFragment = UserAccountFragment.newInstance();
        hostSessionsFragment = HostSessionsFragment.newInstance();
        hostInboxFragment = InboxFragment.newInstance();

        // Add fragments to container and hide them
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("xMainHostUserAccountFragment")) {
            transaction.add(R.id.container_main_host, hostUserAccountFragment,"xMainHostUserAccountFragment");
            transaction.hide(hostUserAccountFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostSessionsFragment")) {
            transaction.add(R.id.container_main_host, hostSessionsFragment,"xMainHostSessionsFragment");
            transaction.hide(hostSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostInboxFragment")) {
            transaction.add(R.id.container_main_host, hostInboxFragment,"xMainHostInboxFragment");
            transaction.hide(hostInboxFragment);
        }

        transaction.commit();
        fragmentManager.executePendingTransactions();

        // Listen to bottom navigation and switch to corresponding fragment
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainHostInboxFragment"));
                            return true;
                        }
                    case 1:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainHostSessionsFragment"));
                            return true;
                        }
                    case 2:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainHostUserAccountFragment"));
                            return true;
                        }
                }
                return false;
            }
        });

        // Set 'start page' / default fragment
        cleanMainActivityAndSwitch(fragmentManager.findFragmentByTag("xMainHostInboxFragment"));

        // Add listener to current user's chats to see if there are any chats the user has unread messages in, if so set notification to bottom bar
        ValueEventListener chatsListener = rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    int nrOfUnreadChats = 0;
                    for (DataSnapshot chatID: dataSnapshot.getChildren()) {
                        Boolean read = (Boolean) chatID.getValue();
                        if (!read) {
                            nrOfUnreadChats++;
                        }
                        if (nrOfUnreadChats>0) {
                            bottomNavigation.setNotification(Integer.toString(nrOfUnreadChats),0);
                        } else {
                            bottomNavigation.setNotification("",0);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listenerMap.put(rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats"), chatsListener);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (hostUserAccountFragment.isVisible() | hostSessionsFragment.isVisible() | hostInboxFragment.isVisible()){
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /* Method to hide all fragments in main container except fragment passed as argument */
    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag()!=null && frag.getTag().length()>5) {
                if (frag.getTag().substring(0,5).equals("xMain")) {
                    if (frag.isVisible()) {
                        transaction.hide(frag);
                    }
                }
            }

        }
        transaction.show(fragment);
        transaction.commit();
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    /* Method to hide all fragments in main container and fill the other container with fullscreen fragment */
    private void cleanMainFullscreenActivityAndSwitch(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        /*List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag()!=null && frag.getTag().length()>5) {
                if (frag.getTag().substring(0,5).equals("xMain")) {
                    if (frag.isVisible()) {
                        transaction.hide(frag);
                    }
                }
            }
        }
        bottomNavigation.setVisibility(View.GONE);*/
        if (addToBackStack) {
            if (!tag.equals("")) {
                transaction.replace(R.id.container_fullscreen_main_host, fragment).addToBackStack(tag).commit();
            } else {
                transaction.replace(R.id.container_fullscreen_main_host, fragment).addToBackStack(null).commit();
            }

        } else {
            transaction.replace(R.id.container_fullscreen_main_host, fragment).commit();
        }
    }

    /* Listener, when session is clicked display session*/
    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {
    }

    @Override
    public void OnSessionBranchClicked(SessionBranch sessionBranch) {

        hostDisplaySessionFragment = DisplaySessionFragment.newInstance(sessionBranch.getSession().getLatitude(),sessionBranch.getSession().getLongitude(), sessionBranch.getSessionID());
        cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true,"");

    }

    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            hostUserProfileFragment = UserProfileFragment.newInstance();
            cleanMainFullscreenActivityAndSwitch(hostUserProfileFragment,true,"");
        }
    }

    /* Listener, when edit "button" in user profile is clicked show edit user profile */
    @Override
    public void onUserProfileFragmentInteraction() {
        hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicEditFragment,true,"");
    }
    /* Listener, when finished editing restart this activity */
    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        getSupportFragmentManager().popBackStack();
    }
    /* Listener, when a user is clicked, get the others user ID and start User profile fragment */
    @Override
    public void OnUserClicked(String otherUserID) {
        Bundle bundle = new Bundle();
        bundle.putString("otherUserID", otherUserID);
        hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
        hostUserProfilePublicFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicFragment, true,"");
    }

    @Override
    public void OnNewMessage() {
        // TODO should this be used?
    }
    /* Listener, create session button FAB is clicked switch to maps fragment in order for user to pick location */
    @Override
    public void OnCreateSessionClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        hostMapsFragment = MapsFragment.newInstance();
        hostMapsFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(hostMapsFragment, true,"editSession");
    }
    // Starts CreateOrEditSessionFragment with a LatLng in order to create a session
    @Override
    public void OnCreateSession(LatLng latLng) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("LatLng", latLng);
        createOrEditSessionFragment = CreateOrEditSessionFragment.newInstance();
        createOrEditSessionFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(createOrEditSessionFragment, true,"");
    }
    // Starts CreateOrEditSessionFragment with a sessionID to edit the session
    @Override
    public void OnEditSession(String sessionID) {
        Bundle bundle = new Bundle();
        bundle.putString("sessionID", sessionID);
        createOrEditSessionFragment = CreateOrEditSessionFragment.newInstance();
        createOrEditSessionFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(createOrEditSessionFragment, true, "editSession");
    }

    @Override
    public void OnEditSession(String sessionID, Session session) {
        Bundle bundle = new Bundle();
        bundle.putString("sessionID", sessionID);
        bundle.putSerializable("session", session);
        createOrEditSessionFragment = CreateOrEditSessionFragment.newInstance();
        createOrEditSessionFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(createOrEditSessionFragment, true, "editSession");
    }

    // Starts Mapsfragment when location on session should be changed.
    @Override
    public void OnEditLocation(String sessionID ,Session session) {
        editedSessionID = sessionID;
        editedSession = session;
        MapsFragment mapsFragment;
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        bundle.putInt("CHANGELOCATION", 1);
        mapsFragment = MapsFragment.newInstance();
        mapsFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(mapsFragment, true,"");
    }
    // Starts CreateOrEditSession again after location on session has been changed.
    @Override
    public void OnSessionLocationChanged(LatLng latLng) {
        editedSession.setLatitude(latLng.latitude);
        editedSession.setLongitude(latLng.longitude);
        Bundle bundle = new Bundle();
        bundle.putSerializable("session", editedSession);
        bundle.putString("sessionID", editedSessionID);
        createOrEditSessionFragment = CreateOrEditSessionFragment.newInstance();
        createOrEditSessionFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(createOrEditSessionFragment, true,"");
    }
    // Pops backstack to displaysession again after session has been updated in CreateOrEditSession. Also reloads lists in hostSessions.
    @Override
    public void OnHostSessionChanged() {
        if (fragmentManager.findFragmentByTag("xMainHostSessionsFragment")!=null) {
            HostSessionsFragment hs = (HostSessionsFragment) fragmentManager.findFragmentByTag("xMainHostSessionsFragment");
            hs.loadPages(true);
        }
        getSupportFragmentManager().popBackStack("editSession", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    /* When back button is pressed while in main host activity override function and replace with following */
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {
            // TODO Add Newsfeed fragment here later when exist
            getSupportFragmentManager().popBackStack();
            if (!hostUserAccountFragment.isVisible()&&!hostSessionsFragment.isVisible()&&!hostInboxFragment.isVisible()){

            }
        }
    }

    // Before starting activity, make sure user is signed-in, otherwise start login activity
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null) {
            //User is signed out
            Intent welcomeIntent = new Intent(MainHostActivity.this, WelcomeActivity.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(welcomeIntent);
            finish();
        }
    }

    // When activity is destroyed, remove all listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        listenerMap.clear();
    }

    @Override
    public void OnBookSession(String sessionID) {
        // Not applicable in Host environment
    }

    @Override
    public void OnCancelBookedSession(String sessionID) {
        // Not applicable in Host environment
    }

    @Override
    public void OnChatClicked(String userID, String userName, String userThumbImage, String chatID) {
        ChatFragment chatFragment = ChatFragment.newInstance(userID,userName,userThumbImage,chatID);
        cleanMainFullscreenActivityAndSwitch(chatFragment,true,"");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnCommentClicked(String postID) {
        CommentFragment commentFragment = CommentFragment.newInstance(postID);
        cleanMainFullscreenActivityAndSwitch(commentFragment,true,"");
    }

    @Override
    public void OnSearchClicked() {
        AllUsersFragment allUsersFragment = AllUsersFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(allUsersFragment,true,"");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Create instances of fragments
        // Add fragments to container and hide them
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("xMainHostUserAccountFragment")) {
            hostUserAccountFragment = UserAccountFragment.newInstance();
            transaction.add(R.id.container_main_host, hostUserAccountFragment,"xMainHostUserAccountFragment");
            transaction.hide(hostUserAccountFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostSessionsFragment")) {
            hostSessionsFragment = HostSessionsFragment.newInstance();
            transaction.add(R.id.container_main_host, hostSessionsFragment,"xMainHostSessionsFragment");
            transaction.hide(hostSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostInboxFragment")) {
            hostInboxFragment = InboxFragment.newInstance();
            transaction.add(R.id.container_main_host, hostInboxFragment,"xMainHostInboxFragment");
            transaction.hide(hostInboxFragment);
        }
        transaction.commit();
    }
}