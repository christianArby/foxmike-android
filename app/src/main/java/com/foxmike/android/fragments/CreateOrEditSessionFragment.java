package com.foxmike.android.fragments;
// Checked
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.foxmike.android.R;
import com.foxmike.android.activities.PayoutPreferencesActivity;
import com.foxmike.android.adapters.ListPayoutMethodsAdapter;
import com.foxmike.android.interfaces.OnHostSessionChangedListener;
import com.foxmike.android.interfaces.OnPayoutMethodClickedListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.TextTimestamp;
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
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * This Fragment sets up a UI session form to the user to fill in and then sends the information to the database.
 * It also updates existing sessions.
 */
public class CreateOrEditSessionFragment extends Fragment implements OnSessionClickedListener{

    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private TextView mLocation;
    private TextInputLayout mDateTIL;
    private TextInputLayout mTimeTIL;
    private TextInputLayout mDurationTIL;
    private TextInputLayout mMaxParticipantsTIL;
    private TextInputLayout mSessionTypeTIL;
    private TextInputLayout mPriceTIL;
    private TextInputEditText mSessionName;
    private TextInputEditText mSessionType;
    private TextInputEditText mDate;
    private TextInputEditText mTime;
    private TextInputEditText mMaxParticipants;
    private TextInputEditText mDuration;
    private EditText mWhat;
    private EditText mWho;
    private EditText mWhere;
    private EditText mPrice;
    private CheckedTextView mAdvertised;
    private Button mCreateSessionBtn;
    private final Calendar myCalendar = Calendar.getInstance();
    private ListView lv;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    private int sessionExist;
    private long mSessionTimestamp;
    private ImageButton mSessionImageButton;
    private static final int GALLERY_REQUEST = 1;
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
    private MapsFragment mapsFragment;
    private FragmentManager fragmentManager;
    private FrameLayout mapsFragmentContainer;
    private OnHostSessionChangedListener onHostSessionChangedListener;
    private OnEditLocationListener onEditLocationListener;
    static CreateOrEditSessionFragment fragment;
    private ProgressBar progressBar;
    private View view;
    private String accountCountry;
    private boolean payoutsEnabled;
    private boolean infoIsValid;
    private Session mUpdatedSession;
    private String accountCurrency;

    public CreateOrEditSessionFragment() {
        // Required empty public constructor
    }

    public static CreateOrEditSessionFragment newInstance() {
        fragment = new CreateOrEditSessionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            existingSessionID = bundle.getString("sessionID");
            clickedLatLng = bundle.getParcelable("LatLng");
            existingSession = (Session) bundle.getSerializable("session");
;        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_or_edit_session, container, false);

        /* Set and inflate "create session" layout*/
        View createSession;
        LinearLayout createSessionContainer = view.findViewById(R.id.create_session_container);
        createSession = inflater.inflate(R.layout.create_or_edit_session, createSessionContainer,false);
        // Setup views
        mLocation = createSession.findViewById(R.id.locationTV);
        mDate = createSession.findViewById(R.id.dateET);
        mSessionName = createSession.findViewById(R.id.sessionNameET);
        mSessionType = createSession.findViewById(R.id.sessionTypeET);
        mSessionTypeTIL = createSession.findViewById(R.id.sessionTypeTIL);
        mTime = createSession.findViewById(R.id.timeET);
        mMaxParticipants = createSession.findViewById(R.id.maxParticipantsET);
        mDuration = createSession.findViewById(R.id.durationET);
        mDateTIL = createSession.findViewById(R.id.dateTIL);
        mTimeTIL = createSession.findViewById(R.id.timeTIL);
        mDurationTIL = createSession.findViewById(R.id.durationTIL);
        mMaxParticipantsTIL = createSession.findViewById(R.id.maxParticipantTIL);
        mWhat = createSession.findViewById(R.id.whatET);
        mWho = createSession.findViewById(R.id.whoET);
        mWhere = createSession.findViewById(R.id.whereET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(getActivity());
        mCreateSessionBtn = createSession.findViewById(R.id.createSessionBtn);
        mPrice = createSession.findViewById(R.id.priceET);
        mSessionImageButton = createSession.findViewById(R.id.sessionImageBtn);
        mAdvertised = createSession.findViewById(R.id.advertised);
        mapsFragmentContainer = view.findViewById(R.id.container_maps_fragment);
        progressBar = createSession.findViewById(R.id.progressBar_cyclic);
        mPriceTIL = createSession.findViewById(R.id.priceTIL);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Set default state of advertised checkedTextview
        mAdvertised.setChecked(true);
        mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_black_24dp);

        progressBar.setVisibility(View.VISIBLE);
        mPriceTIL.setVisibility(View.GONE);
        mCreateSessionBtn.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        currentUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        // Set inital state of map fragment container
        mapsFragmentContainer.setVisibility(View.GONE);

        // Setup advertised toggle button
        mAdvertised.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdvertised.isChecked()) {
                    mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_outline_blank_black_24dp);
                    mAdvertised.setChecked(false);
                } else {
                    mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_black_24dp);
                    mAdvertised.setChecked(true);
                }
            }
        });

        // Add view to create session container
        createSessionContainer.addView(createSession);

        /* Create Geofire object in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

        /*The Firebase Database client in our app can keep the data from the database in two places: in memory and/or on disk.
          This keeps the data on the disk even though listeners are detached*/
        mUserDbRef.keepSynced(true);
        mMarkerDbRef.keepSynced(true);

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

        /*When imagebutton is clicked start gallery in phone to let user choose photo/image*/
        mSessionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                fragment.startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        // Setup location icon click listener
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateSessionObjectFromUI(new OnSessionUpdatedListener() {
                    @Override
                    public void OnSessionUpdated(Session updatedSession) {
                        onEditLocationListener.OnEditLocation(mSessionId, updatedSession);
                    }
                });
            }
        });

        /** When item is clicked create a dialog with the specified title and string array */
        mSessionTypeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mSessionType);
            }
        });
        mSessionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mSessionType);
            }
        });


        /** Set listener on DatePickerDialog to retrieve date when user picks date in Android datepicker
         * Update date label with function updateLabel() in order to set it to correct format */
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();

            }
        };
        /**If date field is clicked start Android datepicker and retrive data */
        mDateTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        /** When time edittext is clicked start android TimePickerDialog and once the user has picked a time set the time to the edittext field */
        mTimeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        mDurationTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });
        mDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });

        mMaxParticipantsTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);
            }
        });
        mMaxParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);
            }
        });

        // FILL VIEW with the session in bundle or with the session with the sessionID
        sessionExist=0;
        if (existingSessionID != null | existingSession!=null) {
            /**If this activity was started from clicking on an edit session or returning from mapsfragment the previous activity should have sent a bundle with the session key or session object, if so
             * extract the key and fill in the existing values in the view (Edit view). Set the text of the button to "Update session"*/
            sessionExist=1;
            if (existingSession==null) {
                final DatabaseReference sessionIDref = mMarkerDbRef.child(existingSessionID);
                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        existingSession = dataSnapshot.getValue(Session.class);
                        fillUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                fillUI();
            }


        } /* If no bundle or sessionID exists, the method takes for granted that the activity was started by clicking on the map and a bundle with the LatLng object should exist,
          if so extract the LatLng and set the image to the default image (Create view)*/
        else {

            String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
            mLocation.setText(address);
            setPrice(0);
            mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER);
        }

        /**When button is clicked set the values in the edittext fields to a session object */
        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionExist==1) {
                    mProgress.setMessage(getString(R.string.updating_session));
                } else {
                    mProgress.setMessage(getString(R.string.creating_session));
                }

                mProgress.show();
                updateSessionObjectFromUI(new OnSessionUpdatedListener() {
                    @Override
                    public void OnSessionUpdated(final Session updatedSession) {
                        mUpdatedSession = updatedSession;

                        if (!payoutsEnabled && updatedSession.isAdvertised()) {

                            LayoutInflater factory = LayoutInflater.from(getContext());
                            final View okDialogView = factory.inflate(R.layout.fragment_dialog, null);
                            final AlertDialog okDialog = new AlertDialog.Builder(getContext()).create();
                            okDialog.setView(okDialogView);
                            TextView tv = okDialogView.findViewById(R.id.textTV);
                            tv.setText("You have no active payout method, please add payout method under your account settings. The session will be saved as not advertised.");
                            okDialogView.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //your business logic
                                    updatedSession.setAdvertised(false);
                                    sendSession(updatedSession);
                                    okDialog.dismiss();
                                }
                            });

                            okDialog.show();

                        } else {
                            if (infoIsValid) {sendSession(updatedSession);} else {
                                Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
            }
        });

        return view;
    }

    private void fillUI() {
        clickedLatLng = new LatLng(existingSession.getLatitude(), existingSession.getLongitude());
        String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
        mLocation.setText(address);
        setImage(existingSession.getImageUrl(),mSessionImageButton);
        mSessionName.setText(existingSession.getSessionName());
        mSessionType.setText(existingSession.getSessionType());
        // Date
        myCalendar.setTimeInMillis(existingSession.getSessionTimestamp());
        updateLabel();
        // Time
        TextTimestamp textTimestamp = new TextTimestamp(existingSession.getSessionTimestamp());
        mTime.setText(textTimestamp.textTime());
        mMaxParticipants.setText(existingSession.getMaxParticipants());
        mDuration.setText(existingSession.getDuration());
        mWhat.setText(existingSession.getWhat());
        mWho.setText(existingSession.getWho());
        mWhere.setText(existingSession.getWhere());

        mCreateSessionBtn.setText(R.string.update_session);
        mAdvertised.setChecked(existingSession.isAdvertised());
        if (existingSession.isAdvertised()) {
            mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_black_24dp);
            mAdvertised.setChecked(true);
        } else {
            mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_outline_blank_black_24dp);
            mAdvertised.setChecked(false);
        }

        setPrice(existingSession.getPrice());
    }

    private void setPrice(final int price) {
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                retrieveStripeAccount(dataSnapshot.getValue().toString()).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                        // If not succesful, show error and return from function, will trigger if account ID does not exist

                        progressBar.setVisibility(View.GONE);
                        mPriceTIL.setVisibility(View.VISIBLE);
                        mCreateSessionBtn.setVisibility(View.VISIBLE);

                        if (!task.isSuccessful()) {
                            Exception e = task.getException();

                            // [START_EXCLUDE]
                            Log.w(TAG, "retrieve:onFailure", e);
                            showSnackbar("An error occurred." + e.getMessage());
                            return;
                            // [END_EXCLUDE]
                        }
                        // If successful, extract
                        HashMap<String, Object> result = task.getResult();

                        if (result.get("resultType").toString().equals("account")) {

                            HashMap<String, Object> account = (HashMap<String, Object>) result.get("account");
                            accountCountry = account.get("country").toString();
                            accountCurrency = account.get("default_currency").toString();
                            if (account.get("payouts_enabled").toString().equals("true")) {
                                payoutsEnabled = true;
                            }

                            if (accountCountry.equals("SE")) {
                                mPrice.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        createDialog("Price per person in SEK", R.array.price_array_SE,mPrice);
                                    }
                                });

                                if (price!=0) {
                                    mPrice.setText(price + " kr");
                                }
                            }

                        } else {
                            HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                            showSnackbar(error.get("message").toString());
                        }
                        // [END_EXCLUDE]
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    public void updateSessionObjectFromUI(final OnSessionUpdatedListener onSessionUpdatedListener) {

        infoIsValid = true;
        final Session session = new Session();

        /**If session exists get the existing session id */
        if (sessionExist==1) {
            mSessionId = existingSessionID;
            session.setPosts(existingSession.getPosts());
        }
        /**If session not exists create a new random session key*/
        else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        mSessionTimestamp = myCalendar.getTimeInMillis();
        session.setSessionName(mSessionName.getText().toString());
        session.setSessionType(mSessionType.getText().toString());
        session.setWhat(mWhat.getText().toString());
        session.setWho(mWho.getText().toString());
        session.setWhere(mWhere.getText().toString());
        session.setSessionTimestamp(mSessionTimestamp);
        session.setMaxParticipants(mMaxParticipants.getText().toString());
        session.setDuration(mDuration.getText().toString());
        session.setLongitude(clickedLatLng.longitude);
        session.setLatitude(clickedLatLng.latitude);
        session.setHost(currentFirebaseUser.getUid());
        session.setAdvertised(mAdvertised.isChecked());

        if (TextUtils.isEmpty(session.getSessionName())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getSessionType())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getMaxParticipants())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getDuration())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getWhat())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getWho())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(session.getWhere())) {
            infoIsValid = false;
        }

        if (accountCountry.equals("SE")) {
            String sPrice = mPrice.getText().toString().replaceAll("[^0-9]", "");
            if (sPrice.length()>1) {
                int intPrice = Integer.parseInt(sPrice);
                session.setPrice(intPrice);
                session.setCurrency(accountCurrency);
            } else {
                infoIsValid=false;
            }
        }

        /**If imageUrl exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/
        if(mImageUri != null && infoIsValid){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                     * to the main activity*/
                    session.setImageUrl(downloadUri);

                    if (infoIsValid){
                        onSessionUpdatedListener.OnSessionUpdated(session);
                    }   else    {
                        Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                    }
                    mProgress.dismiss();
                }
            });
        }
        /**If imageUri does not exists it means that the user has NOT selected a photo from the gallery, check if the session is an existing session*/
        else {
            /**If the session is an existing session set the created session object image uri to the existing image uri and send the updated object to the realtime database
             * and send the user back to the main activity*/
            if (sessionExist==1) {
                session.setImageUrl(existingSession.getImageUrl());
                mProgress.dismiss();

                if (infoIsValid){
                    onSessionUpdatedListener.OnSessionUpdated(session);
                }   else    {
                    Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                }

            }
            /**If the session is NOT an existing session tell the user that a photo must be chosen*/
            else {
                mProgress.dismiss();
                Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
            }
        }
    }

    /**Send session object to database */
    private void sendSession(final Session sendSession) {

        mMarkerDbRef.child(mSessionId).setValue(sendSession);
        geoFire.setLocation(mSessionId, new GeoLocation(sendSession.getLatitude(), sendSession.getLongitude()));
        mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);
        onHostSessionChangedListener.OnHostSessionChanged();
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
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    private void pickTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                mTime.setText( selectedHour + ":" + selectedMinute);
                myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                myCalendar.set(Calendar.MINUTE, selectedMinute);

            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();
    }

    // If location has been changed through opening MapsFragment catch the clickedLatLng from OnSessionClickedListener
    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {
        clickedLatLng = new LatLng(sessionLatitude, sessionLongitude);
        String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
        mLocation.setText(address);
        FragmentTransaction transaction2 = fragmentManager.beginTransaction();
        transaction2.remove(fragmentManager.findFragmentByTag("mapsFragmentChange"));
        transaction2.commit();
        mapsFragmentContainer.setVisibility(View.GONE);
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDate.setText(sdf.format(myCalendar.getTime()));
    }

    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        lv.getDivider().setAlpha(0);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,values);
        lv.setAdapter(adapter);
        final AlertDialog dlg = alertDialogBuilder.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) lv.getItemAtPosition(position);
                mEditText.setText(itemValue);
                dlg.hide();
            }
        });
    }
    /** When user has selected an image from the gallery get that imageURI and save it in mImageUri and set the image to the imagebutton  */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(getResources().getInteger(R.integer.heightOfSessionImageDenominator), getResources().getInteger(R.integer.heightOfSessionImageNumerator))
                    .start(fragment.getContext(), fragment);
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
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size()!=0) {
                /*String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();*/
                String knownName = addresses.get(0).getFeatureName();
                String street = addresses.get(0).getThoroughfare();// Only if available else return NULL

                if (street != null) {

                    if (!street.equals(knownName)) {
                        returnAddress = street + " " + knownName;
                    } else {
                        returnAddress = street;
                    }
                } else {
                    returnAddress = "Unknown area";
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHostSessionChangedListener) {
            onHostSessionChangedListener = (OnHostSessionChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHostSessionChangedListener");
        }
        if (context instanceof OnEditLocationListener) {
            onEditLocationListener = (OnEditLocationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEditLocationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onHostSessionChangedListener = null;
        onEditLocationListener = null;
    }

    public interface OnEditLocationListener {

        void OnEditLocation(String sessionID, Session session);
     }

     public interface OnSessionUpdatedListener {
        void OnSessionUpdated(Session updatedSession);
     }
}