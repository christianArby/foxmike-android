package com.foxmike.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.foxmike.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.foxmike.android.utils.StaticResources.DURATION_INTEGERS;
import static com.foxmike.android.utils.StaticResources.DURATION_STRINGS;

public class SetDurationActivity extends AppCompatActivity {
    @BindView(R.id.durationListView)
    ListView listView;
    private int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_duration);
        ButterKnife.bind(this);

        String[] stringArray = getResources().getStringArray(R.array.duration_array);

        String standard = DURATION_STRINGS.get(getIntent().getIntExtra("standardDuration", 10));
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
                int selectedDuration = DURATION_INTEGERS.get(arrayAdapter.getItem(position));
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedDuration", selectedDuration);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
