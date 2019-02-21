package com.foxmike.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.foxmike.android.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by chris on 2019-02-04.
 */

public class CheckVersion {

    public CheckVersion() {
    }

    public static void checkVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            AlertDialogs alertDialogs = new AlertDialogs(context);
            DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
            rootDbRef.child("minVersionNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        if ((Long) dataSnapshot.getValue()>verCode) {
                            alertDialogs.alertDialogOk(context.getString(R.string.update_text_1),
                                    context.getString(R.string.update_text_2) + version + "." + context.getString(R.string.update_text_3), false,
                                    new AlertDialogs.OnOkPressedListener() {
                                @Override
                                public void OnOkPressed() {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                    homeIntent.addCategory( Intent.CATEGORY_HOME );
                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(homeIntent);
                                }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
