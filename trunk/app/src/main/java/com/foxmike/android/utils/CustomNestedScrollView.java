package com.foxmike.android.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * Created by chris on 2019-03-20.
 */

public class CustomNestedScrollView extends NestedScrollView{

    private OnScrollViewSizeChangedListener onScrollViewSizeChangedListener;

    public CustomNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onScrollViewSizeChangedListener!=null) {
            onScrollViewSizeChangedListener.OnScrollViewSizeChanged();
        }
    }

    public void attachListener(OnScrollViewSizeChangedListener onScrollViewSizeChangedListener) {
        this.onScrollViewSizeChangedListener = onScrollViewSizeChangedListener;
    }

    public interface OnScrollViewSizeChangedListener {
        void OnScrollViewSizeChanged();
    }
}
