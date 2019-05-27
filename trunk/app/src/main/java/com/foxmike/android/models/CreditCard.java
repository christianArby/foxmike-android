package com.foxmike.android.models;

import com.foxmike.android.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 2018-07-29.
 */

public class CreditCard {
    public static final Map<String , Integer> BRAND_CARD_RESOURCE_MAP =
            new HashMap<String , Integer>() {{
                put("amex", R.drawable.ic_amex_card);
                put("diners", R.drawable.ic_diners_card);
                put("discover", R.drawable.ic_discover_card);
                put("jcb", R.drawable.ic_jcb_card);
                put("mastercard", R.drawable.ic_mastercard_card);
                put("visa", R.drawable.ic_visa_card);
                put("unionpay", R.drawable.ic_unknown_card);
                put("unknown", R.drawable.ic_unknown_card);
            }};
}
