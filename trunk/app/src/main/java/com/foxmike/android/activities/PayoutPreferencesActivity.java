package com.foxmike.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListPayoutMethodsAdapter;
import com.foxmike.android.interfaces.OnPayoutMethodClickedListener;
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
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class PayoutPreferencesActivity extends AppCompatActivity{

    private FirebaseFunctions mFunctions;
    private View mainView;
    private RecyclerView listPayoutMethodsRV;
    private ListPayoutMethodsAdapter listPayoutMethodsAdapter;
    private ProgressBar progressBar;
    private TextView addPayoutMethodTV;
    private String stripeAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_preferences);

        mFunctions = FirebaseFunctions.getInstance();

        mainView = findViewById(R.id.mainView);
        progressBar = findViewById(R.id.progressBar_cyclic);
        addPayoutMethodTV = findViewById(R.id.addPayoutMethodTV);
        listPayoutMethodsRV = findViewById(R.id.listPayoutMethodsRV);

        findStripeAccount();
    }

    private void findStripeAccount() {
        listPayoutMethodsRV.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        addPayoutMethodTV.setVisibility(View.GONE);
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    stripeAccountId = dataSnapshot.getValue().toString();
                    retrieveStripeExternalAccounts(stripeAccountId);
                    addPayoutMethodTV.setText(R.string.change_payout_method_text);
                    addPayoutMethodTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            HashMap<String, Object> accountData = new HashMap<>();
                            accountData.put("stripeAccountId", stripeAccountId);
                            Intent updateIntent = new Intent(PayoutPreferencesActivity.this,UpdateStripeAccountWithPayoutActivity.class);
                            updateIntent.putExtra("accountData",accountData);
                            startActivityForResult(updateIntent, 1);

                        }
                    });
                } else {
                    addPayoutMethodTV.setText(R.string.add_payout_method_text);
                    // If no stripe account exist, show add payout method text
                    addPayoutMethodTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent createIntent = new Intent(PayoutPreferencesActivity.this,CreateStripeAccountActivity.class);
                            startActivityForResult(createIntent, 1);
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                    addPayoutMethodTV.setVisibility(View.VISIBLE);
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
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, PayoutPreferencesActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    private void retrieveStripeExternalAccounts(String accountID) {
        // Retrieve Stripe Account
        retrieveStripeAccount(accountID).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                // If not succesful, show error and return from function, will trigger if account ID does not exist
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        // Function error code, will be INTERNAL if the failure
                        // was not handled properly in the function call.
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        // Arbitrary error details passed back from the function,
                        // usually a Map<String, Object>.
                        Object details = ffe.getDetails();
                    }

                    progressBar.setVisibility(View.GONE);
                    addPayoutMethodTV.setVisibility(View.VISIBLE);

                    // [START_EXCLUDE]
                    Log.w(TAG, "retrieve:onFailure", e);
                    showSnackbar("An error occurred." + e.getMessage());
                    return;
                    // [END_EXCLUDE]
                }


                // If successful, extract
                HashMap<String, Object> result = task.getResult();

                if (result.get("resultType").toString().equals("external_accounts")) {

                    HashMap<String, Object> external_accounts = (HashMap<String, Object>) result.get("external_accounts");
                    ArrayList<HashMap<String,Object>> external_accountsDataList = (ArrayList<HashMap<String,Object>>) external_accounts.get("data");

                    listPayoutMethodsRV.setLayoutManager(new LinearLayoutManager(PayoutPreferencesActivity.this));
                    listPayoutMethodsAdapter = new ListPayoutMethodsAdapter(external_accountsDataList, PayoutPreferencesActivity.this, new OnPayoutMethodClickedListener() {
                        @Override
                        public void OnPayoutMethodClicked(String accountId) {
                            showSnackbar(accountId + "Was clicked!");
                        }
                    });
                    listPayoutMethodsRV.setAdapter(listPayoutMethodsAdapter);
                    listPayoutMethodsRV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            //At this point the layout is complete and the
                            //dimensions of recyclerView and any child views are known.
                            listPayoutMethodsRV.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            addPayoutMethodTV.setVisibility(View.VISIBLE);
                        }
                    });

                } else {
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                    showSnackbar(error.get("message").toString());
                }
                // [END_EXCLUDE]
            }
        });
    }

    // Function createStripeAccount
    private Task<HashMap<String, Object>> retrieveStripeAccount(String accountId) {

        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("retrieve")
                .call(accountId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }
}