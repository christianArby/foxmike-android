package com.foxmike.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnPaymentMethodClickedListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2018-06-06.
 */

public class ListPaymentMethodsAdapter extends RecyclerView.Adapter<ListPaymentMethodsAdapter.ListPaymentMethodsViewHolder>{

    private ArrayList<HashMap<String,Object>> sourcesDataList;
    private Context context;
    private OnPaymentMethodClickedListener onPaymentMethodClickedListener;
    private String defaultSource;

    public ListPaymentMethodsAdapter(ArrayList<HashMap<String,Object>> sourcesDataList, Context context, String defaultSource, OnPaymentMethodClickedListener onPaymentMethodClickedListener) {
        this.sourcesDataList = sourcesDataList;
        this.context = context;
        this.onPaymentMethodClickedListener = onPaymentMethodClickedListener;
        this.defaultSource = defaultSource;
    }


    @NonNull
    @Override
    public ListPaymentMethodsAdapter.ListPaymentMethodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_method_single_layout, parent, false);
        return new ListPaymentMethodsAdapter.ListPaymentMethodsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPaymentMethodsAdapter.ListPaymentMethodsViewHolder holder, int position) {

        if (sourcesDataList.get(position).get("object").equals("card")) {
            String sourceId = sourcesDataList.get(position).get("id").toString();
            String cardBrand = sourcesDataList.get(position).get("brand").toString();
            String last4 = sourcesDataList.get(position).get("last4").toString();
            boolean isDefault;

            holder.setCard(cardBrand, last4);

            if (sourceId.equals(defaultSource)) {
                isDefault = true;
                holder.setPaymentMethodStandard(true);
            } else {
                isDefault = false;
                holder.setPaymentMethodStandard(false);
            }

            holder.setPaymentMethodClickedListener(sourceId,cardBrand,last4,isDefault);
        }


    }

    @Override
    public int getItemCount() {
        return sourcesDataList.size();
    }

    public class ListPaymentMethodsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public void setPaymentMethodClickedListener(final String customerID, final String cardBrand, final String last4, final boolean isDefault) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPaymentMethodClickedListener.OnPaymentMethodClicked(customerID, cardBrand, last4, isDefault);
                }
            });
        }

        public ListPaymentMethodsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCard(String cardBrand, String last4) {
            ImageView cardIcon = (ImageView) mView.findViewById(R.id.cardIcon);
            TextView last4TV = (TextView) mView.findViewById(R.id.last4digits);

            last4TV.setText(cardBrand + " " + last4);

        }

        public void setPaymentMethodStandard(Boolean payoutMethodStandard) {
            TextView paymentMethodStandardTV = (TextView) mView.findViewById(R.id.paymentMethodStandard);
            if (payoutMethodStandard) {
                paymentMethodStandardTV.setText(R.string.default_text);
            } else {
                paymentMethodStandardTV.setVisibility(View.GONE);
            }
        }
    }
}
