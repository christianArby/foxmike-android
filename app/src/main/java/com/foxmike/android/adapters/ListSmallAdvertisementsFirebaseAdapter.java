package com.foxmike.android.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.utils.AlertDialogs;
import com.foxmike.android.utils.SmallAdvertisementViewHolder;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by chris on 2018-09-22.
 */


public class ListSmallAdvertisementsFirebaseAdapter extends FirebaseRecyclerAdapter<Advertisement, SmallAdvertisementViewHolder> {

    private Context context;
    private OnSessionClickedListener onSessionClickedListener;
    private long mLastClickTime = 0;
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListSmallAdvertisementsFirebaseAdapter(FirebaseRecyclerOptions<Advertisement> options, Context context, OnSessionClickedListener onSessionClickedListener) {
        super(options);
        this.context = context;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    @NonNull
    @Override
    public SmallAdvertisementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.advertisement_small_single_layout, parent, false);
        return new SmallAdvertisementViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull SmallAdvertisementViewHolder holder, int position, @NonNull Advertisement model) {
        holder.setSessionImage(model.getImageUrl(), context);
        holder.setText1(model.getSessionName());
        String advDateAndTime = TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp());
        advDateAndTime = advDateAndTime.substring(0,1).toUpperCase() + advDateAndTime.substring(1);
        holder.setText2(advDateAndTime);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (!model.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (model.getStatus().equals("cancelled")) {
                        AlertDialogs alertDialogs = new AlertDialogs(context);
                        alertDialogs.alertDialogPositiveOrNegative(context.getResources().getString(R.string.occasion_cancelled), context.getResources().getString(R.string.occasion_cancelled_text) + context.getResources().getString(R.string.you_will_be_refunded),
                                context.getResources().getString(R.string.ok), context.getResources().getString(R.string.show_availability), new AlertDialogs.OnPositiveOrNegativeButtonPressedListener() {
                                    @Override
                                    public void OnPositivePressed() {

                                    }

                                    @Override
                                    public void OnNegativePressed() {
                                        onSessionClickedListener.OnSessionClicked(model.getSessionId(), model.getAdvertisementTimestamp());
                                    }
                                });

                    }
                }


            }
        });
        if (model.getStatus().equals("cancelled")) {
            holder.setCancelled(true);
        }

    }
}
