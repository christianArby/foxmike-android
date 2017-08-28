package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListSessionsActivity extends AppCompatActivity {

    private RecyclerView mSessionList;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sessions);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("sessions");

        mSessionList = (RecyclerView) findViewById(R.id.session_list);
        mSessionList.setHasFixedSize(true);
        mSessionList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Session, SessionViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Session, SessionViewHolder>(

                Session.class,
                R.layout.session_row,
                SessionViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(SessionViewHolder viewHolder, Session model, int position) {

                viewHolder.setTitle(model.getSessionName());
                viewHolder.setDesc(model.getSessionType());
                viewHolder.setImage(getApplicationContext(),model.getImageUri());

            }
        };

        mSessionList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder{


        View mView;


        public SessionViewHolder(View itemView) {
            super(itemView);

            mView= itemView;
        }

        public void setTitle(String title){

            TextView session_title = (TextView) mView.findViewById(R.id.session_title);
            session_title.setText(title);

        }

        public void setDesc(String desc){

            TextView session_desc = (TextView) mView.findViewById(R.id.session_desc);
            session_desc.setText(desc);

        }

        public void setImage(Context ctx, String image){
            ImageView session_image = (ImageView) mView.findViewById(R.id.session_image);
            Glide.with(ctx).load(image).into(session_image);
        }

    }


}
