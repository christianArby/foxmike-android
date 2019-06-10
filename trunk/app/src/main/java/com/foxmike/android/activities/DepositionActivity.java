package com.foxmike.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.fragments.CreateTrainerDepositionFragment;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.stripe.android.model.PaymentIntent;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private boolean hasUpcomingSessions;
    private HashMap<String, Boolean> adsComingHashMap = new HashMap<>();

    public void setPaymentMethod(HashMap paymentMethodMap) { paymentMethodSubject.onNext(paymentMethodMap);     }
    public HashMap  getPaymentMethod()          { return paymentMethodSubject.getValue(); }

    @BindView(R.id.depositionAmount)
    TextView depostionAmount;
    @BindView(R.id.depositionDate) TextView depositionDate;
    @BindView(R.id.claimBtn) AppCompatButton claimBtn;
    @BindView(R.id.dotProgressBarContainer)
    FrameLayout dotProgressBarContainer;
    @BindView(R.id.depositionText) TextView depositionText;
    @BindView(R.id.loadingView) FrameLayout loadingView;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposition);
        ButterKnife.bind(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        depositionText.setMovementMethod(LinkMovementMethod.getInstance());



        fragmentManager = getSupportFragmentManager();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        myProgressBar = new MyProgressBar(progressBar, this);

        myProgressBar.startProgressBar();

        mainView = findViewById(R.id.mainView);

        mFunctions = FirebaseFunctions.getInstance();

        ArrayList<Task<?>> asyncTasks = new ArrayList<>();

        TaskCompletionSource<Boolean> upcomingSessionsSource = new TaskCompletionSource<>();
        Task upcomingSessionsTask = upcomingSessionsSource.getTask();
        asyncTasks.add(upcomingSessionsTask);

        TaskCompletionSource<Boolean> userSource = new TaskCompletionSource<>();
        Task userTask = userSource.getTask();
        asyncTasks.add(userTask);

        Long twentyFourHoursAgoTimestamp = new DateTime(System.currentTimeMillis()).minusHours(24).getMillis();
        // Create query to get all the advertisement keys from the current mSession
        Query keyQuery = FirebaseDatabase.getInstance().getReference().child("advertisementHosts").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByValue().startAt(twentyFourHoursAgoTimestamp);

        FirebaseDatabaseViewModel sessionsComingViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> sessionsComingLiveData = sessionsComingViewModel.getDataSnapshotLiveData(keyQuery);
        sessionsComingLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        TaskCompletionSource<Boolean> adSource = new TaskCompletionSource<>();
                        Task adTask = adSource.getTask();
                        asyncTasks.add(adTask);

                        FirebaseDatabaseViewModel adViewModel = ViewModelProviders.of(DepositionActivity.this).get(FirebaseDatabaseViewModel.class);
                        LiveData<DataSnapshot> adLiveData = adViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("advertisements").child(snapshot.getKey()).child("status"));
                        adLiveData.observe(DepositionActivity.this, new Observer<DataSnapshot>() {
                            @Override
                            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue().toString().equals("cancelled")) {
                                    adsComingHashMap.put(dataSnapshot.getKey(), false);
                                } else {
                                    adsComingHashMap.put(dataSnapshot.getKey(), true);
                                }
                                if (adsComingHashMap.containsValue(true)) {
                                    hasUpcomingSessions = true;
                                }
                                adSource.trySetResult(true);

                            }
                        });

                    }
                }
                upcomingSessionsSource.trySetResult(true);
            }
        });

        FirebaseDatabaseViewModel userViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> userLiveData = userViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        userLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                userSource.trySetResult(true);
            }
        });

        Tasks.whenAll(asyncTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (currentUser.getStripeDepositionPaymentIntentId()==null) {
                    // if not dep
                    if (fragmentManager.findFragmentByTag("createTrainerDepositionFragment")==null) {
                        CreateTrainerDepositionFragment createTrainerDepositionFragment = CreateTrainerDepositionFragment.newInstance("foxmike://deposition");
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.create_trainer_fragments_container, createTrainerDepositionFragment, "createTrainerDepositionFragment");
                        transaction.commitNow();
                        loadingView.setVisibility(View.GONE);
                    }

                } else {
                    loadingView.setVisibility(View.GONE);
                    retrievePaymentIntentAndChargeId(currentUser.getStripeDepositionPaymentIntentId()).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                            // If not succesful, show error and return from function, will trigger if account ID does not exist
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                // [START_EXCLUDE]
                                Log.w(TAG, "retrieve:onFailure", e);
                                showSnackbar(getString(R.string.bad_internet));
                                return;
                                // [END_EXCLUDE]
                            }
                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("resultType").toString().equals("paymentIntent")) {
                                Gson gson = new Gson();
                                String json = gson.toJson(result.get("paymentIntent"));
                                PaymentIntent paymentIntent = PaymentIntent.fromString(json);
                                depostionAmount.setText(getString(R.string.amount) + paymentIntent.getAmount()/100 + " " + paymentIntent.getCurrency());
                                depositionDate.setText(getString(R.string.date_colon) + TextTimestamp.textSDF(paymentIntent.getCreated()*1000));
                                dotProgressBarContainer.setVisibility(View.GONE);
                                claimBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (hasUpcomingSessions) {
                                            alertDialogOk(getResources().getString(R.string.upcoming_sessions_claim_dep_title), getResources().getString(R.string.claim_not_possible_upcoming_sessions), false);
                                            return;
                                        }

                                        alertDialogPositiveOrNegative(getResources().getString(R.string.claim_deposition), getResources().getString(R.string.deposition_withdraw_question), getResources().getString(R.string.claim_deposition), getResources().getString(R.string.cancel), new OnPositiveOrNegativeButtonPressedListener() {
                                            @Override
                                            public void OnPositivePressed() {
                                                myProgressBar.startProgressBar();

                                                HashMap<String, Object> refundMap = new HashMap<>();
                                                refundMap.put("customerFirebaseId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                refundMap.put("chargeId", result.get("chargeId"));

                                                refundDeposition(refundMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                                        if (!task.isSuccessful()) {
                                                            Exception e = task.getException();
                                                            // [START_EXCLUDE]
                                                            Log.w(TAG, "retrieve:onFailure", e);
                                                            showSnackbar(getString(R.string.bad_internet));
                                                            return;
                                                            // [END_EXCLUDE]
                                                        }
                                                        // If successful, extract
                                                        HashMap<String, Object> result = task.getResult();
                                                        if (result.get("resultType").toString().equals("refund")) {
                                                            Map<String,Object> refund = (Map) result.get("refund");
                                                            int amount = (int) refund.get("amount");
                                                            float sweAmount = amount;
                                                            String refundAmount = String.format(Locale.FRANCE,"%.2f", sweAmount/100);
                                                            String currency = (String) refund.get("currency");
                                                            myProgressBar.stopProgressBar();
                                                            alertDialogOk(getResources().getString(R.string.deposition_refunded_title), getResources().getString(R.string.your_deposition_text_1) + " " + refundAmount + " " + currency + " " + getResources().getString(R.string.your_deposition_text_2),true);
                                                        } else {
                                                            HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                                            showSnackbar(error.get("message").toString());
                                                        }
                                                    }
                                                });
                                            }
                                            @Override
                                            public void OnNegativePressed() {
                                            }
                                        });
                                    }
                                });
                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                showSnackbar(error.get("message").toString());
                            }
                        }
                    });
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
    public void OnCreateTrainerDeposition(boolean deposit) {
        if (deposit) {
            for (Fragment fragment:getSupportFragmentManager().getFragments()) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            recreate();
        } else {
            setResult(RESULT_OK, null);
            finish();
        }

    }

    @Override
    public void OnCreateTrainerDepositionNotPossible() {
        alertDialogOk(getResources().getString(R.string.claim_deposit_not_possible_title), getResources().getString(R.string.claim_deposit_not_possible_text), true);
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

    private Task<HashMap<String, Object>> retrievePaymentIntentAndChargeId(String paymentIntentId) {
        return mFunctions
                .getHttpsCallable("retrievePaymentIntentAndChargeId")
                .call(paymentIntentId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private Task<HashMap<String, Object>> refundDeposition(HashMap<String, Object> depositionRefundMap) {
        return mFunctions
                .getHttpsCallable("refundDeposition")
                .call(depositionRefundMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(RESULT_OK, null);
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setResult(RESULT_OK, null);
                finish();
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

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }
}
