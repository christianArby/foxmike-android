package com.foxmike.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.functions.HttpsCallableResult;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CancelBookingActivity extends AppCompatActivity {

    private ProgressBar progressBarHorizontal;

    private FirebaseFunctions mFunctions;
    private Long bookingTimestamp;
    private Long advertisementTimestamp;
    private String advertisementId;
    private String participantId;
    private String hostId;
    private int adPrice;

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
        advertisementTimestamp = getIntent().getLongExtra("advertisementTimestamp",0);
        advertisementId = getIntent().getStringExtra("advertisementId");
        adPrice = getIntent().getIntExtra("adPrice", 0);
        participantId = getIntent().getStringExtra("participantId");
        hostId = getIntent().getStringExtra("hostId");

        Long currentTimestamp = System.currentTimeMillis();
        tryRefund(currentTimestamp);
    }

    private void tryRefund(Long currentTimestamp) {
        // TODO CHECK SO THAT TIMESTAMP IS NOT LOCAL
        DateTime currentTime = new DateTime(currentTimestamp);
        DateTime sessionTime = new DateTime(advertisementTimestamp);
        DateTime bookedTime = new DateTime(bookingTimestamp);
        Duration durationCurrentToSession = new Duration(currentTime, sessionTime);
        Duration durationBookedToCurrent = new Duration(bookedTime, currentTime);
        if (currentTimestamp> advertisementTimestamp) {
            alertDialogOk(getString(R.string.cancellation_not_possible), getString(R.string.session_has_passed));
            return;
        }
        if (adPrice>0) {
            if (durationCurrentToSession.getStandardHours()<6 && durationBookedToCurrent.getStandardMinutes()>30) {
                alertDialogCancelWithoutRefund(getString(R.string.refund_not_possible), getString(R.string.session_is_too_close_to_refund));
                return;
            }
        }

        HashMap<String,Object> cancelMap = new HashMap<>();
        cancelMap.put("hostId", hostId);
        cancelMap.put("participantUserID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        cancelMap.put("advertisementId", advertisementId);
        cancelMap.put("hostCancellation", "false");

        cancelBooking(cancelMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    progressBarHorizontal.setProgress(100);
                    Log.w(TAG, "retrieve:onFailure", e);
                    Toast.makeText(CancelBookingActivity.this, getString(R.string.bad_internet), Toast.LENGTH_LONG).show();
                    return;
                }
                progressBarHorizontal.setProgress(80);
                // If successful, extract
                HashMap<String, Object> result = task.getResult();
                if (result.get("operationResult").toString().equals("success")) {
                    progressBarHorizontal.setProgress(100);
                    if (result.get("operationType").toString().equals("REFUND")) {
                        Map<String,Object> refund = (Map) result.get("refund");
                        int amount = (int) refund.get("amount");
                        float sweAmount = amount;
                        String refundAmount = String.format(Locale.FRANCE,"%.2f", sweAmount/100);
                        String currency = (String) refund.get("currency");
                        Intent data = new Intent();
                        data.putExtra("operationType", "REFUND");
                        data.putExtra("refundAmount", refundAmount);
                        data.putExtra("currency", currency);
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        setResult(RESULT_OK, null);
                        finish();
                    }
                } else {
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                    Toast.makeText(CancelBookingActivity.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void alertDialogCancelWithoutRefund(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CancelBookingActivity.this);
        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton(R.string.cancel_booking_anyway, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                adPrice = 0;
                Long currentTimestamp = System.currentTimeMillis();
                tryRefund(currentTimestamp);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertDialogOk(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CancelBookingActivity.this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);

        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private Task<HashMap<String, Object>> cancelBooking(HashMap<String, Object> cancelMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("cancelBooking")
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
