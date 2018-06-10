package com.foxmike.android.interfaces;

/**
 * Created by chris on 2018-06-03.
 */

public interface OnPayoutMethodClickedListener {
    void OnPayoutMethodClicked(String accountId, String externalAccountId, String last4, String currency, Boolean isDefault);
}
