package com.foxmike.android.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsAdapter;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.interfaces.OnSessionBranchesFoundListener;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.Studio;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DisplayStudioFragment extends Fragment {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private ConstraintLayout sessionImageCardView;
    //private TextView mSessionType;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView sessionImage;
    private TextView editTV;
    private Button advertiseBtn;
    private TextView locationTV;
    private TextView studioTypeTV;

    private static final String STUDIO_ID = "studioID";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private String studioID="";
    private View view;
    private Toolbar toolbar;
    private Studio studio;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private ConstraintLayout nothingAdvContainer;
    private CardView addAdditionalSessionBtn;

    private TextView advertisedHeading;
    private TextView notAdvertisedHeading;
    private TextView description;

    private boolean studioLoaded;
    private boolean studioAndViewUsed;

    private boolean sessionsAdvLoaded;
    private boolean sessionsAdvAndViewUsed;

    private boolean sessionsNotAdvLoaded;
    private boolean sessionsNotAdvAndViewUsed;

    private RecyclerView smallAdvertisedSessionsListRV;
    private ListSmallSessionsAdapter sessionsAdvertisedAdapter;
    private RecyclerView smallNotAdvertisedSessionsListRV;
    private ListSmallSessionsAdapter sessionsNotAdvertisedAdapter;

    private ArrayList<SessionBranch> sessionsAdvBranches = new ArrayList<>();
    private ArrayList<SessionBranch> sessionsNotAdvBranches = new ArrayList<>();

    private OnStudioInteractionListener onStudioInteractionListener;
    private OnSessionBranchClickedListener onSessionBranchClickedListener;

    private OnCreateSessionClickedListener onCreateSessionClickedListener;

    public DisplayStudioFragment() {
        // Required empty public constructor
    }

    public static DisplayStudioFragment newInstance(String studioID) {

        DisplayStudioFragment fragment = new DisplayStudioFragment();
        Bundle args = new Bundle();
        args.putString(STUDIO_ID, studioID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            studioID = getArguments().getString(STUDIO_ID);
        }

        if (!listenerMap.containsKey(rootDbRef.child(getString(R.string.studios_pathname)).child(studioID))) {
            ValueEventListener studioListener = rootDbRef.child(getString(R.string.studios_pathname)).child(studioID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
                    sessionsAdvBranches.clear();
                    sessionsAdvLoaded = false;
                    sessionsAdvAndViewUsed = false;
                    sessionsNotAdvBranches.clear();
                    sessionsNotAdvLoaded = false;
                    sessionsNotAdvAndViewUsed = false;
                    final HashMap<String, Long> sessionsAdvHashMap = new HashMap<>();
                    final HashMap<String, Long> sessionsNotAdvHashMap = new HashMap<>();
                    studio = dataSnapshot.getValue(Studio.class);
                    studioID = dataSnapshot.getRef().getKey();

                    studioLoaded = true;
                    onAsyncTaskFinished();

                    if (studio.getSessions().size()>0) {
                        Long currentTimestamp = System.currentTimeMillis();
                        for (String sessionId: studio.getSessions().keySet()) {
                            if (studio.getSessions().get(sessionId)>currentTimestamp) {
                                sessionsAdvHashMap.put(sessionId, null);
                            } else {
                                sessionsNotAdvHashMap.put(sessionId, null);
                            }
                        }

                        // Get Advertised sessions
                        if (sessionsAdvHashMap.size()>0) {
                            myFirebaseDatabase.getSessionBranches(sessionsAdvHashMap, new OnSessionBranchesFoundListener() {
                                @Override
                                public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches) {
                                    Collections.sort(sessionBranches);
                                    sessionsAdvBranches = sessionBranches;
                                    sessionsAdvertisedAdapter.updateData(sessionBranches);
                                    sessionsAdvLoaded = true;
                                    onAsyncTaskFinished();
                                }
                            });
                        } else {
                            sessionsAdvLoaded = true;
                            onAsyncTaskFinished();
                        }

                        // Get Not Advertised sessions
                        // TODO limit the number of sessions not advertised downloaded, pagination...
                        if (sessionsNotAdvHashMap.size()>0) {
                            myFirebaseDatabase.getSessionBranches(sessionsNotAdvHashMap, new OnSessionBranchesFoundListener() {
                                @Override
                                public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches) {
                                    Collections.sort(sessionBranches);
                                    sessionsNotAdvBranches = sessionBranches;
                                    sessionsNotAdvertisedAdapter.updateData(sessionBranches);
                                    sessionsNotAdvLoaded = true;
                                    onAsyncTaskFinished();
                                }
                            });
                        } else {
                            sessionsNotAdvLoaded = true;
                            onAsyncTaskFinished();
                        }
                        // if no sessions exist
                    } else {
                        sessionsNotAdvLoaded = true;
                        sessionsAdvLoaded = true;
                        onAsyncTaskFinished();
                    }


                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            listenerMap.put(rootDbRef.child(getString(R.string.studios_pathname)).child(studioID),studioListener);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_display_studio, container, false);

        setRetainInstance(true);

        sessionImageCardView = view.findViewById(R.id.sessionImageCardView);
        //mSessionType = view.findViewById(R.id.sessionType);
        sessionImage = view.findViewById(R.id.displaySessionImage);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);

        LinearLayout displayStudioContainer = view.findViewById(R.id.display_studio_container);
        View displayStudio = inflater.inflate(R.layout.display_studio,displayStudioContainer,false);
        editTV = displayStudio.findViewById(R.id.edit_studio_question);
        advertiseBtn = displayStudio.findViewById(R.id.createSessionBtn);
        displayStudioContainer.addView(displayStudio);
        nothingAdvContainer = displayStudio.findViewById(R.id.noSessionsContainer);
        description = displayStudio.findViewById(R.id.descriptionTV);
        locationTV = displayStudio.findViewById(R.id.locationTV);
        studioTypeTV = displayStudio.findViewById(R.id.studioTypeTV);

        smallAdvertisedSessionsListRV = (RecyclerView) displayStudio.findViewById(R.id.smallAdvertisedSessionsListRV);
        smallAdvertisedSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionsAdvertisedAdapter = new ListSmallSessionsAdapter(sessionsAdvBranches, onSessionBranchClickedListener, "displaySession", getContext());
        smallAdvertisedSessionsListRV.setAdapter(sessionsAdvertisedAdapter);
        addAdditionalSessionBtn = displayStudio.findViewById(R.id.addAdditionalSessionBtn);
        advertisedHeading = displayStudio.findViewById(R.id.upcomingHeading);
        notAdvertisedHeading = displayStudio.findViewById(R.id.pastHeading);

        smallNotAdvertisedSessionsListRV = (RecyclerView) displayStudio.findViewById(R.id.smallNotAdvertisedSessionsListRV);
        smallNotAdvertisedSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionsNotAdvertisedAdapter = new ListSmallSessionsAdapter(sessionsNotAdvBranches, onSessionBranchClickedListener,"displaySession", getContext());
        smallNotAdvertisedSessionsListRV.setAdapter(sessionsNotAdvertisedAdapter);

        toolbar = view.findViewById(R.id.toolbar);
        nothingAdvContainer.setVisibility(View.GONE);
        smallAdvertisedSessionsListRV.setVisibility(View.GONE);
        advertisedHeading.setVisibility(View.GONE);
        notAdvertisedHeading.setVisibility(View.GONE);


        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.displaySessionAppBar);
        /*appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset!=0) {
                    mSessionType.setVisibility(View.GONE);
                } else {
                    mSessionType.setVisibility(View.VISIBLE);
                }
            }
        });*/

        // Setup standard aspect ratio of session image
        sessionImageCardView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) sessionImageCardView.getLayoutParams();
                mParams.height = sessionImageCardView.getWidth()*getResources().getInteger(R.integer.heightOfStudioImageNumerator)/getResources().getInteger(R.integer.heightOfStudioImageDenominator);
                sessionImageCardView.setLayoutParams(mParams);
                sessionImageCardView.postInvalidate();
            }
        });



        return view;
    }

    private void onAsyncTaskFinished() {

        if (studioLoaded && getView()!=null && !studioAndViewUsed) {
            studioAndViewUsed = true;

            // set the image
            setImage(studio.getImageUrl(), sessionImage);
            sessionImage.setColorFilter(R.color.foxmikePrimaryDarkColor, PorterDuff.Mode.LIGHTEN);

            collapsingToolbarLayout.setTitle(studio.getStudioName());
            description.setText(studio.getDescription());
            locationTV.setText(studio.getLocation());
            studioTypeTV.setText(studio.getStudioType());

            // edit studio
            editTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioInteractionListener.OnEditStudio(studioID, studio);
                }
            });

            // setup advertise buttons
            advertiseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createSession();
                }
            });
            addAdditionalSessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createSession();
                }
            });

        }

        if (sessionsAdvLoaded && getView()!=null && !sessionsAdvAndViewUsed) {
            sessionsAdvAndViewUsed = true;

            if (sessionsAdvBranches.size()>0) {
                nothingAdvContainer.setVisibility(View.GONE);
                smallAdvertisedSessionsListRV.setVisibility(View.VISIBLE);
                sessionsAdvertisedAdapter.updateData(sessionsAdvBranches);
                advertisedHeading.setVisibility(View.VISIBLE);
            } else {
                nothingAdvContainer.setVisibility(View.VISIBLE);
                advertisedHeading.setVisibility(View.GONE);
                addAdditionalSessionBtn.setVisibility(View.GONE);
            }
        }

        if (sessionsNotAdvLoaded && getView()!=null && !sessionsNotAdvAndViewUsed) {
            sessionsNotAdvAndViewUsed = true;
            sessionsNotAdvertisedAdapter.updateData(sessionsNotAdvBranches);
            if (sessionsNotAdvBranches.size()>0) {
                notAdvertisedHeading.setVisibility(View.VISIBLE);
            } else {
                notAdvertisedHeading.setVisibility(View.GONE);
            }
        }
    }

    private void createSession() {
        if (studio.getSessions().size()>0) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Do you want to use a previous session as a template for the new session?")
                    .setTitle("New session");

            // Add the buttons
            builder.setPositiveButton("Select template", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
                    myFirebaseDatabase.getSessionBranches(studio.getSessions(), new OnSessionBranchesFoundListener() {
                        @Override
                        public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches) {
                            ListSmallSessionsFragment listSmallSessionsFragment = ListSmallSessionsFragment.newInstance(sessionBranches, "createSession");
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            if (null == fragmentManager.findFragmentByTag("listSmallSessionsFragment")) {
                                transaction.add(R.id.container_fullscreen_display_studio, listSmallSessionsFragment,"listSmallSessionsFragment").addToBackStack("changeStudio");
                                transaction.commit();
                            }

                        }
                    });

                }
            });
            builder.setNegativeButton("Start from scratch", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    onCreateSessionClickedListener.OnCreateSessionClicked(studioID, studio);
                }
            });
            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onAsyncTaskFinished();
    }

    private void setImage(String image, ImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        listenerMap.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        studioAndViewUsed = false;
        sessionsAdvAndViewUsed = false;
        sessionsNotAdvAndViewUsed = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStudioInteractionListener) {
            onStudioInteractionListener = (OnStudioInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStudioInteractionListener");
        }
        if (context instanceof OnSessionBranchClickedListener) {
            onSessionBranchClickedListener = (OnSessionBranchClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionBranchClickedListener");
        }
        super.onAttach(context);
        if (context instanceof OnCreateSessionClickedListener) {
            onCreateSessionClickedListener = (OnCreateSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onCreateSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStudioInteractionListener = null;
        onSessionBranchClickedListener = null;
        onCreateSessionClickedListener = null;
    }

    public interface OnStudioInteractionListener {
        void OnEditStudio(String studioID , Studio studio);
        void OnPreviewStudio(String studioID , Studio studio);
        void OnAdvertiseStudio(String studioID , Studio studio);
    }

    public interface OnCreateSessionClickedListener {
        void OnCreateSessionClicked(String studioId, Studio studio);
    }

}
