package com.foxmike.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnPaymentMethodClickedListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;

/**
 * Created by chris on 2018-06-06.
 */

public class ListPaymentMethodsAdapter extends RecyclerView.Adapter<ListPaymentMethodsAdapter.ListPaymentMethodsViewHolder>{

    private ArrayList<HashMap<String,Object>> paymentMethodDataList;
    private Context context;
    private OnPaymentMethodClickedListener onPaymentMethodClickedListener;
    private String defaultPaymentMethod;

    public ListPaymentMethodsAdapter(ArrayList<HashMap<String,Object>> paymentMethodDataList, Context context, String defaultPaymentMethod, OnPaymentMethodClickedListener onPaymentMethodClickedListener) {
        this.paymentMethodDataList = paymentMethodDataList;
        this.context = context;
        this.onPaymentMethodClickedListener = onPaymentMethodClickedListener;
        this.defaultPaymentMethod = defaultPaymentMethod;
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

        if (paymentMethodDataList.get(position).get("type").equals("card")) {
            String paymentMethodId = paymentMethodDataList.get(position).get("id").toString();
            HashMap<String,Object> card = (HashMap<String,Object>) paymentMethodDataList.get(position).get("card");
            String cardBrand = card.get("brand").toString();
            String last4 = card.get("last4").toString();
            boolean isDefault;
            holder.setCard(BRAND_CARD_RESOURCE_MAP.get(cardBrand), cardBrand, last4);
            if (paymentMethodId.equals(defaultPaymentMethod)) {
                isDefault = true;
                holder.setPaymentMethodStandard(true);
            } else {
                isDefault = false;
                holder.setPaymentMethodStandard(false);
            }
            holder.setPaymentMethodClickedListener(paymentMethodId,cardBrand,last4,isDefault);
        }
    }

    @Override
    public int getItemCount() {
        return paymentMethodDataList.size();
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

        public void setPaymentMethodStandard(Boolean payoutMethodStandard) {
            TextView paymentMethodStandardTV = (TextView) mView.findViewById(R.id.paymentMethodStandard);
            if (payoutMethodStandard) {
                paymentMethodStandardTV.setText(R.string.default_text);
            } else {
                paymentMethodStandardTV.setVisibility(View.GONE);
            }
        }

        //@SuppressWarnings("deprecation")
        private void setCard(@DrawableRes int iconResourceId, String cardBrand, String last4) {

            TextView cardTV = (TextView) mView.findViewById(R.id.cardTV);

            cardTV.setText(cardBrand + " " + last4);

            cardTV.setCompoundDrawablesWithIntrinsicBounds(iconResourceId, 0, 0, 0);
        }
    }
}
