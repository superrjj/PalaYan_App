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
import com.example.palayan.TabFragment.FavoritesFragment;
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
                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(new AllFragment());
                        break;
                    case 1:
                        loadFragment(new TarlacFragment());
                        break;
                    case 2:
                        loadFragment(new FavoritesFragment());
                        break;
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

        root.ivFilter.setOnClickListener(v -> {
            RiceFilterBottomSheetDialogFragment bottomSheet = new RiceFilterBottomSheetDialogFragment();
            if (currentFragment != null) {
                bottomSheet.setTargetFragment(currentFragment, 0);
            }
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });

        return root.getRoot();
    }

    private void setupCustomTabs() {
        String[] tabTitles = {"All", "Tarlac Province", "Favorites"};
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

                    String currentQuery = root.svSearchBar.getQuery().toString();
                    // Safe delay via post para sure attached na ang view at adapter
                    root.getRoot().post(() -> {
                        if (fragment instanceof SearchableFragment) {
                            ((SearchableFragment) fragment).filter(currentQuery);
                        }
                    });
                })
                .commit();
    }
}
