package com.foxmike.android.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foxmike.android.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CancelAdvertisementActivity extends AppCompatActivity {

    private ProgressBar progressBarHorizontal;

    private String advertisementId;
    private String sessionId;
    private HashMap<String,String> participantsIds;
    private Long advertisementTimestamp;
    private String accountId;

    private int refundedCounter = 0;
    private String currentUserId;

    private HashMap<String,Object> refundMap = new HashMap<>();
    private Map cancelMap = new HashMap<>();

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_advertisement);

        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        advertisementId = getIntent().getStringExtra("advertisementId");
        sessionId = getIntent().getStringExtra("sessionId");
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp",0);
        participantsIds = (HashMap) getIntent().getSerializableExtra("participantsIds");
        accountId = getIntent().getStringExtra("accountId");

        if (participantsIds.size()==0) {
            rootDbRef.child("advertisements").child(advertisementId).child("status").setValue("cancelled");
            rootDbRef.child("sessions").child(sessionId).child("advertisements").child(advertisementId).setValue(0);
            rootDbRef.child("users").child(currentUserId).child("advertisementsHosting").child(advertisementId).setValue(0);
        } else {
            refundAll();
        }
    }

    private void takeDeposition() {
    }

    private void evaluateDeposition() {
    }

    private void refundAll() {

        for (String participantId: participantsIds.keySet()) {
            refundMap.put("chargeId", participantsIds.get(participantId));
            refundMap.put("accountId", accountId);
            refundMap.put("userID", currentUserId);
            refundMap.put("advertisementId", advertisementId);
            refundMap.put("hostCancellation", "hostCancellation");

            refundCharge(refundMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {


                    int progress = (refundedCounter/participantsIds.size())*(60);

                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        progressBarHorizontal.setProgress(20 + progress);
                        // [START_EXCLUDE]
                        Log.w(TAG, "retrieve:onFailure", e);
                        Toast.makeText(CancelAdvertisementActivity.this, "An error occurred." + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                        // [END_EXCLUDE]
                    }

                    // If successful, extract
                    HashMap<String, Object> result = task.getResult();

                    if (result.get("operationResult").toString().equals("success")) {
                        progressBarHorizontal.setProgress(20 + progress);

                        refundedCounter++;
                        if (participantsIds.size()==refundedCounter) {
                            Long currentTimestamp = System.currentTimeMillis();
                            DateTime currentTime = new DateTime(currentTimestamp);
                            DateTime adTime = new DateTime(advertisementTimestamp);
                            Duration durationCurrentToAdvertisement = new Duration(currentTime, adTime);

                            if (durationCurrentToAdvertisement.getStandardHours()>6) {
                                rootDbRef.child("advertisements").child(advertisementId).child("status").setValue("cancelled");
                                rootDbRef.child("sessions").child(sessionId).child("advertisements").child(advertisementId).setValue(0);
                                rootDbRef.child("users").child(currentUserId).child("advertisementsHosting").child(advertisementId).setValue(0);
                                //rootDbRef.updateChildren(cancelMap);
                                evaluateDeposition();
                            } else {
                                //rootDbRef.updateChildren(cancelMap);
                                takeDeposition();
                            }
                        }

                    } else {
                        progressBarHorizontal.setProgress(20 + progress);
                        HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                        Toast.makeText(CancelAdvertisementActivity.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private Task<HashMap<String, Object>> refundCharge(HashMap<String, Object> refundMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("refundCharge")
                .call(refundMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
