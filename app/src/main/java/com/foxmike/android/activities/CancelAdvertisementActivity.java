package com.foxmike.android.activities;

import android.content.DialogInterface;
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
    private HashMap<String,String> participantsTimestamps;
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
        participantsTimestamps = (HashMap) getIntent().getSerializableExtra("participantsTimestamps");
        accountId = getIntent().getStringExtra("accountId");

        cancelMap.put("sessionName", sessionName);
        cancelMap.put("participantsTimestamps", participantsTimestamps);
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

        // If session has already taken place show alert dialog explaining cancellation not possible
        if (currentTime.isAfter(adTime)) {
            alertDialogOk(getString(R.string.cancellation_not_possible), getString(R.string.session_has_passed), true, new OnOkPressedListener() {
                @Override
                public void OnOkPressed() {
                    setResult(RESULT_CANCELED, null);
                    finish();
                }
            });
        } else {
            // If no participants, cancel advertisement without cloud function
            if (participantsTimestamps.size()==0) {
                cancelAd();
                /*rootDbRef.child("advertisements").child(advertisementId).child("status").setValue("cancelled");
                rootDbRef.child("sessionAdvertisements").child(sessionId).child(advertisementId).setValue(0);
                progressBar.setVisibility(View.GONE);
                finishCancellation();*/
            } else {
                // If participants but with
                if (durationCurrentToAdvertisement.getStandardHours() > 24) {
                    alertDialogPositiveOrNegative(getString(R.string.cancellation), getString(R.string.cancellation_small_fee_warning), getString(R.string.cancel_session), getString(R.string.do_not_cancel_session), new OnPositiveOrNegativeButtonPressedListener() {
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
                    alertDialogPositiveOrNegative(getString(R.string.session_is_within_24_hours), getString(R.string.cancellation_large_fee_warning), getString(R.string.cancel_session), getString(R.string.do_not_cancel_session), new OnPositiveOrNegativeButtonPressedListener() {
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

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside, OnOkPressedListener onOkPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onOkPressedListener.OnOkPressed();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();
    }


    public void alertDialogPositiveOrNegative(String title, String message, String positiveButton, String negativeButton, OnPositiveOrNegativeButtonPressedListener onPositiveOrNegativeButtonPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);

        // Add the buttons
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                onPositiveOrNegativeButtonPressedListener.OnPositivePressed();
            }
        });

        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onPositiveOrNegativeButtonPressedListener.OnNegativePressed();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();

    }

    public interface OnOkPressedListener {
        void OnOkPressed();
    }

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }
}
