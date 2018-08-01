package com.foxmike.android.activities;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foxmike.android.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CancelBookingActivity extends AppCompatActivity {

    private ProgressBar progressBarHorizontal;

    private FirebaseFunctions mFunctions;
    private Long bookingTimestamp;
    private Long sessionTimestamp;
    private String sessionId;
    private String participantId;
    private String chargeId;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_booking);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);

        progressBarHorizontal.setProgress(20);

        mFunctions = FirebaseFunctions.getInstance();

        bookingTimestamp = getIntent().getLongExtra("bookingTimestamp",0);
        sessionTimestamp = getIntent().getLongExtra("sessionTimestamp",0);
        sessionId = getIntent().getStringExtra("sessionID");
        participantId = getIntent().getStringExtra("participantId");
        chargeId = getIntent().getStringExtra("chargeId");
        accountId = getIntent().getStringExtra("accountId");

        getTimestamp().addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull Task<Long> task) {
                // If not succesful, show error
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                    }
                    Log.w("CancelActivity", "getTimestamp:onFailure", e);
                    return;
                }
                progressBarHorizontal.setProgress(40);

                Long currentTimestamp = task.getResult();
                tryRefund(currentTimestamp);

            }
        });

    }

    private void tryRefund(Long currentTimestamp) {

        // TODO CHECK SO THAT TIMESTAMP IS NOT LOCAL


        DateTime currentTime = new DateTime(currentTimestamp);
        DateTime sessionTime = new DateTime(sessionTimestamp);
        DateTime bookedTime = new DateTime(bookingTimestamp);
        Duration durationCurrentToSession = new Duration(currentTime, sessionTime);
        Duration durationBookedToCurrent = new Duration(bookedTime, currentTime);

        if (currentTimestamp>sessionTimestamp) {
            Toast.makeText(CancelBookingActivity.this,"Session has passed, refund is not possible", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK, null);
            finish();
            return;
        }

        if (durationCurrentToSession.getStandardHours()<6 && durationBookedToCurrent.getStandardMinutes()>30) {
            Toast.makeText(CancelBookingActivity.this,"Session is closer to 6 hours and booking was made more than 30 minutes ago, refund is not possible", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK, null);
            finish();
            return;
        }

        HashMap<String,Object> refundMap = new HashMap<>();
        refundMap.put("chargeId", chargeId);
        refundMap.put("accountId", accountId);
        refundMap.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        refundMap.put("sessionId", sessionId);


        refundCharge(refundMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {

                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    progressBarHorizontal.setProgress(100);
                    // [START_EXCLUDE]
                    Log.w(TAG, "retrieve:onFailure", e);
                    Toast.makeText(CancelBookingActivity.this, "An error occurred." + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                    // [END_EXCLUDE]
                }

                progressBarHorizontal.setProgress(80);

                // If successful, extract
                HashMap<String, Object> result = task.getResult();

                if (result.get("operationResult").toString().equals("success")) {
                    progressBarHorizontal.setProgress(100);
                    Map<String,Object> refund = (Map) result.get("refund");
                    Toast.makeText(CancelBookingActivity.this, "Refund successful, status:" + refund.get("status").toString(), Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, null);
                    finish();
                } else {
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                    Toast.makeText(CancelBookingActivity.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    // Function createCharge
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



    private Task<Long> getTimestamp() {
        return mFunctions
                .getHttpsCallable("getTimestamp")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Long>() {
                    @Override
                    public Long then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Long> result = (Map<String, Long>) task.getResult().getData();
                        return (Long) result.get("operationResult");
                    }
                });
    }
}
