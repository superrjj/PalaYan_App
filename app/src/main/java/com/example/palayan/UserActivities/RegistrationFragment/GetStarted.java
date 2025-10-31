package com.example.palayan.UserActivities.RegistrationFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.palayan.R;
import com.example.palayan.UserActivities.FarmerRegistration;

public class GetStarted extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_started, container, false);
        Button btnNext = view.findViewById(R.id.btnMagpatuloy);
        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof FarmerRegistration) {
                ((FarmerRegistration) getActivity()).goToStep(1);
            }
        });
        return view;
    }
}

