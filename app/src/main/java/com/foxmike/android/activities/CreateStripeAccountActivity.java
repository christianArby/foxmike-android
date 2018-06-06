package com.foxmike.android.activities;

import android.support.v4.app.FragmentManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.foxmike.android.R;
import com.foxmike.android.fragments.FinalizeStripeAccountCreationFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class CreateStripeAccountActivity extends AppCompatActivity implements FinalizeStripeAccountCreationFragment.OnStripeAccountCreatedListener {

    private TextInputEditText firstNameET;
    private TextInputEditText lastNameET;
    private TextInputEditText addressStreetET;
    private TextInputEditText addressPostalCodeET;
    private TextInputEditText addressCityET;
    private TextInputEditText dobET;
    private TextInputLayout firstNameTIL;
    private TextInputLayout lastNameTIL;
    private TextInputLayout addressStreetTIL;
    private TextInputLayout addressPostalCodeTIL;
    private TextInputLayout addressCityTIL;
    private TextInputLayout dobTIL;
    private Button createStripeAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stripe_account);

        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        addressStreetET = findViewById(R.id.addressLine1ET);
        addressPostalCodeET = findViewById(R.id.postalCodeET);
        addressCityET = findViewById(R.id.cityET);
        dobET = findViewById(R.id.dobYearET);
        firstNameTIL = findViewById(R.id.firstNameTIL);
        lastNameTIL = findViewById(R.id.lastNameTIL);
        addressStreetTIL = findViewById(R.id.addressLine1TIL);
        addressPostalCodeTIL = findViewById(R.id.postalCodeTIL);
        addressCityTIL = findViewById(R.id.cityTIL);
        dobTIL = findViewById(R.id.dobYearTIL);
        createStripeAccountBtn = findViewById(R.id.createStripeAccountBtn);

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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addressPostalCodeTIL.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

        createStripeAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstName = firstNameET.getText().toString();
                String lastName = lastNameET.getText().toString();
                String addressStreet = addressStreetET.getText().toString();
                String addressPostalCode = addressPostalCodeET.getText().toString();
                String addressCity = addressCityET.getText().toString();
                String dob = dobET.getText().toString();

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

                if (TextUtils.isEmpty(dob)) {
                    dobTIL.setError(getString(R.string.no_dob_error));
                    infoIsValid = false;
                }

                if (dob.length()!=10) {
                    dobTIL.setError(getString(R.string.no_valid_dob_error));
                    infoIsValid = false;
                }

                if (infoIsValid) {

                    int dobYear = Integer.parseInt(dob.substring(0,4));
                    int dobMonth = Integer.parseInt(dob.substring(5,7));
                    int dobDay = Integer.parseInt(dob.substring(8,10));

                    HashMap<String, Object> accountData = new HashMap<>();
                    accountData.put("firstName", firstName);
                    accountData.put("lastName", lastName);
                    accountData.put("addressStreet", addressStreet);
                    accountData.put("addressPostalCode", addressPostalCode);
                    accountData.put("addressCity", addressCity);
                    accountData.put("dobYear",dobYear);
                    accountData.put("dobMonth",dobMonth);
                    accountData.put("dobDay",dobDay);
                    accountData.put("ip", getLocalIpAddress());
                    accountData.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    accountData.put("accountType", "custom");
                    accountData.put("country","SE");
                    accountData.put("legalEntityType", "individual");


                    FinalizeStripeAccountCreationFragment finalizeStripeAccountCreationFragment = new FinalizeStripeAccountCreationFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("accountData",accountData);
                    finalizeStripeAccountCreationFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                    transaction.add(R.id.container_finalize_fragment, finalizeStripeAccountCreationFragment, "finalize").addToBackStack(null);
                    transaction.commit();
                }
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
                        Log.i(TAG, "***** IP="+ ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    @Override
    public void OnStripeAccountCreated() {
        setResult(RESULT_OK, null);
        finish();
    }
}
