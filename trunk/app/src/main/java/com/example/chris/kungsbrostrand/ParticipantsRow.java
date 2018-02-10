package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2017-11-16.
 */

public class ParticipantsRow {

    Context context;

    public ParticipantsRow () {}

    // Method to populate the LinearLayout list with multiple session_small_single_layout's
    public void populateList(final HashMap<String,Boolean> participantsHashmap, Context context, final LinearLayout listParticipants) {
        this.context = context;
        final LayoutInflater inflater = LayoutInflater.from(context);

        MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        myFirebaseDatabase.getUsers(participantsHashmap, new OnUsersFoundListener() {
            @Override
            public void OnUsersFound(ArrayList<User> participants) {

                if (!participantsHashmap.containsValue(false)) {
                    for (int i=0; i < participants.size(); i++) {
                        View usersRowView  = inflater.inflate(R.layout.participant_row_view, listParticipants, false);
                        ImageView images = usersRowView.findViewById(R.id.participant_image);
                        TextView myTitle = usersRowView.findViewById(R.id.participant_name);
                        TextView myDescription = usersRowView.findViewById(R.id.participant_info);

                        myTitle.setText(participants.get(i).getName());
                        myDescription.setText(participants.get(i).getName());
                        setImage(participants.get(i).getImage(),images);
                        // set item content in view
                        final int t = i;
                        listParticipants.addView(usersRowView);

                        // When participant is clicked...
                        usersRowView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                    }
                }
            }
        });
    }

    // Method to set and scale an image into an imageView
    private void setImage(String image, ImageView imageView) {
        Glide.with(context).load(image).into(imageView);
    }
}
