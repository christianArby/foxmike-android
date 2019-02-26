package com.foxmike.android.models;

import android.text.SpannableStringBuilder;

/**
 * Created by chris on 2019-02-26.
 */

public class InAppNotification {

    private String notificationThumbnail;
    private SpannableStringBuilder notificationText;

    public InAppNotification(String notificationThumbnail, SpannableStringBuilder notificationText) {
        this.notificationThumbnail = notificationThumbnail;
        this.notificationText = notificationText;
    }

    public InAppNotification() {
    }

    public String getNotificationThumbnail() {
        return notificationThumbnail;
    }

    public void setNotificationThumbnail(String notificationThumbnail) {
        this.notificationThumbnail = notificationThumbnail;
    }

    public SpannableStringBuilder getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(SpannableStringBuilder notificationText) {
        this.notificationText = notificationText;
    }
}
