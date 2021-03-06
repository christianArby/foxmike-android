package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;


public class CreateTrainerExternalAccountFragment extends Fragment {

    public static final String TAG = CreateTrainerExternalAccountFragment.class.getSimpleName();

    private FirebaseFunctions mFunctions;
    private View mainView;
    private HashMap<String, Object> accountData = new HashMap<>();
    private Button createAccountBtn;
    private Button addPayoutMethodLaterBtn;
    private EditText ibanET;
    private ProgressBar progressBar;
    private long mLastClickTime = 0;
    private InputMethodManager imm;
    private String stripeAccountId;
    private boolean update;

    private OnCreateTrainerExternalAccountListener onCreateTrainerExternalAccountListener;

    public CreateTrainerExternalAccountFragment() {
        // Required empty public constructor
    }


    public static CreateTrainerExternalAccountFragment newInstance(String stripeAccountId, Boolean update) {

        Bundle args = new Bundle();
        args.putString("stripeAccountId", stripeAccountId);
        args.putBoolean("update", update);
        CreateTrainerExternalAccountFragment fragment = new CreateTrainerExternalAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data sent from previous activity
        if (getArguments() != null) {
            stripeAccountId = getArguments().getString("stripeAccountId");
            update = getArguments().getBoolean("update");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_trainer_external_account, container, false);

        mainView = view.findViewById(R.id.mainView);
        createAccountBtn = view.findViewById(R.id.createStripeAccountBtn);
        addPayoutMethodLaterBtn = view.findViewById(R.id.addPayoutMethodLaterBtn);
        ibanET = view.findViewById(R.id.ibanET);
        progressBar = view.findViewById(R.id.progressBar_cyclic);

        if (update) {
            addPayoutMethodLaterBtn.setVisibility(View.GONE);
            // Setup toolbar
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }



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

        addPayoutMethodLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onCreateTrainerExternalAccountListener.OnCreateTrainerExternalAccountLater();
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                myProgressBar.startProgressBar();

                String iban;
                BankAccount bankAccount;

                // TODO REMOVE TEST IBAN
                if (getString(R.string.release_type).equals("debug")) {
                    iban = "DE89370400440532013000";
                    bankAccount = new BankAccount(iban,"de","eur","");
                } else {
                    iban = ibanET.getText().toString().trim().replaceAll("\\s+","");
                    if (iban.length()!=24) {
                        ibanET.setError("IBAN number is not valid.");
                        return;
                    }
                    bankAccount = new BankAccount(iban,"se","sek","");

                }

                Stripe stripe = new Stripe(getActivity().getApplicationContext(), getString(R.string.stripe_publishable_key));
                // Create Bank Account token
                stripe.createBankAccountToken(bankAccount, new TokenCallback() {
                    @Override
                    public void onError(Exception error) {
                        // Show localized error message
                        Toast.makeText(getActivity().getApplicationContext(),
                                error.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    @Override
                    public void onSuccess(Token token) {
                        accountData.put("stripeAccountId", stripeAccountId);
                        accountData.put("account_token", token.getId());
                        accountData.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        // If successfully created Bank Account create Stripe Account with all the collected info
                        createExternalAccount(accountData).addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (isAdded()) {
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
                                        myProgressBar.stopProgressBar();
                                        onCreateTrainerExternalAccountListener.OnCreateTrainerExternalAccountFailed();
                                        Log.w(TAG, "create:onFailure", e);
                                        showSnackbar(e.getMessage());
                                        return;
                                        // [END_EXCLUDE]
                                    }

                                    // Show the string passed from the Firebase server if task/function call on server is successful
                                    String result = task.getResult();
                                    if (result.equals("success")) {
                                        myProgressBar.stopProgressBar();
                                        onCreateTrainerExternalAccountListener.OnCreateTrainerExternalAccountCreated();

                                    } else {
                                        myProgressBar.stopProgressBar();
                                        onCreateTrainerExternalAccountListener.OnCreateTrainerExternalAccountFailed();
                                        showSnackbar("An error occurred:" + " " + result);
                                    }
                                }

                            }
                        });


                    }
                });

            }
        });

        return view;
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

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity)getActivity()).setSupportActionBar(null);
        hideKeyboard();
        imm = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerExternalAccountListener) {
            onCreateTrainerExternalAccountListener = (OnCreateTrainerExternalAccountListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerExternalAccountListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerExternalAccountListener = null;
    }

    public interface OnCreateTrainerExternalAccountListener {
        void OnCreateTrainerExternalAccountCreated();
        void OnCreateTrainerExternalAccountFailed();
        void OnCreateTrainerExternalAccountLater();
    }
}
