package com.foxmike.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.activities.CreateTrainerActivity;
import com.foxmike.android.activities.DepositionActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethod;

import org.joda.time.DateTime;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;
import static com.foxmike.android.utils.StaticResources.DEPOSITION_AMOUNT_INTEGERS;


public class CreateTrainerDepositionFragment extends Fragment {

    public static final String TAG = CreateTrainerDepositionFragment.class.getSimpleName();

    private View mainView;
    private DatabaseReference rootDbRef;
    private FirebaseAuth mAuth;
    @BindView(R.id.paymentMethod)
    AppCompatTextView paymentMethodTV;
    @BindView(R.id.addPaymentMethodTV) AppCompatTextView addPaymentMethodTV;
    @BindView(R.id.makeDepositionBtn)
    AppCompatButton makeDepositionBtn;
    @BindView(R.id.addDepostionLaterBtn) AppCompatButton addDepostionLaterBtn;
    @BindView(R.id.dotProgressBarContainer) FrameLayout dotProgressBarContainer;
    private boolean hasPaymentMethod;
    @BindView(R.id.depProgressBar) ProgressBar depProgressBar;
    @BindView(R.id.depExplanation)
    TextView depExplanation;
    private OnCreateTrainerDepositionListener onCreateTrainerDepositionListener;
    private MyProgressBar myProgressBar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private PaymentMethod paymentMethod;
    private String returnURL;
    private boolean paymentMethodLoaded;
    private User currentUser;
    private boolean currentUserLoaded;
    private boolean UIset;


    //private OnDepositionFragmentInteractionListener onDepositionFragmentInteractionListener;

    public CreateTrainerDepositionFragment() {
        // Required empty public constructor
    }
    public static CreateTrainerDepositionFragment newInstance(String returnURL) {
        CreateTrainerDepositionFragment fragment = new CreateTrainerDepositionFragment();
        Bundle args = new Bundle();
        args.putString("returnURL", returnURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            returnURL = getArguments().getString("returnURL");
        }
        getDefaultPaymentMethod();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        getDefaultPaymentMethod();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_create_trainer_deposition, container, false);
        ButterKnife.bind(this, mainView);

        // Setup toolbar
        Toolbar toolbar = mainView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        myProgressBar = new MyProgressBar(depProgressBar, getActivity());

        depExplanation.setMovementMethod(LinkMovementMethod.getInstance());




        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseDatabaseViewModel userViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> userLiveData = userViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        userLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                currentUserLoaded = true;
                setupUI();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myProgressBar = null;
    }

    private void getDefaultPaymentMethod() {
        try {
            CreateTrainerActivity createTrainerActivity = (CreateTrainerActivity) getActivity();
            Disposable subscription = createTrainerActivity.paymentMethodSubject.subscribe(new Consumer<HashMap>() {
                @Override
                public void accept(HashMap hashMap) throws Exception {
                    if (hashMap.get("card")!=null) {
                        Gson gson = new Gson();
                        String json = gson.toJson(hashMap);
                        paymentMethod = PaymentMethod.fromString(json);
                        paymentMethodLoaded = true;
                        hasPaymentMethod = true;
                        UIset = false;
                        setupUI();
                    } else {
                        paymentMethodLoaded = true;
                        hasPaymentMethod = false;
                        UIset = false;
                        setupUI();
                    }
                }
            });
            return;
        } catch (RuntimeException e){
            try {
                DepositionActivity depositionActivity = (DepositionActivity) getActivity();
                Disposable subscription = depositionActivity.paymentMethodSubject.subscribe(new Consumer<HashMap>() {
                    @Override
                    public void accept(HashMap hashMap) throws Exception {
                        if (hashMap.get("card")!=null) {
                            Gson gson = new Gson();
                            String json = gson.toJson(hashMap);
                            paymentMethod = PaymentMethod.fromString(json);
                            paymentMethodLoaded = true;
                            hasPaymentMethod = true;
                            UIset = false;
                            setupUI();
                        } else {
                            paymentMethodLoaded = true;
                            hasPaymentMethod = false;
                            UIset = false;
                            setupUI();
                        }
                    }
                });
                return;
            } catch (RuntimeException e2){
                paymentMethodLoaded = true;
                hasPaymentMethod = false;
                UIset = false;
                setupUI();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
    }

    private void setupUI() {

        if (isAdded() && paymentMethodLoaded && currentUserLoaded && !UIset) {
            UIset = true;
            if (hasPaymentMethod) {
                paymentMethodTV.setVisibility(View.VISIBLE);
                addPaymentMethodTV.setVisibility(View.GONE);
                dotProgressBarContainer.setVisibility(View.GONE);

                makeDepositionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));

                String last4 = paymentMethod.card.last4;
                paymentMethodTV.setText("**** " + last4);
                String cardBrand = paymentMethod.card.brand;
                int resourceId = BRAND_CARD_RESOURCE_MAP.get(cardBrand);
                paymentMethodTV.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);

                makeDepositionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long currentTimestamp = System.currentTimeMillis();

                        DateTime currentDateTime = new DateTime(currentTimestamp);
                        DateTime lastRefund = new DateTime(currentUser.getDepositRefunded());

                        if (currentDateTime.minusDays(30).isBefore(lastRefund)) {
                            onCreateTrainerDepositionListener.OnCreateTrainerDepositionNotPossible();
                            return;
                        }

                        myProgressBar.startProgressBar();
                        // MAKE DEPOSITION
                        HashMap<String, Object> depositionMap = new HashMap<>();
                        // TODO Make country in future paymentMethod.card.country
                        depositionMap.put("amount", DEPOSITION_AMOUNT_INTEGERS.get("SE"));
                        depositionMap.put("currency", "sek");
                        depositionMap.put("customerFirebaseId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        depositionMap.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        depositionMap.put("returnURL", returnURL);

                        submitDeposition(depositionMap).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                                // If error
                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    myProgressBar.startProgressBar();
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
                                        Uri redirectUrl = Uri.parse((String) result.get("redirectUrl"));
                                        if (redirectUrl != null) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, redirectUrl));
                                        }
                                        return;
                                    } else if (status.equals(PaymentIntent.Status.Succeeded.toString())){
                                        myProgressBar.stopProgressBar();
                                        Bundle bundle = new Bundle();
                                        bundle.putDouble("deposition_price", (double) DEPOSITION_AMOUNT_INTEGERS.get("SE"));
                                        bundle.putString("deposition_currency", "SEK");
                                        bundle.putString("trainer_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                        bundle.putString("trainer_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mFirebaseAnalytics.logEvent("deposition", bundle);

                                        onCreateTrainerDepositionListener.OnCreateTrainerDeposition(true);
                                    } else {
                                        myProgressBar.stopProgressBar();
                                        showSnackbar("Payment cancelled");
                                    }
                                } else {
                                    // If error, show error in snackbar
                                    HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                    showSnackbar(error.get("message").toString());
                                }

                            }
                        });
                    }
                });

            } else {
                paymentMethodTV.setVisibility(View.GONE);
                addPaymentMethodTV.setVisibility(View.VISIBLE);
                dotProgressBarContainer.setVisibility(View.GONE);

                makeDepositionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_gray));

                makeDepositionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ADD PAYMENT METHOD
                        Intent paymentPreferencesIntent = new Intent(getActivity().getApplicationContext(),PaymentPreferencesActivity.class);
                        startActivity(paymentPreferencesIntent);
                    }
                });

                addPaymentMethodTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ADD PAYMENT METHOD
                        Intent paymentPreferencesIntent = new Intent(getActivity().getApplicationContext(),PaymentPreferencesActivity.class);
                        startActivity(paymentPreferencesIntent);
                    }
                });

            }

            addDepostionLaterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // MOVE ON
                    onCreateTrainerDepositionListener.OnCreateTrainerDeposition(false);
                }
            });

        }



    }

    // Function bookSessionWithSCA
    private Task<HashMap<String, Object>> submitDeposition(HashMap<String, Object> depositionMap) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("submitDeposition")
                .call(depositionMap)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerDepositionListener) {
            onCreateTrainerDepositionListener = (OnCreateTrainerDepositionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerDepositionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerDepositionListener = null;
    }
    public interface OnCreateTrainerDepositionListener {

        void OnCreateTrainerDeposition(boolean deposit);
        void OnCreateTrainerDepositionNotPossible();
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }
}
