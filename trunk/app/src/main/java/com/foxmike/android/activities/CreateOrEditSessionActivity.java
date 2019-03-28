package com.foxmike.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.Price;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateOrEditSessionActivity extends AppCompatActivity {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private TextView mLocation;
    private TextInputLayout mSessionNameTIL;
    private TextInputLayout mLocationTIL;
    private TextInputLayout mSessionTypeTIL;
    private TextInputLayout mDateTIL;
    private TextInputLayout mDurationTIL;
    private TextInputLayout mMaxParticipantsTIL;
    private TextInputLayout mWhatTIL;
    private TextInputLayout mWhoTIL;
    private TextInputLayout mWhereTIL;
    private TextInputEditText mSessionName;
    private TextInputEditText mSessionType;
    private TextInputEditText mMaxParticipants;
    private TextInputEditText mDuration;
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
    private static final int UPDATE_SESSION_LOCATION_REQUEST = 3;
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
    private ProgressBar progressBar;
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
    private HashMap<Long, Integer> advertisementTimestampsAndPrices = new HashMap<>();
    private HashMap<String, Long> advertisements = new HashMap<>();
    private long mLastClickTime = 0;
    private LinearLayout allExceptCalendar;
    private boolean addAdvertisements;
    private int currentPrice = 0;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_session);

        existingSessionID = getIntent().getStringExtra("sessionID");
        type = getIntent().getStringExtra("type");
        clickedLatLng = getIntent().getParcelableExtra("LatLng");
        existingSession = getIntent().getParcelableExtra("session");
        addAdvertisements = getIntent().getBooleanExtra("addAdvertisements", false);

        /* Create Geofire object in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

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
        mMaxParticipants = createSession.findViewById(R.id.maxParticipantsET);
        mDuration = createSession.findViewById(R.id.durationET);
        mDateTIL = createSession.findViewById(R.id.dateTIL);
        mDurationTIL = createSession.findViewById(R.id.durationTIL);
        mWhatTIL = createSession.findViewById(R.id.whatTIL);
        mWhoTIL = createSession.findViewById(R.id.whoTIL);
        mWhereTIL = createSession.findViewById(R.id.whereTIL);
        mMaxParticipantsTIL = createSession.findViewById(R.id.maxParticipantTIL);
        mWhat = createSession.findViewById(R.id.whatET);
        mWho = createSession.findViewById(R.id.whoET);
        mWhere = createSession.findViewById(R.id.whereET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(this);
        mCreateSessionBtn = createSession.findViewById(R.id.createSessionBtn);
        mSessionImageButton = createSession.findViewById(R.id.sessionImageBtn);
        mapsFragmentContainer = findViewById(R.id.container_maps_fragment);
        progressBar = createSession.findViewById(R.id.progressBar_cyclic);
        imageErrorText = createSession.findViewById(R.id.imageErrorText);
        compactCalendarView = (CompactCalendarView) createSession.findViewById(R.id.compactcalendar_view);
        calendarHeadingTV = createSession.findViewById(R.id.calendarHeadingTV);
        allExceptCalendar = createSession.findViewById(R.id.allExceptCalendar);
        createOrEditSV = findViewById(R.id.scrollview_create_session);

        Formatter fmt = new Formatter();
        fmt.format("%tB", compactCalendarView.getFirstDayOfCurrentMonth());
        calendarHeadingTV.setText(fmt.toString());
        // Listen to clicks
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                Date currentDate = new Date();

                if (dateClicked.before(currentDate) && !DateUtils.isToday(dateClicked.getTime())) {
                    cannotCreateSessionInPastPopUp();
                    return;
                }

                compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.grayTextColor));
                // set the calendar to the clicked date
                myCalendar.setTime(dateClicked);
                pickTime();
                // Open timepicker in order for user to set time for the event
            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Formatter fmt = new Formatter();
                fmt.format("%tB", compactCalendarView.getFirstDayOfCurrentMonth());
                calendarHeadingTV.setText(fmt.toString());
            }
        });

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        mCreateSessionBtn.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        currentUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        // Add view to create session container
        createSessionContainer.addView(createSession);

        /* Create Geofire object in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

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
                        setupUI();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                setupUI();
            }
        } /* If no bundle or sessionID exists, the method takes for granted that the activity was started by clicking on the map and a bundle with the LatLng object should exist,
          if so extract the LatLng and set the image to the default image (Create view)*/
        else {
            if (clickedLatLng==null) {
                mLocation.setText(getString(R.string.choose_location));
            } else {
                address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
                mLocation.setText(address);
            }

            setPrice(0);
            mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER);
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

        if (existingSession!=null) {

            clickedLatLng = new LatLng(existingSession.getLatitude(), existingSession.getLongitude());
            address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
            mLocation.setText(address);
            setImage(existingSession.getImageUrl(),mSessionImageButton);
            mSessionName.setText(existingSession.getSessionName());
            mSessionType.setText(existingSession.getSessionType());
            // Existing advertisements
            if (existingSession.getAdvertisements()!=null) {
                for (Long advertisementTimestamp: existingSession.getAdvertisements().values()) {
                    if (advertisementTimestamp!=0) {
                        Event sessionTime = new Event(getResources().getColor(R.color.foxmikePrimaryDarkColor), advertisementTimestamp, "existingAd");
                        compactCalendarView.addEvent(sessionTime);
                    }
                }
            }
            mMaxParticipants.setText(existingSession.getMaxParticipants());
            mDuration.setText(existingSession.getDurationInMin() + getString(R.string.minutes_append));
            mWhat.setText(existingSession.getWhat());
            mWho.setText(existingSession.getWho());
            mWhere.setText(existingSession.getWhereAt());
            setPrice(existingSession.getPrice());

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

        mSessionName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                /*FragmentManager fragmentManager = getChildFragmentManager();

                if (fragmentManager.findFragmentByTag(ChooseLocationFragment.TAG)!=null) {
                    FragmentTransaction removeTransaction = fragmentManager.beginTransaction();
                    removeTransaction.remove(fragmentManager.findFragmentByTag(ChooseLocationFragment.TAG)).commit();
                };

                chooseLocationFragment = ChooseLocationFragment.newInstance("updateSession");
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.container_maps_fragment, chooseLocationFragment, ChooseLocationFragment.TAG).addToBackStack(null);
                transaction.commit();*/

                Intent chooseLocatonIntent = new Intent(CreateOrEditSessionActivity.this, ChooseLocationActivity.class);
                startActivityForResult(chooseLocatonIntent, UPDATE_SESSION_LOCATION_REQUEST);

            }
        });

        /** When item is clicked create a dialog where use can choose between different session types */
        mSessionTypeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionTypeTIL.setError(null);

                Locale current = getResources().getConfiguration().locale;
                DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(current.toString());
                sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> sessionTypeArray = new ArrayList<>();
                        for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                            sessionTypeArray.add(sessionTypeSnap.getKey());
                        }
                        createDialogWithArray(getString(R.string.choose_session_type), sessionTypeArray, mSessionType);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        mSessionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSessionTypeTIL.setError(null);

                Locale current = getResources().getConfiguration().locale;
                DatabaseReference sessionTypeArrayLocalReference = FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(current.toString());
                sessionTypeArrayLocalReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> sessionTypeArray = new ArrayList<>();
                        for (DataSnapshot sessionTypeSnap : dataSnapshot.getChildren()) {
                            sessionTypeArray.add(sessionTypeSnap.getKey());
                        }
                        createDialogWithArray(getString(R.string.choose_session_type), sessionTypeArray, mSessionType);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        mDurationTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDurationTIL.setError(null);
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });
        mDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDurationTIL.setError(null);
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });

        mMaxParticipantsTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMaxParticipantsTIL.setError(null);
                changeNumberOfParticipants(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);

            }
        });
        mMaxParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMaxParticipantsTIL.setError(null);
                changeNumberOfParticipants(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);
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

    private void setPrice(final int price) {
        // --------------CHECK IF PAYOUTS ARE ENABLED------
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // ----------- PAYOUTS NOT ENABLED --------------
                    payoutsEnabled = false;
                    showPriceView();
                } else {
                    stripeAccountId = dataSnapshot.getValue().toString();
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
                                showPriceView();
                                return;
                            }
                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("resultType").toString().equals("account")) {

                                HashMap<String, Object> account = (HashMap<String, Object>) result.get("account");
                                accountCountry = account.get("country").toString();
                                accountCurrency = account.get("default_currency").toString();

                                // ----------- PAYOUTS ENABLED --------------
                                if (account.get("payouts_enabled").toString().equals("true")) {
                                    payoutsEnabled = true;
                                    showPriceView();

                                } else {
                                    // ----------- PAYOUTS NOT ENABLED --------------
                                    payoutsEnabled = false;
                                    showPriceView();
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

            }
        });

    }

    private void showPriceView() {
        progressBar.setVisibility(View.GONE);
        mCreateSessionBtn.setVisibility(View.VISIBLE);
    }

    public void updateSessionObjectFromUI(OnSessionUpdatedListener onSessionUpdatedListener) {
        //final Session session = new Session();
        Map sessionMap = new HashMap();

        if (updateSession) {
            mSessionId = existingSessionID;
            if (existingSession.getAdvertisements()!=null) {
                for (String ad : existingSession.getAdvertisements().keySet()) {
                    advertisements.put(ad, existingSession.getAdvertisements().get(ad));
                }
            }
        } else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        sessionMap.put("sessionId", mSessionId);
        sessionMap.put("sessionName", mSessionName.getText().toString());
        sessionMap.put("sessionType", mSessionType.getText().toString());
        sessionMap.put("what",mWhat.getText().toString());
        sessionMap.put("who", mWho.getText().toString());
        sessionMap.put("whereAt", mWhere.getText().toString());
        sessionMap.put("maxParticipants", mMaxParticipants.getText().toString());
        sessionMap.put("address", address);

        String sDur = mDuration.getText().toString().replaceAll("[^0-9]", "");
        if (sDur.length()>1) {
            int intDur = Integer.parseInt(sDur);
            sessionMap.put("durationInMin", intDur);
        } else {
            sessionMap.put("durationInMin", 0);
        }

        sessionMap.put("longitude", clickedLatLng.longitude);
        sessionMap.put("latitude", clickedLatLng.latitude);
        sessionMap.put("host", currentFirebaseUser.getUid());
        sessionMap.put("stripeAccountId", stripeAccountId);

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

        if (TextUtils.isEmpty(mDuration.getText().toString())) {
            mDurationTIL.setError(getString(R.string.please_choose_session_duration));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(mMaxParticipants.getText().toString())) {
            mMaxParticipantsTIL.setError(getString(R.string.please_choose_maximum_nr_of_participants));
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

        if (payoutsEnabled) {
            sessionMap.put("price", currentPrice);
            sessionMap.put("currency", accountCurrency);
        } else {
            currentPrice = 0;
            sessionMap.put("price", 0);
            sessionMap.put("currency", "free");
        }



        /**If imageUrl exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/
        if(mImageUri != null && infoIsValid){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri = uri.toString();
                            /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                             * to the main activity*/
                            sessionMap.put("imageUrl", downloadUri);

                            if (infoIsValid){
                                onSessionUpdatedListener.OnSessionUpdated(sessionMap);
                            }
                            mProgress.dismiss();

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
                imageErrorText.setVisibility(View.VISIBLE);
                mProgress.dismiss();
            }
        }
    }

    /**Send session object to database */
    private void sendSession(Map sendSession) {

        ArrayList<String> writeReferences = new ArrayList<>();
        // Create (or update) session button has been pressed. Create advertisements of the occasions set in the calendar.
        // Loop through the timestamps created by clicking and making events in the calendar
        for (Long advertisementTimestamp: advertisementTimestampsAndPrices.keySet()) {
            // For each timestamp, create an Advertisement object of the class Advertisement, take nost of the data from the current session being created
            String advertisementKey = rootDbRef.child("advertisements").push().getKey();
            Advertisement advertisement = new Advertisement("active",
                    (String) sendSession.get("sessionId"),
                    (String) sendSession.get("host"),
                    (String) sendSession.get("sessionName"),
                    (String) sendSession.get("imageUrl"),
                    advertisementKey,
                    (String) sendSession.get("maxParticipants"),
                    new HashMap<String, Long>(),
                    (int) sendSession.get("durationInMin"),
                    (String) sendSession.get("currency"),
                    advertisementTimestamp,
                    advertisementTimestampsAndPrices.get(advertisementTimestamp)
            );
            // Save the advertisement Id and the ad timestamp in a hashmap to be saved under session
            advertisements.put(advertisementKey, advertisementTimestamp);
            // send the ad to the database
            rootDbRef.child("advertisements").child(advertisementKey).setValue(advertisement).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // save the key and ad timestamp under user/advertisementHosting
                    writeReferences.add(rootDbRef.child("advertisements").child(advertisementKey).toString());
                    rootDbRef.child("advertisementHosts").child(currentFirebaseUser.getUid()).child(advertisementKey).setValue(advertisementTimestamp);
                }
            });
        }
        // Save the hashmap of ad Ids and timestamps under session
        sendSession.put("advertisements", advertisements);
        // Update the session (with 'updateChildren' so not all child nodes are overwritten)
        rootDbRef.child("sessions").child(mSessionId).updateChildren(sendSession);
        // Update user object with sessionsHosting
        rootDbRef.child("sessionHosts").child(currentFirebaseUser.getUid()).child(mSessionId).setValue(true);
        // create geoFire reference
        geoFire.setLocation(mSessionId, new GeoLocation((double)sendSession.get("latitude"), (double)sendSession.get("longitude")));
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

    private void pickTime() {
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                myCalendar.set(Calendar.MINUTE, selectedMinute);

                Date pickedDate = new Date(myCalendar.getTime().getTime());
                Date currentDate = new Date();

                if (pickedDate.before(currentDate)) {
                    cannotCreateSessionInPastPopUp();
                } else {

                    if (payoutsEnabled) {
                        if (accountCountry.equals("SE")) {
                            createPriceDialog(getString(R.string.price_per_person_in_sek), R.array.price_array_SE, new OnPriceClickedListener() {
                                @Override
                                public void OnPriceClicked() {
                                    Event event = new Event(getResources().getColor(R.color.foxmikePrimaryColor),myCalendar.getTimeInMillis());
                                    compactCalendarView.addEvent(event);
                                    compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
                                    if (DateUtils.isToday(myCalendar.getTimeInMillis())) {
                                        compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
                                    }
                                    advertisementTimestampsAndPrices.put(myCalendar.getTimeInMillis(), currentPrice);

                                }
                            });
                        }
                        return;
                    }
                    // ---- Payouts not enabled -----
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrEditSessionActivity.this);
                    builder.setMessage(R.string.create_free_session_question);
                    builder.setPositiveButton(R.string.create_free_session, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Event event = new Event(getResources().getColor(R.color.foxmikePrimaryColor),myCalendar.getTimeInMillis());
                            compactCalendarView.addEvent(event);
                            compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
                            if (DateUtils.isToday(myCalendar.getTimeInMillis())) {
                                compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.foxmikePrimaryColor));
                            }
                            advertisementTimestampsAndPrices.put(myCalendar.getTimeInMillis(), 0);
                            currentPrice = 0;
                        }
                    });
                    builder.setNegativeButton(R.string.add_payout_method_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent paymentPreferencesIntent = new Intent(CreateOrEditSessionActivity.this, PayoutPreferencesActivity.class);
                            startActivityForResult(paymentPreferencesIntent, PAYOUT_METHOD_REQUEST);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();









                }
            }
        }, 12, 0, true);//Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();
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
    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        lv.getDivider().setAlpha(0);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,values);
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
    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createPriceDialog(String title, int string_array,  OnPriceClickedListener onPriceClickedListener) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_radiogroup, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        currencySettingRadioGroup = (RadioGroup) convertView.findViewById(R.id.dialogRadioGroup);
        TextView priceOkButton = convertView.findViewById(R.id.priceOK);
        radioGroupHashMap.clear();
        currencySettingRadioGroup.removeAllViews();
        String[] prices = getResources().getStringArray(string_array);
        int n=1;
        for (String stringPrice: prices ) {
            if (!radioGroupHashMap.containsKey(stringPrice)) {
                RadioButton rb = new RadioButton(this);
                rb.setText(stringPrice);
                rb.setTextAppearance(this, android.R.style.TextAppearance_Material_Subhead);
                rb.setId(n);
                n++;
                currencySettingRadioGroup.addView(rb);
                radioGroupHashMap.put(stringPrice, rb);
            }
        }
        radioGroupHashMap.get(Price.PRICES_STRINGS_SE.get(currentPrice)).setChecked(true);
        dlg = alertDialogBuilder.show();
        currencySettingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                currentPrice = Price.PRICES_INTEGERS_SE.get(prices[i-1]);
            }
        });
        priceOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.dismiss();
                onPriceClickedListener.OnPriceClicked();

            }
        });
    }
    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void changeNumberOfParticipants(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        lv.getDivider().setAlpha(0);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
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

        if(requestCode == UPDATE_SESSION_LOCATION_REQUEST && resultCode== Activity.RESULT_OK) {
            updateLocation(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
        }

        // TODO Change with RxJava instead listening to variable hasPaymentin MainHostAc
        if(requestCode == PAYOUT_METHOD_REQUEST) {
            if (existingSession!=null) {
                setPrice(existingSession.getPrice());
            } else {
                setPrice(0);
            }
        }

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(getResources().getInteger(R.integer.heightOfSessionImageDenominator), getResources().getInteger(R.integer.heightOfSessionImageNumerator))
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mSessionImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /**Method setImage scales the chosen image*/
    private void setImage(String image, ImageView imageView) {
        mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(image).into(imageView);
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
}
