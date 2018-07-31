package com.foxmike.android.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.activities.PaymentPreferencesActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NeedPaymentMethodFragment extends DialogFragment {

    private Button cancelBtn;
    private Button addPaymentMethodBtn;


    public NeedPaymentMethodFragment() {
        // Required empty public constructor
    }

    public static NeedPaymentMethodFragment newInstance() {

        Bundle args = new Bundle();

        NeedPaymentMethodFragment fragment = new NeedPaymentMethodFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_need_payment_method, container, false);

        cancelBtn = view.findViewById(R.id.cancelBtn);
        addPaymentMethodBtn = view.findViewById(R.id.addPaymentMethodBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        addPaymentMethodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
                dismiss();
            }
        });


        return view;
    }

}
