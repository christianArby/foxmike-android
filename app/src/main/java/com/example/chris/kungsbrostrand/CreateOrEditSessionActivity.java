package com.example.chris.kungsbrostrand;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateOrEditSessionActivity extends AppCompatActivity {

    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private EditText mSessionName;
    private EditText mSessionType;
    private EditText mDate;
    private EditText mTime;
    private EditText mLevel;
    private EditText mMaxParticipants;
    private EditText mDescription;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        /** Set and inflate "create session" layout*/
        View createSession;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout createSessionContainer = findViewById(R.id.create_session_container);
        createSession = inflater.inflate(R.layout.create_session, createSessionContainer,false);

        mDate = createSession.findViewById(R.id.dateET);
        mSessionName = createSession.findViewById(R.id.sessionNameET);
        mSessionType = createSession.findViewById(R.id.sessionTypeET);
        mTime = createSession.findViewById(R.id.timeET);
        mLevel = createSession.findViewById(R.id.levelET);
        mMaxParticipants = createSession.findViewById(R.id.maxParticipantsET);
        mDescription = createSession.findViewById(R.id.descriptionET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(this);
        mCreateSessionBtn = createSession.findViewById(R.id.createSessionBtn);
        mSessionImageButton = createSession.findViewById(R.id.sessionImageBtn);

        createSessionContainer.addView(createSession);

        /** Create Geofire onject in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

        /**The Firebase Database client in our app can keep the data from the database in two places: in memory and/or on disk.
         * This keeps the data on the disk even though listeners are detached*/
        mUserDbRef.keepSynced(true);
        mMarkerDbRef.keepSynced(true);


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
                        setImage(existingSession.getImageUri(),mSessionImageButton);
                        mSessionName.setText(existingSession.getSessionName());
                        mSessionType.setText(existingSession.getSessionType());
                        // Date
                        myCalendar.set(Calendar.YEAR, existingSession.getSessionDate().year);
                        myCalendar.set(Calendar.MONTH, existingSession.getSessionDate().month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, existingSession.getSessionDate().day);
                        updateLabel();
                        // Time
                        mTime.setText( existingSession.getSessionDate().hour + ":" + existingSession.getSessionDate().minute);
                        mLevel.setText(existingSession.getLevel());
                        mMaxParticipants.setText(existingSession.getMaxParticipants());
                        mDescription.setText(existingSession.getDescription());
                        mCreateSessionBtn.setText(R.string.update_session);
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
                session.setSessionDate(mSessionDate);
                session.setLevel(mLevel.getText().toString());
                session.setMaxParticipants(mMaxParticipants.getText().toString());
                session.setDescription(mDescription.getEditableText().toString());
                session.setCountParticipants(0);
                session.setLongitude(clickedLatLng.longitude);
                session.setLatitude(clickedLatLng.latitude);
                session.setHost(currentFirebaseUser.getUid());

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

        mLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("Choose level", R.array.level_array,mLevel);
            }
        });

        mMaxParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("Choose nr of participants", R.array.max_participants_array,mMaxParticipants);
            }
        });
    }

    /**Send session object to database */
    private void sendSession(final Session session, int sessionExist) {

        /**If session exists get the existing session id */
        if (sessionExist==1) {
            mSessionId = existingSessionID;
        }
        /**If session not exists create a new random session key*/
        else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        /**If imageUri exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/
        if(mImageUri != null){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                     * to the main activity*/
                    session.setImageUri(downloadUri);

                    if (session.getSessionDate() != null){
                        mMarkerDbRef.child(mSessionId).setValue(session);
                        geoFire.setLocation(mSessionId, new GeoLocation(session.getLatitude(), session.getLongitude()));
                        mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                        Intent setupIntent = new Intent(CreateOrEditSessionActivity.this,MainPlayerActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

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
                session.setImageUri(existingSession.getImageUri());
                mProgress.dismiss();

                if (session.getSessionDate() != null){
                    mMarkerDbRef.child(mSessionId).setValue(session);
                    geoFire.setLocation(mSessionId, new GeoLocation(session.getLatitude(), session.getLongitude()));
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                    Intent mainIntent = new Intent(CreateOrEditSessionActivity.this,MainPlayerActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
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
}