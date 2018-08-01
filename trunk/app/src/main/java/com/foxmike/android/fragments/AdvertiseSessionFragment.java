package com.foxmike.android.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnStudioChangedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.Studio;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AdvertiseSessionFragment extends Fragment {

    private DatabaseReference rootDbRef  = FirebaseDatabase.getInstance().getReference();

    private View view;
    private String stripeAccountResult;
    private String accountCountry;
    private String accountCurrency;
    private ProgressBar progressBar;
    private ConstraintLayout noAccountContainer;
    private LinearLayout advInputContainer;
    private EditText priceET;
    private TextInputEditText dateET;
    private TextInputLayout dateTIL;
    private TextInputEditText timeET;
    private TextInputLayout timeTIL;
    private Calendar myCalendar = Calendar.getInstance();
    private Button advertiseBtn;
    private Button addPayoutMethod;

    private boolean payoutMethodChecked;
    private boolean payoutMethodAndViewUsed;

    private Studio studio;
    private String studioId;
    private String stripeAccountId;

    private OnStudioChangedListener onStudioChangedListener;

    public static AdvertiseSessionFragment newInstance(String studioId, Studio studio) {

        Bundle args = new Bundle();
        args.putSerializable("studio", studio);
        args.putString("studioId", studioId);
        AdvertiseSessionFragment fragment = new AdvertiseSessionFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public AdvertiseSessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments()!=null) {
            studio = (Studio) getArguments().getSerializable("studio");
            studioId = getArguments().getString("studioId");
        }

        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue()==null) {
                    stripeAccountResult = "payouts_disabled";
                    payoutMethodChecked = true;
                    onAsyncTaskFinished();
                } else {

                    stripeAccountId = dataSnapshot.getValue().toString();

                    retrieveStripeAccount(dataSnapshot.getValue().toString()).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                            // If not succesful, show error and return from function, will trigger if account ID does not exist
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                // [START_EXCLUDE]
                                Log.w(TAG, "retrieve:onFailure", e);
                                stripeAccountResult = "An error occurred." + e.getMessage();
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
                                    stripeAccountResult = "payouts_enabled";
                                } else {
                                    stripeAccountResult = "payouts_disabled";
                                }

                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                stripeAccountResult = error.get("message").toString();
                            }
                            payoutMethodChecked = true;
                            onAsyncTaskFinished();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_advertise_session, container, false);
        priceET = view.findViewById(R.id.priceET);
        dateET = view.findViewById(R.id.dateET);
        dateTIL = view.findViewById(R.id.dateTIL);
        timeET = view.findViewById(R.id.timeET);
        timeTIL = view.findViewById(R.id.timeTIL);
        progressBar = view.findViewById(R.id.progressBar_cyclic);
        noAccountContainer = view.findViewById(R.id.noAccountContainer);
        advInputContainer = view.findViewById(R.id.advInputContainer);
        advertiseBtn = view.findViewById(R.id.advertiseBtn);

        progressBar.setVisibility(View.VISIBLE);
        noAccountContainer.setVisibility(View.GONE);
        advInputContainer.setVisibility(View.GONE);

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
        dateTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        dateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        /** When time edittext is clicked start android TimePickerDialog and once the user has picked a time set the time to the edittext field */
        timeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        timeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime();
            }
        });

        advertiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean infoIsValid = true;

                if (TextUtils.isEmpty(dateET.getText().toString().trim())) {
                    infoIsValid = false;
                    dateTIL.setError("Please specify date of session.");
                }

                if (TextUtils.isEmpty(timeET.getText().toString().trim())) {
                    infoIsValid = false;
                    timeTIL.setError("Please specify time of session.");
                }

                Session session = new Session();

                if (accountCountry.equals("SE")) {
                    String sPrice = priceET.getText().toString().replaceAll("[^0-9]", "");
                    if (sPrice.length()>1) {
                        int intPrice = Integer.parseInt(sPrice);
                        session.setPrice(intPrice);
                        session.setCurrency(accountCurrency);
                    } else {
                        infoIsValid=false;
                    }
                }

                if (infoIsValid) {
                    session.setStudioId(studioId);
                    session.setImageUrl(studio.getImageUrl());
                    session.setLongitude(studio.getLongitude());
                    session.setLatitude(studio.getLatitude());
                    session.setDuration(studio.getDuration());
                    session.setHost(studio.getHostId());
                    session.setMaxParticipants(studio.getMaxParticipants());
                    session.setSessionName(studio.getSessionName());
                    session.setSessionType(studio.getSessionType());
                    session.setWhat(studio.getWhat());
                    session.setWho(studio.getWho());
                    session.setWhereAt(studio.getWhere());
                    session.setStripeAccountId(stripeAccountId);

                    session.setSessionTimestamp(myCalendar.getTimeInMillis());

                    sendSession(session);
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onAsyncTaskFinished();
    }

    private void onAsyncTaskFinished() {

        if (payoutMethodChecked && !payoutMethodAndViewUsed && getView()!=null) {
            payoutMethodAndViewUsed = true;

            if (stripeAccountResult.equals("payouts_enabled")) {
                progressBar.setVisibility(View.GONE);
                advInputContainer.setVisibility(View.VISIBLE);

                if (accountCountry.equals("SE")) {
                    priceET.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            createDialog("Price per person in SEK", R.array.price_array_SE,priceET);
                        }
                    });

                    if (studio.getPrice()!=0) {
                        priceET.setText(studio.getPrice() + " kr");
                    }
                }
            } else if (stripeAccountResult.equals("payouts_disabled")) {
                progressBar.setVisibility(View.GONE);
                noAccountContainer.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                showSnackbar(stripeAccountResult);
            }
        }
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

    /**Send session object to database */
    private void sendSession(final Session sendSession) {
        String sessionId = rootDbRef.child("sessions").push().getKey();
        rootDbRef.child("sessions").child(sessionId).setValue(sendSession);
        rootDbRef.child("studiosTEST").child(studioId).child("sessions").child(sessionId).setValue(sendSession.getSessionTimestamp());
        onStudioChangedListener.OnStudioChanged();
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        final ListView lv = convertView.findViewById(R.id.listView1);
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

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateET.setText(sdf.format(myCalendar.getTime()));
    }

    private void pickTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeET.setText( selectedHour + ":" + selectedMinute);
                myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                myCalendar.set(Calendar.MINUTE, selectedMinute);

            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStudioChangedListener) {
            onStudioChangedListener = (OnStudioChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStudioChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStudioChangedListener = null;
    }

}
