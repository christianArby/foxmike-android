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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BookingActivity extends AppCompatActivity {

    private String advertisementId;
    private String hostId;
    private String stripeCustomerId;
    private String stripeAccountId;
    private Long advertisementTimestamp;
    private ProgressBar progressBarHorizontal;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private View mainView;
    private int amount;
    private String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        mAuth = FirebaseAuth.getInstance();
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        mainView = findViewById(R.id.mainView);

        progressBarHorizontal.setProgress(20);
        // Get data from previous activity
        advertisementId = getIntent().getStringExtra("advertisementId");
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp", 0L);
        hostId = getIntent().getStringExtra("hostId");
        stripeCustomerId = getIntent().getStringExtra("stripeCustomerId");
        amount = getIntent().getIntExtra("amount", 0);
        currency = getIntent().getStringExtra("currency");
        // If advertisement is free, just add the participant to the participant list
        if (amount==0) {
            progressBarHorizontal.setProgress(100);
            addCurrentUserToSessionParticipantList();
        } else {
            // Get the host stripe account id of the host
            rootDbRef.child("users").child(hostId).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBarHorizontal.setProgress(40);
                    stripeAccountId = dataSnapshot.getValue().toString();
                    // Retrieve the stripe customer object from Stripe with the stripe function retrieveStripeCustomer
                    retrieveStripeCustomer(stripeCustomerId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                            progressBarHorizontal.setProgress(60);
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                progressBarHorizontal.setProgress(100);
                                Log.w(TAG, "retrieve:onFailure", e);
                                showSnackbar("An error occurred." + e.getMessage());
                                return;
                            }
                            // If call to function is successful, extract the default source of the customer
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("resultType").toString().equals("customer")) {
                                HashMap<String, Object> customer = (HashMap<String, Object>) result.get("customer");
                                String defaultSource;
                                if (result.get("default_source")!= null) {
                                    // ----------------------- ALL INFO FOR CHARGE TOKEN ------------------------
                                    // All info to make a charge token is now available
                                    HashMap<String,String> idMap = new HashMap<>();
                                    idMap.put("customer", stripeCustomerId);
                                    idMap.put("account", stripeAccountId);
                                    // Start by creating a charge token with the stripe function createChargeToken
                                    createChargeToken(idMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                                        @Override
                                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                            progressBarHorizontal.setProgress(80);
                                            // If error
                                            if (!task.isSuccessful()) {
                                                Exception e = task.getException();
                                                progressBarHorizontal.setProgress(100);
                                                Log.w(TAG, "retrieve:onFailure", e);
                                                showSnackbar("An error occurred." + e.getMessage());
                                                return;
                                            }
                                            // If successful, extract the tokenId which can be used to make a charge
                                            HashMap<String, Object> result = task.getResult();
                                            if (result.get("tokenId").toString()!=null) {
                                                // ----------------------- ALL SET, CREATE CHARGE ------------------------
                                                // All information to make the charge is now available, se below
                                                HashMap<String,Object> chargeMap = new HashMap<>();
                                                chargeMap.put("amount", amount*100);
                                                chargeMap.put("currency", currency);
                                                chargeMap.put("tokenId", result.get("tokenId").toString());
                                                chargeMap.put("account", stripeAccountId);
                                                chargeMap.put("userID", mAuth.getCurrentUser().getUid());
                                                chargeMap.put("advertisementId", advertisementId);
                                                chargeMap.put("advertisementTimestamp", advertisementTimestamp);
                                                chargeMap.put("applicationFee", amount*0.1*100);
                                                chargeMap.put("email", mAuth.getCurrentUser().getEmail());
                                                createCharge(chargeMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                                        progressBarHorizontal.setProgress(100);
                                                        // If error
                                                        if (!task.isSuccessful()) {
                                                            Exception e = task.getException();
                                                            progressBarHorizontal.setProgress(100);
                                                            Log.w(TAG, "retrieve:onFailure", e);
                                                            showSnackbar("An error occurred." + e.getMessage());
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
                                            } else {
                                                // if tokenId was not created show error message
                                                showSnackbar(result.get("error").toString());
                                            }
                                        }
                                    });

                                } else {
                                    // If default_source is null display could not find payment method
                                    showSnackbar(getString(R.string.could_not_find_payment_method));
                                }

                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                showSnackbar(error.get("message").toString());
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
    }

    private void addCurrentUserToSessionParticipantList() {
        // write current user as participant in session to database
        Map requestMap = new HashMap<>();
        requestMap.put("advertisements/" + advertisementId + "/participantsIds/" + mAuth.getCurrentUser().getUid(), "FREE");
        requestMap.put("advertisements/" + advertisementId + "/participantsTimestamps/" + mAuth.getCurrentUser().getUid(), ServerValue.TIMESTAMP);
        requestMap.put("users/" + mAuth.getCurrentUser().getUid() + "/sessionsAttending/" + advertisementId, advertisementTimestamp);
        rootDbRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setResult(RESULT_OK, null);
                finish();
            }
        });
    }

    // Function createStripeAccount
    private Task<HashMap<String, Object>> retrieveStripeCustomer(String customerID) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("retrieveCustomer")
                .call(customerID)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    // Function createChargeToken
    private Task<HashMap<String, Object>> createChargeToken(HashMap<String, String> idMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("createChargeToken")
                .call(idMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    // Function createCharge
    private Task<HashMap<String, Object>> createCharge(HashMap<String, Object> chargeMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("createCharge")
                .call(chargeMap)
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
