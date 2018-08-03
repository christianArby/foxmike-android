package com.foxmike.android.interfaces;

/**
 * Created by chris on 2018-04-07.
 */

public interface OnCommentClickedListener {
    void OnCommentClicked(String postID, String heading, String time, String message, String thumb_image);
}
