package com.foxmike.android.models;

import com.stripe.android.model.Token;

/**
 * Created by chris on 2018-05-12.
 */

public class Payment {
    int amount;
    Token token;
    Object charge;

    public Payment(int amount, Token token, Object charge) {
        this.amount = amount;
        this.token = token;
        this.charge = charge;
    }

    public Payment() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Object getCharge() {
        return charge;
    }

    public void setCharge(Object charge) {
        this.charge = charge;
    }
}
