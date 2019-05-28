package com.foxmike.android.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Created by chris on 2019-03-20.
 */

public class CustomConstraintLayout extends ConstraintLayout {
    private OnCustomLayoutChangedListener onCustomLayoutChangedListener;
    public CustomConstraintLayout(Context context) {
        super(context);
    }

    public CustomConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onCustomLayoutChangedListener!=null) {
            onCustomLayoutChangedListener.OnCustomLayoutChangedChanged();
        }
    }

    public void attachListener(OnCustomLayoutChangedListener onCustomLayoutChangedListener) {
        this.onCustomLayoutChangedListener = onCustomLayoutChangedListener;
    }

    public interface OnCustomLayoutChangedListener {
        void OnCustomLayoutChangedChanged();
    }
}
