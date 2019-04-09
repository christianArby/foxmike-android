package com.foxmike.android.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.utils.TextTimestamp;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;
import static com.foxmike.android.utils.StaticResources.DURATION_STRINGS;

public class CreateAdvertisementActivity extends AppCompatActivity {

    @BindView(R.id.time) TimePicker timePicker;
    @BindView(R.id.durationTV) TextView durationTV;
    @BindView(R.id.maxParticipantsTV) TextView maxParticipantsTV;
    @BindView(R.id.priceTV) TextView priceTV;
    @BindView(R.id.priceLayout) ConstraintLayout priceLayout;
    @BindView(R.id.durationLayout) ConstraintLayout durationLayout;
    @BindView(R.id.maxParticipantsLayout) ConstraintLayout maxParticipantsLayout;
    @BindView(R.id.priceOverlay) View priceOverlay;
    @BindView(R.id.payoutProgressBar) DotProgressBar dotProgressBar;
    @BindView(R.id.advertisementsRV) RecyclerView advertisementsRV;
    private static final int DURATION_REQUEST = 1000;
    private static final int MAX_PARTICIPANTS_REQUEST = 1001;
    private static final int PRICE_REQUEST = 1002;
    private static final int PAYOUT_METHOD_REQUEST = 1003;
    private TextView dateTV;
    private TextView cancelTV;
    private TextView saveTV;
    private int duration;
    private int maxParticipants;
    private int price;
    private boolean payoutsEnabled = false;
    private String accountCurrency;
    private View mainView;
    private Long date;
    private final Calendar myCalendar = Calendar.getInstance();
    private int hour;
    private int minute;
    private ArrayList<Advertisement> advertisementArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advertisement);
        ButterKnife.bind(this);

        mainView = findViewById(R.id.mainView);

        Toolbar toolbar = (Toolbar)  findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setElevation(0);

        timePicker.setIs24HourView(true);

        View action_bar_view = getLayoutInflater().inflate(R.layout.create_ad_custom_toolbar, null);

        // make sure the whole action bar is filled with the custom view
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(action_bar_view, layoutParams);

        dateTV = findViewById(R.id.dateTV);
        saveTV = findViewById(R.id.save);
        cancelTV = findViewById(R.id.cancel);

        date = getIntent().getLongExtra("date", 0);
        String dateClickedString = TextTimestamp.textSessionDate(date);
        dateTV.setText(dateClickedString);

        myCalendar.setTimeInMillis(date);

        duration = getIntent().getIntExtra("duration", -1);
        maxParticipants = getIntent().getIntExtra("maxParticipants", -1);
        price = getIntent().getIntExtra("price", -1);
        payoutsEnabled = getIntent().getBooleanExtra("payoutsEnabled", false);
        accountCurrency = getIntent().getStringExtra("accountCurrency");
        advertisementArrayList = (ArrayList<Advertisement>) getIntent().getSerializableExtra("advertisementArrayList");

        if (duration>0) {
            durationTV.setText(DURATION_STRINGS.get(duration));
        }

        if (maxParticipants>0) {
            maxParticipantsTV.setText(Integer.toString(maxParticipants));
        }

        if (price>=0) {
            priceTV.setText(PRICES_STRINGS_SE.get(price));
        }

        if (advertisementArrayList == null) {
            advertisementArrayList = new ArrayList<>();
        }

        durationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setDurationIntent = new Intent(CreateAdvertisementActivity.this, SetDurationActivity.class);
                setDurationIntent.putExtra("standardDuration", duration);
                startActivityForResult(setDurationIntent, DURATION_REQUEST);
            }
        });

        maxParticipantsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setMaxParticipantsIntent = new Intent(CreateAdvertisementActivity.this, SetMaxParticipantsActivity.class);
                setMaxParticipantsIntent.putExtra("standardMaxParticipants", maxParticipants);
                startActivityForResult(setMaxParticipantsIntent, MAX_PARTICIPANTS_REQUEST);
            }
        });

        priceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (payoutsEnabled) {
                    Intent setPriceIntent = new Intent(CreateAdvertisementActivity.this, SetPriceActivity.class);
                    setPriceIntent.putExtra("standardPrice", price);
                    setPriceIntent.putExtra("accountCurrency", accountCurrency);
                    startActivityForResult(setPriceIntent, PRICE_REQUEST);
                    return;
                }
                // ---- Payouts not enabled -----
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAdvertisementActivity.this);
                builder.setMessage(R.string.create_free_session_question);
                builder.setPositiveButton(R.string.create_free_session, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        price = 0;
                        priceTV.setText(getResources().getString(R.string.free));
                    }
                });
                builder.setNegativeButton(R.string.add_payout_method_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        priceOverlay.setVisibility(View.VISIBLE);
                        dotProgressBar.setVisibility(View.VISIBLE);
                        Intent paymentPreferencesIntent = new Intent(CreateAdvertisementActivity.this, PayoutPreferencesActivity.class);
                        startActivityForResult(paymentPreferencesIntent, PAYOUT_METHOD_REQUEST);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        saveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration==-1) {
                    Toast.makeText(CreateAdvertisementActivity.this, getResources().getString(R.string.please_choose_session_duration), Toast.LENGTH_LONG).show();
                    return;
                }
                if (maxParticipants==-1) {
                    Toast.makeText(CreateAdvertisementActivity.this, getResources().getString(R.string.please_choose_maximum_nr_of_participants), Toast.LENGTH_LONG).show();
                    return;
                }
                if (price==-1) {
                    Toast.makeText(CreateAdvertisementActivity.this, getResources().getString(R.string.please_set_session_price), Toast.LENGTH_LONG).show();
                    return;
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hour = timePicker.getHour();
                } else {
                    hour = timePicker.getCurrentHour();
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    minute = timePicker.getMinute();
                } else {
                    minute = timePicker.getCurrentMinute();
                }


                myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                myCalendar.set(Calendar.MINUTE, minute);

                DateTime pickedDate = new DateTime(myCalendar.getTime().getTime());

                if (advertisementArrayList.size()>0) {
                    for (Advertisement advertisement: advertisementArrayList) {
                        DateTime adTime = new DateTime(advertisement.getAdvertisementTimestamp());
                        if (pickedDate.equals(adTime)) {
                            Toast.makeText(CreateAdvertisementActivity.this, R.string.already_session_planned_at_this_time, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                Advertisement advertisement = new Advertisement();
                advertisement.setDurationInMin(duration);
                advertisement.setMaxParticipants(maxParticipants);
                advertisement.setPrice(price);
                advertisement.setAdvertisementTimestamp(myCalendar.getTimeInMillis());

                advertisementArrayList.add(advertisement);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("duration", duration);
                resultIntent.putExtra("maxParticipants", maxParticipants);
                resultIntent.putExtra("price", price);
                resultIntent.putExtra("payoutsEnabled", payoutsEnabled);
                resultIntent.putExtra("dateAndTime", myCalendar.getTimeInMillis());
                resultIntent.putExtra("advertisementArrayList", advertisementArrayList);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payoutsEnabled", payoutsEnabled);
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payoutsEnabled", payoutsEnabled);
        setResult(Activity.RESULT_CANCELED, resultIntent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==DURATION_REQUEST && resultCode==RESULT_OK) {
            duration = data.getIntExtra("selectedDuration", 0);
            durationTV.setText(DURATION_STRINGS.get(duration));
        }

        if (requestCode==MAX_PARTICIPANTS_REQUEST && resultCode==RESULT_OK) {
            maxParticipants = data.getIntExtra("selectedMaxParticipants", 0);
            maxParticipantsTV.setText(Integer.toString(maxParticipants));
        }

        if (requestCode==PRICE_REQUEST && resultCode==RESULT_OK) {
            price = data.getIntExtra("selectedPrice", 100);
            priceTV.setText(PRICES_STRINGS_SE.get(price));
        }

        if (requestCode==PAYOUT_METHOD_REQUEST) {
            checkIfPayOutsIsEnabled();
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay,  R.anim.slide_out_down);

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

    private void checkIfPayOutsIsEnabled() {
        // --------------CHECK IF PAYOUTS ARE ENABLED------
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // ----------- PAYOUTS NOT ENABLED --------------
                    priceOverlay.setVisibility(View.GONE);
                    dotProgressBar.setVisibility(View.GONE);
                    payoutsEnabled = false;
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
                                priceOverlay.setVisibility(View.GONE);
                                dotProgressBar.setVisibility(View.GONE);
                                return;
                            }
                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("resultType").toString().equals("account")) {

                                HashMap<String, Object> account = (HashMap<String, Object>) result.get("account");
                                accountCurrency = account.get("default_currency").toString();

                                // ----------- PAYOUTS ENABLED --------------
                                if (account.get("payouts_enabled").toString().equals("true")) {
                                    payoutsEnabled = true;
                                    priceOverlay.setVisibility(View.GONE);
                                    dotProgressBar.setVisibility(View.GONE);

                                } else {
                                    // ----------- PAYOUTS NOT ENABLED --------------
                                    payoutsEnabled = false;
                                    priceOverlay.setVisibility(View.GONE);
                                    dotProgressBar.setVisibility(View.GONE);
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
                priceOverlay.setVisibility(View.GONE);
                dotProgressBar.setVisibility(View.GONE);
                payoutsEnabled = false;
            }
        });
    }
}
