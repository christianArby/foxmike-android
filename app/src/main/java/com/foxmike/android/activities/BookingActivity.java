package com.foxmike.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BookingActivity extends AppCompatActivity {

    private String sessionId;
    private String hostId;
    private String stripeCustomerId;
    private String stripeAccountId;
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

        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        mainView = findViewById(R.id.mainView);

        progressBarHorizontal.setProgress(20);

        // Get data from previous activity
        sessionId = getIntent().getStringExtra("sessionId");
        hostId = getIntent().getStringExtra("hostId");
        stripeCustomerId = getIntent().getStringExtra("stripeCustomerId");
        amount = getIntent().getIntExtra("amount", 0);
        currency = getIntent().getStringExtra("currency");

        // get host stripeAccount
        rootDbRef.child("users").child(hostId).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBarHorizontal.setProgress(40);
                if (dataSnapshot.getValue()==null) {
                    progressBarHorizontal.setProgress(100);
                    addCurrentUserToSessionParticipantList();
                } else {

                    stripeAccountId = dataSnapshot.getValue().toString();

                    // If stripe customerID is null create customer
                    if (stripeCustomerId==null) {
                        Intent createIntent = new Intent(BookingActivity.this, CreateStripeCustomerActivity.class);
                        startActivityForResult(createIntent, 1);
                    } else {

                        retrieveStripeCustomer(stripeCustomerId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                progressBarHorizontal.setProgress(60);

                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    progressBarHorizontal.setProgress(100);
                                    // [START_EXCLUDE]
                                    Log.w(TAG, "retrieve:onFailure", e);
                                    showSnackbar("An error occurred." + e.getMessage());
                                    return;
                                    // [END_EXCLUDE]
                                }

                                // If successful, extract
                                HashMap<String, Object> result = task.getResult();
                                if (result.get("resultType").toString().equals("customer")) {
                                    HashMap<String, Object> customer = (HashMap<String, Object>) result.get("customer");

                                    String defaultSource;

                                    if (result.get("default_source")!= null) {

                                        // ----------------------- ALL INFO IS AVAILABLE ------------------------
                                        HashMap<String,String> idMap = new HashMap<>();

                                        idMap.put("customer", stripeCustomerId);
                                        idMap.put("account", stripeAccountId);

                                        createChargeToken(idMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                                            @Override
                                            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                                progressBarHorizontal.setProgress(80);

                                                if (!task.isSuccessful()) {
                                                    Exception e = task.getException();
                                                    progressBarHorizontal.setProgress(100);
                                                    // [START_EXCLUDE]
                                                    Log.w(TAG, "retrieve:onFailure", e);
                                                    showSnackbar("An error occurred." + e.getMessage());
                                                    return;
                                                    // [END_EXCLUDE]
                                                }

                                                // If successful, extract
                                                HashMap<String, Object> result = task.getResult();

                                                if (result.get("tokenId").toString()!=null) {

                                                    // ----------------------- ALL SET, CREATE CHARGE ------------------------

                                                    HashMap<String,Object> chargeMap = new HashMap<>();
                                                    chargeMap.put("amount", amount*100);
                                                    chargeMap.put("currency", currency);
                                                    chargeMap.put("tokenId", result.get("tokenId").toString());
                                                    chargeMap.put("account", stripeAccountId);


                                                    createCharge(chargeMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                                            progressBarHorizontal.setProgress(100);
                                                            if (!task.isSuccessful()) {
                                                                Exception e = task.getException();
                                                                progressBarHorizontal.setProgress(100);
                                                                // [START_EXCLUDE]
                                                                Log.w(TAG, "retrieve:onFailure", e);
                                                                showSnackbar("An error occurred." + e.getMessage());
                                                                return;
                                                                // [END_EXCLUDE]
                                                            }

                                                            // If successful, extract
                                                            HashMap<String, Object> result = task.getResult();

                                                            if (result.get("operationResult").toString().equals("success")) {
                                                                showSnackbar("Booking successful.");
                                                                addCurrentUserToSessionParticipantList();

                                                            } else {
                                                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                                                                showSnackbar(error.get("message").toString());
                                                            }

                                                        }
                                                    });
                                                } else {
                                                    showSnackbar(result.get("error").toString());
                                                }
                                            }
                                        });

                                    } else {
                                        Intent createIntent = new Intent(BookingActivity.this,CreateStripeCustomerActivity.class);
                                        HashMap<String, Object> customerData = new HashMap<>();
                                        customerData.put("updateWithCustomerId", stripeCustomerId);
                                        createIntent.putExtra("customerData",customerData);
                                        startActivityForResult(createIntent, 1);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //LOOP UNTIL ALL INFO IS AVAILABLE
        if(resultCode==RESULT_OK){
            rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("stripeCustomerId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Intent refresh = new Intent(BookingActivity.this, BookingActivity.class);
                    refresh.putExtra("sessionId", sessionId);
                    refresh.putExtra("hostId", hostId);
                    refresh.putExtra("stripeCustomerId", dataSnapshot.getValue().toString());
                    refresh.putExtra("amount",amount);
                    refresh.putExtra("currency",currency);
                    startActivity(refresh);
                    BookingActivity.this.finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            this.finish();
        }

    }

    private void addCurrentUserToSessionParticipantList() {
        // write current user as participant in session to database
        Map requestMap = new HashMap<>();
        requestMap.put("sessions/" + sessionId + "/participants/" + mAuth.getCurrentUser().getUid(), true);
        requestMap.put("users/" + mAuth.getCurrentUser().getUid() + "/sessionsAttending/" + sessionId, true);
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
