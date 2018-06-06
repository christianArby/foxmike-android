package com.foxmike.android.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CreateStripeCustomerActivity extends AppCompatActivity {

    private View mainView;

    private Button createStripeCustomerBtn;

    private static final String TAG = "CreateStripeCustomer";
    private FirebaseFunctions mFunctions;

    private Card cardToSave;
    private CardMultilineWidget mCardInputWidget;
    private ProgressBar progressBar;
    private HashMap<String, Object> customerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stripe_customer);

        mainView = findViewById(R.id.mainView);

        mCardInputWidget = (CardMultilineWidget) findViewById(R.id.card_input_widget);
        progressBar = findViewById(R.id.progressBar_cyclic);
        createStripeCustomerBtn = findViewById(R.id.createStripeCustomerBtn);

        mFunctions = FirebaseFunctions.getInstance();

        if (getIntent().getSerializableExtra("customerData")!= null) {
            customerData = (HashMap) getIntent().getSerializableExtra("customerData");
        }

        createStripeCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChargeClicked();
            }
        });
    }

    private void onChargeClicked() {

        cardToSave = mCardInputWidget.getCard();

        if (cardToSave == null) {
            Toast.makeText(CreateStripeCustomerActivity.this, "Please fill in card details.",Toast.LENGTH_LONG).show();
        } else {

            final MyProgressBar myProgressBar = new MyProgressBar(progressBar, CreateStripeCustomerActivity.this);
            myProgressBar.startProgressBar();

            Card card = new Card(
                    cardToSave.getNumber(),
                    cardToSave.getExpMonth(),
                    cardToSave.getExpYear(),
                    cardToSave.getCVC());

            if (!card.validateCard()) {
                myProgressBar.stopProgressBar();
                // Show errors
                Toast.makeText(CreateStripeCustomerActivity.this, "Invalid card details",Toast.LENGTH_LONG).show();
            } else {
                // Card is valid get Token from Stripe
                Stripe stripe = new Stripe(CreateStripeCustomerActivity.this, "pk_test_6IcNIdHpN4LegxE3t8KzvmHx");

                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                                customerData.put("email", mAuth.getCurrentUser().getEmail());
                                customerData.put("tokenId", token.getId());
                                customerData.put("userID", mAuth.getCurrentUser().getUid());

                                createStripeCustomer(customerData).addOnCompleteListener(new OnCompleteListener<String>() {
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

                                            myProgressBar.stopProgressBar();

                                            // [START_EXCLUDE]
                                            Log.w(TAG, "createCustomer:onFailure", e);
                                            showSnackbar("An error occurred." + e.getMessage());
                                            return;
                                            // [END_EXCLUDE]
                                        }

                                        // Show the string passed from the Firebase server if task/function call on server is successful
                                        String result = task.getResult();
                                        if (result.equals("success")) {
                                            myProgressBar.stopProgressBar();
                                            setResult(RESULT_OK, null);
                                            finish();
                                        }
                                    }
                                });

                            }
                            public void onError(Exception error) {
                                myProgressBar.stopProgressBar();
                                // Show localized error message
                                Toast.makeText(CreateStripeCustomerActivity.this,
                                        error.getLocalizedMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
            }
        }

    }

    // Function createStripeAccount
    private Task<String> createStripeCustomer(Map<String, Object> customerData) {

        String function = "createCustomer";

        if (customerData.get("updateWithCustomerId")!=null) {
            function = "addSourceForCustomer";
        }

        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable(function)
                .call(customerData)
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
