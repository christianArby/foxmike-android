package com.foxmike.android.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.foxmike.android.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

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

        // Get data from previous activity
        advertisementId = getIntent().getStringExtra("advertisementId");
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp", 0L);
        hostId = getIntent().getStringExtra("hostId");
        amount = getIntent().getIntExtra("amount", 0);
        advertisementDurationInMin = getIntent().getIntExtra("advertisementDurationInMin", 0);
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

            bookSession(bookMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                    progressBar.setVisibility(View.GONE);
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
    // Function bookSession
    private Task<HashMap<String, Object>> bookSession(HashMap<String, Object> bookMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("bookSession")
                .call(bookMap)
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
}
