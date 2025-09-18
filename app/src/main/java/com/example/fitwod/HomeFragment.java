package com.example.fitwod;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bessadi.fitwod.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnAchievements = view.findViewById(R.id.btnGoToAchievements);
        Button btnWorkout = view.findViewById(R.id.btnGoToWorkout);

        btnAchievements.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.achievementsFragment);
        });

        btnWorkout.setOnClickListener(v -> {
            // Navigate to workout fragment if you have one
            // Navigation.findNavController(v).navigate(R.id.workoutFragment);
        });

        return view;
    }
}