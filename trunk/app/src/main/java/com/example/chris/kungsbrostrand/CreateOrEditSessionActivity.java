package com.example.chris.kungsbrostrand;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import android.view.LayoutInflater;
import android.view.View;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateOrEditSessionActivity extends AppCompatActivity implements OnSessionClickedListener{

    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private TextView mLocation;
    private EditText mSessionName;
    private EditText mSessionType;
    private EditText mDate;
    private EditText mTime;
    private EditText mMaxParticipants;
    private EditText mDuration;
    private EditText mWhat;
    private EditText mWho;
    private EditText mWhere;
    private CheckedTextView mAdvertised;
    private Button mCreateSessionBtn;
    private final Calendar myCalendar = Calendar.getInstance();
    private ListView lv;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    private int sessionExist;
    private SessionDate mSessionDate;
    private ImageButton mSessionImageButton;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;
    private LatLng clickedLatLng;
    private String existingSessionID;
    private String mSessionId;
    private Session existingSession;
    private GeoFire geoFire;
    private DatabaseReference currentUserDbRef;
    private FirebaseAuth mAuth;

    private MapsFragment mapsFragment;
    private FragmentManager fragmentManager;
    private FrameLayout mapsFragmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_session);

        mAuth = FirebaseAuth.getInstance();
        currentUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        /** Set and inflate "create session" layout*/
        View createSession;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout createSessionContainer = findViewById(R.id.create_session_container);
        createSession = inflater.inflate(R.layout.create_or_edit_session, createSessionContainer,false);

        mLocation = createSession.findViewById(R.id.locationTV);
        mDate = createSession.findViewById(R.id.dateET);
        mSessionName = createSession.findViewById(R.id.sessionNameET);
        mSessionType = createSession.findViewById(R.id.sessionTypeET);
        mTime = createSession.findViewById(R.id.timeET);
        mMaxParticipants = createSession.findViewById(R.id.maxParticipantsET);
        mDuration = createSession.findViewById(R.id.durationET);
        mWhat = createSession.findViewById(R.id.whatET);
        mWho = createSession.findViewById(R.id.whoET);
        mWhere = createSession.findViewById(R.id.whereET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(this);
        mCreateSessionBtn = createSession.findViewById(R.id.createSessionBtn);
        mSessionImageButton = createSession.findViewById(R.id.sessionImageBtn);
        mAdvertised = createSession.findViewById(R.id.advertised);
        mapsFragmentContainer = findViewById(R.id.container_maps_fragment);

        mapsFragmentContainer.setVisibility(View.GONE);

        mAdvertised.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdvertised.isChecked()) {
                    mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_outline_blank_black_24dp);
                    mAdvertised.setChecked(false);
                }
                if (!mAdvertised.isChecked()) {
                    mAdvertised.setCheckMarkDrawable(R.mipmap.ic_check_box_black_24dp);
                    mAdvertised.setChecked(true);
                }
            }
        });

        createSessionContainer.addView(createSession);

        /** Create Geofire object in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

        /**The Firebase Database client in our app can keep the data from the database in two places: in memory and/or on disk.
         * This keeps the data on the disk even though listeners are detached*/
        mUserDbRef.keepSynced(true);
        mMarkerDbRef.keepSynced(true);

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsFragmentContainer.setVisibility(View.VISIBLE);
                fragmentManager = getSupportFragmentManager();
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
                bundle.putInt("CHANGELOCATION", 1);

                mapsFragment = MapsFragment.newInstance();
                mapsFragment.setArguments(bundle);
                if (null == fragmentManager.findFragmentByTag("mapsFragmentChange")) {
                    transaction.add(R.id.container_maps_fragment, mapsFragment,"mapsFragmentChange");
                    transaction.commit();
                }
            }
        });


        Bundle sessionIdBundle = getIntent().getExtras();
        existingSessionID = "new";
        sessionExist=0;
        if (sessionIdBundle != null) {
            existingSessionID = sessionIdBundle.getString("key");

            /**If this activity was started from clicking on an existing session the previous activity should have sent a bundle with the session key, if so
             * extract the key and fill in the existing values in the view (Edit view). Set the text of the button to "Update session"*/
            if (existingSessionID != null){

                sessionExist=1;

                final DatabaseReference sessionIDref = mMarkerDbRef.child(existingSessionID);
                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        existingSession = dataSnapshot.getValue(Session.class);

                        clickedLatLng = new LatLng(existingSession.getLatitude(), existingSession.getLongitude());
                        String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
                        mLocation.setText(address);
                        setImage(existingSession.getImageUrl(),mSessionImageButton);
                        mSessionName.setText(existingSession.getSessionName());
                        mSessionType.setText(existingSession.getSessionType());
                        // Date
                        myCalendar.set(Calendar.YEAR, existingSession.getSessionDate().year);
                        myCalendar.set(Calendar.MONTH, existingSession.getSessionDate().month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, existingSession.getSessionDate().day);
                        updateLabel();
                        // Time
                        mTime.setText( existingSession.getSessionDate().hour + ":" + existingSession.getSessionDate().minute);
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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            /** If no bundle exists, the method takes for granted that the activity was started by clicking on the map and a bundle with the LatLng object should exist,
             * if so extract the LatLng and set the image to the default image (Create view)*/
            else {

                clickedLatLng = getIntent().getExtras().getParcelable("LatLng");
                String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
                mLocation.setText(address);
                mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER);
            }
        }

        /**When imagebutton is clicked start gallery in phone to let user choose photo/image*/
        mSessionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        /**When button is clicked set the values in the edittext fields to a session object */
        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Session session = new Session();

                mSessionDate = new SessionDate(myCalendar);
                session.setSessionName(mSessionName.getText().toString());
                session.setSessionType(mSessionType.getText().toString());
                session.setWhat(mWhat.getText().toString());
                session.setWho(mWho.getText().toString());
                session.setWhere(mWhere.getText().toString());
                session.setSessionDate(mSessionDate);
                session.setMaxParticipants(mMaxParticipants.getText().toString());
                session.setDuration(mDuration.getText().toString());
                session.setLongitude(clickedLatLng.longitude);
                session.setLatitude(clickedLatLng.latitude);
                session.setHost(currentFirebaseUser.getUid());
                session.setAdvertised(mAdvertised.isChecked());

                /**If session exists (checked on create) send session to database with method sendSession, display progress*/
                if (sessionExist == 1){
                    mProgress.setMessage("Updating session ...");
                    mProgress.show();
                    sendSession(session, sessionExist);
                }

                /**If session not exists (checked on create) send session to database with method sendSession, display progress*/
                else {
                    mProgress.setMessage("Creating session ...");
                    mProgress.show();
                    sendSession(session, sessionExist);
                }
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
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CreateOrEditSessionActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        /** When time edittext is clicked start android TimePickerDialog and once the user has picked a time set the time to the edittext field */
        mTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateOrEditSessionActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTime.setText( selectedHour + ":" + selectedMinute);
                        myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        myCalendar.set(Calendar.MINUTE, selectedMinute);

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        /** When item is clicked create a dialog with the specified title and string array */
        mSessionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("Choose session type", R.array.sessionType_array,mSessionType);
            }
        });

        mDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("Hur länge kommer passet pågå?", R.array.duration_array,mDuration);
            }
        });

        mMaxParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("Choose nr of participants", R.array.max_participants_array,mMaxParticipants);
            }
        });
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

    /**Send session object to database */
    private void sendSession(final Session session, int sessionExist) {

        /**If session exists get the existing session id */
        if (sessionExist==1) {
            mSessionId = existingSessionID;
            session.setPosts(existingSession.getPosts());
        }
        /**If session not exists create a new random session key*/
        else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        /**If imageUrl exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/
        if(mImageUri != null){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                     * to the main activity*/
                    session.setImageUrl(downloadUri);

                    if (session.getSessionDate() != null){
                        mMarkerDbRef.child(mSessionId).setValue(session);
                        geoFire.setLocation(mSessionId, new GeoLocation(session.getLatitude(), session.getLongitude()));
                        mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                        goToMain();

                    }   else    {
                        Toast.makeText(getApplicationContext(),"Type in neccesary information",Toast.LENGTH_LONG).show();
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

                if (session.getSessionDate() != null){
                    mMarkerDbRef.child(mSessionId).setValue(session);
                    geoFire.setLocation(mSessionId, new GeoLocation(session.getLatitude(), session.getLongitude()));
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                    goToMain();
                }   else    {
                    Toast.makeText(getApplicationContext(),"Type in neccesary information",Toast.LENGTH_LONG).show();
                }

            }
            /**If the session is NOT an existing session tell the user that a photo must be chosen*/
            else {
                mProgress.dismiss();
                Toast.makeText(getApplicationContext(),"Choose photo",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToMain() {
        Intent mainIntent = new Intent(CreateOrEditSessionActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDate.setText(sdf.format(myCalendar.getTime()));
    }


    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateOrEditSessionActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateOrEditSessionActivity.this,android.R.layout.simple_list_item_1,values);
        lv.setAdapter(adapter);
        final AlertDialog dlg = alertDialog.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) lv.getItemAtPosition(position);

                // Show Alert
                mEditText.setText(itemValue);
                Toast.makeText(
                        getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : "
                                + itemValue, Toast.LENGTH_LONG).show();
                dlg.dismiss();
            }
        });
    }

    /** When user has selected an image from the gallery get that imageURI and save it in mImageUri and set the image to the imagebutton  */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(2,1)
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
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
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
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {

            returnAddress = "failed";
        }

        return returnAddress;

    }

}