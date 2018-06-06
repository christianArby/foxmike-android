package com.foxmike.android.activities;

import android.content.Intent;
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
        addPaymentMethodTV = findViewById(R.id.addPaymentMethodTV);
        listPaymentMethodsRV = findViewById(R.id.listPaymentMethodsRV);

        addPaymentMethodTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createIntent = new Intent(PaymentPreferencesActivity.this,CreateStripeCustomerActivity.class);
                startActivityForResult(createIntent,1);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, PaymentPreferencesActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }
}
