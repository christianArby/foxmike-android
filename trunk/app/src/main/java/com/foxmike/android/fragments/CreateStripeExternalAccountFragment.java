package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class CreateStripeExternalAccountFragment extends Fragment {

    private FirebaseFunctions mFunctions;
    private View mainView;
    private HashMap<String, Object> accountData;
    private Button createAccountBtn;
    private Button addPayoutMethodLaterBtn;
    private EditText ibanET;
    private ProgressBar progressBar;

    private OnStripeExternalAccountCreatedListener onStripeExternalAccountCreatedListener;

    public CreateStripeExternalAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data sent from previous activity
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            if(bundle.getSerializable("accountData") != null)
                accountData = (HashMap<String, Object>)bundle.getSerializable("accountData");
        }
    }

    public static CreateStripeExternalAccountFragment newInstance() {

        Bundle args = new Bundle();

        CreateStripeExternalAccountFragment fragment = new CreateStripeExternalAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_stripe_external_account, container, false);

        mainView = view.findViewById(R.id.mainView);
        createAccountBtn = view.findViewById(R.id.createStripeAccountBtn);
        addPayoutMethodLaterBtn = view.findViewById(R.id.addPayoutMethodLaterBtn);
        ibanET = view.findViewById(R.id.ibanET);
        progressBar = view.findViewById(R.id.progressBar_cyclic);

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
                onStripeExternalAccountCreatedListener.OnStripeExternalAccountCreated();
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                myProgressBar.startProgressBar();

                //String iban = ibanET.getText().toString().trim().replaceAll("\\s+","");

                // TODO REMOVE TEST IBAN
                String iban = "DE89370400440532013000";

                BankAccount bankAccount = new BankAccount(iban,"de","eur","");
                Stripe stripe = new Stripe(getContext(), "pk_test_6IcNIdHpN4LegxE3t8KzvmHx");

                // Create Bank Account token
                stripe.createBankAccountToken(bankAccount, new TokenCallback() {
                    @Override
                    public void onError(Exception error) {
                        // Show localized error message
                        Toast.makeText(getContext(),
                                error.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    @Override
                    public void onSuccess(Token token) {
                        accountData.put("account_token", token.getId());
                        // If successfully created Bank Account create Stripe Account with all the collected info
                        createStripeAccount(accountData).addOnCompleteListener(new OnCompleteListener<String>() {
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
                                    Log.w(TAG, "create:onFailure", e);
                                    showSnackbar("An error occurred.");
                                    return;
                                    // [END_EXCLUDE]
                                }

                                // Show the string passed from the Firebase server if task/function call on server is successful
                                String result = task.getResult();
                                if (result.equals("success")) {
                                    myProgressBar.stopProgressBar();
                                    onStripeExternalAccountCreatedListener.OnStripeExternalAccountCreated();
                                } else {
                                    myProgressBar.stopProgressBar();
                                    showSnackbar("An error occurred:" + " " + result);
                                }
                            }
                        });


                    }
                });

            }
        });

        return view;
    }

    // Function createStripeAccount
    private Task<String> createStripeAccount(Map<String, Object> accountData) {

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStripeExternalAccountCreatedListener) {
            onStripeExternalAccountCreatedListener = (OnStripeExternalAccountCreatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStripeExternalAccountCreatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStripeExternalAccountCreatedListener = null;
    }

    public interface OnStripeExternalAccountCreatedListener {
        void OnStripeExternalAccountCreated();
    }
}
