package com.foxmike.android.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class UpdateStripeAccountWithPayoutActivity extends AppCompatActivity {

    private FirebaseFunctions mFunctions;
    private View mainView;
    private HashMap<String, Object> accountData;
    private Button updateAccountBtn;
    private EditText ibanET;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_stripe_account_with_payout);

        mainView = findViewById(R.id.mainView);
        updateAccountBtn = findViewById(R.id.createStripeAccountBtn);
        ibanET = findViewById(R.id.ibanET);
        progressBar = findViewById(R.id.progressBar_cyclic);

        accountData = (HashMap) getIntent().getSerializableExtra("accountData");

        mFunctions = FirebaseFunctions.getInstance();
        // Set default iban text
        ibanET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (ibanET.getText().length()==0) {
                        ibanET.setText("SE");
                    }
                }
            }
        });
        // Fix iban format
        ibanET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                CharSequence charSequence = " ";
                int[] intArray = {4,9,14,19,24};
                // Loop through all characters
                for (int i =0; i < editable.length(); i++) {

                    // If it is a S position and does not contain a S insert a S
                    if (i==0) {
                        CharSequence s = "S";
                        if (editable.charAt(i)!='S') {
                            editable.insert(i, s);
                        }
                    } else {
                        // If it is NOT a S position and contains a S position remove a character
                        if (editable.charAt(i)=='S') {
                            editable.replace(i,i+1, "");
                        }
                    }

                    // If it is a E position and does not contain a E insert a E
                    if (i==1) {
                        CharSequence e = "E";
                        if (editable.charAt(i)!='E') {
                            editable.insert(i, e);
                        }
                    } else {
                        // If it is NOT a S position and contains a S position remove a character
                        if (editable.charAt(i)=='E') {
                            editable.replace(i,i+1, "");
                        }
                    }

                    boolean spacePosition = false;
                    // Check if position is a space position
                    for (int n = 0; n < intArray.length; n++) {
                        if (i==intArray[n]) {
                            spacePosition = true;
                        }
                    }
                    // If it is a space position and does not contain a space position insert a space position
                    if (spacePosition) {
                        if (editable.charAt(i)!=' ') {
                            editable.insert(i, charSequence);
                        }
                    } else {
                        // If it is NOT a space position and contains a space position remove a character
                        if (editable.charAt(i)==' ') {
                            editable.replace(i,i+1, "");
                        }
                    }
                }
            }
        });

        updateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MyProgressBar myProgressBar = new MyProgressBar(progressBar, UpdateStripeAccountWithPayoutActivity.this);
                myProgressBar.startProgressBar();
                //String iban = ibanET.getText().toString().trim().replaceAll("\\s+","");

                // TODO REMOVE TEST IBAN
                String iban = "DE89370400440532013000";
                //String iban = "DE62370400440532013001";

                BankAccount bankAccount = new BankAccount(iban,"de","eur","");
                Stripe stripe = new Stripe(UpdateStripeAccountWithPayoutActivity.this, "pk_test_6IcNIdHpN4LegxE3t8KzvmHx");

                // Create Bank Account token
                stripe.createBankAccountToken(bankAccount, new TokenCallback() {
                    @Override
                    public void onError(Exception error) {
                        // Show localized error message
                        Toast.makeText(UpdateStripeAccountWithPayoutActivity.this,
                                error.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    @Override
                    public void onSuccess(Token token) {
                        accountData.put("account_token", token.getId());
                        // If successfully created Bank Account create Stripe Account with all the collected info
                        createExternalAccount(accountData).addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                // If not succesful, show error
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

                                    // [START_EXCLUDE]
                                    Log.w(TAG, "createExternalAccount:onFailure", e);
                                    showSnackbar("An error occurred.");
                                    return;
                                    // [END_EXCLUDE]
                                }

                                // Show the string passed from the Firebase server if task/function call on server is successful
                                String result = task.getResult();
                                if (result.equals("success")) {
                                    myProgressBar.stopProgressBar();
                                    setResult(RESULT_OK, null);
                                    finish();
                                } else {
                                    myProgressBar.stopProgressBar();
                                    showSnackbar("An error occurred:" + " " + result);
                                }
                                // [END_EXCLUDE]
                            }
                        });


                    }
                });

            }
        });
    }

    // Function createStripeAccount
    private Task<String> createExternalAccount(Map<String, Object> accountData) {

        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("createExternalAccount")
                .call(accountData)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("operationResult");
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }
}
