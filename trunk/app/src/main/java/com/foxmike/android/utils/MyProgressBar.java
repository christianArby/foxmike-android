package com.foxmike.android.utils;
// Checked
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
 * Creates a progress bar
 */

public class MyProgressBar {
    private ProgressBar progressBar;
    private Activity activity;

    public MyProgressBar(ProgressBar progressBar, Activity activity) {
        this.progressBar = progressBar;
        this.activity = activity;
    }

    public void startProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setFocusable(true);
        progressBar.setFocusableInTouchMode(true);
        progressBar.requestFocus();
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void stopProgressBar() {
        progressBar.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
