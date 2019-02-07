package com.foxmike.android.activities;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.foxmike.android.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class WriteAdminNotification extends AppCompatActivity {

    @BindView(R.id.param1) EditText param1;
    @BindView(R.id.param2) EditText param2;
    @BindView(R.id.param3) EditText param3;
    @BindView(R.id.sourceId) EditText sourceId;
    @BindView(R.id.image) CircleImageView image;
    @BindView(R.id.sendNotification) Button sendNotification;
    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_admin_notification);
        ButterKnife.bind(this);

        sendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String,Object> notificationMap = new HashMap<>();
                notificationMap.put("param1", param1.getText().toString());
                notificationMap.put("param2", param2.getText().toString());
                notificationMap.put("param3", param3.getText().toString());
                notificationMap.put("sourceId", sourceId.getText().toString());
                notificationMap.put("thumbNail", "");

                adminNotification(notificationMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            Log.w(TAG, "retrieve:onFailure", e);
                            Toast.makeText(WriteAdminNotification.this, "An error occurred." + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        // If successful, extract
                        HashMap<String, Object> result = task.getResult();
                        if (result.get("operationResult").toString().equals("success")) {
                            Toast.makeText(WriteAdminNotification.this, "Notification sent", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                            Toast.makeText(WriteAdminNotification.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                mFunctions.getHttpsCallable("listAllUsers").call(new HashMap<>());
            }
        });






    }

    private Task<HashMap<String, Object>> adminNotification(HashMap<String, Object> notificationMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("adminNotification")
                .call(notificationMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
