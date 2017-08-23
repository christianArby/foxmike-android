package com.example.chris.kungsbrostrand;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrainingSessionActivity extends AppCompatActivity {

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    EditText mSessionName;
    EditText mSessionType;
    EditText mDate;
    EditText mTime;
    EditText mLevel;
    EditText mMaxParticipants;
    private Button mCreateSessionBtn;
    Calendar myCalendar = Calendar.getInstance();
    ListView lv;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    int sessionExist;


    SessionDate mSessionDate;
    ImageButton mSessionImageButton;

    private static final int GALLERY_REQUEST = 1;

    private Uri mImageUri = null;

    private ProgressDialog mProgress;

    private LatLng clickedLatLng;
    Bundle sessionIdBundle;
    String existingSessionID;
    String mSessionId;
    Session existingSession;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);
        mDate = (EditText)findViewById(R.id.dateET);
        mSessionName = (EditText) findViewById(R.id.sessionNameET);
        mSessionType = (EditText) findViewById(R.id.sessionTypeET);
        mTime = (EditText) findViewById(R.id.timeET);
        mLevel = (EditText) findViewById(R.id.levelET);
        mMaxParticipants = (EditText) findViewById(R.id.maxParticipantsET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(this);
        mCreateSessionBtn =(Button) findViewById(R.id.createSessionBtn);
        mSessionImageButton = (ImageButton) findViewById(R.id.sessionImageBtn);


        sessionIdBundle = getIntent().getExtras();
        existingSessionID = "new";
        sessionExist=0;
        if (sessionIdBundle != null) {
            existingSessionID = sessionIdBundle.getString("key");

            if (existingSessionID != null){

                sessionExist=1;

                final DatabaseReference sessionIDref = mMarkerDbRef.child(existingSessionID);
                sessionIDref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        existingSession = dataSnapshot.getValue(Session.class);

                        clickedLatLng = new LatLng(existingSession.latitude, existingSession.longitude);

                        setImage(existingSession.imageUri,mSessionImageButton);



                        mSessionName.setText(existingSession.sessionName);
                        mSessionType.setText(existingSession.sessionType);

                        // Date
                        myCalendar.set(Calendar.YEAR, existingSession.sessionDate.year);
                        myCalendar.set(Calendar.MONTH, existingSession.sessionDate.month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, existingSession.sessionDate.day);
                        updateLabel();

                        // Time
                        mTime.setText( existingSession.sessionDate.hour + ":" + existingSession.sessionDate.minute);

                        mLevel.setText(existingSession.level);
                        mMaxParticipants.setText(existingSession.maxParticipants);

                        mCreateSessionBtn.setText("Update session");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            else {

                clickedLatLng = getIntent().getExtras().getParcelable("LatLng");
            }
        }




        mSessionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });


        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Session session = new Session();

                mSessionDate = new SessionDate(myCalendar);
                session.sessionName = mSessionName.getText().toString();
                session.sessionType = mSessionType.getText().toString();
                session.sessionDate = mSessionDate;
                session.level = mLevel.getText().toString();
                session.maxParticipants = mMaxParticipants.getText().toString();
                session.countParticipants = 0;
                session.longitude = clickedLatLng.longitude;
                session.latitude = clickedLatLng.latitude;
                session.host = currentFirebaseUser.getUid();

                if (sessionExist == 1){

                    mProgress.setMessage("Updating session ...");
                    mProgress.show();
                    sendSession(session, sessionExist);

                }

                else {

                    mProgress.setMessage("Creating session ...");
                    mProgress.show();
                    sendSession(session, sessionExist);

                }
            }
        });




        //Date picker
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

        mDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(TrainingSessionActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Time picker
        mTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TrainingSessionActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        // Sessiontype
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

    private void sendSession(final Session session, int sessionExist) {

        if (sessionExist==1) {

            mSessionId = existingSessionID;
        }

        else {
            mSessionId = mMarkerDbRef.push().getKey();
        }

        if(mImageUri != null){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    session.imageUri = downloadUri;

                    mProgress.dismiss();

                    if (session.sessionDate != null){
                        mMarkerDbRef.child(mSessionId).setValue(session);
                        mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                        Intent mainIntent = new Intent(TrainingSessionActivity.this, MapsActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    }   else    {
                        Toast.makeText(getApplicationContext(),"Type in neccesary information",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {

            if (sessionExist==1) {

                session.imageUri = existingSession.imageUri;

                mProgress.dismiss();

                if (session.sessionDate != null){
                    mMarkerDbRef.child(mSessionId).setValue(session);
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);

                    Intent mainIntent = new Intent(TrainingSessionActivity.this, MapsActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }   else    {
                    Toast.makeText(getApplicationContext(),"Type in neccesary information",Toast.LENGTH_LONG).show();
                }

            }

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

    public void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(TrainingSessionActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_listview, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(title);
        lv =(ListView) convertView.findViewById(R.id.listView1);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(TrainingSessionActivity.this,android.R.layout.simple_list_item_1,values);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mSessionImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void setImage(String image, ImageView imageView) {


        //ImageView profileImage = (ImageView) profile.findViewById(R.id.profileIV);
        Picasso.with(this).load(image).into(imageView);


    }
}