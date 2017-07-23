package com.example.chris.kungsbrostrand;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    SessionDate mSessionDate;


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

        mCreateSessionBtn =(Button) findViewById(R.id.createSessionBtn);

        mCreateSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = new Session();
                LatLng clickedLatLng = getIntent().getExtras().getParcelable("LatLng");

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

                if (session.sessionDate != null){
                    String mSessionId = mMarkerDbRef.push().getKey();
                    mMarkerDbRef.child(mSessionId).setValue(session);
                    //DatabaseReference userIDref = mUserDbRef.child(currentFirebaseUser.getUid()).child("hostingSessions");
                    mUserDbRef.child(currentFirebaseUser.getUid()).child("sessionsHosting").child(mSessionId).setValue(true);
                    finish();
                }   else    {
                    Toast.makeText(getApplicationContext(),"Type in neccesary information",Toast.LENGTH_LONG).show();
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
}