package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
 * Created by chris on 2017-11-30.
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
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void stopProgressBar() {
        progressBar.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
