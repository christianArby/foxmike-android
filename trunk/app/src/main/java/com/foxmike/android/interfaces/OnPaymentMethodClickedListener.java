package com.foxmike.android.interfaces;

/**
 * Created by chris on 2018-06-06.
 */

public interface OnPaymentMethodClickedListener {
    void OnPaymentMethodClicked(String sourceId, String cardBrand, String last4, Boolean isDefault);
}
