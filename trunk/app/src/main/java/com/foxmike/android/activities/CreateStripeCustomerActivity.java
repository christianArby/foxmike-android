package com.foxmike.android.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardMultilineWidget;

import java.util.HashMap;
import java.util.Map;

public class CreateStripeCustomerActivity extends AppCompatActivity {

    private View mainView;
    private Button createStripeCustomerBtn;
    private static final String TAG = "CreateStripeCustomer";
    private FirebaseFunctions mFunctions;
    private Card cardToSave;
    private CardMultilineWidget mCardInputWidget;
    private ProgressBar progressBar;
    private HashMap<String, Object> customerData;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stripe_customer);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mainView = findViewById(R.id.mainView);
        mCardInputWidget = (CardMultilineWidget) findViewById(R.id.card_input_widget);
        progressBar = findViewById(R.id.progressBar_cyclic);
        createStripeCustomerBtn = findViewById(R.id.createStripeCustomerBtn);
        mFunctions = FirebaseFunctions.getInstance();

        if (getIntent().getSerializableExtra("customerData")!= null) {
            customerData = (HashMap) getIntent().getSerializableExtra("customerData");
        } else {
            customerData = new HashMap<>();
        }

        createStripeCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onAddButtonClicked();
            }
        });

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void onAddButtonClicked() {
        cardToSave = mCardInputWidget.getCard();
        if (cardToSave == null) {
            Toast.makeText(CreateStripeCustomerActivity.this, "Please fill in card details.",Toast.LENGTH_LONG).show();
        } else {
            final MyProgressBar myProgressBar = new MyProgressBar(progressBar, CreateStripeCustomerActivity.this);
            myProgressBar.startProgressBar();
            if (!cardToSave.validateCard()) {
                myProgressBar.stopProgressBar();
                // Show errors
                Toast.makeText(CreateStripeCustomerActivity.this, "Invalid card details",Toast.LENGTH_LONG).show();
            } else {
                // Card is valid get Token from Stripe
                Stripe stripe = new Stripe(CreateStripeCustomerActivity.this, getString(R.string.stripe_publishable_key));
                stripe.createToken(
                        cardToSave,
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
                                            myProgressBar.stopProgressBar();
                                            showSnackbar("An error occurred." + e.getMessage());
                                            return;
                                        }
                                        // Show the string passed from the Firebase server if task/function call on server is successful
                                        String result = task.getResult();
                                        if (result.equals("success")) {
                                            myProgressBar.stopProgressBar();
                                            setResult(RESULT_OK, null);
                                            finish();
                                        } else {
                                            hideKeyboard(CreateStripeCustomerActivity.this);
                                            myProgressBar.stopProgressBar();
                                            showSnackbar(getString(R.string.the_card_could_not_be_added));
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
