package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.palayan.AdminActivities.AdminFragment.AdminDashboardFragment;
import com.example.palayan.R;
import com.example.palayan.UserActivities.MainActivity;
import com.google.android.material.navigation.NavigationView;

public class AdminDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private TextView tvAdminInitials, tvAdminFullName, tvRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolBar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolBar);

        drawerLayout = findViewById(R.id.admin_drawer_layout);
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Retrieve values from intent
        String userRole = getIntent().getStringExtra("userRole");
        String fullName = getIntent().getStringExtra("fullName");
        String initials = getIntent().getStringExtra("initials");
        int userId = getIntent().getIntExtra("userId", -1);

        // Access header views
        View headerView = navView.getHeaderView(0);
        tvAdminInitials = headerView.findViewById(R.id.tvInitialName);
        tvAdminFullName = headerView.findViewById(R.id.tvFullName);
        tvRole = headerView.findViewById(R.id.tvRole);  // NEW: get tvRole from header layout

        // Set values to header views
        tvAdminInitials.setText(initials != null ? initials : "--");
        tvAdminFullName.setText(fullName != null ? fullName : "Admin User");
        tvRole.setText(userRole != null ? userRole : "Unknown Role");  // Set role display

        // Load dashboard fragment and pass role and other data
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("userId", userId);
            bundle.putString("userRole", tvRole.getText().toString());  // Pass role from TextView
            bundle.putString("fullName", fullName);
            bundle.putString("initials", initials);

            AdminDashboardFragment fragment = new AdminDashboardFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_logout) {
            Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
