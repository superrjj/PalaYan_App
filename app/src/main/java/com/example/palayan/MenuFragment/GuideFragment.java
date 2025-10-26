package com.example.palayan.MenuFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.palayan.R;
import com.example.palayan.UserActivities.TermsAndConditionsActivity;

public class GuideFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        
        // Navigate to Terms and Conditions when fragment is shown
        Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
        startActivity(intent);
        
        // Go back after navigation
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Return a simple invisible view
        View view = new View(getContext());
        view.setVisibility(View.GONE);
        return view;
    }
}