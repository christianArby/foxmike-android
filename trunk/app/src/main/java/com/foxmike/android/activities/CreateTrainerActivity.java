package com.foxmike.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.fragments.CreateTrainerAboutMeFragment;
import com.foxmike.android.fragments.CreateTrainerAccountDetailsFragment;
import com.foxmike.android.fragments.CreateTrainerCountryFragment;
import com.foxmike.android.fragments.CreateTrainerDepositionFragment;
import com.foxmike.android.fragments.CreateTrainerDobTosFragment;
import com.foxmike.android.fragments.CreateTrainerExternalAccountFragment;
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
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.model.PaymentIntent;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.BehaviorSubject;

import static android.content.ContentValues.TAG;
import static com.foxmike.android.utils.StaticResources.DEPOSITION_AMOUNT_INTEGERS;

public class CreateTrainerActivity extends AppCompatActivity implements
        CreateTrainerCountryFragment.OnCreateTrainerCountryListener,
        CreateTrainerAccountDetailsFragment.OnCreateTrainerAccountDetailsListener,
        CreateTrainerAboutMeFragment.OnCreateTrainerAboutMeListener,
        CreateTrainerDobTosFragment.OnCreateTrainerDobTosListener,
        CreateTrainerExternalAccountFragment.OnCreateTrainerExternalAccountListener,
        CreateTrainerDepositionFragment.OnCreateTrainerDepositionListener{

    private AppCompatButton nextBtn;
    private String country;
    private HashMap<String, Object> accountData = new HashMap<>();
    private User currentUser;
    private boolean stripeAccountCreated;

    private String stripeDefaultPaymentMethodId;
    // rxJava
    public final BehaviorSubject<HashMap> paymentMethodSubject = BehaviorSubject.create();
    public void setPaymentMethod(HashMap paymentMethodMap) { paymentMethodSubject.onNext(paymentMethodMap);     }
    public HashMap  getPaymentMethod()          { return paymentMethodSubject.getValue(); }

    private String stripeCustomerId;
    private FirebaseFunctions mFunctions;
    private View mainView;
    private MyProgressBar myProgressBar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TextView becomeFtText;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trainer);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        becomeFtText = findViewById(R.id.becomeFtText);

        becomeFtText.setMovementMethod(LinkMovementMethod.getInstance());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        myProgressBar = new MyProgressBar(progressBar, this);

        myProgressBar.startProgressBar();

        mainView = findViewById(R.id.mainView);

        setPaymentMethod(new HashMap());

        mFunctions = FirebaseFunctions.getInstance();

        nextBtn = findViewById(R.id.nextBtn);

        FirebaseDatabaseViewModel userViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> sessionsComingLiveData = userViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        sessionsComingLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO NOT IN USE
                        /*CreateTrainerCountryFragment createTrainerCountryFragment = new CreateTrainerCountryFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                        transaction.add(R.id.create_trainer_fragments_container, createTrainerCountryFragment, "createTrainerCountryFragment").addToBackStack("1st");
                        transaction.commit();*/

                        CreateTrainerAccountDetailsFragment createTrainerAccountDetailsFragment = CreateTrainerAccountDetailsFragment.newInstance(currentUser.getFirstName(), currentUser.getLastName());
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                        transaction.add(R.id.create_trainer_fragments_container, createTrainerAccountDetailsFragment, "createTrainerAccountDetailsFragment").addToBackStack("2nd");
                        transaction.commit();
                    }
                });

                myProgressBar.stopProgressBar();
            }
        });

        FirebaseDatabaseViewModel stripeDefaultPaymentMethodViewModel = ViewModelProviders.of(CreateTrainerActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDefaultPaymentMethodLiveData = stripeDefaultPaymentMethodViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDefaultPaymentMethod"));
        stripeDefaultPaymentMethodLiveData.observe(CreateTrainerActivity.this, new Observer<DataSnapshot>() {
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
    public void onCreateTrainerCountry(String country) {
        this.country = country;
        // TODO NOT IN USE
        CreateTrainerAccountDetailsFragment createTrainerAccountDetailsFragment = CreateTrainerAccountDetailsFragment.newInstance(currentUser.getFirstName(), currentUser.getLastName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.add(R.id.create_trainer_fragments_container, createTrainerAccountDetailsFragment, "createTrainerAccountDetailsFragment").addToBackStack("2nd");
        transaction.commit();
    }

    @Override
    public void onCreateTrainerAccountDetails(HashMap accountData) {
        this.accountData = accountData;
        this.accountData.put("country", "SE");
        this.accountData.put("ip", getLocalIpAddress());

        boolean aboutMeTextEmpty = false;
        if (currentUser.getAboutMe()==null) {
            aboutMeTextEmpty = true;
        } else {
            if (currentUser.getAboutMe().length()<1) {
                aboutMeTextEmpty = true;
            }
        }

        if (aboutMeTextEmpty) {
            CreateTrainerAboutMeFragment createTrainerAboutMeFragment = new CreateTrainerAboutMeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
            transaction.add(R.id.create_trainer_fragments_container, createTrainerAboutMeFragment, "createTrainerAboutMeFragment").addToBackStack("3rd");
            transaction.commit();
        } else {

            CreateTrainerDobTosFragment createTrainerDobTosFragment = new CreateTrainerDobTosFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
            transaction.add(R.id.create_trainer_fragments_container, createTrainerDobTosFragment, "createTrainerDobTosFragment").addToBackStack("3rd");
            transaction.commit();

        }


    }

    @Override
    public void onCreateTrainerAboutMe() {

        CreateTrainerDobTosFragment createTrainerDobTosFragment = new CreateTrainerDobTosFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.add(R.id.create_trainer_fragments_container, createTrainerDobTosFragment, "createTrainerDobTosFragment").addToBackStack("4th");
        transaction.commit();
    }

    @Override
    public void onCreateTrainerDobTos(HashMap<String, Object> accountDataDoB) {

        myProgressBar.startProgressBar();

        for (String key: accountDataDoB.keySet()) {
            accountData.put(key, accountDataDoB.get(key));
        }

        createStripeAccount(accountData).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                // If not succesful, show error
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                    }
                    // [START_EXCLUDE]
                    myProgressBar.stopProgressBar();
                    Log.w(TAG, "create:onFailure", e);
                    showSnackbar("An error occurred.");
                    return;
                    // [END_EXCLUDE]
                }
                // If successful, extract
                HashMap<String, Object> result = task.getResult();
                if (result.get("resultType").toString().equals("accountId")) {

                    myProgressBar.stopProgressBar();
                    
                    // ACCOUNT CREATED

                    stripeAccountCreated = true;

                    CreateTrainerExternalAccountFragment createTrainerExternalAccountFragment = CreateTrainerExternalAccountFragment.newInstance(result.get("accountId").toString(), false);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                    transaction.replace(R.id.create_trainer_fragments_container, createTrainerExternalAccountFragment, "finalize");
                    transaction.commit();
                    
                    

                } else {
                    myProgressBar.stopProgressBar();
                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                    showSnackbar(error.get("message").toString());
                }
            }
        });
    }

    @Override
    public void OnCreateTrainerExternalAccountCreated() {

        CreateTrainerDepositionFragment createTrainerDepositionFragment = CreateTrainerDepositionFragment.newInstance("foxmike://create.trainer", false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.replace(R.id.create_trainer_fragments_container, createTrainerDepositionFragment, "createTrainerDepositionFragment");
        transaction.commit();

    }

    @Override
    public void OnCreateTrainerExternalAccountFailed() {

    }

    @Override
    public void OnCreateTrainerExternalAccountLater() {


        CreateTrainerDepositionFragment createTrainerDepositionFragment = CreateTrainerDepositionFragment.newInstance("foxmike://create.trainer", false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.replace(R.id.create_trainer_fragments_container, createTrainerDepositionFragment, "createTrainerDepositionFragment");
        transaction.commit();
        
    }

    @Override
    public void OnCreateTrainerDeposition(boolean deposit) {
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void OnCreateTrainerDepositionNotPossible() {

    }


    // Function createStripeAccount
    private Task<HashMap<String, Object>> createStripeAccount(Map<String, Object> accountData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("create")
                .call(accountData)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    // Function createExternalAccount
    private Task<String> createExternalAccount(Map<String, Object> accountData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("createExternalAccount")
                .call(accountData)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("operationResult");
                    }
                });
    }



    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
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

    private Task<HashMap<String, Object>> retrieveStripeCustomer(String customerID) {
        return mFunctions
                .getHttpsCallable("retrieveCustomer")
                .call(customerID)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
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
