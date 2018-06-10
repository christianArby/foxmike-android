package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateStripeExternalAccountFragment extends Fragment {

    private String accountId;
    private String externalAccountId;
    private String last4;
    private String currency;
    private boolean isDefault;
    private TextView payoutHeadingTV;
    private TextView last4TV;
    private TextView isDefaultTV;
    private TextView deleteTV;
    private TextView makeDefaultTV;
    private View view;
    private FirebaseFunctions mFunctions;
    private ProgressBar progressBar;
    private HashMap<String, Object> accountData;

    private OnStripeAccountUpdatedListener onStripeAccountUpdatedListener;


    public UpdateStripeExternalAccountFragment() {
        // Required empty public constructor
    }

    public static UpdateStripeExternalAccountFragment newInstance(String accountId, String externalAccountId, String last4, String currency, Boolean isDefault) {

        Bundle args = new Bundle();

        args.putString("accountId", accountId);
        args.putString("externalAccountId", externalAccountId);
        args.putString("last4", last4);
        args.putString("currency", currency);
        args.putBoolean("isDefault", isDefault);

        UpdateStripeExternalAccountFragment fragment = new UpdateStripeExternalAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_update_stripe_external_account, container, false);

        payoutHeadingTV = view.findViewById(R.id.payoutPreferencesHeading);
        last4TV = view.findViewById(R.id.last4digits);
        isDefaultTV = view.findViewById(R.id.payoutMethodStandard);
        deleteTV = view.findViewById(R.id.deletePayoutMethodTV);
        makeDefaultTV = view.findViewById(R.id.setAsDefaultTV);
        progressBar = view.findViewById(R.id.progressBar_cyclic);


        accountId = getArguments().getString("accountId");
        externalAccountId = getArguments().getString("externalAccountId");
        last4 = getArguments().getString("last4");
        currency = getArguments().getString("currency");
        isDefault = getArguments().getBoolean("isDefault");

        mFunctions = FirebaseFunctions.getInstance();

        accountData = new HashMap<>();

        accountData.put("accountId", accountId);
        accountData.put("externalAccountId", externalAccountId);

        payoutHeadingTV.setText(getResources().getString(R.string.bank_account_text));

        last4TV.setText("IBAN" + " *****" + last4 + " (" + currency.toUpperCase() + ")");

        if (isDefault) {
            isDefaultTV.setText("STANDARD");
        } else {
            isDefaultTV.setVisibility(View.GONE);
        }

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAccount("deleteExternalAccountForAccount");
            }
        });

        makeDefaultTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAccount("setDefaultExternalAccountForAccount");
            }
        });

        return view;
    }

    private void updateAccount(String type) {
        final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
        myProgressBar.startProgressBar();

        updateStripeAccount(accountData, type).addOnCompleteListener(new OnCompleteListener<String>() {
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

                    myProgressBar.stopProgressBar();

                    // [START_EXCLUDE]
                    Log.w(TAG, "deleteStripeExternalAccount:onFailure", e);
                    showSnackbar("An error occurred." + e.getMessage());
                    return;
                    // [END_EXCLUDE]
                }

                // Show the string passed from the Firebase server if task/function call on server is successful
                String result = task.getResult();
                if (result.equals("success")) {
                    myProgressBar.stopProgressBar();
                    onStripeAccountUpdatedListener.OnStripeAccountUpdated();
                } else {
                    myProgressBar.stopProgressBar();
                    showSnackbar("An error occurred:" + " " + result);
                }
            }
        });
    }

    // Function createStripeAccount
    private Task<String> updateStripeAccount(Map<String, Object> accountData, String function) {

        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable(function)
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
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdateStripeExternalAccountFragment.OnStripeAccountUpdatedListener) {
            onStripeAccountUpdatedListener = (UpdateStripeExternalAccountFragment.OnStripeAccountUpdatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStripeAccountUpdatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStripeAccountUpdatedListener = null;
    }

    public interface OnStripeAccountUpdatedListener {
        void OnStripeAccountUpdated();
    }

}
