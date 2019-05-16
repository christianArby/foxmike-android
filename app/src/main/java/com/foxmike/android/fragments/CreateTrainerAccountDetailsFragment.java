package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.foxmike.android.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;


public class CreateTrainerAccountDetailsFragment extends Fragment {

    private OnCreateTrainerAccountDetailsListener onCreateTrainerAccountDetailsListener;

    private TextInputEditText firstNameET;
    private TextInputEditText lastNameET;
    private TextInputEditText addressStreetET;
    private TextInputEditText addressPostalCodeET;
    private TextInputEditText addressCityET;
    private TextInputLayout firstNameTIL;
    private TextInputLayout lastNameTIL;
    private TextInputLayout addressStreetTIL;
    private TextInputLayout addressPostalCodeTIL;
    private TextInputLayout addressCityTIL;
    private Button createStripeAccountBtn;
    private HashMap<String, Object> accountData;
    private long mLastClickTime = 0;
    private String firstName;
    private String lastName;

    public CreateTrainerAccountDetailsFragment() {
        // Required empty public constructor
    }



    public static CreateTrainerAccountDetailsFragment newInstance(String firstName, String lastName) {

        Bundle args = new Bundle();
        args.putString("firstName", firstName);
        args.putString("lastName", lastName);
        CreateTrainerAccountDetailsFragment fragment = new CreateTrainerAccountDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data sent from previous activity
        if (getArguments() != null) {
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_trainer_account_details, container, false);

        firstNameET = view.findViewById(R.id.firstNameET);
        lastNameET = view.findViewById(R.id.lastNameET);
        addressStreetET = view.findViewById(R.id.addressLine1ET);
        addressPostalCodeET = view.findViewById(R.id.postalCodeET);
        addressCityET = view.findViewById(R.id.cityET);
        firstNameTIL = view.findViewById(R.id.firstNameTIL);
        lastNameTIL = view.findViewById(R.id.lastNameTIL);
        addressStreetTIL = view.findViewById(R.id.addressLine1TIL);
        addressPostalCodeTIL = view.findViewById(R.id.postalCodeTIL);
        addressCityTIL = view.findViewById(R.id.cityTIL);
        createStripeAccountBtn = view.findViewById(R.id.createStripeAccountBtn);

        firstNameET.setText(firstName);
        lastNameET.setText(lastName);

        firstNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                firstNameTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lastNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastNameTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressStreetET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addressStreetTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressPostalCodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                addressPostalCodeTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                CharSequence charSequence = " ";
                int spacePosition = 3;
                // Loop through all characters
                for (int i =0; i < editable.length(); i++) {
                    boolean isSpacePosition = false;
                    // Check if position is a space position
                    if (i==spacePosition) {
                        isSpacePosition = true;
                    }
                    // If it is a space position and does not contain a space position insert a space position
                    if (isSpacePosition) {
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



        addressCityET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addressCityTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        createStripeAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                String firstName = firstNameET.getText().toString();
                String lastName = lastNameET.getText().toString();
                String addressStreet = addressStreetET.getText().toString();
                String addressPostalCode = addressPostalCodeET.getText().toString().replaceAll(" ", "");
                String addressCity = addressCityET.getText().toString();

                boolean infoIsValid = true;

                if (TextUtils.isEmpty(firstName)) {
                    firstNameTIL.setError(getString(R.string.no_first_name_error));
                    infoIsValid = false;
                }
                if (TextUtils.isEmpty(lastName)) {
                    lastNameTIL.setError(getString(R.string.no_last_name_error));
                    infoIsValid = false;
                }
                if (TextUtils.isEmpty(addressStreet)) {
                    addressStreetTIL.setError(getString(R.string.no_street_address_error));
                    infoIsValid = false;
                }
                if (TextUtils.isEmpty(addressPostalCode)) {
                    addressPostalCodeTIL.setError(getString(R.string.no_postal_code_error));
                    infoIsValid = false;
                }
                if (addressPostalCode.length()!=5) {
                    addressPostalCodeTIL.setError(getString(R.string.no_valid_postal_code_error));
                    infoIsValid = false;
                }
                if (TextUtils.isEmpty(addressCity)) {
                    addressCityTIL.setError(getString(R.string.no_city_address_error));
                    infoIsValid = false;
                }

                if (infoIsValid) {

                    accountData = new HashMap<>();
                    accountData.put("firstName", firstName);
                    accountData.put("lastName", lastName);
                    accountData.put("addressStreet", addressStreet);
                    accountData.put("addressPostalCode", addressPostalCode);
                    accountData.put("addressCity", addressCity);


                    accountData.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    accountData.put("accountType", "custom");
                    accountData.put("country","SE");
                    accountData.put("legalEntityType", "individual");

                    onCreateTrainerAccountDetailsListener.onCreateTrainerAccountDetails(accountData);

                }
            }
        });
        
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerAccountDetailsListener) {
            onCreateTrainerAccountDetailsListener = (OnCreateTrainerAccountDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerAccountDetailsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerAccountDetailsListener = null;
    }

    public interface OnCreateTrainerAccountDetailsListener {

        void onCreateTrainerAccountDetails(HashMap accountData);
    }
}
