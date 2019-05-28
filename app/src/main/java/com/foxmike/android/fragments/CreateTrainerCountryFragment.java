package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.foxmike.android.R;

public class CreateTrainerCountryFragment extends Fragment {

    private OnCreateTrainerCountryListener onCreateTrainerCountryListener;
    private AppCompatButton nextBtn;

    public CreateTrainerCountryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_trainer_country, container, false);

        nextBtn = view.findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateTrainerCountryListener.onCreateTrainerCountry("SE");
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerCountryListener) {
            onCreateTrainerCountryListener = (OnCreateTrainerCountryListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerCountryListener = null;
    }

    public interface OnCreateTrainerCountryListener {
        void onCreateTrainerCountry(String country);
    }
}
