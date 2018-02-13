package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.view.View.LAYER_TYPE_HARDWARE;

/**
 * Created by chris on 2018-02-11.
 */



public class ListSmallSessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<Session> sessionArrayList;
    private OnSessionClickedListener onSessionClickedListener;
    private Context context;
    private HashMap<Integer,String> sessionsSectionHeaders;

    public ListSmallSessionsAdapter(ArrayList<Session> sessionArrayList, HashMap<Integer,String> sessionsSectionHeaders, OnSessionClickedListener onSessionClickedListener, Context context) {
        this.sessionArrayList = sessionArrayList;
        this.onSessionClickedListener = onSessionClickedListener;
        this.context = context;
        this.sessionsSectionHeaders = sessionsSectionHeaders;
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
        if (sessionsSectionHeaders.get(position)!=null) {

            ((ListSmallSessionsSectionHeaderViewHolder) holder).setHeader(sessionsSectionHeaders.get(position));

        } else {
            if (sessionArrayList.size()>0) {
                ((ListSmallSessionsViewHolder) holder).setSessionImage(sessionArrayList.get(position).getImageUrl());
                ((ListSmallSessionsViewHolder) holder).setText1(sessionArrayList.get(position).getSessionName());

                Session session = sessionArrayList.get(position);

                String sessionDateAndTime = session.getSessionDate().textFullDay() + " " + session.getSessionDate().day + " " + session.getSessionDate().textMonth() + " " + session.textTime();
                sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                ((ListSmallSessionsViewHolder) holder).setText2(sessionDateAndTime);


                //holder.setText2(sessionArrayList.get(position).getSessionType());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date sessionDate = sdf.parse(sessionArrayList.get(position).getSessionDate().textSDF());
                    ((ListSmallSessionsViewHolder) holder).setSessionClickedListener(sessionArrayList.get(position).getLatitude(), sessionArrayList.get(position).getLongitude(), sessionDate);
                } catch (ParseException e) {
                    //handle exception
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return sessionArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (sessionsSectionHeaders.get(position) != null) {
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

        public void setSessionClickedListener(final double latitude, final double longitude, Date sessionDate) {
            ConstraintLayout frame = mView.findViewById(R.id.small_session_frame);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(latitude,longitude);
                }
            });

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final Calendar cal = Calendar.getInstance();
            Date todaysDate = cal.getTime();
            cal.add(Calendar.DATE,14);
            Date twoWeeksDate = cal.getTime();



            if (sessionDate.after(todaysDate) && sessionDate.before(twoWeeksDate)) {
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
            }

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
}



