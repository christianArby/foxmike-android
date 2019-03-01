package com.foxmike.android.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;

/**
 * Created by chris on 2019-02-27.
 */

public class PatchedCrystalRangeSeekbar extends CrystalRangeSeekbar {
    private final int mThumbsSize = 60;

    public PatchedCrystalRangeSeekbar(Context context) {
        super(context);
    }

    public PatchedCrystalRangeSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PatchedCrystalRangeSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ... override constructors

    @Override
    protected float getThumbWidth() {
        return mThumbsSize;
    }

    @Override
    protected float getThumbHeight() {
        return mThumbsSize;
    }
}
