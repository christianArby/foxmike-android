package com.foxmike.android.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListPayoutMethodsAdapter;
import com.google.firebase.functions.FirebaseFunctions;

public class PaymentPreferencesActivity extends AppCompatActivity {

    private FirebaseFunctions mFunctions;
    private View mainView;
    private RecyclerView listPaymentMethodsRV;
    private ListPayoutMethodsAdapter listPaymentMethodsAdapter;
    private ProgressBar progressBar;
    private TextView addPaymentMethodTV;
    private String stripeAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_preferences);

        mFunctions = FirebaseFunctions.getInstance();

        mainView = findViewById(R.id.mainView);
        progressBar = findViewById(R.id.progressBar_cyclic);
        addPaymentMethodTV = findViewById(R.id.addPayoutMethodTV);
        listPaymentMethodsRV = findViewById(R.id.listPayoutMethodsRV);


    }
}
