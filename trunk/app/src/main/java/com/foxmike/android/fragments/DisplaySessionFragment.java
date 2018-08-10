package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.foxmike.android.interfaces.OnChatClickedListener;
import com.foxmike.android.interfaces.OnCommentClickedListener;
import com.foxmike.android.models.Post;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.Studio;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.TextTimestamp;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.support.v4.app.Fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;

/**
 * This fragment takes a longitude and latitude and displays the corresponding session with that longitude and latitude.
 */
public class DisplaySessionFragment extends Fragment implements OnMapReadyCallback {

    private final DatabaseReference mSessionDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<Query, ChildEventListener> childEventListenerMap;
    private ConstraintLayout sessionImageCardView;
    private TextView mDateAndTime;
    private TextView mParticipants;
    private TextView mDuration;
    private Button mDisplaySessionBtn;
    private CircleImageView mHostImage;
    private CircleImageView mCurrentUserPostImage;
    private TextView mHostAboutTV;
    private TextView mHost;
    private TextView mWhatTW;
    private TextView mWhoTW;
    private TextView mWhereTW;
    private TextView mSessionType;
    private TextView mAddressAndSessionType;
    private TextView mSendMessageToHost;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    private LinearLayout writePostLsyout;
    private LinearLayout commentLayout;
    private static final String SESSION_ID = "sessionID";
    private Double sessionLatitude;
    private Double sessionLongitude;
    private String sessionID="";
    private ChildEventListener sessionChildEventListener;
    private ValueEventListener sessionListener;
    private Session session;
    private TextView priceTV;
    private LinearLayoutManager linearLayoutManager;
    private Map<Long, String> postIDs = new HashMap<Long, String>();
    private ArrayList<PostBranch> postBranchArrayList;
    private Map<String, Long> nrOfComments = new HashMap<String, Long>();
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();
    private GoogleMap mMap;
    private RecyclerView postList;
    private RecyclerView.Adapter<PostsViewHolder> postsViewHolderAdapter;
    private User currentUser;
    private OnEditSessionListener onEditSessionListener;
    private OnBookSessionListener onBookSessionListener;
    private OnCancelBookedSessionListener onCancelBookedSessionListener;
    private OnCommentClickedListener onCommentClickedListener;
    private UserAccountFragment.OnUserAccountFragmentInteractionListener onUserAccountFragmentInteractionListener;
    private DisplayStudioFragment.OnStudioInteractionListener onStudioInteractionListener;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private User host;
    private boolean currentUserLoaded;
    private boolean sessionLoaded;
    private boolean hostLoaded;
    private boolean postsLoaded;
    private boolean postCommentsLoaded;
    private Studio studio;
    private String studioId;
    private TextView paymentMethodTV;
    private DotProgressBar paymentMethodProgressBar;
    private boolean hasPaymentSystem;
    private TextView addPaymentMethodTV;
    private LinearLayout paymentFrame;
    private HashMap defaultSourceMap;
    private boolean mapReady;


    private boolean currentUserAndViewUsed;
    private boolean sessionUsed;
    private boolean currentUserAndSessionAndViewAndMapUsed;
    private boolean hostAndViewUsed;
    private boolean postsUsed;
    private boolean postCommentsUsed;
    private boolean paymentMethodLoaded;
    private boolean sessionAndPaymentAndViewUsed;


    private ImageView sessionImage;
    private int asyncTasksFinished = 0;

    private OnChatClickedListener onChatClickedListener;

    public DisplaySessionFragment() {
        // Required empty public constructor
    }

    public static DisplaySessionFragment newInstance(String studioId, Studio studio) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putString("studioId", studioId);
        args.putSerializable("studio", studio);
        fragment.setArguments(args);
        return fragment;
    }

    public static DisplaySessionFragment newInstance(String sessionID) {
        DisplaySessionFragment fragment = new DisplaySessionFragment();
        Bundle args = new Bundle();
        args.putString(SESSION_ID, sessionID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup static map with session location
        GoogleMapOptions options = new GoogleMapOptions();
        options.liteMode(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);

        postBranchArrayList = new ArrayList<>();
        childEventListenerMap = new HashMap<>();

        if (getArguments() != null) {
            /*
         Get latitude and longitude of session from previous activity.
         Use latitiude and longitude in method findSessionAndFillInUI to fill view
         with session details.
         */
            sessionID = getArguments().getString(SESSION_ID);
            studioId = getArguments().getString("studioId");
            studio = (Studio) getArguments().getSerializable("studio");
        }

        // GET THE PAYMENT INFO FROM CURRENT USER
        getDefaultSourceMap();


        // GET CURRENT USER FROM DATABASE

        ValueEventListener currentUserListener = mUserDbRef.child(currentFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserAndViewUsed = false;
                currentUserAndSessionAndViewAndMapUsed = false;
                currentUser = dataSnapshot.getValue(User.class);
                currentUserLoaded =true;
                onTaskFinished();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listenerMap.put(mUserDbRef.child(currentFirebaseUser.getUid()),currentUserListener);

        // GET SESSION FROM DATABASE
        if (!sessionID.equals("")) {
            // FINDS SESSION AND FILLS UI
            // Get the session information
            if (!listenerMap.containsKey(mSessionDbRef.child(sessionID))) {
                sessionListener = mSessionDbRef.child(sessionID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        session = dataSnapshot.getValue(Session.class);
                        sessionLongitude = session.getLongitude();
                        sessionLatitude = session.getLatitude();
                        sessionID = dataSnapshot.getRef().getKey();
                        currentUserAndSessionAndViewAndMapUsed = false;
                        sessionAndPaymentAndViewUsed = false;
                        sessionUsed = false;
                        sessionLoaded = true;
                        onTaskFinished();
                        getPosts();
                        getSessionHost();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                listenerMap.put(mSessionDbRef.child(sessionID),sessionListener);
            }
        } else {
            Toast toast = Toast.makeText(getActivity(), R.string.Session_not_found_please_try_again_later,Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void getDefaultSourceMap () {
        try {
            MainPlayerActivity mainPlayerActivity = (MainPlayerActivity) getActivity();
            mainPlayerActivity.subject.subscribe(new Consumer<HashMap>() {
                @Override
                public void accept(HashMap hashMap) throws Exception {

                    if (hashMap.get("brand")!=null) {
                        defaultSourceMap = hashMap;
                        hasPaymentSystem = true;
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onTaskFinished();

                    } else {
                        hasPaymentSystem = false;
                        defaultSourceMap = new HashMap();
                        sessionAndPaymentAndViewUsed = false;
                        paymentMethodLoaded = true;
                        onTaskFinished();

                    }
                }
            });
            return;
        } catch (RuntimeException e){
            defaultSourceMap = new HashMap();
            sessionAndPaymentAndViewUsed = false;
            paymentMethodLoaded = true;
            onTaskFinished();
        }
    }

    private void getSessionHost() {
        /*
            Get the host image from the database (found under users with the userID=session.host)
            */
        mUserDbRef.child(session.getHost()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                host = dataSnapshot.getValue(User.class);
                hostLoaded = true;
                onTaskFinished();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getPosts() {

        if (!listenerMap.containsKey(mSessionDbRef.child(sessionID).child("posts"))) {
            ValueEventListener postsListener = mSessionDbRef.child(sessionID).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Session session = new Session();
                    postBranchArrayList.clear();
                    session.setPosts((HashMap<String,Boolean>)dataSnapshot.getValue());
                    if (dataSnapshot.getChildrenCount()>0) {
                        for (final String postID : session.getPosts().keySet()) {
                            rootDbRef.child("posts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Post post = dataSnapshot.getValue(Post.class);
                                    PostBranch postBranch = new PostBranch(dataSnapshot.getKey(),post);
                                    postBranchArrayList.add(postBranch);
                                    if (postBranchArrayList.size()==session.getPosts().size()) {
                                        Collections.sort(postBranchArrayList);
                                        postsLoaded = true;
                                        onTaskFinished();
                                    }
                                    // Number of comments listener
                                    if (!listenerMap.containsKey(rootDbRef.child("postMessages").child(postID))) {

                                        ValueEventListener postMessagesListener = rootDbRef.child("postMessages").child(postID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                nrOfComments.put(dataSnapshot.getKey(), dataSnapshot.getChildrenCount());
                                                if (nrOfComments.size()==session.getPosts().size()) {
                                                    postCommentsUsed = false;
                                                    postCommentsLoaded = true;
                                                    onTaskFinished();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        listenerMap.put(rootDbRef.child("postMessages").child(postID), postMessagesListener);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            listenerMap.put(mSessionDbRef.child(sessionID).child("posts"), postsListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_session, container, false);

        setRetainInstance(true);

        LinearLayout displaySessionContainer;
        View displaySession;
        displaySessionContainer = view.findViewById(R.id.display_session_container);
        displaySession = inflater.inflate(R.layout.display_session,displaySessionContainer,false);

        mDateAndTime = displaySession.findViewById(R.id.dateAndTimeTW);
        mDuration = displaySession.findViewById(R.id.durationTV);
        mParticipants = displaySession.findViewById(R.id.participantsTW);
        mHostImage = displaySession.findViewById(R.id.displaySessionHostImage);
        mHost = displaySession.findViewById(R.id.hostName);
        mHostAboutTV = displaySession.findViewById(R.id.hostAbout);
        mAddressAndSessionType = displaySession.findViewById(R.id.addressAndSessionTypeTW);
        writePostLsyout = displaySession.findViewById(R.id.write_post_layout);
        mWhatTW = displaySession.findViewById(R.id.whatTW);
        mWhoTW = displaySession.findViewById(R.id.whoTW);
        mWhereTW = displaySession.findViewById(R.id.whereTW);
        mCurrentUserPostImage = displaySession.findViewById(R.id.session_post_current_user_image);
        sessionImageCardView = view.findViewById(R.id.sessionImageCardView);
        mSessionType = view.findViewById(R.id.sessionType);
        mSendMessageToHost = displaySession.findViewById(R.id.sendMessageToHost);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        toolbar = view.findViewById(R.id.toolbar);
        priceTV = view.findViewById(R.id.priceTV);
        mDisplaySessionBtn = view.findViewById(R.id.displaySessionBtn);
        displaySessionContainer.addView(displaySession);
        // Set the session image
        sessionImage = view.findViewById(R.id.displaySessionImage);
        postList = (RecyclerView) view.findViewById(R.id.post_list);
        postList.setVisibility(View.GONE);
        paymentMethodProgressBar = view.findViewById(R.id.paymentMethodProgressBar);
        addPaymentMethodTV = view.findViewById(R.id.addPaymentMethodTV);
        paymentFrame = view.findViewById(R.id.framePayment);
        paymentMethodTV = view.findViewById(R.id.paymentMethod);

        paymentFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

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

        // Setup wall, Posts are displayed in a RecyclerView
        postList.setLayoutManager(new LinearLayoutManager(getContext()));
        postsViewHolderAdapter = new RecyclerView.Adapter<PostsViewHolder>() {
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_post_single_layout, parent, false);
                return new PostsViewHolder(view);
            }
            @Override
            public void onBindViewHolder(PostsViewHolder holder, int position) {
                if (postBranchArrayList.size()>0) {
                    holder.setHeading(postBranchArrayList.get(position).getPost().getSenderName());

                    TextTimestamp textTimestamp = new TextTimestamp((long) postBranchArrayList.get(position).getPost().getTimestamp());
                    String timeText = textTimestamp.textDateAndTime();
                    holder.setTime(timeText);

                    holder.setUserImage(postBranchArrayList.get(position).getPost().getSenderThumbImage(), getContext());
                    holder.setMessage(postBranchArrayList.get(position).getPost().getMessage());
                    holder.setCommentClickListener(postBranchArrayList.get(position).getPostID(),postBranchArrayList.get(position).getPost().getSenderName(),timeText, postBranchArrayList.get(position).getPost().getMessage(),postBranchArrayList.get(position).getPost().getSenderThumbImage());
                    holder.setNrOfComments(nrOfComments.get(postBranchArrayList.get(position).getPostID()));
                }
            }
            @Override
            public int getItemCount() {
                return postBranchArrayList.size();
            }
        };
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        postList.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onTaskFinished();
    }

    private void onTaskFinished() {

        // ---------------- CURRENTUSER && VIEW-----------------
        if (currentUserLoaded && getView()!=null && !currentUserAndViewUsed) {
            currentUserAndViewUsed=true;
            // Set the users profile image to the "write post" layout
            setImage(currentUser.getThumb_image(), mCurrentUserPostImage);
        }

        // ---------------- CURRENTUSER && SESSION && VIEW && MAP-----------------
        if (currentUserLoaded && sessionLoaded && mapReady && getView()!=null && !currentUserAndSessionAndViewAndMapUsed) {
            currentUserAndSessionAndViewAndMapUsed =true;
            // SETUP MAP
            LatLng markerLatLng = new LatLng(sessionLatitude, sessionLongitude);
            TextTimestamp textTimestamp = new TextTimestamp(session.getSessionTimestamp());
            mMap.addMarker(new MarkerOptions().position(markerLatLng).title(session.getSessionType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_on_black_24dp)).
                    snippet(textTimestamp.textTime()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,14f));
            // Setup Booking, Cancelling and Editing Button
            mDisplaySessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*
                If  session host equals current user (button will display edit session) start CreateOrEditSessionActivity when button is clicked
                and send the session key to that activity as bundle.
                */
                    if (session.getHost().equals(currentFirebaseUser.getUid())) {
                        //onEditSessionListener.OnEditSession(sessionID);
                        if (!currentUser.isTrainerMode()) {
                            Toast.makeText(getContext(), R.string.not_possible_to_edit_as_participant,Toast.LENGTH_LONG).show();
                        } else {
                            if (sessionID.equals("preview")) {
                                onStudioInteractionListener.OnEditStudio(studioId, studio);
                            } else {
                                onEditSessionListener.OnEditSession(sessionID,session);
                            }
                        }
                    }
                /*
                 Else if current user is a participant in the session (button will display cancel booking) and button is clicked
                remove the current user from that session participant list and go back to main activity.
                */
                    else if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {
                        onCancelBookedSessionListener.OnCancelBookedSession(currentUser.getSessionsAttending().get(sessionID),session.getSessionTimestamp(),sessionID,currentFirebaseUser.getUid(),session.getParticipants().get(currentFirebaseUser.getUid()),session.getStripeAccountId());
                    }

                    else {
                        if (!hasPaymentSystem) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.you_need_a_payment_method_in_order_to_book_this_session).setTitle(R.string.booking_failed);
                            builder.setPositiveButton(R.string.add_payment_method, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                                    startActivity(paymentPreferencesIntent);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        /*
                        Else (button will show join session) add the user id to the session participant list and
                        the user sessions attending list when button is clicked.
                        */
                        onBookSessionListener.OnBookSession(sessionID, session.getHost(), defaultSourceMap.get("customer").toString(), session.getPrice(), session.getCurrency(), currentUser.isDontShowBookingText());
                    }
                }
            });
        }

        // ---------------- HOST && VIEW-----------------
        if (hostLoaded && getView()!=null && !hostAndViewUsed) {
            hostAndViewUsed=true;
            setImage(host.getImage(), mHostImage);
            String hostText = getString(R.string.hosted_by_text) + " " + host.getFirstName();
            mHost.setText(hostText);
            mHostAboutTV.setText(host.getAboutMe());
        }

        if (postsLoaded && getView()!=null && !postsUsed) {
            postsUsed = true;
            postsViewHolderAdapter.notifyDataSetChanged();
            postList.setAdapter(postsViewHolderAdapter);
        }

        if (postCommentsLoaded && getView()!=null && !postCommentsUsed) {
            postCommentsUsed = true;
            postsViewHolderAdapter.notifyDataSetChanged();
        }

        // ------------------------------ SESSION --- PAYMENT METHOD -----------LOADED ------------------
        if (sessionLoaded && getView()!=null && paymentMethodLoaded && !sessionAndPaymentAndViewUsed) {
            sessionAndPaymentAndViewUsed = true;

            if (sessionID.equals("preview")) {
                postList.setVisibility(View.GONE);
                writePostLsyout.setVisibility(View.GONE);
            } else {

                if (session.getParticipants().containsKey(currentFirebaseUser.getUid()) | session.getHost().equals(currentFirebaseUser.getUid())) {

                    postList.setVisibility(View.VISIBLE);
                    writePostLsyout.setVisibility(View.VISIBLE);
                    writePostLsyout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WritePostFragment writePostFragment = WritePostFragment.newInstance(sessionID);
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            if (null == fragmentManager.findFragmentByTag("writePostFragment")) {
                                transaction.add(R.id.container_fullscreen_display_session, writePostFragment,"writePostFragment").addToBackStack(null);
                                transaction.commit();
                            }
                        }
                    });

                } else {
                    postList.setVisibility(View.GONE);
                    writePostLsyout.setVisibility(View.GONE);
                }
            }

            // ---------- Set price text ---------------
            String currencyString = "?";
            if (session.getCurrency()==null) {
                currencyString = "";
            } else {
                currencyString = "kr";
            }
            String priceText;
            if (session.getPrice()== 0) {
                priceText = "Free";
            } else {
                priceText = session.getPrice() + " " + currencyString + " " + "per person";
            }
            priceTV.setText(priceText);

            // -----------  set the number of participants ------------
            long countParticipants;
            if (session.getParticipants().size()>0) {
                countParticipants = session.getParticipants().size();
            } else {
                countParticipants = 0;
            }
            mParticipants.setText(countParticipants +"/" + session.getMaxParticipants());

            // set the image
            setImage(session.getImageUrl(), sessionImage);
            sessionImage.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);

            // -----------  Set the session information in UI from session object --------------
            TextTimestamp textTimestamp = new TextTimestamp(session.getSessionTimestamp());
            String sessionDateAndTime = textTimestamp.textSessionDateAndTime();
            sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
            mDateAndTime.setText(sessionDateAndTime);
            collapsingToolbarLayout.setTitle(session.getSessionName());
            String address = getAddress(session.getLatitude(),session.getLongitude());
            mAddressAndSessionType.setText(address);
            mWhatTW.setText(session.getWhat());
            mWhoTW.setText(session.getWho());
            mWhereTW.setText(session.getWhereAt());
            mSessionType.setText(session.getSessionType());
            mDuration.setText(session.getDuration());

            // Set an onclicklistener to number of participants and start dialog fragment listing participants if clicked
            mParticipants.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParticipantsFragment participantsFragment = ParticipantsFragment.newInstance(session.getParticipants());
                    FragmentManager fragmentManager = getChildFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (participantsFragment!=null) {
                        transaction.remove(participantsFragment);
                    }
                    participantsFragment.show(transaction,"participantsFragment");
                }
            });

            // ------------------ Set the text to the display session button ----------------------------------

            // ---------- BOOK SESSION ---------------------------
            // Set default text on button

            if (!session.getHost().equals(currentFirebaseUser.getUid())) {
                if (defaultSourceMap.get("brand")!=null) {
                    String last4 = defaultSourceMap.get("last4").toString();
                    paymentMethodTV.setText("**** " + last4);
                    String cardBrand = defaultSourceMap.get("brand").toString();
                    int resourceId = BRAND_CARD_RESOURCE_MAP.get(cardBrand);
                    paymentMethodTV.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
                    paymentMethodTV.setVisibility(View.VISIBLE);
                    paymentMethodProgressBar.setVisibility(View.GONE);
                    mDisplaySessionBtn.setEnabled(true);
                    addPaymentMethodTV.setVisibility(View.GONE);
                    mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));
                } else {
                    hasPaymentSystem = false;
                    mDisplaySessionBtn.setEnabled(true);
                    addPaymentMethodTV.setVisibility(View.VISIBLE);
                    paymentMethodTV.setVisibility(View.GONE);
                    mDisplaySessionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_gray));
                    paymentMethodProgressBar.setVisibility(View.GONE);
                }
            }
            mDisplaySessionBtn.setText(getString(R.string.book_session));
            // ---------- CANCEL BOOKING -----------------------------------------
            /**
             If participants are more than zero, see if the current user is one of the participants and if so
             change the button text to "Cancel booking"
             */
            if (session.getParticipants() != null) {
                if (session.getParticipants().containsKey(currentFirebaseUser.getUid())) {

                    mDisplaySessionBtn.setEnabled(true);
                    addPaymentMethodTV.setVisibility(View.GONE);
                    paymentMethodTV.setVisibility(View.GONE);
                    paymentMethodProgressBar.setVisibility(View.GONE);


                    writePostLsyout.setVisibility(View.VISIBLE);
                    mDisplaySessionBtn.setText(R.string.cancel_booking);
                }
            }
            // ----------- EDIT SESSION -----------------------------------------------------
            /**
             If the current user is the session host change the button text to "Edit session"
             */
            if (session.getHost().equals(currentFirebaseUser.getUid())) {

                mDisplaySessionBtn.setEnabled(true);
                addPaymentMethodTV.setVisibility(View.GONE);
                paymentMethodTV.setVisibility(View.GONE);
                paymentMethodProgressBar.setVisibility(View.GONE);

                mDisplaySessionBtn.setText(R.string.edit_session);
                mSendMessageToHost.setText(R.string.show_and_edit_profile_text);
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserAccountFragmentInteractionListener.OnUserAccountFragmentInteraction("edit");
                    }
                });
            } else {
                mSendMessageToHost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onChatClickedListener.OnChatClicked(session.getHost(),host.getFirstName(),host.getThumb_image(),null);
                    }
                });
            }

            // -----------------------------------------------------------------------------------------------
        }
    }

    // Setup static map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        currentUserAndSessionAndViewAndMapUsed = false;
        mapReady = true;
        onTaskFinished();
    }

    // ------------   Posts viewholder for the post recyclerview ----------------------
    public class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setHeading(String heading) {
            TextView headingTV = (TextView) mView.findViewById(R.id.session_post_name);
            headingTV.setText(heading);
        }
        public void setTime(String text) {
            TextView messageView = (TextView) mView.findViewById(R.id.session_post_time);
            messageView.setText(text);
        }
        public void setMessage(String text) {
            TextView messageView = (TextView) mView.findViewById(R.id.session_post_message);
            messageView.setText(text);
        }
        public void setUserImage(String thumb_image, android.content.Context context) {
            CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.session_post_image);
            Glide.with(context).load(thumb_image).into(userProfileImageIV);
        }
        public void setCommentClickListener(String postID, String heading, String time, String message, String thumb_image) {
            TextView commentLayout = mView.findViewById(R.id.session_post_comment_text);
            TextView NrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCommentFragment(postID, heading, time, message, thumb_image);
                }
            });
            NrOfCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCommentFragment(postID, heading, time, message, thumb_image);
                }
            });

        }
        public void setNrOfComments(Long nr) {
            TextView NrOfCommentsLayout = mView.findViewById(R.id.post_nr_comments_text);
            if (nr==null || nr<1) {
                NrOfCommentsLayout.setVisibility(View.GONE);
            } else if (nr<2) {
                NrOfCommentsLayout.setVisibility(View.VISIBLE);
                NrOfCommentsLayout.setText(nr+getString(R.string.comment_text));
            } else {
                NrOfCommentsLayout.setVisibility(View.VISIBLE);
                NrOfCommentsLayout.setText(nr+getString(R.string.comments_text));
            }
        }
    }

    private void startCommentFragment(String postID, String heading, String time, String message, String thumb_image) {
        CommentFragment commentFragment = CommentFragment.newInstance(postID, heading, time, message, thumb_image);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("commentFragment")) {
            transaction.add(R.id.container_fullscreen_display_session, commentFragment,"commentFragment").addToBackStack(null);
            transaction.commit();
        }
    }

    private void setImage(String image, ImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size()!=0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String address2 = addresses.get(0).getAddressLine(1);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                String street = addresses.get(0).getThoroughfare();// Only if available else return NULL

                if (street != null) {

                    if (!street.equals(knownName)) {
                        returnAddress = street + " " + knownName;
                    } else {
                        returnAddress = street;
                    }
                } else {
                    if (addresses.get(0).getLocality()!=null) {
                        returnAddress = addresses.get(0).getLocality() + " " + addresses.get(0).getPremises();
                    } else {
                        returnAddress = "Unknown area";
                    }

                }
            } else {
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {
            returnAddress = "failed";
        }
        return returnAddress;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Map.Entry<Query, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            Query ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        childEventListenerMap.clear();
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
        currentUserAndViewUsed = false;
        currentUserAndSessionAndViewAndMapUsed = false;
        hostAndViewUsed = false;
        sessionUsed = false;
        sessionAndPaymentAndViewUsed = false;
        postsUsed = false;
        postCommentsUsed = false;
    }

    public void cleanListeners () {

        for (Map.Entry<Query, ChildEventListener> entry : childEventListenerMap.entrySet()) {
            Query ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    // Model PostBranch which is used to reflect the branch posts in the database
    public class PostBranch implements Comparable<PostBranch>{
        String postID;
        Post post;
        public PostBranch(String postID, Post post) {
            this.postID = postID;
            this.post = post;
        }

        public PostBranch() {
        }
        public String getPostID() {
            return postID;
        }

        public void setPostID(String postID) {
            this.postID = postID;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        @Override
        public int compareTo(@NonNull PostBranch postBranch) {
            return ((int) (long) this.post.getTimestamp() - (int) (long) postBranch.post.getTimestamp());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnEditSessionListener) {
            onEditSessionListener = (OnEditSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEditSessionListener");
        }
        if (context instanceof OnBookSessionListener) {
            onBookSessionListener = (OnBookSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBookSessionListener");
        }
        if (context instanceof OnCancelBookedSessionListener) {
            onCancelBookedSessionListener = (OnCancelBookedSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCancelBookedSessionListener");
        }
        if (context instanceof OnCommentClickedListener) {
            onCommentClickedListener = (OnCommentClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCommentClickedListener");
        }
        if (context instanceof OnChatClickedListener) {
            onChatClickedListener = (OnChatClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatClickedListener");
        }
        if (context instanceof UserAccountFragment.OnUserAccountFragmentInteractionListener) {
            onUserAccountFragmentInteractionListener = (UserAccountFragment.OnUserAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserAccountFragmentInteractionListener");
        }
        if (context instanceof DisplayStudioFragment.OnStudioInteractionListener) {
            onStudioInteractionListener = (DisplayStudioFragment.OnStudioInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStudioInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onEditSessionListener = null;
        onBookSessionListener = null;
        onCancelBookedSessionListener = null;
        onCommentClickedListener = null;
        onChatClickedListener = null;
        onUserAccountFragmentInteractionListener = null;
        onStudioInteractionListener = null;
    }
    public interface OnEditSessionListener {
        void OnEditSession(String sessionID);
        void OnEditSession(String sessionID , Session session);
    }
    public interface OnBookSessionListener {
        void OnBookSession(String sessionId, String hostId, String stripeCustomerId, int amount, String currency, boolean dontShowBookingText);
    }
    public interface OnCancelBookedSessionListener {
        void OnCancelBookedSession(Long bookingTimestamp, Long sessionTimestamp, String sessionID, String participantId, String chargeId, String accountId);
    }
}