/*
package com.foxmike.android.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

*/
/**
 * Created by chris on 2019-01-18.
 *//*


public class AlertDialogs {

    Context context;

    public AlertDialogs(Context context) {
        this.context = context;
    }

    public AlertDialogs() {
    }

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside, OnOkPressedListener onOkPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onOkPressedListener.OnOkPressed();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();
    }


    public void alertDialogPositiveOrNegative(String title, String message, String positiveButton, String negativeButton, OnPositiveOrNegativeButtonPressedListener onPositiveOrNegativeButtonPressedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);

        // Add the buttons
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                onPositiveOrNegativeButtonPressedListener.OnPositivePressed();
            }
        });

        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onPositiveOrNegativeButtonPressedListener.OnNegativePressed();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();

    }

    public interface OnOkPressedListener {
        void OnOkPressed();
    }

    public interface OnPositiveOrNegativeButtonPressedListener {
        void OnPositivePressed();
        void OnNegativePressed();
    }
}
*/
