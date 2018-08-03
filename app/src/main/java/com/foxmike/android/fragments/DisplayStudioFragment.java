package com.foxmike.android.fragments;


import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.Studio;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class DisplayStudioFragment extends Fragment {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private ConstraintLayout sessionImageCardView;
    private TextView mSessionType;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView sessionImage;
    private TextView previewTV;
    private TextView editTV;
    private Button advertiseBtn;
    private TextView advertisedText;

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

    private void onAsyncTaskFinished() {

        if (studioLoaded && getView()!=null && !studioAndViewUsed) {
            studioAndViewUsed = true;

            // set the image
            setImage(studio.getImageUrl(), sessionImage);
            sessionImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);

            collapsingToolbarLayout.setTitle(studio.getSessionName());
            mSessionType.setText(studio.getSessionType());

            // edit studio
            editTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioInteractionListener.OnEditStudio(studioID, studio);
                }
            });

            // setup buttons
            previewTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioInteractionListener.OnPreviewStudio(studioID, studio);
                }
            });

            // setup advertise buttons
            advertiseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioInteractionListener.OnAdvertiseStudio(studioID, studio);
                }
            });
            addAdditionalSessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioInteractionListener.OnAdvertiseStudio(studioID, studio);
                }
            });

        }

        if (sessionsAdvLoaded && getView()!=null && !sessionsAdvAndViewUsed) {
            sessionsAdvAndViewUsed = true;

            if (sessionsAdvBranches.size()>0) {
                smallAdvertisedSessionsListRV.setVisibility(View.VISIBLE);
                sessionsAdvertisedAdapter.updateData(sessionsAdvBranches);
                advertisedText.setVisibility(View.VISIBLE);
                advertisedHeading.setVisibility(View.VISIBLE);
            } else {
                previewTV.setVisibility(View.VISIBLE);
                nothingAdvContainer.setVisibility(View.VISIBLE);
                advertisedHeading.setVisibility(View.GONE);
                addAdditionalSessionBtn.setVisibility(View.GONE);
                advertisedText.setVisibility(View.GONE);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_display_studio, container, false);

        setRetainInstance(true);

        sessionImageCardView = view.findViewById(R.id.sessionImageCardView);
        mSessionType = view.findViewById(R.id.sessionType);
        sessionImage = view.findViewById(R.id.displaySessionImage);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);

        LinearLayout displayStudioContainer = view.findViewById(R.id.display_studio_container);
        View displayStudio = inflater.inflate(R.layout.display_studio,displayStudioContainer,false);
        previewTV = displayStudio.findViewById(R.id.preview_studio_question);
        editTV = displayStudio.findViewById(R.id.edit_studio_question);
        advertiseBtn = displayStudio.findViewById(R.id.advertiseBtn);
        displayStudioContainer.addView(displayStudio);
        nothingAdvContainer = displayStudio.findViewById(R.id.nothingAdvContainer);
        advertisedText = view.findViewById(R.id.advertisedText);

        smallAdvertisedSessionsListRV = (RecyclerView) displayStudio.findViewById(R.id.smallAdvertisedSessionsListRV);
        smallAdvertisedSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionsAdvertisedAdapter = new ListSmallSessionsAdapter(sessionsAdvBranches, onSessionBranchClickedListener, getContext());
        smallAdvertisedSessionsListRV.setAdapter(sessionsAdvertisedAdapter);
        addAdditionalSessionBtn = displayStudio.findViewById(R.id.addAdditionalSessionBtn);
        advertisedHeading = displayStudio.findViewById(R.id.advertisedHeading);
        notAdvertisedHeading = displayStudio.findViewById(R.id.notAdvertisedHeading);

        smallNotAdvertisedSessionsListRV = (RecyclerView) displayStudio.findViewById(R.id.smallNotAdvertisedSessionsListRV);
        smallNotAdvertisedSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionsNotAdvertisedAdapter = new ListSmallSessionsAdapter(sessionsNotAdvBranches, onSessionBranchClickedListener, getContext());
        smallNotAdvertisedSessionsListRV.setAdapter(sessionsNotAdvertisedAdapter);

        toolbar = view.findViewById(R.id.toolbar);
        advertisedText.setVisibility(View.GONE);
        previewTV.setVisibility(View.GONE);
        nothingAdvContainer.setVisibility(View.GONE);
        smallAdvertisedSessionsListRV.setVisibility(View.GONE);
        advertisedHeading.setVisibility(View.GONE);
        notAdvertisedHeading.setVisibility(View.GONE);


        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.displaySessionAppBar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset!=0) {
                    mSessionType.setVisibility(View.GONE);
                } else {
                    mSessionType.setVisibility(View.VISIBLE);
                }
            }
        });

        // Setup standard aspect ratio of session image
        sessionImageCardView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) sessionImageCardView.getLayoutParams();
                mParams.height = sessionImageCardView.getWidth()*getResources().getInteger(R.integer.heightOfSessionImageNumerator)/getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                sessionImageCardView.setLayoutParams(mParams);
                sessionImageCardView.postInvalidate();
            }
        });



        return view;
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStudioInteractionListener = null;
        onSessionBranchClickedListener = null;
    }

    public interface OnStudioInteractionListener {
        void OnEditStudio(String studioID , Studio studio);
        void OnPreviewStudio(String studioID , Studio studio);
        void OnAdvertiseStudio(String studioID , Studio studio);
    }

}