package com.foxmike.android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListCreateAdvertisementsAdapter;
import com.foxmike.android.interfaces.IImageCompressTaskListener;
import com.foxmike.android.interfaces.OnAdvertisementArrayListChangedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionTypeDictionary;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.ImageCompressTask;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateOrEditSessionActivity extends AppCompatActivity {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private TextView mLocation;
    private TextInputLayout mSessionNameTIL;
    private TextInputLayout mLocationTIL;
    private TextInputLayout mSessionTypeTIL;
    private TextInputLayout mDateTIL;
    private TextInputLayout mWhatTIL;
    private TextInputLayout mWhoTIL;
    private TextInputLayout mWhereTIL;
    private TextInputEditText mSessionName;
    private TextInputEditText mSessionType;
    private EditText mWhat;
    private EditText mWho;
    private EditText mWhere;
    private Button mCreateSessionBtn;
    private final Calendar myCalendar = Calendar.getInstance();
    private ListView lv;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    private boolean updateSession;
    private long mSessionTimestamp;
    private ImageButton mSessionImageButton;
    private static final int GALLERY_REQUEST = 1;
    private static final int PAYOUT_METHOD_REQUEST = 2;
    static final int GET_USER_REQUEST = 5;
    private static final int UPDATE_SESSION_LOCATION_REQUEST = 3;
    private static final int CREATE_ADVERTISEMENT = 4;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;
    private LatLng clickedLatLng;
    private String existingSessionID;
    private String thisSessionID;
    private Session thisSession;
    private String mSessionId;
    private Session existingSession;
    private Session exSession;
    private GeoFire geoFire;
    private DatabaseReference currentUserDbRef;
    private FirebaseAuth mAuth;
    private FragmentManager fragmentManager;
    private FrameLayout mapsFragmentContainer;
    private String accountCountry;
    private boolean payoutsEnabled;
    private boolean infoIsValid = true;
    private Session mUpdatedSession;
    private String accountCurrency;
    private String stripeAccountId;
    private boolean hasParticipants = false;
    private TextView imageErrorText;
    private CompactCalendarView compactCalendarView;
    private TextView calendarHeadingTV;
    private HashMap<Long, Advertisement> timestampsAndAdvertisements = new HashMap<>();
    private ArrayList<Advertisement> mAdsArrayList = new ArrayList<>();
    private ArrayList<Advertisement> mExistingAdsArrayList = new ArrayList<>();
    private Map advertisements = new HashMap();
    private long mLastClickTime = 0;
    private LinearLayout allExceptCalendar;
    private boolean addAdvertisements;
    private String type;
    private NestedScrollView createOrEditSV;
    private String address;
    private HashMap<String, RadioButton> radioGroupHashMap = new HashMap<>();
    private RadioGroup currencySettingRadioGroup;
    private InputMethodManager imm;
    private ArrayAdapter<String> adapter;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog dlg;
    private View mainView;
    private View progressOverlay;
    private DotProgressBar progressBar;
    private String accountCountry1;
    private int duration;
    private int maxParticipants;
    private int price =-1;
    private boolean advertisementsIsLoaded;
    private boolean stripeAccountIsLoaded;
    private RecyclerView adRecyclerView;
    private ConstraintLayout recyclerViewContainer;
    private DateTime selectedDate;
    private ListCreateAdvertisementsAdapter listCreateAdvertisementsAdapter;
    private TextView addAdvertisement;
    private boolean sessionLoadedIfAny;
    private HashMap<String, String> sessionTypeFilterMap;
    private ArrayList<String> sessionTypeArray;
    private boolean sessionTypesLoaded;
    private boolean uiLoaded;
    private Query sessionAdRef;
    private ValueEventListener sessionAdListener;
    private boolean calendarLoaded;
    private TextView secondaryHostTV;
    private HashMap<Query, Boolean> liveDataQueries = new HashMap<>();


    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_PHOTO = 101;

    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private ImageCompressTask imageCompressTask;
    private Uri compressedImageUri = null;
    private Uri compressedImageHiResUri = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean userIsLoaded;
    private User currentUser;
    private String secondaryHostId;
    private String secondaryHostFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_session);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        checkIfPayOutsIsEnabled();

        existingSessionID = getIntent().getStringExtra("sessionID");
        type = getIntent().getStringExtra("type");
        clickedLatLng = getIntent().getParcelableExtra("LatLng");
        existingSession = getIntent().getParcelableExtra("session");
        addAdvertisements = getIntent().getBooleanExtra("addAdvertisements", false);

        mainView = findViewById(R.id.mainView);

        /* Set and inflate "create session" layout*/
        View createSession;
        LinearLayout createSessionContainer = findViewById(R.id.create_session_container);
        createSession = getLayoutInflater().inflate(R.layout.create_or_edit_session, createSessionContainer,false);
        // Setup views
        mLocation = createSession.findViewById(R.id.locationTV);
        mSessionName = createSession.findViewById(R.id.sessionNameET);
        mSessionType = createSession.findViewById(R.id.sessionTypeET);
        mSessionNameTIL = createSession.findViewById(R.id.sessionNameTIL);
        mLocationTIL = createSession.findViewById(R.id.locationTIL);
        mSessionTypeTIL = createSession.findViewById(R.id.sessionTypeTIL);
        mDateTIL = createSession.findViewById(R.id.dateTIL);
        mWhatTIL = createSession.findViewById(R.id.whatTIL);
        mWhoTIL = createSession.findViewById(R.id.whoTIL);
        mWhereTIL = createSession.findViewById(R.id.whereTIL);
        mWhat = createSession.findViewById(R.id.whatET);
        mWho = createSession.findViewById(R.id.whoET);
        mWhere = createSession.findViewById(R.id.whereET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(this);
        mCreateSessionBtn = createSession.findViewById(R.id.createSessionBtn);
        mSessionImageButton = createSession.findViewById(R.id.sessionImageBtn);
        mapsFragmentContainer = findViewById(R.id.container_maps_fragment);
        imageErrorText = createSession.findViewById(R.id.imageErrorText);
        compactCalendarView = (CompactCalendarView) createSession.findViewById(R.id.compactcalendar_view);
        calendarHeadingTV = createSession.findViewById(R.id.calendarHeadingTV);
        allExceptCalendar = createSession.findViewById(R.id.allExceptCalendar);
        createOrEditSV = findViewById(R.id.scrollview_create_session);
        progressOverlay = createSession.findViewById(R.id.payoutProgressOverlay);
        progressBar = createSession.findViewById(R.id.payoutProgressBar);
        adRecyclerView = createSession.findViewById(R.id.advertisementsRV);
        recyclerViewContainer = createSession.findViewById(R.id.recyclerViewContainer);
        addAdvertisement = createSession.findViewById(R.id.addAdvertisement);
        secondaryHostTV = createSession.findViewById(R.id.secondaryHost);

        adRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectedDate = new DateTime().minusMonths(2);

        String language = getResources().getConfiguration().locale.getLanguage();
        DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(language);
        sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sessionTypeFilterMap = new SessionTypeDictionary(getResources().getString(R.string.other));
                sessionTypeArray = new ArrayList<>();
                if (dataSnapshot.getValue()!=null) {
                    for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                        sessionTypeFilterMap.put(sessionTypeSnap.getKey(), sessionTypeSnap.getValue().toString());
                        sessionTypeArray.add(sessionTypeSnap.getValue().toString());
                    }
                    sessionTypesLoaded = true;
                    setupUI();
                } else {
                    DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child("en");
                    sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                                sessionTypeFilterMap.put(sessionTypeSnap.getKey(), sessionTypeSnap.getValue().toString());
                                sessionTypeArray.add(sessionTypeSnap.getValue().toString());
                            }
                            sessionTypesLoaded = true;
                            setupUI();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(currentUserRef);
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                userIsLoaded = true;
                setupUI();
            }
        });


        Formatter fmt = new Formatter();
        fmt.format("%tB", compactCalendarView.getFirstDayOfCurrentMonth());
        calendarHeadingTV.setText(fmt.toString());
        // Listen to clicks
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                selectedDate = new DateTime(dateClicked);
                boolean currentDayHasAds = false;

                for (Advertisement ad: mAdsArrayList) {
                    DateTime adDateTime = new DateTime(ad.getAdvertisementTimestamp());
                    if (selectedDate.toLocalDate().equals(adDateTime.toLocalDate())) {
                        currentDayHasAds = true;
                    }
                }

                for (Advertisement exAd: mExistingAdsArrayList) {
                    DateTime adDateTime = new DateTime(exAd.getAdvertisementTimestamp());
                    if (selectedDate.toLocalDate().equals(adDateTime.toLocalDate())) {
                        currentDayHasAds = true;
                    }
                }

                if (currentDayHasAds) {
                    if (listCreateAdvertisementsAdapter==null) {
                        listCreateAdvertisementsAdapter = new ListCreateAdvertisementsAdapter(mExistingAdsArrayList, mAdsArrayList, selectedDate, new OnAdvertisementArrayListChangedListener() {
                            @Override
                            public void OnAdvertisementArrayList(ArrayList<Advertisement> advertisementArrayList) {
                                mAdsArrayList = advertisementArrayList;
                                checkIfRecyclerViewShouldBeVisible();
                            }
                        });
                        adRecyclerView.setAdapter(listCreateAdvertisementsAdapter);
                    } else {
                        listCreateAdvertisementsAdapter.updateAdvertisements(mExistingAdsArrayList, mAdsArrayList, selectedDate);
                    }
                    checkIfRecyclerViewShouldBeVisible();
                } else {
                    checkIfRecyclerViewShouldBeVisible();
                    createAdvertisement(dateClicked);
                }


            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Formatter fmt = new Formatter();
                fmt.format("%tB", compactCalendarView.getFirstDayOfCurrentMonth());
                calendarHeadingTV.setText(fmt.toString());
            }
        });
        compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        checkIfRecyclerViewShouldBeVisible();

        addAdvertisement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAdvertisement(selectedDate.toDate());
            }
        });

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mCreateSessionBtn.setVisibility(View.GONE);

        // Add view to create session container
        createSessionContainer.addView(createSession);

        /*The Firebase Database client in our app can keep the data from the database in two places: in memory and/or on disk.
          This keeps the data on the disk even though listeners are detached*/

        // Setup standard aspect ratio of session image
        mSessionImageButton.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) mSessionImageButton.getLayoutParams();
                mParams.height = mSessionImageButton.getWidth()*getResources().getInteger(R.integer.heightOfSessionImageNumerator)/getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                mSessionImageButton.setLayoutParams(mParams);
                mSessionImageButton.postInvalidate();
            }
        });

        // FILL VIEW with the session in bundle or with the session with the sessionID

        if (existingSessionID != null | existingSession!=null) {
            if (addAdvertisements) {
                allExceptCalendar.setVisibility(View.GONE);
            } else {
                mDateTIL.setVisibility(View.GONE);
            }
            updateSession = true;
            /**If this activity was started from clicking on an edit session or returning from mapsfragment the previous activity should have sent a bundle with the session key or session object, if so
             * extract the key and fill in the existing values in the view (Edit view). Set the text of the button to "Update session"*/
            if (existingSession==null) {
                final DatabaseReference sessionIDref = mMarkerDbRef.child(existingSessionID);
                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        existingSession = dataSnapshot.getValue(Session.class);
                        sessionLoadedIfAny = true;
                        setupUI();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                sessionLoadedIfAny = true;
                setupUI();
            }
        } /* If no bundle or sessionID exists, the method takes for granted that the activity was started by clicking on the map and a bundle with the LatLng object should exist,
          if so extract the LatLng and set the image to the default image (Create view)*/
        else {
            if (clickedLatLng==null) {

            } else {
                address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
                mLocation.setText(address);
            }
            mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER);
            sessionLoadedIfAny = true;
            setupUI();
        }

        /**When button is clicked set the values in the edittext fields to a session object */
        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (updateSession) {
                    mProgress.setMessage(getString(R.string.updating_session));
                } else {
                    mProgress.setMessage(getString(R.string.creating_session));
                }

                if (clickedLatLng==null) {
                    Toast.makeText(CreateOrEditSessionActivity.this, "Still fetching existing session information, please wait... No pain no gain ;)", Toast.LENGTH_LONG).show();
                    return;
                }

                mProgress.show();

                updateSessionObjectFromUI(new OnSessionUpdatedListener() {
                    @Override
                    public void OnSessionUpdated(final Map sessionMap) {

                        if (!payoutsEnabled && (int) sessionMap.get("price")!=0) {

                            LayoutInflater factory = LayoutInflater.from(CreateOrEditSessionActivity.this);
                            View okDialogView = factory.inflate(R.layout.fragment_dialog, null);
                            AlertDialog okDialog = new AlertDialog.Builder(CreateOrEditSessionActivity.this).create();
                            okDialog.setView(okDialogView);
                            TextView tv = okDialogView.findViewById(R.id.textTV);
                            tv.setText(R.string.no_active_payout_method);
                            okDialogView.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    okDialog.dismiss();
                                }
                            });

                            okDialog.show();

                        } else {
                            if (infoIsValid) {sendSession(sessionMap);} else {
                                Toast.makeText(getApplicationContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });

        if (type!=null) {
            if (type.equals("what")) {
                mWhat.post(new Runnable() {
                    @Override
                    public void run() {
                        createOrEditSV.smoothScrollTo(0, mWhatTIL.getBottom());
                        mWhat.setFocusableInTouchMode(true);
                        mWhat.requestFocus();
                        mWhat.setSelection(mWhat.getText().length());
                        showKeyboard();
                    }
                });
            }
            if (type.equals("who")) {
                mWho.post(new Runnable() {
                    @Override
                    public void run() {
                        createOrEditSV.smoothScrollTo(0, mWhoTIL.getBottom());
                        mWho.setFocusableInTouchMode(true);
                        mWho.requestFocus();
                        mWho.setSelection(mWho.getText().length());
                        showKeyboard();
                    }
                });
            }
            if (type.equals("where")) {
                mWhere.post(new Runnable() {
                    @Override
                    public void run() {
                        createOrEditSV.smoothScrollTo(0, mWhereTIL.getBottom());
                        mWhere.setFocusableInTouchMode(true);
                        mWhere.requestFocus();
                        mWhere.setSelection(mWhere.getText().length());
                        showKeyboard();
                    }
                });
            }
        }
    }

    private void createAdvertisement(Date dateClicked) {
        Date currentDate = new Date();

        if (dateClicked.before(currentDate) && !DateUtils.isToday(dateClicked.getTime())) {
            cannotCreateSessionInPastPopUp();
            return;
        }
        //compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.grayTextColor));
        // set the calendar to the clicked date
        myCalendar.setTime(dateClicked);

        Intent createAdIntent = new Intent(CreateOrEditSessionActivity.this, CreateAdvertisementActivity.class);
        createAdIntent.putExtra("date", dateClicked.getTime());

        if (existingSession!=null) {
            duration = existingSession.getDurationInMin();
            maxParticipants = existingSession.getMaxParticipants();
            price = existingSession.getPrice();
        }

        createAdIntent.putExtra("duration", duration);
        createAdIntent.putExtra("maxParticipants", maxParticipants);
        createAdIntent.putExtra("price", price);

        createAdIntent.putExtra("advertisementArrayList", mAdsArrayList);
        createAdIntent.putExtra("payoutsEnabled", payoutsEnabled);
        createAdIntent.putExtra("accountCurrency", accountCurrency);
        startActivityForResult(createAdIntent, CREATE_ADVERTISEMENT);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay );
    }

    private void checkIfRecyclerViewShouldBeVisible() {
        Boolean hasAdvertisements = false;
        compactCalendarView.removeAllEvents();
        for (Advertisement ad: mAdsArrayList) {
            DateTime adDateTime = new DateTime(ad.getAdvertisementTimestamp());
            if (selectedDate.toLocalDate().equals(adDateTime.toLocalDate())) {
                hasAdvertisements = true;
            }

            Event event = new Event(getResources().getColor(R.color.foxmikePrimaryColor), ad.getAdvertisementTimestamp());
            compactCalendarView.addEvent(event);

        }
        for (Advertisement mExAd: mExistingAdsArrayList) {
            DateTime adDateTime = new DateTime(mExAd.getAdvertisementTimestamp());
            if (selectedDate.toLocalDate().equals(adDateTime.toLocalDate())) {
                hasAdvertisements = true;
            }

            Event event = new Event(getResources().getColor(R.color.foxmikePrimaryDarkColor), mExAd.getAdvertisementTimestamp());
            compactCalendarView.addEvent(event);

        }
        if (hasAdvertisements) {
            recyclerViewContainer.setVisibility(View.VISIBLE);
            recyclerViewContainer.post(new Runnable() {
                @Override
                public void run() {
                    createOrEditSV.smoothScrollTo(0, mCreateSessionBtn.getBottom());
                }
            });

        } else {
            recyclerViewContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void cannotCreateSessionInPastPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrEditSessionActivity.this);
        builder.setMessage("Cannot create a session in the past.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupUI() {

        if (!sessionLoadedIfAny || !sessionTypesLoaded || uiLoaded || !userIsLoaded) {
            return;
        }
        uiLoaded = true;

        if (currentUser.isSuperAdmin()) {
            secondaryHostTV.setVisibility(View.VISIBLE);
            secondaryHostTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent getUserIntent = new Intent(CreateOrEditSessionActivity.this, GetUserActivity.class);
                    startActivityForResult(getUserIntent, GET_USER_REQUEST);
                }
            });
        }

        if (existingSession!=null) {

            if (existingSession.getSecondaryHostId()!=null) {
                secondaryHostId = existingSession.getSecondaryHostId();
                secondaryHostTV.setText("Has secondary host");
            }

            clickedLatLng = new LatLng(existingSession.getLatitude(), existingSession.getLongitude());
            address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
            mLocation.setText(address);
            setImage(existingSession.getImageUrl(),mSessionImageButton);
            mSessionName.setText(existingSession.getSessionName());
            mSessionType.setText(sessionTypeFilterMap.get(existingSession.getSessionType()));
            Long currentTimestamp = System.currentTimeMillis();
            // Existing advertisements
            FirebaseDatabaseViewModel sessionAdvertisementsViewModel = ViewModelProviders.of(CreateOrEditSessionActivity.this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> sessionAdvertisementsLiveData = sessionAdvertisementsViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(existingSession.getSessionId()).orderByValue().startAt(currentTimestamp));
            sessionAdvertisementsLiveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        setupAdDependentViews(true);
                    } else {
                        setupAdDependentViews(false);
                    }
                    mExistingAdsArrayList.clear();
                    ArrayList<Task<?>> tasks = new ArrayList<>();
                    if (dataSnapshot.getChildrenCount()>0) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
                            Task dbTask = dbSource.getTask();
                            DatabaseReference ref = rootDbRef.child("advertisements").child(snapshot.getKey());
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dbSource.trySetResult(dataSnapshot);
                                    if (dataSnapshot.getValue()!=null) {
                                        mExistingAdsArrayList.add(dataSnapshot.getValue(Advertisement.class));
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    dbSource.setException(databaseError.toException());
                                }
                            });
                            tasks.add(dbTask);
                        }
                        Tasks.whenAll(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                advertisementsIsLoaded = true;
                                tryLoadCalendar();
                            }
                        });
                    } else {
                        advertisementsIsLoaded = true;
                        tryLoadCalendar();
                    }

                }
            });

            mWhat.setText(existingSession.getWhat());
            mWho.setText(existingSession.getWho());
            mWhere.setText(existingSession.getWhereAt());

        } else {
            setupAdDependentViews(false);
            advertisementsIsLoaded = true;
            tryLoadCalendar();
        }

        if (updateSession) {
            mCreateSessionBtn.setText(getString(R.string.update_session));
        } else {
            mProgress.setMessage(getString(R.string.create_session));
        }

        // -------------- Set on button click listeners --------------------

        /*When imagebutton is clicked start gallery in phone to let user choose photo/image*/
        mSessionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                imageErrorText.setVisibility(View.GONE);

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mSessionName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mSessionNameTIL.setError(null);
            }
        });

        mWhat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length()<51) {
                    mWhatTIL.setError(getString(R.string.please_write_longer_session_description));
                    infoIsValid = false;
                } else {
                    infoIsValid = true;
                    mWhatTIL.setError(null);
                }

            }
        });

        mWho.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mWhoTIL.setError(null);
            }
        });

        mWhere.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mWhereTIL.setError(null);
            }
        });

    }

    private void setupAdDependentViews(boolean hasAds) {
        if (hasAds) {
            /** When item is clicked create a dialog where use can choose between different session types */
            mSessionTypeTIL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogOk(getString(R.string.not_editable), getString(R.string.not_editable_session_type));
                }
            });
            mSessionType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogOk(getString(R.string.not_editable), getString(R.string.not_editable_session_type));
                }
            });
            mSessionName.setInputType(InputType.TYPE_NULL);
            mSessionName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogOk(getString(R.string.not_editable), getString(R.string.not_editable_session_name));
                }
            });
            // Setup location icon click listener
            mLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogOk(getString(R.string.not_editable), getString(R.string.not_editable_location));
                }
            });
        } else {
            /** When item is clicked create a dialog where use can choose between different session types */
            mSessionTypeTIL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSessionTypeTIL.setError(null);
                    createDialogWithArray(getString(R.string.choose_session_type), sessionTypeArray, mSessionType);
                }
            });
            mSessionType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSessionTypeTIL.setError(null);
                    createDialogWithArray(getString(R.string.choose_session_type), sessionTypeArray, mSessionType);
                }
            });
            // Setup location icon click listener
            mLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    mLocationTIL.setError(null);

                    Intent chooseLocatonIntent = new Intent(CreateOrEditSessionActivity.this, ChooseLocationActivity.class);
                    startActivityForResult(chooseLocatonIntent, UPDATE_SESSION_LOCATION_REQUEST);

                }
            });
        }

    }

    private void tryLoadCalendar() {
        if (stripeAccountIsLoaded && advertisementsIsLoaded && !calendarLoaded) {
            calendarLoaded = true;

            compactCalendarView.removeAllEvents();

            if (mExistingAdsArrayList.size()>0 || mAdsArrayList.size()>0) {

                if (mExistingAdsArrayList.size()>0) {
                    for (Advertisement advertisement: mExistingAdsArrayList) {
                        Event event = new Event(getResources().getColor(R.color.foxmikePrimaryDarkColor), advertisement.getAdvertisementTimestamp());
                        compactCalendarView.addEvent(event);
                    }
                }

                if (mAdsArrayList.size()>0) {
                    for (Advertisement advertisement: mAdsArrayList) {
                        Event event = new Event(getResources().getColor(R.color.foxmikePrimaryColor), advertisement.getAdvertisementTimestamp());
                        compactCalendarView.addEvent(event);
                    }
                }


                listCreateAdvertisementsAdapter = new ListCreateAdvertisementsAdapter(mExistingAdsArrayList, mAdsArrayList, new DateTime(selectedDate), new OnAdvertisementArrayListChangedListener() {
                    @Override
                    public void OnAdvertisementArrayList(ArrayList<Advertisement> advertisementArrayList) {
                        mAdsArrayList = advertisementArrayList;
                        checkIfRecyclerViewShouldBeVisible();
                    }
                });
                adRecyclerView.setAdapter(listCreateAdvertisementsAdapter);
            }

            progressOverlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            mCreateSessionBtn.setVisibility(View.VISIBLE);
        }
    }

    private void checkIfPayOutsIsEnabled() {
        // --------------CHECK IF PAYOUTS ARE ENABLED------
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // ----------- PAYOUTS NOT ENABLED --------------
                    payoutsEnabled = false;
                    stripeAccountIsLoaded = true;
                    tryLoadCalendar();
                } else {
                    String stripeAccountId = dataSnapshot.getValue().toString();
                    // Stripe function
                    retrieveStripeAccount(stripeAccountId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                            // If not succesful, show error and return from function, will trigger if account ID does not exist
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                showSnackbar("An error occurred." + e.getMessage());
                                // ----------- PAYOUTS NOT ENABLED --------------
                                payoutsEnabled = false;
                                stripeAccountIsLoaded = true;
                                tryLoadCalendar();
                                return;
                            }
                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("resultType").toString().equals("account")) {

                                HashMap<String, Object> account = (HashMap<String, Object>) result.get("account");
                                accountCountry1 = account.get("country").toString();
                                accountCurrency = account.get("default_currency").toString();

                                // ----------- PAYOUTS ENABLED --------------
                                if (account.get("payouts_enabled").toString().equals("true")) {
                                    payoutsEnabled = true;
                                    stripeAccountIsLoaded = true;
                                    tryLoadCalendar();

                                } else {
                                    // ----------- PAYOUTS NOT ENABLED --------------
                                    payoutsEnabled = false;
                                    stripeAccountIsLoaded = true;
                                    tryLoadCalendar();
                                }

                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                showSnackbar(error.get("message").toString());
                            }
                            // [END_EXCLUDE]
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                payoutsEnabled = false;
                stripeAccountIsLoaded = true;
                tryLoadCalendar();
            }
        });
    }

    public void updateSessionObjectFromUI(OnSessionUpdatedListener onSessionUpdatedListener) {

        if (TextUtils.isEmpty(mLocation.getText().toString())) {
            mLocationTIL.setError(getString(R.string.please_choose_location));
            infoIsValid = false;
            mProgress.dismiss();
            return;
        }

        if (existingSession!=null) {
            maxParticipants = existingSession.getMaxParticipants();
            duration = existingSession.getDurationInMin();
            price = existingSession.getPrice();
        }

        //final Session session = new Session();
        Map sessionMap = new HashMap();

        if (updateSession) {
            mSessionId = existingSessionID;
        } else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        if (secondaryHostId!=null) {
            sessionMap.put("secondaryHostId", secondaryHostId);
        }

        if (currentUser.isSuperAdmin() && secondaryHostId==null) {
            sessionMap.put("superHosted", true);
        } else {
            sessionMap.put("superHosted", false);
        }

        sessionMap.put("sessionId", mSessionId);
        sessionMap.put("sessionName", mSessionName.getText().toString());
        String sessionTypeCode = "000";
        String chosenSessionType = mSessionType.getText().toString();
        for (String sessionTypeFilterCode: sessionTypeFilterMap.keySet()) {
            if (sessionTypeFilterMap.get(sessionTypeFilterCode).equals(chosenSessionType)) {
                sessionMap.put("sessionType", sessionTypeFilterCode);
                sessionTypeCode = sessionTypeFilterCode;
            }
        }
        if (sessionTypeCode.equals("000")) {
            Toast.makeText(CreateOrEditSessionActivity.this, "Sorry, could not find session type, please try again later or contact Foxmike.", Toast.LENGTH_LONG).show();
        }
        sessionMap.put("what",mWhat.getText().toString());
        sessionMap.put("who", mWho.getText().toString());
        sessionMap.put("whereAt", mWhere.getText().toString());
        sessionMap.put("maxParticipants", maxParticipants);
        sessionMap.put("durationInMin", duration);
        sessionMap.put("maxParticipants", maxParticipants);
        if (payoutsEnabled) {
            sessionMap.put("price", price);
            sessionMap.put("currency", accountCurrency);
        } else {
            sessionMap.put("price", 0);
            sessionMap.put("currency", "free");
        }

        sessionMap.put("longitude", clickedLatLng.longitude);
        sessionMap.put("latitude", clickedLatLng.latitude);
        sessionMap.put("host", currentFirebaseUser.getUid());
        sessionMap.put("stripeAccountId", stripeAccountId);
        sessionMap.put("address", mLocation.getText().toString());

        if (TextUtils.isEmpty(mSessionName.getText().toString())) {
            mSessionNameTIL.setError(getString(R.string.please_choose_session_name));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(mLocation.getText().toString())) {
            mLocationTIL.setError(getString(R.string.please_choose_location_for_session));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(mSessionType.getText().toString())) {
            mSessionTypeTIL.setError(getString(R.string.please_choose_session_type));
            infoIsValid = false;
        }


        if (TextUtils.isEmpty(mWhat.getText().toString())) {
            mWhatTIL.setError(getString(R.string.please_write_longer_session_description));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(mWho.getText().toString())) {
            mWhoTIL.setError(getString(R.string.please_write_session_who));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(mWhere.getText().toString())) {
            mWhereTIL.setError(getString(R.string.please_explain_session_where));
            infoIsValid = false;
        }

        /**If imageUrl exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/

        if(compressedImageUri != null && infoIsValid){
            StorageReference filepath = mStorageSessionImage.child(mSessionId);
            filepath.putFile(compressedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri = uri.toString();
                            /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                             * to the main activity*/
                            sessionMap.put("imageUrl", downloadUri);

                            StorageReference hiResfFilepath = mStorageSessionImage.child(mSessionId + "hiRes");

                            hiResfFilepath.putFile(compressedImageHiResUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    hiResfFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUri = uri.toString();
                                            sessionMap.put("imageUrlHiRes", downloadUri);
                                            if (infoIsValid){
                                                onSessionUpdatedListener.OnSessionUpdated(sessionMap);
                                            }
                                            mProgress.dismiss();

                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
        /**If imageUri does not exists it means that the user has NOT selected a photo from the gallery, check if the session is an existing session*/
        else {
            /**If the session is an existing session set the created session object image uri to the existing image uri and send the updated object to the realtime database
             * and send the user back to the main activity*/
            if (updateSession) {
                sessionMap.put("imageUrl", existingSession.getImageUrl());
                mProgress.dismiss();

                if (infoIsValid){
                    onSessionUpdatedListener.OnSessionUpdated(sessionMap);
                }

            }
            /**If the session is NOT an existing session tell the user that a photo must be chosen*/
            else {
                if (compressedImageUri == null) {
                    imageErrorText.setVisibility(View.VISIBLE);
                }
                mProgress.dismiss();
            }
        }
    }

    /**Send session object to database */
    private void sendSession(Map sendSession) {

        ArrayList<String> writeReferences = new ArrayList<>();
        // Create (or update) session button has been pressed. Create advertisements of the occasions set in the calendar.
        // Loop through the timestamps created by clicking and making events in the calendar
        for (Advertisement advertisement: mAdsArrayList) {
            if (advertisement.getSessionId()==null) {
                // For each timestamp, create an Advertisement object of the class Advertisement, take nost of the data from the current session being created
                String advertisementKey = rootDbRef.child("advertisements").push().getKey();

                advertisement.setAdvertisementId(advertisementKey);
                advertisement.setStatus("active");
                advertisement.setSessionId((String) sendSession.get("sessionId"));
                advertisement.setHost((String) sendSession.get("host"));
                advertisement.setSessionName((String) sendSession.get("sessionName"));
                advertisement.setImageUrl((String) sendSession.get("imageUrl"));
                advertisement.setCurrency(accountCurrency);

                // Save the advertisement Id and the ad timestamp in a hashmap to be saved under sessionAdvertisements
                advertisements.put(advertisementKey, advertisement.getAdvertisementTimestamp());
                // send the ad to the database
                rootDbRef.child("advertisements").child(advertisementKey).setValue(advertisement).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // save the key and ad timestamp under user/advertisementHosting
                        writeReferences.add(rootDbRef.child("advertisements").child(advertisementKey).toString());
                        rootDbRef.child("advertisementHosts").child(currentFirebaseUser.getUid()).child(advertisementKey).setValue(advertisement.getAdvertisementTimestamp());
                    }
                });
                // create geoFire reference
                /* Create Geofire object in order to store latitude and longitude under in Geofire structure */

                String geoFireDateNode = TextTimestamp.textSDF(advertisement.getAdvertisementTimestamp());


                String sessionType = (String) sendSession.get("sessionType");
                String currency = accountCurrency;
                String price = "0000";
                String rating = "99";

                String stringPrice = Integer.toString(advertisement.getPrice());
                switch(stringPrice.length()) {
                    case 0:
                        break;
                    case 1:
                        price = "000" + stringPrice;
                        break;
                    case 2:
                        price = "00" + stringPrice;
                        break;
                    case 3:
                        price = "0" + stringPrice;
                        break;
                    case 4:
                        price = stringPrice;
                        break;
                    default:
                        price = "0000";
                }

                if (existingSession!=null) {
                    int ratingInt = Math.round(existingSession.getRating()*10) ;
                    if (ratingInt<10) {
                        rating = "0" + Integer.toString(ratingInt);
                    } else {
                        rating = Integer.toString(ratingInt);
                    }
                }

                String geoFireKey = advertisement.getAdvertisementTimestamp() + advertisement.getAdvertisementId() + advertisement.getSessionId() + sessionType + currency + price + rating;

                DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geoFire").child(geoFireDateNode);
                geoFire = new GeoFire(mGeofireDbRef);
                geoFire.setLocation(geoFireKey, new GeoLocation((double)sendSession.get("latitude"), (double)sendSession.get("longitude")));

                // Save the reference so it is easy to remove from geofire if cancelled
                FirebaseDatabase.getInstance().getReference().child("advertisementGeoFireReference").child(advertisement.getAdvertisementId()).setValue(geoFireDateNode + "/" +geoFireKey);

                Bundle bundle = new Bundle();
                bundle.putString("session_id", advertisement.getSessionId());
                bundle.putString("advertisement_id", advertisement.getAdvertisementId());
                bundle.putString("advertisement_date", TextTimestamp.textSDF(advertisement.getAdvertisementTimestamp()));
                bundle.putString("session_name", (String) sendSession.get("sessionName"));
                bundle.putString("session_type", (String) sendSession.get("sessionType"));
                bundle.putDouble("session_price", (double) advertisement.getPrice());
                bundle.putString("session_currency", advertisement.getCurrency());
                bundle.putString("session_host", advertisement.getHost());
                mFirebaseAnalytics.logEvent("advertisement", bundle);
            }
        }
        // Save the hashmap of ad Ids and timestamps under sessionAdvertisements
        rootDbRef.child("sessionAdvertisements").child(mSessionId).updateChildren(advertisements);
        // Update the session (with 'updateChildren' so not all child nodes are overwritten)
        rootDbRef.child("sessions").child(mSessionId).updateChildren(sendSession);
        // Update user object with sessionsHosting
        rootDbRef.child("sessionHosts").child(currentFirebaseUser.getUid()).child(mSessionId).setValue(true);
        finish();
    }

    // Function retrieveStripeAccount
    private Task<HashMap<String, Object>> retrieveStripeAccount(String accountId) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("retrieveAccount")
                .call(accountId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }

    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialogWithArray(String title, ArrayList<String> string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        lv.getDivider().setAlpha(0);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,string_array);
        lv.setAdapter(adapter);
        final AlertDialog dlg = alertDialogBuilder.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) lv.getItemAtPosition(position);
                mEditText.setText(itemValue);
                dlg.dismiss();
            }
        });
    }

    /** When user has selected an image from the gallery get that imageURI and save it in mImageUri and set the image to the imagebutton  */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                secondaryHostId = data.getStringExtra("secondaryHostId");
                secondaryHostFullName = data.getStringExtra("secondaryHostFullName");
                secondaryHostTV.setText("Secondary host: " + secondaryHostFullName);
            }
        }

        if (requestCode == CREATE_ADVERTISEMENT) {
            payoutsEnabled = data.getBooleanExtra("payoutsEnabled", payoutsEnabled);
            if (resultCode == RESULT_OK) {

                duration = data.getIntExtra("duration", 0);
                maxParticipants = data.getIntExtra("maxParticipants", 0);
                price = data.getIntExtra("price", 100);
                Long timestamp = data.getLongExtra("dateAndTime", 0);
                mAdsArrayList = (ArrayList<Advertisement>) data.getSerializableExtra("advertisementArrayList");

                if (mAdsArrayList==null) {
                    mAdsArrayList = new ArrayList<>();
                }

                if (mExistingAdsArrayList==null) {
                    mExistingAdsArrayList = new ArrayList<>();
                }

                if (existingSession!=null) {
                    existingSession.setDurationInMin(duration);
                    existingSession.setMaxParticipants(maxParticipants);
                    existingSession.setPrice(price);
                }

                if (listCreateAdvertisementsAdapter!=null) {
                    listCreateAdvertisementsAdapter.updateAdvertisements(mExistingAdsArrayList, mAdsArrayList, selectedDate);
                    checkIfRecyclerViewShouldBeVisible();
                } else {
                    listCreateAdvertisementsAdapter = new ListCreateAdvertisementsAdapter(mExistingAdsArrayList, mAdsArrayList, selectedDate, new OnAdvertisementArrayListChangedListener() {
                        @Override
                        public void OnAdvertisementArrayList(ArrayList<Advertisement> advertisementArrayList) {
                            mAdsArrayList = advertisementArrayList;
                            checkIfRecyclerViewShouldBeVisible();
                        }
                    });
                    adRecyclerView.setAdapter(listCreateAdvertisementsAdapter);
                    checkIfRecyclerViewShouldBeVisible();

                }



            }
        }

        if(requestCode == UPDATE_SESSION_LOCATION_REQUEST && resultCode== RESULT_OK) {
            updateLocation(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
        }

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(getResources().getInteger(R.integer.heightOfCreatedSessionImageDenominator), getResources().getInteger(R.integer.heightOfCreatedSessionImageNumerator))
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                /*Cursor cursor = getContentResolver().query(mImageUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);

                //Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), mImageUri, new String[]{MediaStore.Images.Media.DATA});

                if(cursor != null && cursor.moveToFirst()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    //Create ImageCompressTask and execute with Executor.
                    imageCompressTask = new ImageCompressTask(this, path, iImageCompressTaskListener);

                    mExecutorService.execute(imageCompressTask);
                }*/

                imageCompressTask = new ImageCompressTask(this, mImageUri.getPath(), iImageCompressTaskListener);

                mExecutorService.execute(imageCompressTask);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //image compress task callback
    private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
        @Override
        public void onComplete(List<File> compressed) {
            //photo compressed. Yay!

            //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

            File file = compressed.get(0);

            compressedImageUri = Uri.fromFile(file);
            compressedImageHiResUri = Uri.fromFile(compressed.get(1));

            mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mSessionImageButton.setImageURI(compressedImageUri);
        }

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
        }
    };

    /**Method setImage scales the chosen image*/
    private void setImage(String image, ImageView imageView) {
        mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(image).into(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSupportActionBar(null);
        hideKeyboard();
        imm = null;
        if (lv!=null) {
            lv.setAdapter(null);
        }
        adapter = null;
        alertDialogBuilder=null;
        dlg =null;

        if (sessionAdRef!=null) {
            sessionAdRef.removeEventListener(sessionAdListener);
        }

        //clean up!
        mExecutorService.shutdown();

        mExecutorService = null;
        imageCompressTask = null;

    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

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

    public void updateLocation(Double latitude, Double longitude) {
        clickedLatLng = new LatLng(latitude, longitude);
        String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
        mLocation.setText(address);
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public interface OnPriceClickedListener {
        void OnPriceClicked();
    }


    public interface OnSessionUpdatedListener {
        void OnSessionUpdated(Map sessionMap);
    }

    public void alertDialogOk(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
