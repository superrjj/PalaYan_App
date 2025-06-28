package com.example.palayan.BottomFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.palayan.R;
import com.example.palayan.TabFragment.AllFragment;
import com.example.palayan.TabFragment.TarlacFragment;
import com.google.android.material.tabs.TabLayout;

public class RiceSeedsFragment extends Fragment {

    private TabLayout tabLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rice_seeds, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupCustomTabs();
        loadFragment(new AllFragment()); //default fragment

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabView(tab, true);
                if (tab.getPosition() == 0) {
                    loadFragment(new AllFragment());
                } else {
                    loadFragment(new TarlacFragment());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //No need to input code here
            }
        });

        return view;
    }

    private void setupCustomTabs() {
        String[] tabTitles = {"All", "Tarlac Province"};
        for (String title : tabTitles) {
            TabLayout.Tab tab = tabLayout.newTab();
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabText = customView.findViewById(R.id.tabText);
            tabText.setText(title);
            tab.setCustomView(customView);
            tabLayout.addTab(tab);
        }

        //highlight first tab as selected initially
        updateTabView(tabLayout.getTabAt(0), true);
    }

    private void updateTabView(TabLayout.Tab tab, boolean selected) {
        if (tab != null && tab.getCustomView() != null) {
            tab.getCustomView().setSelected(selected);
        }
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
