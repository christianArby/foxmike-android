package com.foxmike.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.foxmike.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetMaxParticipantsActivity extends AppCompatActivity {
    @BindView(R.id.maxParticipantsListView)
    ListView listView;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_max_participants);

        ButterKnife.bind(this);

        String[] stringArray = getResources().getStringArray(R.array.max_participants_array);

        String standard = Integer.toString(getIntent().getIntExtra("standardMaxParticipants", 10));
        index = -1;
        for (int i=0;i<stringArray.length;i++) {
            if (stringArray[i].equals(standard)) {
                index = i;
                break;
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringArray) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                if (position == index) {
                    row.setBackgroundColor(getResources().getColor(R.color.foxmikeSelectedColor));
                } else {
                    row.setBackgroundColor(getResources().getColor(R.color.color_background_light));
                }
                return row;
            }
        };

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int maxParticipants = Integer.parseInt(arrayAdapter.getItem(position));
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedMaxParticipants", maxParticipants);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
