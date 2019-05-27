package com.foxmike.android.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.model.PaymentIntent;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class BookingActivity extends AppCompatActivity {

    private String advertisementId;
    private String hostId;
    private Long advertisementTimestamp;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private View mainView;
    private int amount;
    private int advertisementDurationInMin;
    private  String sessionType;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int currentNrOfParticipants;
    private TextView bookingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        mAuth = FirebaseAuth.getInstance();
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        progressBar = findViewById(R.id.progressBar);
        mainView = findViewById(R.id.mainView);
        findViewById(R.id.bookingText);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Get data from previous activity
        advertisementId = getIntent().getStringExtra("advertisementId");
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp", 0L);
        hostId = getIntent().getStringExtra("hostId");
        sessionType = getIntent().getStringExtra("sessionType");
        amount = getIntent().getIntExtra("amount", 0);
        advertisementDurationInMin = getIntent().getIntExtra("advertisementDurationInMin", 0);
        currentNrOfParticipants = getIntent().getIntExtra("currentNrOfParticipants", 0);
        // If advertisement is free, just add the participant to the participant list
        if (amount==0) {
            addCurrentUserToSessionParticipantList();
        } else {
            HashMap bookMap = new HashMap();
            bookMap.put("hostFirebaseId", hostId);
            bookMap.put("customerFirebaseId", mAuth.getCurrentUser().getUid());
            bookMap.put("advertisementId", advertisementId);
            bookMap.put("email", mAuth.getCurrentUser().getEmail());
            bookMap.put("advertisementDurationInMin", advertisementDurationInMin);
            bookMap.put("returnURL", "foxmike://booking");

            bookSessionWithSCA(bookMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {

                    // If error
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        progressBar.setVisibility(View.GONE);
                        Log.w(TAG, "retrieve:onFailure", e);
                        showSnackbar(getString(R.string.bad_internet));
                        return;
                    }
                    // If successful the variable "operationResult" will say "success,
                    // If so finish the activity with a result ok so the previous activity knows it's a success
                    HashMap<String, Object> result = task.getResult();

                    if (result.get("resultType").toString().equals("paymentIntentParameters")) {
                        String status = (String) result.get("paymentIntentStatus");
                        if (status.equals(PaymentIntent.Status.RequiresAction.toString())) {
                            Uri redirectUrl = Uri.parse((String) result.get("redirectUrl"));
                            if (redirectUrl != null) {
                                startActivity(new Intent(Intent.ACTION_VIEW, redirectUrl));
                            }
                            return;
                        } else if (status.equals(PaymentIntent.Status.Succeeded.toString())){
                            progressBar.setVisibility(View.GONE);
                            Bundle bundle = new Bundle();
                            bundle.putDouble("session_price", (double) amount);
                            bundle.putString("session_price_string", Integer.toString(amount));
                            bundle.putString("session_currency", "SEK");
                            bundle.putString("session_type", sessionType);
                            bundle.putString("session_host_id", hostId);
                            bundle.putString("participant_email", mAuth.getCurrentUser().getEmail());
                            bundle.putString("participant_id", mAuth.getCurrentUser().getUid());
                            bundle.putDouble("currentNrOfParticipants", currentNrOfParticipants);
                            mFirebaseAnalytics.logEvent("booking", bundle);
                            setResult(RESULT_OK, null);
                            // close activity
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showSnackbar("Payment cancelled");
                        }
                    } else {
                        // If error, show error in snackbar
                        HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                        showSnackbar(error.get("message").toString());
                    }
                }
            });
        }
    }

    private void addCurrentUserToSessionParticipantList() {
        // write current user as participant in session to database
        HashMap<String,Object> freeChargeMap = new HashMap<>();
        freeChargeMap.put("userID", mAuth.getCurrentUser().getUid());
        freeChargeMap.put("advertisementId", advertisementId);
        freeChargeMap.put("advertisementTimestamp", advertisementTimestamp);
        freeChargeMap.put("advertisementDurationInMin", advertisementDurationInMin);
        createFreeCharge(freeChargeMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                // If error
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "retrieve:onFailure", e);
                    showSnackbar(getString(R.string.bad_internet));
                    return;
                }
                // If successful the variable "operationResult" will say "success,
                // If so finish the activity with a result ok so the previous activity knows it's a success
                HashMap<String, Object> result = task.getResult();
                if (result.get("operationResult").toString().equals("success")) {
                    progressBar.setVisibility(View.GONE);

                    Bundle bundle = new Bundle();
                    bundle.putDouble("session_price", (double) amount);
                    bundle.putString("session_currency", "SEK");
                    bundle.putString("session_type", sessionType);
                    bundle.putString("session_host_id", hostId);
                    bundle.putString("participant_email", mAuth.getCurrentUser().getEmail());
                    bundle.putString("participant_id", mAuth.getCurrentUser().getUid());
                    bundle.putDouble("currentNrOfParticipants", currentNrOfParticipants);
                    mFirebaseAnalytics.logEvent("booking", bundle);

                    setResult(RESULT_OK, null);
                    // close activity
                    finish();
                } else {
                    // If error, show error in snackbar
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                    showSnackbar(error.get("message").toString());
                }
            }
        });
    }
    // Function createFreeCharge
    private Task<HashMap<String, Object>> createFreeCharge(HashMap<String, Object> chargeMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("createFreeCharge")
                .call(chargeMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
    // Function bookSessionWithSCA
    private Task<HashMap<String, Object>> bookSessionWithSCA(HashMap<String, Object> bookMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("bookSessionWithSCA")
                .call(bookMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    // Function bookSessionWithSCA
    private Task<HashMap<String, Object>> confirmPaymentIntent(HashMap<String, Object> paymentIntentMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("confirmPaymentIntent")
                .call(paymentIntentMap)
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        bookingText = findViewById(R.id.bookingText);

        if (intent.getData() != null && intent.getData().getQuery() != null) {
            String paymentIntentId = intent.getData().getQueryParameter(
                    "payment_intent");

            HashMap<String, Object> paymentIntentMap = new HashMap<>();
            paymentIntentMap.put("paymentIntentId", paymentIntentId);
            paymentIntentMap.put("hostFirebaseId", hostId);
            paymentIntentMap.put("customerFirebaseId", mAuth.getCurrentUser().getUid());
            paymentIntentMap.put("advertisementId", advertisementId);
            paymentIntentMap.put("email", mAuth.getCurrentUser().getEmail());
            paymentIntentMap.put("advertisementDurationInMin", advertisementDurationInMin);

            confirmPaymentIntent(paymentIntentMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                    // If error
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        progressBar.setVisibility(View.GONE);
                        Log.w(TAG, "retrieve:onFailure", e);
                        showSnackbar(getString(R.string.bad_internet));
                        return;
                    }
                    // If successful the variable "operationResult" will say "success,
                    // If so finish the activity with a result ok so the previous activity knows it's a success
                    HashMap<String, Object> result = task.getResult();

                    if (result.get("resultType").toString().equals("paymentIntentParameters")) {
                        String status = (String) result.get("paymentIntentStatus");
                        if (status.equals(PaymentIntent.Status.RequiresAction.toString())) {
                            Uri redirectUrl = Uri.parse((String) result.get("redirectUrl"));
                            if (redirectUrl != null) {
                                startActivity(new Intent(Intent.ACTION_VIEW, redirectUrl));
                            }
                            return;
                        } else if (status.equals((PaymentIntent.Status.Succeeded.toString()))){
                            progressBar.setVisibility(View.GONE);
                            Bundle bundle = new Bundle();
                            bundle.putDouble("session_price", (double) amount);
                            bundle.putString("session_price_string", Integer.toString(amount));
                            bundle.putString("session_currency", "SEK");
                            bundle.putString("session_type", sessionType);
                            bundle.putString("session_host_id", hostId);
                            bundle.putString("participant_email", mAuth.getCurrentUser().getEmail());
                            bundle.putString("participant_id", mAuth.getCurrentUser().getUid());
                            bundle.putDouble("currentNrOfParticipants", currentNrOfParticipants);
                            mFirebaseAnalytics.logEvent("booking", bundle);
                            setResult(RESULT_OK, null);
                            // close activity
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showSnackbar(getResources().getString(R.string.booking_cancelled));
                            bookingText.setText(R.string.booking_cancelled);
                        }
                    } else {
                        // If error, show error in snackbar
                        progressBar.setVisibility(View.GONE);
                        HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                        showSnackbar(getResources().getString(R.string.booking_cancelled));
                        bookingText.setText(R.string.booking_cancelled);
                    }

                }
            });

            /*final PaymentIntentParams retrievePaymentIntentParams =
                    PaymentIntentParams.createRetrievePaymentIntentParams(clientSecret);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // retrieve the PaymentIntent on a background thread
                    final PaymentIntent paymentIntent =
                            mStripe.retrievePaymentIntentSynchronous(
                                    retrievePaymentIntentParams,
                                    "pk_test_6IcNIdHpN4LegxE3t8KzvmHx");
                }
            });

            // If you had a dialog open when your user went elsewhere, remember to close it here.
            mRedirectDialogController.dismissDialog();*/
        }
    }


}
