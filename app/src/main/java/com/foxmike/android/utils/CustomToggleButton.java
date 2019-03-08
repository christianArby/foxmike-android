package com.foxmike.android.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.widget.ToggleButton;

/**
 * Created by chris on 2019-02-19.
 */

public class CustomToggleButton extends ToggleButton {
    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        applyCustomFont(context);
    }

    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public CustomToggleButton(Context context) {
        super(context);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        if (FoxmikeFont.hasCustomFont) {
            Typeface customFont = ResourcesCompat.getFont(context, FoxmikeFont.customFontRegular);
            setTypeface(customFont);
        }

    }
}
