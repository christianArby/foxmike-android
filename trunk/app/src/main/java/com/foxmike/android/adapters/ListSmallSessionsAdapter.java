package com.foxmike.android.adapters;
//Checked
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.models.SessionBranch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This adapter takes an arraylist of sessions and fills a RecyclerView
 * If a session has the string "sectionHeader" in the session variable imageURL, an alternate view will be inflated with the string stored under the variable sessionName in the session object
 */

public class ListSmallSessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<SessionBranch> sessionBranchArrayList;
    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private Context context;

    public ListSmallSessionsAdapter(ArrayList<SessionBranch> sessionBranchArrayList, OnSessionBranchClickedListener onSessionBranchClickedListener, Context context) {
        this.sessionBranchArrayList = sessionBranchArrayList;
        this.onSessionBranchClickedListener = onSessionBranchClickedListener;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType==1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_sessions_section_header_layout, parent, false);
            return new ListSmallSessionsSectionHeaderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_small_single_layout, parent, false);
            return new ListSmallSessionsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (sessionBranchArrayList.get(position).getSession().getImageUrl().equals("sectionHeader")) {

            ((ListSmallSessionsSectionHeaderViewHolder) holder).setHeader(sessionBranchArrayList.get(position).getSession().getSessionName());

        } else {
            if (sessionBranchArrayList.size()>0) {
                ((ListSmallSessionsViewHolder) holder).setSessionImage(sessionBranchArrayList.get(position).getSession().getImageUrl());
                ((ListSmallSessionsViewHolder) holder).setText1(sessionBranchArrayList.get(position).getSession().getSessionName());
                SessionBranch sessionBranch = sessionBranchArrayList.get(position);
                String sessionDateAndTime = sessionBranch.getSession().supplyTextTimeStamp().textSessionDateAndTime();
                sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                ((ListSmallSessionsViewHolder) holder).setText2(sessionDateAndTime);
                ((ListSmallSessionsViewHolder) holder).setSessionClickedListener(sessionBranchArrayList.get(position));
            }
        }

    }

    @Override
    public int getItemCount() {
        return sessionBranchArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (sessionBranchArrayList.get(position).getSession().getImageUrl().equals("sectionHeader")) {
            return 1;
        }
        return 0;
    }

    public class ListSmallSessionsSectionHeaderViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListSmallSessionsSectionHeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setHeader(String header) {
            TextView headerTV = mView.findViewById(R.id.header_TV);
            headerTV.setText(header);
        }
    }

    public class ListSmallSessionsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListSmallSessionsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setSessionClickedListener(final SessionBranch sessionBranch) {
            ConstraintLayout frame = mView.findViewById(R.id.small_session_frame);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionBranchClickedListener.OnSessionBranchClicked(sessionBranch);
                }
            });

            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final Calendar cal = Calendar.getInstance();
            Date todaysDate = cal.getTime();
            cal.add(Calendar.DATE,14);
            Date twoWeeksDate = cal.getTime();*/

            /*if (sessionBranch.getSession().getSessionTimestamp().getDateOfSession().after(todaysDate) && sessionBranch.getSession().getSessionTimestamp().getDateOfSession().before(twoWeeksDate)) {
                // // Remove the hardware layer
                //v.setLayerType(LAYER_TYPE_NONE, null);
            } else {
                // Create a paint object with 0 saturation (black and white)
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                Paint greyscalePaint = new Paint();
                greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
                // Create a hardware layer with the greyscale paint
                frame.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
            }*/

        }

        public void setSessionImage(String sessionImage) {
            ImageView sessionIV = (ImageView) mView.findViewById(R.id.icon);
            Glide.with(context).load(sessionImage).into(sessionIV);
        }

        public void setText1(String text1) {
            TextView text1TV = (TextView) mView.findViewById(R.id.text1);
            text1TV.setText(text1);
        }

        public void setText2(String text2) {
            TextView text2TV = (TextView) mView.findViewById(R.id.text2);
            text2TV.setText(text2);
        }
    }

    public void updateData(ArrayList<SessionBranch> sessionBranchArrayList) {
        this.sessionBranchArrayList = sessionBranchArrayList;
        this.notifyDataSetChanged();
    }
}



