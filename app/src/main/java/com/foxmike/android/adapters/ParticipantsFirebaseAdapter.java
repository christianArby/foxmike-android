package com.foxmike.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.models.UserPublic;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2018-11-04.
 */

public class ParticipantsFirebaseAdapter extends FirebaseRecyclerAdapter<UserPublic, ParticipantsFirebaseAdapter.ParticipantsRowViewHolder>{

    private Context ctx;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ParticipantsFirebaseAdapter(@NonNull FirebaseRecyclerOptions<UserPublic> options, Context ctx) {
        super(options);
        this.ctx = ctx;
    }

    @Override
    protected void onBindViewHolder(@NonNull ParticipantsRowViewHolder holder, int position, @NonNull UserPublic model) {
        holder.setAvatar(model.getThumb_image());
    }

    @NonNull
    @Override
    public ParticipantsRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.participant_avatar_single_layout, parent, false);
        return new ParticipantsRowViewHolder(view);
    }

    public class ParticipantsRowViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ParticipantsRowViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAvatar(String image) {
            CircleImageView participantAvtar = mView.findViewById(R.id.participantAvatar);
            Glide.with(ctx).load(image).into(participantAvtar);
        }
    }

}
