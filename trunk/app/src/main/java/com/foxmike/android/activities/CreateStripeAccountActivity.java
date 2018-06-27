package com.foxmike.android.activities;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.foxmike.android.R;
import com.foxmike.android.fragments.CreateStripeAccountDobTosFragment;
import com.foxmike.android.fragments.CreateStripeExternalAccountFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class CreateStripeAccountActivity extends AppCompatActivity implements CreateStripeAccountDobTosFragment.OnStripeAccountCreatedListener, CreateStripeExternalAccountFragment.OnStripeExternalAccountCreatedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stripe_account);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        addressStreetET = findViewById(R.id.addressLine1ET);
        addressPostalCodeET = findViewById(R.id.postalCodeET);
        addressCityET = findViewById(R.id.cityET);
        firstNameTIL = findViewById(R.id.firstNameTIL);
        lastNameTIL = findViewById(R.id.lastNameTIL);
        addressStreetTIL = findViewById(R.id.addressLine1TIL);
        addressPostalCodeTIL = findViewById(R.id.postalCodeTIL);
        addressCityTIL = findViewById(R.id.cityTIL);
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

        createStripeAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstName = firstNameET.getText().toString();
                String lastName = lastNameET.getText().toString();
                String addressStreet = addressStreetET.getText().toString();
                String addressPostalCode = addressPostalCodeET.getText().toString();
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

                    accountData.put("ip", getLocalIpAddress());
                    accountData.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    accountData.put("accountType", "custom");
                    accountData.put("country","SE");
                    accountData.put("legalEntityType", "individual");


                    CreateStripeAccountDobTosFragment createStripeAccountDobTosFragment = new CreateStripeAccountDobTosFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("accountData",accountData);
                    createStripeAccountDobTosFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
                    transaction.add(R.id.container_finalize_fragment, createStripeAccountDobTosFragment, "finalize").addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
    public void OnStripeAccountCreated(String accountId) {
        CreateStripeExternalAccountFragment createStripeExternalAccountFragment = new CreateStripeExternalAccountFragment();

        Bundle bundle = new Bundle();
        accountData.put("stripeAccountId",accountId);
        bundle.putSerializable("accountData",accountData);
        createStripeExternalAccountFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.add(R.id.container_finalize_fragment, createStripeExternalAccountFragment, "finalize").addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void OnStripeExternalAccountCreated() {
        setResult(RESULT_OK, null);
        finish();
    }
}
