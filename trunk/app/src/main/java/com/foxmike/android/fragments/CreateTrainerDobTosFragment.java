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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateTrainerDobTosFragment extends Fragment {

    public static final String TAG = CreateTrainerDobTosFragment.class.getSimpleName();

    private FirebaseFunctions mFunctions;
    private View view;
    private HashMap<String, Object> accountDataDoB = new HashMap<>();
    private Button createAccountBtn;
    private TextView agreeServiceAgreementTV;
    private CheckBox TOSCheckBox;
    private TextInputLayout dobTIL;
    private TextInputEditText dobET;
    private TextInputLayout TOSTIL;
    private ProgressBar progressBar;
    private boolean infoIsValid;
    private InputMethodManager imm;

    private OnCreateTrainerDobTosListener onCreateTrainerDobTosListener;


    public CreateTrainerDobTosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data sent from previous activity
        if (getArguments() != null) {
        }
    }

    public static CreateTrainerDobTosFragment newInstance() {

        Bundle args = new Bundle();

        CreateTrainerDobTosFragment fragment = new CreateTrainerDobTosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_trainer_dob_tos, container, false);

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

                if (!TOSCheckBox.isChecked()) {
                    TOSTIL.setError(getString(R.string.you_must_agree_to_above));
                    infoIsValid = false;
                    return;
                }

                if (infoIsValid) {

                    int dobYear = Integer.parseInt(dob.substring(0,4));
                    int dobMonth = Integer.parseInt(dob.substring(5,7));
                    int dobDay = Integer.parseInt(dob.substring(8,10));

                    accountDataDoB.put("dobYear",dobYear);
                    accountDataDoB.put("dobMonth",dobMonth);
                    accountDataDoB.put("dobDay",dobDay);

                    final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                    myProgressBar.startProgressBar();

                    onCreateTrainerDobTosListener.onCreateTrainerDobTos(accountDataDoB);

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
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
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
        if (context instanceof OnCreateTrainerDobTosListener) {
            onCreateTrainerDobTosListener = (OnCreateTrainerDobTosListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerDobTosListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerDobTosListener = null;
    }

    public interface OnCreateTrainerDobTosListener {
        void onCreateTrainerDobTos(HashMap<String, Object> accountDataDoB);
    }

}
