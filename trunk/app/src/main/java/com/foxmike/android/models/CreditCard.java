package com.foxmike.android.models;

import com.foxmike.android.R;
import com.stripe.android.model.Card;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 2018-07-29.
 */

public class CreditCard {

    public static final Map<String , Integer> BRAND_CARD_RESOURCE_MAP =
            new HashMap<String , Integer>() {{
                put(Card.AMERICAN_EXPRESS, R.drawable.ic_amex_card);
                put(Card.DINERS_CLUB, R.drawable.ic_diners_card);
                put(Card.DISCOVER, R.drawable.ic_discover_card);
                put(Card.JCB, R.drawable.ic_jcb_card);
                put(Card.MASTERCARD, R.drawable.ic_mastercard_card);
                put(Card.VISA, R.drawable.ic_visa_card);
                put(Card.UNIONPAY, R.drawable.ic_unknown_card);
                put(Card.UNKNOWN, R.drawable.ic_unknown_card);
            }};


}
