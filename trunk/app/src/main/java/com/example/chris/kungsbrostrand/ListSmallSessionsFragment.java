package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.LAYER_TYPE_HARDWARE;


public class ListSmallSessionsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnSessionClickedListener onSessionClickedListener;

    private RecyclerView smallSessionsListRV;
    private RecyclerView.Adapter<ListSmallSessionsViewHolder> listSmallSessionsViewHolderAdapter;
    private ArrayList<Session> sessionArrayList = new ArrayList<>();


    public ListSmallSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSmallSessionsFragment newInstance(ArrayList<Session> sessionArrayList) {


        ListSmallSessionsFragment fragment = new ListSmallSessionsFragment();
        Bundle args = new Bundle();
        String str = new Gson().toJson(sessionArrayList);
        args.putString("str",str);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String str = getArguments().getString("str");
            sessionArrayList = new Gson().fromJson(str, new TypeToken<ArrayList<Session>>(){}.getType());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_small_sessions, container, false);

        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);

        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));

        listSmallSessionsViewHolderAdapter = new RecyclerView.Adapter<ListSmallSessionsViewHolder>() {
            @Override
            public ListSmallSessionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.session_small_single_layout, parent, false);
                return new ListSmallSessionsViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ListSmallSessionsViewHolder holder, int position) {
                if (sessionArrayList.size()>0) {
                    holder.setSessionImage(sessionArrayList.get(position).getImageUrl());
                    holder.setText1(sessionArrayList.get(position).getSessionName());

                    Session session = sessionArrayList.get(position);

                    String sessionDateAndTime = session.getSessionDate().textFullDay() + " " + session.getSessionDate().day + " " + session.getSessionDate().textMonth() + " " + session.textTime();
                    sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
                    holder.setText2(sessionDateAndTime);


                    //holder.setText2(sessionArrayList.get(position).getSessionType());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date sessionDate = sdf.parse(sessionArrayList.get(position).getSessionDate().textSDF());
                        holder.setSessionClickedListener(sessionArrayList.get(position).getLatitude(), sessionArrayList.get(position).getLongitude(), sessionDate);
                    } catch (ParseException e) {
                        //handle exception
                    }
                }
            }

            @Override
            public int getItemCount() {
                return sessionArrayList.size();
            }
        };



        smallSessionsListRV.setAdapter(listSmallSessionsViewHolderAdapter);
        listSmallSessionsViewHolderAdapter.notifyDataSetChanged();

        return view;


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
            Glide.with(getContext()).load(sessionImage).into(sessionIV);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }

}
