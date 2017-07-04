package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    ListView list;
    ArrayList<String> titles;
    ArrayList<String> description;
    int [] imgs = {R.drawable.twitter, R.drawable.twitter, R.drawable.twitter, R.drawable.twitter};
    ArrayList<String> sessionNameArray;
    ArrayList<Session> sessionArray;
    public MyAdapter adapter;
    String test;
    public LatLng sessionLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Resources res = getResources();

        titles =  new ArrayList<>();
        description = new ArrayList<>();
        sessionNameArray= new ArrayList<>();
        sessionArray= new ArrayList<>();
        test = "hej";

        list = (ListView) findViewById(R.id.list1);
        adapter = new MyAdapter(this, titles,imgs,description);

        usersDbRef.child(currentFirebaseUser.getUid()).child("sessions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user.sessions.add(snapshot.getKey().toString());
                }
                sessionNameArray =user.sessions;
                populateSessionArray(user.sessions);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sessionLatLng = new LatLng(sessionArray.get(position).latitude, sessionArray.get(position).longitude);
                joinSession(sessionLatLng);
            }
        });
    }

    public void joinSession(LatLng markerLatLng) {
        Intent intent = new Intent(this, JoinSessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }

    public void populateSessionArray(final ArrayList<String> sessionNameArray){
        dbRef.child("sessions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Session session;
                for (int i=0; i < sessionNameArray.size(); i++){
                    session = dataSnapshot.child(sessionNameArray.get(i)).getValue(Session.class);
                    sessionArray.add(session);
                }
                for (int i=0; i < sessionArray.size(); i++){
                    description.add(sessionArray.get(i).sessionType + ' ' + sessionArray.get(i).sessionDate.day + '/' + sessionArray.get(i).sessionDate.month + ' ' + sessionArray.get(i).sessionDate.hour + ':' +sessionArray.get(i).sessionDate.minute);
                    titles.add(sessionArray.get(i).sessionName);
                }
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class MyAdapter extends ArrayAdapter<String>{
        Context context;
        int [] imgs;
        ArrayList<String> myTitles;
        ArrayList<String> myDescription;

        MyAdapter(Context c, ArrayList<String> titles, int[] imgs, ArrayList<String> description){
          super(c,R.layout.row,R.id.text1,titles);
            this.context = c;
            this.imgs =imgs;
            this.myDescription = description;
            this.myTitles = titles;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row,parent,false);
            ImageView images = (ImageView) row.findViewById(R.id.icon);
            TextView myTitle =(TextView) row.findViewById(R.id.text1);
            TextView myDescription = (TextView) row.findViewById(R.id.text2);
            images.setImageResource(imgs[position]);
            myTitle.setText(titles.get(position));
            myDescription.setText(description.get(position));
            return row;
        }
    }
}
