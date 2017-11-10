package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by chris on 2017-11-10.
 */

public class SessionRow {

    LatLng sessionLatLng;
    Context context;

    public SessionRow() {

    }

    // Method to populate the LinearLayout list with multiple session_row_view's
    public void populateList(final ArrayList<Session> sessionArray, Context context, LinearLayout listSessions) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i=0; i < sessionArray.size(); i++) {
            View sessionRowView  = inflater.inflate(R.layout.session_row_view, listSessions, false);
            ImageView images = sessionRowView.findViewById(R.id.icon);
            TextView myTitle = sessionRowView.findViewById(R.id.text1);
            TextView myDescription = sessionRowView.findViewById(R.id.text2);
            myTitle.setText(sessionArray.get(i).getSessionName());
            myDescription.setText(sessionArray.get(i).getSessionType());
            setImage(sessionArray.get(i).getImageUri(),images);
            // set item content in view
            listSessions.addView(sessionRowView);
            final int t = i;

            // When session_row_view is clicked start the DisplaySessionActivity
            sessionRowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sessionLatLng = new LatLng(sessionArray.get(t).getLatitude(), sessionArray.get(t).getLongitude());
                    displaySession(sessionLatLng);
                }
            });
        }
    }

    // Method to set and scale an image into an imageView
    private void setImage(String image, ImageView imageView) {
        Glide.with(context).load(image).into(imageView);
    }

    private void displaySession(LatLng markerLatLng) {
        Intent intent = new Intent(context, DisplaySessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        context.startActivity(intent);
    }
}
