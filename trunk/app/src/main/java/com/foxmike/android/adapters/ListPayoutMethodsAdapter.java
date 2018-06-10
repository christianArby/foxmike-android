package com.foxmike.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnPayoutMethodClickedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2018-06-03.
 */

public class ListPayoutMethodsAdapter extends RecyclerView.Adapter<ListPayoutMethodsAdapter.ListPayoutMethodsViewHolder> {

    private ArrayList<HashMap<String,Object>> external_accountsDataList;
    private Context context;
    private OnPayoutMethodClickedListener onPayoutMethodClickedListener;

    public ListPayoutMethodsAdapter(ArrayList<HashMap<String,Object>> external_accountsDataList, Context context, OnPayoutMethodClickedListener onPayoutMethodClickedListener) {
        this.external_accountsDataList = external_accountsDataList;
        this.context = context;
        this.onPayoutMethodClickedListener = onPayoutMethodClickedListener;
    }


    @NonNull
    @Override
    public ListPayoutMethodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payout_method_single_layout, parent, false);
        return new ListPayoutMethodsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPayoutMethodsViewHolder holder, int position) {

        if (external_accountsDataList.get(position).get("object").equals("bank_account")) {
            holder.setPayoutMethodType(context.getString(R.string.bank_account_text));
            String last4 = external_accountsDataList.get(position).get("last4").toString();
            String currency = external_accountsDataList.get(position).get("currency").toString();
            holder.setPayoutMethodLast4("IBAN" + " *****" + last4 + " (" + currency.toUpperCase() + ")");
        } else {
            holder.setPayoutMethodType(external_accountsDataList.get(position).get("object").toString());
            String last4 = external_accountsDataList.get(position).get("last4").toString();
            String currency = external_accountsDataList.get(position).get("currency").toString();
            holder.setPayoutMethodLast4("*****" + last4 + "(" + currency.toUpperCase() + ")");
        }

        if (external_accountsDataList.get(position).get("default_for_currency").toString().equals("true")) {
            holder.setPayoutMethodStandard(true);
        } else {
            holder.setPayoutMethodStandard(false);
        }

        holder.setPayoutMethodClickedListener(external_accountsDataList.get(position).get("id").toString());
    }

    @Override
    public int getItemCount() {
        return external_accountsDataList.size();
    }

    public class ListPayoutMethodsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public void setPayoutMethodClickedListener(final String accountID) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPayoutMethodClickedListener.OnPayoutMethodClicked(accountID);
                }
            });
        }

        public ListPayoutMethodsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setPayoutMethodType(String payoutMethodType) {
            TextView payoutMethodTypeTV = (TextView) mView.findViewById(R.id.payoutMethodType);
            payoutMethodTypeTV.setText(payoutMethodType);
        }

        public void setPayoutMethodLast4(String payoutMethodLast4) {
            TextView payoutMethodLast4TV = (TextView) mView.findViewById(R.id.last4digits);
            payoutMethodLast4TV.setText(payoutMethodLast4);
        }

        public void setPayoutMethodStandard(Boolean payoutMethodStandard) {
            TextView payoutMethodStandardTV = (TextView) mView.findViewById(R.id.payoutMethodStandard);
            if (payoutMethodStandard) {
                payoutMethodStandardTV.setText(R.string.default_text);
            } else {
                payoutMethodStandardTV.setVisibility(View.GONE);
            }
        }
    }
}
