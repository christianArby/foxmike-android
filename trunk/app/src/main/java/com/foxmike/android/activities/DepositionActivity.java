package com.foxmike.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.fragments.CreateTrainerDepositionFragment;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.model.PaymentIntent;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.BehaviorSubject;

import static android.content.ContentValues.TAG;
import static com.foxmike.android.utils.StaticResources.DEPOSITION_AMOUNT_INTEGERS;

public class DepositionActivity extends AppCompatActivity implements CreateTrainerDepositionFragment.OnCreateTrainerDepositionListener {

    private AppCompatButton nextBtn;
    private String country;
    private HashMap<String, Object> accountData = new HashMap<>();
    private User currentUser;
    private boolean stripeAccountCreated;
    // rxJava

    private String stripeCustomerId;
    private FirebaseFunctions mFunctions;
    private View mainView;
    private MyProgressBar myProgressBar;
    private FirebaseAnalytics mFirebaseAnalytics;

    private String stripeDefaultPaymentMethodId;
    // rxJava
    public final BehaviorSubject<HashMap> paymentMethodSubject = BehaviorSubject.create();
    private FragmentManager fragmentManager;

    public void setPaymentMethod(HashMap paymentMethodMap) { paymentMethodSubject.onNext(paymentMethodMap);     }
    public HashMap  getPaymentMethod()          { return paymentMethodSubject.getValue(); }

    @BindView(R.id.claimBtn) AppCompatButton claimBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposition);
        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        myProgressBar = new MyProgressBar(progressBar, this);

        myProgressBar.startProgressBar();

        mainView = findViewById(R.id.mainView);

        setPaymentMethod(new HashMap());

        mFunctions = FirebaseFunctions.getInstance();



        FirebaseDatabaseViewModel userViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> sessionsComingLiveData = userViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        sessionsComingLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);

                if (currentUser.getStripeDepositionPaymentIntentId()==null) {
                    // if not dep
                    if (fragmentManager.findFragmentByTag("createTrainerDepositionFragment")==null) {
                        CreateTrainerDepositionFragment createTrainerDepositionFragment = CreateTrainerDepositionFragment.newInstance("foxmike://deposition");
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.create_trainer_fragments_container, createTrainerDepositionFragment, "createTrainerDepositionFragment");
                        transaction.commit();
                    }

                } else {

                }



                myProgressBar.stopProgressBar();
            }
        });

        FirebaseDatabaseViewModel stripeDefaultPaymentMethodViewModel = ViewModelProviders.of(DepositionActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDefaultPaymentMethodLiveData = stripeDefaultPaymentMethodViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
        stripeDefaultPaymentMethodLiveData.observe(DepositionActivity.this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    stripeDefaultPaymentMethodId = dataSnapshot.getValue().toString();
                    updateStripeCustomerInfo();
                } else {
                    setPaymentMethod(new HashMap());
                }

            }
        });
    }

    @Override
    public void OnCreateTrainerDeposition() {
        setResult(RESULT_OK, null);
        finish();
    }

    // __________________________ STRIPE BELOW _____________________________


    private void updateStripeCustomerInfo() {

        if (stripeDefaultPaymentMethodId==null) {
            setPaymentMethod(new HashMap());
        }

        retrievePaymentMethod(stripeDefaultPaymentMethodId).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                // If not succesful, show error and return from function, will trigger if account ID does not exist
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    // [START_EXCLUDE]
                    Log.w(TAG, "retrieve:onFailure", e);
                    showSnackbar(getString(R.string.bad_internet));
                    setPaymentMethod(new HashMap());
                    return;
                    // [END_EXCLUDE]
                }
                // If successful, extract
                HashMap<String, Object> result = task.getResult();
                if (result.get("resultType").toString().equals("paymentMethod")) {
                    setPaymentMethod((HashMap) result.get("paymentMethod"));
                } else {
                    setPaymentMethod(new HashMap());
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                    showSnackbar(error.get("message").toString());
                }

            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getData() != null && intent.getData().getQuery() != null) {
            String paymentIntentId = intent.getData().getQueryParameter(
                    "payment_intent");

            myProgressBar.startProgressBar();

            HashMap<String, Object> depositionMap = new HashMap<>();
            depositionMap.put("paymentIntentId", paymentIntentId);
            depositionMap.put("customerFirebaseId", FirebaseAuth.getInstance().getCurrentUser().getUid());

            confirmDepositionPaymentIntent(depositionMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                @Override
                public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                    // If error
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        myProgressBar.stopProgressBar();
                        Log.w(TAG, "retrieve:onFailure", e);
                        showSnackbar(getString(R.string.bad_internet));
                        return;
                    }
                    // If successful the variable "operationResult" will say "success,
                    // If so finish the activity with a result ok so the previous activity knows it's a success
                    HashMap<String, Object> result = task.getResult();

                    if (result.get("resultType").toString().equals("paymentIntentParameters")) {
                        String status = (String) result.get("paymentIntentStatus");
                        if (status.equals(PaymentIntent.Status.RequiresAction.toString())) {
                            myProgressBar.stopProgressBar();
                            showSnackbar(getString(R.string.payment_canelled));
                            return;
                        } else if (status.equals((PaymentIntent.Status.Succeeded.toString()))){
                            myProgressBar.stopProgressBar();
                            Bundle bundle = new Bundle();
                            bundle.putDouble("deposition_price", (double) DEPOSITION_AMOUNT_INTEGERS.get("sek"));
                            bundle.putString("deposition_currency", "SEK");
                            bundle.putString("trainer_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            bundle.putString("trainer_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            mFirebaseAnalytics.logEvent("deposition", bundle);
                            setResult(RESULT_OK, null);
                            finish();
                        } else {
                            myProgressBar.stopProgressBar();
                            showSnackbar(getString(R.string.payment_canelled));
                        }
                    } else {
                        // If error, show error in snackbar
                        myProgressBar.stopProgressBar();
                        HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                        showSnackbar(getString(R.string.payment_canelled));
                    }

                }
            });
        }
    }

    // Function bookSessionWithSCA
    private Task<HashMap<String, Object>> confirmDepositionPaymentIntent(HashMap<String, Object> depostionConfirmationMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("confirmDepositionPaymentIntent")
                .call(depostionConfirmationMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private Task<HashMap<String, Object>> retrievePaymentMethod(String paymentMethodId) {
        return mFunctions
                .getHttpsCallable("retrievePaymentMethod")
                .call(paymentMethodId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
