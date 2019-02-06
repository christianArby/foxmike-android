package com.foxmike.android.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.utils.AlertDialogs;
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

import static android.content.ContentValues.TAG;

public class CancelAdvertisementActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private String advertisementId;
    private String sessionId;
    private String imageUrl;
    private HashMap<String,String> chargeIds;
    private Long advertisementTimestamp;
    private String accountId;
    private String currentUserId;
    private String sessionName;
    private HashMap<String,Object> cancelMap = new HashMap<>();

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_advertisement);

        progressBar = findViewById(R.id.progressBar);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        sessionName = getIntent().getStringExtra("sessionName");
        advertisementId = getIntent().getStringExtra("advertisementId");
        imageUrl = getIntent().getStringExtra("imageUrl");
        sessionId = getIntent().getStringExtra("sessionId");
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp",0);
        chargeIds = (HashMap) getIntent().getSerializableExtra("chargeIds");
        accountId = getIntent().getStringExtra("accountId");

        cancelMap.put("sessionName", sessionName);
        cancelMap.put("chargeIds", chargeIds);
        cancelMap.put("imageUrl", imageUrl);
        cancelMap.put("accountId", accountId);
        cancelMap.put("advertisementId", advertisementId);
        cancelMap.put("sessionId", sessionId);
        cancelMap.put("hostCancellation", "true");
        cancelMap.put("currentUserId", currentUserId);
        cancelMap.put("advertisementTimestamp", advertisementTimestamp);

        Long currentTimestamp = System.currentTimeMillis();
        DateTime currentTime = new DateTime(currentTimestamp);
        DateTime adTime = new DateTime(advertisementTimestamp);

        Duration durationCurrentToAdvertisement = new Duration(currentTime, adTime);
        AlertDialogs alertDialogs = new AlertDialogs(CancelAdvertisementActivity.this);

        // If session has already taken place show alert dialog explaining cancellation not possible
        if (currentTime.isAfter(adTime)) {
            alertDialogs.alertDialogOk(getString(R.string.cancellation_not_possible), getString(R.string.session_has_passed), new AlertDialogs.OnOkPressedListener() {
                @Override
                public void OnOkPressed() {
                    finishCancellation();
                }
            });
        } else {
            // If no participants, cancel advertisement without cloud function
            if (chargeIds.size()==0) {
                rootDbRef.child("advertisements").child(advertisementId).child("status").setValue("cancelled");
                rootDbRef.child("sessions").child(sessionId).child("advertisements").child(advertisementId).setValue(0);
                rootDbRef.child("users").child(currentUserId).child("advertisementsHosting").child(advertisementId).setValue(0);
                progressBar.setVisibility(View.GONE);
                finishCancellation();
            } else {
                // If participants but with
                if (durationCurrentToAdvertisement.getStandardHours() > 24) {
                    alertDialogs.alertDialogPositiveOrNegative(getString(R.string.cancellation), getString(R.string.cancellation_small_fee_warning), getString(R.string.cancel_session), getString(R.string.do_not_cancel_session), new AlertDialogs.OnPositiveOrNegativeButtonPressedListener() {
                        @Override
                        public void OnPositivePressed() {
                            cancelAd();
                        }

                        @Override
                        public void OnNegativePressed() {
                            finishCancellation();
                        }
                    });
                } else {
                    alertDialogs.alertDialogPositiveOrNegative(getString(R.string.session_is_within_24_hours), getString(R.string.cancellation_large_fee_warning), getString(R.string.cancel_session), getString(R.string.do_not_cancel_session), new AlertDialogs.OnPositiveOrNegativeButtonPressedListener() {
                        @Override
                        public void OnPositivePressed() {
                            cancelAd();
                        }

                        @Override
                        public void OnNegativePressed() {
                            finishCancellation();
                        }
                    });
                }
            }
        }
    }

    private void cancelAd() {
        cancelAdvertisement(cancelMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {

                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    // [START_EXCLUDE]
                    Log.w(TAG, "retrieve:onFailure", e);
                    Toast.makeText(CancelAdvertisementActivity.this, "An error occurred." + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                    // [END_EXCLUDE]
                }
                // If successful, extract
                HashMap<String, Object> result = task.getResult();
                Toast.makeText(getApplicationContext(), result.get("operationResult").toString(), Toast.LENGTH_LONG).show();

                if (result.get("operationResult").toString().equals("success")) {
                    progressBar.setVisibility(View.GONE);
                    finishCancellation();
                } else {
                    progressBar.setVisibility(View.GONE);
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                    Toast.makeText(CancelAdvertisementActivity.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void finishCancellation() {
        setResult(RESULT_OK);
        finish();
    }

    private Task<HashMap<String, Object>> cancelAdvertisement(HashMap<String, Object> cancelMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("cancelAdvertisement")
                .call(cancelMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
