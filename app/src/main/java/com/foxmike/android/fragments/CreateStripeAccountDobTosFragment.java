package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateStripeAccountDobTosFragment extends Fragment {

    private FirebaseFunctions mFunctions;
    private View view;
    private HashMap<String, Object> accountData;
    private Button createAccountBtn;
    private TextView agreeServiceAgreementTV;
    private CheckBox TOSCheckBox;
    private TextInputLayout dobTIL;
    private TextInputEditText dobET;
    private TextInputLayout TOSTIL;
    private ProgressBar progressBar;
    private boolean infoIsValid;

    private OnStripeAccountCreatedListener onStripeAccountCreatedListener;


    public CreateStripeAccountDobTosFragment() {
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

    public static CreateStripeAccountDobTosFragment newInstance() {

        Bundle args = new Bundle();

        CreateStripeAccountDobTosFragment fragment = new CreateStripeAccountDobTosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_stripe_account_dob_tos, container, false);

        createAccountBtn = view.findViewById(R.id.createStripeAccountBtn);
        agreeServiceAgreementTV = view.findViewById(R.id.agreeTermsPrivacyTV);
        agreeServiceAgreementTV.setMovementMethod(LinkMovementMethod.getInstance());
        TOSCheckBox = view.findViewById(R.id.TOScheckbox);
        TOSTIL = view.findViewById(R.id.TOSTIL);
        dobET = view.findViewById(R.id.dobYearET);
        dobTIL = view.findViewById(R.id.dobYearTIL);
        progressBar = view.findViewById(R.id.progressBar_cyclic);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mFunctions = FirebaseFunctions.getInstance();

        TOSCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                TOSTIL.setError(null);
            }
        });


        dobET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                dobTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                CharSequence charSequence = " ";
                int[] intArray = {4,7};
                // Loop through all characters
                for (int i =0; i < editable.length(); i++) {
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

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String dob = dobET.getText().toString();
                infoIsValid = true;

                if (TextUtils.isEmpty(dob)) {
                    dobTIL.setError(getString(R.string.no_dob_error));
                    infoIsValid = false;
                }

                if (dob.length()!=10) {
                    dobTIL.setError(getString(R.string.no_valid_dob_error));
                    infoIsValid = false;
                }

                if (TOSCheckBox.isChecked() && infoIsValid) {

                    int dobYear = Integer.parseInt(dob.substring(0,4));
                    int dobMonth = Integer.parseInt(dob.substring(5,7));
                    int dobDay = Integer.parseInt(dob.substring(8,10));

                    accountData.put("dobYear",dobYear);
                    accountData.put("dobMonth",dobMonth);
                    accountData.put("dobDay",dobDay);

                    final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                    myProgressBar.startProgressBar();

                    createStripeAccount(accountData).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
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

                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();

                            if (result.get("resultType").toString().equals("accountId")) {

                                myProgressBar.stopProgressBar();
                                onStripeAccountCreatedListener.OnStripeAccountCreated(result.get("accountId").toString());

                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("error");
                                showSnackbar(error.get("message").toString());
                            }
                        }
                    });

                } else {
                    TOSTIL.setError("You must agree to the above in order to register your account.");
                }

            }
        });

        return view;
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
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStripeAccountCreatedListener) {
            onStripeAccountCreatedListener = (OnStripeAccountCreatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStripeAccountCreatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStripeAccountCreatedListener = null;
    }

    public interface OnStripeAccountCreatedListener {
        void OnStripeAccountCreated(String accountId);
    }

}
