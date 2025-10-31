package com.example.palayan.UserActivities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityFarmerRegistrationBinding;
import com.example.palayan.UserActivities.RegistrationFragment.GetStarted;
import com.example.palayan.UserActivities.RegistrationFragment.FarmerInfo;

public class FarmerRegistration extends AppCompatActivity {

    private ActivityFarmerRegistrationBinding root;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityFarmerRegistrationBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());
        viewPager = root.viewPager;
        viewPager.setAdapter(new RegistrationPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        viewPager.setUserInputEnabled(true);
    }

    public void goToStep(int stepIndex) {
        if (viewPager != null) {
            viewPager.setCurrentItem(stepIndex, true);
        }
    }

    private static class RegistrationPagerAdapter extends FragmentStateAdapter {
        public RegistrationPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new GetStarted();
            }
            return new FarmerInfo();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}