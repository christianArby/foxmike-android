package com.foxmike.android.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnStudioBranchClickedListener;
import com.foxmike.android.models.StudioBranch;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2018-07-08.
 */

public class ListStudiosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<StudioBranch> studioBranchArrayList;
    private OnStudioBranchClickedListener onStudioBranchClickedListener;
    private Context context;

    public ListStudiosAdapter(ArrayList<StudioBranch> studioBranchArrayList, OnStudioBranchClickedListener onStudioBranchClickedListener, Context context) {
        this.studioBranchArrayList = studioBranchArrayList;
        this.onStudioBranchClickedListener = onStudioBranchClickedListener;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType==1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_sessions_section_header_layout, parent, false);
            return new ListStudiosSectionHeaderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.studio_small_single_layout, parent, false);
            return new ListStudiosViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (studioBranchArrayList.get(position).getStudio().getImageUrl().equals("sectionHeader")) {

            ((ListStudiosSectionHeaderViewHolder) holder).setHeader(studioBranchArrayList.get(position).getStudio().getStudioName());

        } else {
            if (studioBranchArrayList.size()>0) {
                ((ListStudiosViewHolder) holder).setSessionImage(studioBranchArrayList.get(position).getStudio().getImageUrl());
                ((ListStudiosViewHolder) holder).setText1(studioBranchArrayList.get(position).getStudio().getStudioName());
                StudioBranch studioBranch = studioBranchArrayList.get(position);
                ((ListStudiosViewHolder) holder).setText2(studioBranch.getStudio().getStudioType());
                ((ListStudiosViewHolder) holder).setStudioClickedListener(studioBranchArrayList.get(position));
                Long currentTimestamp = System.currentTimeMillis();
                boolean isAdvertised = false;
                for (Long sessionTimestamp: studioBranchArrayList.get(position).getStudio().getSessions().values()) {
                    if (sessionTimestamp>currentTimestamp) {
                        isAdvertised =true;
                    }
                }
                ((ListStudiosViewHolder) holder).setAdvertised(isAdvertised);
            }
        }

    }

    @Override
    public int getItemCount() {
        return studioBranchArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (studioBranchArrayList.get(position).getStudio().getImageUrl().equals("sectionHeader")) {
            return 1;
        }
        return 0;
    }

    public class ListStudiosSectionHeaderViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListStudiosSectionHeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setHeader(String header) {
            TextView headerTV = mView.findViewById(R.id.header_TV);
            headerTV.setText(header);
        }
    }

    public class ListStudiosViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListStudiosViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setStudioClickedListener(final StudioBranch studioBranch) {
            ConstraintLayout frame = mView.findViewById(R.id.small_session_frame);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStudioBranchClickedListener.OnStudioBranchClicked(studioBranch);
                }
            });
        }

        public void setSessionImage(String sessionImage) {
            ImageView sessionIV = (ImageView) mView.findViewById(R.id.icon);
            Glide.with(context).load(sessionImage).into(sessionIV);
            sessionIV.setColorFilter(R.color.foxmikePrimaryDarkColor, PorterDuff.Mode.LIGHTEN);
        }

        public void setText1(String text1) {
            TextView text1TV = (TextView) mView.findViewById(R.id.text1);
            text1TV.setText(text1);
        }

        public void setText2(String text2) {
            TextView text2TV = (TextView) mView.findViewById(R.id.text2);
            text2TV.setText(text2);
        }

        public void setAdvertised(boolean isAdvertised) {
            TextView advertised = (TextView) mView.findViewById(R.id.advertisedStamp);
            TextView notAdvertised = (TextView) mView.findViewById(R.id.notAdvertisedStamp);
            if (isAdvertised) {
                advertised.setVisibility(View.VISIBLE);
                notAdvertised.setVisibility(View.GONE);
            } else {
                advertised.setVisibility(View.GONE);
                notAdvertised.setVisibility(View.VISIBLE);
            }
        }
    }

    public void updateData(ArrayList<StudioBranch> studioBranchArrayList) {
        this.studioBranchArrayList = studioBranchArrayList;
        this.notifyDataSetChanged();
    }
}
