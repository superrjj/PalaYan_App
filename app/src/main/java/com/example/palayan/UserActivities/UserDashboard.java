package com.example.palayan.UserActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.palayan.AdminActivities.AdminDashboard;
import com.example.palayan.BottomFragment.HomeFragment;
import com.example.palayan.BottomFragment.RiceSeedsFragment;
import com.example.palayan.MenuFragment.DiseaseFragment;
import com.example.palayan.MenuFragment.PestFragment;
import com.example.palayan.MenuFragment.GuideFragment;
import com.example.palayan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private ImageView logo;
    private FirebaseFirestore firestore;

    private final long HOLD_DURATION = 3000;
    private final Handler handler = new Handler();
    private boolean triggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);

        firestore = FirebaseFirestore.getInstance();

        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bot_nav_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            } else if (id == R.id.bot_nav_rice) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RiceSeedsFragment()).commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNav.setSelectedItemId(R.id.bot_nav_home);
        }

        View headerView = navView.getHeaderView(0);
        logo = headerView.findViewById(R.id.img_logo);

        if (logo != null) {
            logo.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        triggered = false;
                        handler.postDelayed(() -> {
                            triggered = true;
                            showAdminLoginDialog();
                        }, HOLD_DURATION);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacksAndMessages(null);
                        return true;
                }
                return false;
            });
        } else {
            Toast.makeText(this, "Logo not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_pest) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PestFragment()).commit();
        } else if (id == R.id.nav_disease) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiseaseFragment()).commit();
        } else if (id == R.id.nav_disease){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuideFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.admin_login_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etUsername = dialogView.findViewById(R.id.txtUsername);
        TextInputEditText etPassword = dialogView.findViewById(R.id.txtPassword);
        Button btnLogin = dialogView.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            firestore.collection("accounts")
                    .whereEqualTo("username", username)
                    .whereEqualTo("password", password)
                    .whereEqualTo("archived", false)//for non deleted accounts
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            String role = queryDocumentSnapshots.getDocuments().get(0).getString("role");
                            String fullName = queryDocumentSnapshots.getDocuments().get(0).getString("fullName");
                            int userId = queryDocumentSnapshots.getDocuments().get(0).getLong("userId").intValue();

                            String initials;
                            if (fullName != null) {
                                String[] nameParts = fullName.split(" ");
                                if (nameParts.length >= 2) {
                                    initials = nameParts[0].substring(0, 1).toUpperCase() + nameParts[1].substring(0, 1).toUpperCase();
                                } else if (nameParts.length == 1) {
                                    initials = nameParts[0].substring(0, 1).toUpperCase();
                                } else {
                                    initials = "";
                                }
                            } else {
                                initials = "";
                            }


                            firestore.collection("accounts")
                                    .document(docId)
                                    .update("lastActive", com.google.firebase.firestore.FieldValue.serverTimestamp())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(UserDashboard.this, AdminDashboard.class);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("userRole", role);
                                        intent.putExtra("fullName", fullName);
                                        intent.putExtra("initials", initials);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update last active.", Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm.getActiveNetworkInfo();
        return active != null && active.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
