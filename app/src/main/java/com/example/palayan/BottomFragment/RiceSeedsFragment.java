package com.example.palayan.BottomFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.palayan.Helper.SearchQuery.SearchableFragment;
import com.example.palayan.R;
import com.example.palayan.TabFragment.AllFragment;
import com.example.palayan.TabFragment.TarlacFragment;
import com.example.palayan.databinding.FragmentRiceSeedsBinding;
import com.google.android.material.tabs.TabLayout;

public class RiceSeedsFragment extends Fragment {

    private FragmentRiceSeedsBinding root;
    private Fragment currentFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentRiceSeedsBinding.inflate(inflater, container, false);

        setupCustomTabs();
        loadFragment(new AllFragment()); // default

        root.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        root.svSearchBar.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (currentFragment instanceof SearchableFragment) {
                    ((SearchableFragment) currentFragment).filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentFragment instanceof SearchableFragment) {
                    ((SearchableFragment) currentFragment).filter(newText);
                }
                return true;
            }
        });

        return root.getRoot();
    }

    private void setupCustomTabs() {
        String[] tabTitles = {"All", "Tarlac Province"};
        for (String title : tabTitles) {
            TabLayout.Tab tab = root.tabLayout.newTab();
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabText = customView.findViewById(R.id.tabText);
            tabText.setText(title);
            tab.setCustomView(customView);
            root.tabLayout.addTab(tab);
            root.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        }
        updateTabView(root.tabLayout.getTabAt(0), true);
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
                .runOnCommit(() -> {
                    currentFragment = fragment;

                    // Reapply current query to new fragment
                    String currentQuery = root.svSearchBar.getQuery().toString();
                    if (fragment instanceof SearchableFragment) {
                        ((SearchableFragment) fragment).filter(currentQuery);
                    }
                })
                .commit();
    }
}
