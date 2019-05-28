package com.foxmike.android.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListPaymentMethodsAdapter;
import com.foxmike.android.fragments.UpdateStripeSourceFragment;
import com.foxmike.android.interfaces.OnPaymentMethodClickedListener;
import com.foxmike.android.models.User;
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
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class PaymentPreferencesActivity extends AppCompatActivity implements UpdateStripeSourceFragment.OnStripeCustomerUpdatedListener {

    private FirebaseFunctions mFunctions;
    private View mainView;
    private RecyclerView listPaymentMethodsRV;
    private ListPaymentMethodsAdapter listPaymentMethodsAdapter;
    private ProgressBar progressBar;
    private TextView addPaymentMethodTV;
    private long mLastClickTime = 0;
    private DatabaseReference maintenanceRef;
    private ValueEventListener maintenanceListener;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_preferences);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mFunctions = FirebaseFunctions.getInstance();

        mainView = findViewById(R.id.mainView);
        progressBar = findViewById(R.id.progressBar_cyclic);
        addPaymentMethodTV = findViewById(R.id.addPaymentMethodTV);
        listPaymentMethodsRV = findViewById(R.id.listPaymentMethodsRV);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        findStripeCustomer();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void findStripeCustomer() {
        listPaymentMethodsRV.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        addPaymentMethodTV.setVisibility(View.GONE);
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(PaymentPreferencesActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()));
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    currentUser = dataSnapshot.getValue(User.class);
                    if (currentUser.getStripeCustomerId()!=null) {
                        retrieveStripeCustomerSources(currentUser.getStripeCustomerId());
                        addPaymentMethodTV.setText(getResources().getString(R.string.add_payment_method));
                        addPaymentMethodTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();
                                Intent createIntent = new Intent(PaymentPreferencesActivity.this,CreateStripeCustomerActivity.class);
                                HashMap<String, Object> customerData = new HashMap<>();
                                customerData.put("customerId", currentUser.getStripeCustomerId());
                                createIntent.putExtra("customerData",customerData);
                                startActivityForResult(createIntent, 1);
                            }
                        });
                    } else {
                        addPaymentMethodTV.setText(getResources().getString(R.string.add_payment_method));
                        // If no stripe account exist, show add payout method text
                        addPaymentMethodTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();
                                Intent createIntent = new Intent(PaymentPreferencesActivity.this,CreateStripeCustomerActivity.class);
                                startActivityForResult(createIntent, 1);
                            }
                        });
                        progressBar.setVisibility(View.GONE);
                        addPaymentMethodTV.setVisibility(View.VISIBLE);

                    }

                } else {
                    addPaymentMethodTV.setText(getResources().getString(R.string.add_payment_method));
                    // If no stripe account exist, show add payout method text
                    addPaymentMethodTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            Intent createIntent = new Intent(PaymentPreferencesActivity.this,CreateStripeCustomerActivity.class);
                            startActivityForResult(createIntent, 1);
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                    addPaymentMethodTV.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, PaymentPreferencesActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    private void retrieveStripeCustomerSources(String customerID) {
        // Retrieve Stripe Account
        retrieveCustomerPaymentMethods(customerID).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
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
                    addPaymentMethodTV.setVisibility(View.VISIBLE);
                    // [START_EXCLUDE]
                    Log.w(TAG, "retrieve:onFailure", e);
                    showSnackbar(getString(R.string.bad_internet));
                    return;
                    // [END_EXCLUDE]
                }
                // If successful, extract
                HashMap<String, Object> result = task.getResult();
                if (result.get("resultType").toString().equals("paymentMethods")) {
                    HashMap<String, Object> paymentMethods = (HashMap<String, Object>) result.get("paymentMethods");
                    ArrayList<HashMap<String,Object>> paymentMethodDataList = (ArrayList<HashMap<String,Object>>) paymentMethods.get("data");
                    listPaymentMethodsRV.setLayoutManager(new LinearLayoutManager(PaymentPreferencesActivity.this));
                    listPaymentMethodsAdapter = new ListPaymentMethodsAdapter(paymentMethodDataList, PaymentPreferencesActivity.this, currentUser.getStripeDefaultPaymentMethod(), new OnPaymentMethodClickedListener() {
                        @Override
                        public void OnPaymentMethodClicked(String paymentMethodId, String cardBrand, String last4, Boolean isDefault) {
                            UpdateStripeSourceFragment updateStripeSourceFragment = UpdateStripeSourceFragment.newInstance(currentUser.getStripeCustomerId(), paymentMethodId, cardBrand, last4, isDefault);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                            transaction.add(R.id.container_update_fragment, updateStripeSourceFragment, "updateSource").addToBackStack(null);
                            transaction.commit();
                        }
                    });
                    listPaymentMethodsRV.setAdapter(listPaymentMethodsAdapter);
                    listPaymentMethodsRV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            //At this point the layout is complete and the
                            //dimensions of recyclerView and any child views are known.
                            addPaymentMethodTV.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            listPaymentMethodsRV.setVisibility(View.VISIBLE);
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

    // Function retrieveCustomerPaymentMethods
    private Task<HashMap<String, Object>> retrieveCustomerPaymentMethods(String customerID) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("retrieveCustomerPaymentMethods")
                .call(customerID)
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

    @Override
    public void OnStripeCustomerUpdated() {
        Intent refresh = new Intent(this, PaymentPreferencesActivity.class);
        startActivity(refresh);
        this.finish();
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
                        Intent welcomeIntent = new Intent(PaymentPreferencesActivity.this,WelcomeActivity.class);
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
