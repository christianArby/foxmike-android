package com.foxmike.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private DatabaseReference maintenanceRef;
    private ValueEventListener maintenanceListener;
    private InputMethodManager imm;
    private MyProgressBar myProgressBar;

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

        Typeface tf = Typeface.create("sans-serif", Typeface.NORMAL);
        setTypeface(tf, mCardInputWidget);

        if (getIntent().getSerializableExtra("customerData")!= null) {
            customerData = (HashMap) getIntent().getSerializableExtra("customerData");
        } else {
            customerData = new HashMap<>();
        }

        FirebaseDatabaseViewModel customerIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> customerIdLiveData = customerIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeCustomerId"));
        customerIdLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    customerData.put("customerId", dataSnapshot.getValue().toString());
                }
            }
        });

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
            myProgressBar = new MyProgressBar(progressBar, CreateStripeCustomerActivity.this);
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
                                if (customerData.get("customerId")!=null) {
                                    addPaymentMethod(customerData).addOnCompleteListener(new OnCompleteListener<String>() {
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
                                                hideKeyboard();
                                                myProgressBar.stopProgressBar();
                                                showSnackbar(getString(R.string.the_card_could_not_be_added));
                                            }
                                        }
                                    });
                                } else {
                                    createCustomerSCA(customerData).addOnCompleteListener(new OnCompleteListener<String>() {
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
                                                hideKeyboard();
                                                myProgressBar.stopProgressBar();
                                                showSnackbar(getString(R.string.the_card_could_not_be_added));
                                            }
                                        }
                                    });
                                }
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
    private Task<String> createCustomerSCA(Map<String, Object> customerData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("createCustomerSCA")
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

    // Function createStripeAccount
    private Task<String> addPaymentMethod(Map<String, Object> customerData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("addPaymentMethodForCustomer")
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

    public void hideKeyboard() {
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public static void setTypeface(Typeface tf, View v) {
        if (v instanceof TextView) {
            ((TextView) v).setTypeface(tf);
        }
        else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;

            for (int i = 0; i < vg.getChildCount(); i++) {
                setTypeface(tf, vg.getChildAt(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if maintenance
        MaintenanceViewModel maintenanceViewModel = ViewModelProviders.of(this).get(MaintenanceViewModel.class);
        LiveData<DataSnapshot> maintenanceLiveData = maintenanceViewModel.getDataSnapshotLiveData();
        maintenanceLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if ((boolean) dataSnapshot.getValue()) {
                        Intent welcomeIntent = new Intent(CreateStripeCustomerActivity.this,WelcomeActivity.class);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(welcomeIntent);
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            }
        });
    }
}
