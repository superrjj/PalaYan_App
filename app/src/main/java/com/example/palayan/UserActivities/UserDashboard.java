package com.example.palayan.UserActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.palayan.AdminActivities.AdminDashboard;
import com.example.palayan.BottomFragment.HomeFragment;
import com.example.palayan.BottomFragment.RiceSeedsFragment;
import com.example.palayan.Helper.Validator.TextHelp;
import com.example.palayan.MenuFragment.DiseaseFragment;
import com.example.palayan.MenuFragment.PestFragment;
import com.example.palayan.MenuFragment.GuideFragment;
import com.example.palayan.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private ImageView logo;
    private FirebaseFirestore firestore;
    AlertDialog loadingDialog;

    private final long HOLD_DURATION = 2000;
    private final Handler handler = new Handler();
    private boolean triggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_user_dashboard);

        firestore = FirebaseFirestore.getInstance();

        MaterialToolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        //action bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //menu for bottom nav
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bot_nav_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            } else if (id == R.id.bot_nav_rice) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RiceSeedsFragment()).commit();
            } else if (id == R.id.bot_nav_text) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.palayan.BottomFragment.TextBasedFragment())
                        .commit();
            }
            return true;
        });

        //default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNav.setSelectedItemId(R.id.bot_nav_home);
        }

        View headerView = navView.getHeaderView(0);
        logo = headerView.findViewById(R.id.img_logo);

        //for long press admin login
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

    //for the menu drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_pest) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PestFragment()).commit();
        } else if (id == R.id.nav_disease) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiseaseFragment()).commit();
        } else if (id == R.id.nav_guide){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuideFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //show admin login dialog
    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.admin_login_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etUsername = dialogView.findViewById(R.id.txtUsername);
        TextInputEditText etPassword = dialogView.findViewById(R.id.txtPassword);

        TextInputLayout layoutUsername = dialogView.findViewById(R.id.layoutUsername);
        TextInputLayout layoutPassword = dialogView.findViewById(R.id.layoutPassword);
        TextHelp.addValidation(layoutUsername, etUsername, "Field required");
        TextHelp.addValidation(layoutPassword, etPassword, "Field required");

        Button btnLogin = dialogView.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                return;
            }

            //check the fields if not empty
            if (!TextHelp.isFilled(layoutUsername, etUsername, "Please fill out this field.") ||
                    !TextHelp.isFilled(layoutPassword, etPassword, "Please fill out this field.")) {
                return;
            }

            //loading dialog
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(50, 50, 50, 50);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            ProgressBar progressBar = new ProgressBar(this);
            layout.addView(progressBar);

            TextView message = new TextView(this);
            message.setText("Logging in...");
            message.setTextSize(16);
            message.setTextColor(Color.BLACK);
            message.setPadding(30, 0, 0, 0);
            layout.addView(message);

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setCancelable(false);
            builder1.setView(layout);
            loadingDialog = builder1.create();
            loadingDialog.show();

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

                            //get the initial name of the admin
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

                            //retrieving the data from the collection accounts
                            firestore.collection("accounts")
                                    .document(docId)
                                    .update("lastActive", com.google.firebase.firestore.FieldValue.serverTimestamp())
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
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
                            //validation for wrong username or password
                            layoutPassword.setError("Incorrect username or password.");
                            layoutUsername.setError("Incorrect username or password.");
                            loadingDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Login failed. Try again.", Toast.LENGTH_SHORT).show();
                    });
        });

        //text view for forgot password
        TextView btnForgotPassword = dialogView.findViewById(R.id.tv_forgot_password);
        btnForgotPassword.setOnClickListener(v -> {
            dialog.dismiss();
            showForgotPasswordUsernameDialog();
        });

    }

    //show the forgot password
    private void showForgotPasswordUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        //initialize the text layout, text input, button
        TextInputEditText txtEnterUsername = dialogView.findViewById(R.id.txtEnterUsername);
        Button btnNext = dialogView.findViewById(R.id.btnNextUsername);
        TextInputLayout layoutEnter = dialogView.findViewById(R.id.layoutEnterUsername);
        //live validation for username
        TextHelp.addValidation(layoutEnter, txtEnterUsername, "Field required");

        btnNext.setOnClickListener(v -> {
            //validation for empty field
            String username = txtEnterUsername.getText().toString().trim();
            if (username.isEmpty()) {
               TextHelp.isFilled(layoutEnter, txtEnterUsername, "Please fill out this field.");
                return;
            }

            //retrieve the username
            firestore.collection("accounts")
                    .whereEqualTo("username", username)
                    .whereEqualTo("archived", false)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        if (!snapshots.isEmpty()) {
                            String docId = snapshots.getDocuments().get(0).getId();
                            String secQ1 = snapshots.getDocuments().get(0).getString("security1");
                            String secQ2 = snapshots.getDocuments().get(0).getString("security2");

                            dialog.dismiss();
                            showSecurityQuestionsDialog(docId, secQ1, secQ2);
                        } else {
                            //if the username is not already created
                           layoutEnter.setError("Username not found");
                        }
                    });
        });
    }

    //show the security question
    private void showSecurityQuestionsDialog(String docId, String secQ1, String secQ2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_security_questions, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        //initialize the text input
        TextInputEditText txtSecOne = dialogView.findViewById(R.id.txtSecOne);
        TextInputEditText txtSecTwo = dialogView.findViewById(R.id.txtSecTwo);
        Button btnNextSec = dialogView.findViewById(R.id.btnConfirm);

        //initialize the text layout
        TextInputLayout layoutSecOne = dialogView.findViewById(R.id.layoutSecOne);
        TextInputLayout layoutSecTwo = dialogView.findViewById(R.id.layoutSecTwo);


        btnNextSec.setOnClickListener(v -> {
            //validation for empty fields
            if (!TextHelp.isFilled(layoutSecOne, txtSecOne, "Please fill out this field.") ||
                    !TextHelp.isFilled(layoutSecTwo, txtSecTwo, "Please fill out this field.")) {
                return;
            }

            String ansOne = txtSecOne.getText().toString().trim();
            String ansTwo = txtSecTwo.getText().toString().trim();

            //retrieve the correct answer
            firestore.collection("accounts").document(docId).get()
                    .addOnSuccessListener(document -> {
                        String correctOne = document.getString("security1");
                        String correctTwo = document.getString("security2");


                        boolean isCorrect1 = ansOne.equals(correctOne);
                        boolean isCorrect2 = ansTwo.equals(correctTwo);


                        layoutSecOne.setError(null);
                        layoutSecTwo.setError(null);

                        //dismiss the dialog if correct the answer
                        if (isCorrect1 && isCorrect2) {
                            dialog.dismiss();
                            showNewPasswordDialog(docId);
                        } else {
                            //validation for wrong answer
                            if (!isCorrect1) layoutSecOne.setError("Incorrect answer");
                            if (!isCorrect2) layoutSecTwo.setError("Incorrect answer");
                        }
                    });
        });

    }

    //show the dialog for reset password
    private void showNewPasswordDialog(String docId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_new_password, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        //initialize the text input
        TextInputEditText etNewPass = view.findViewById(R.id.txtNewPassword);
        TextInputEditText etConfirmPass = view.findViewById(R.id.txtConfirmPass);
        Button btnConfirm = view.findViewById(R.id.btnSubmit);

        //initialize the text layout
        TextInputLayout layoutNewPass = view.findViewById(R.id.layoutNewPass);
        TextInputLayout layoutConfirmPass = view.findViewById(R.id.layoutConfrimPass);
        //live validation for text change
        TextHelp.addValidation(layoutNewPass, etNewPass, "Field required");
        TextHelp.addValidation(layoutConfirmPass, etConfirmPass, "Field required");

        btnConfirm.setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            //validation for empty fields
            if (!TextHelp.isFilled(layoutNewPass, etNewPass, "Please fill out this field.") ||
                    !TextHelp.isFilled(layoutConfirmPass, etConfirmPass, "Please fill out this field.")) {
                return;
            }

            //validations for password match
            if (!newPass.equals(confirmPass)) {
                layoutConfirmPass.setError("Passwords do not match");
                return;
            } else {
                layoutConfirmPass.setError(null);
            }

            //reset the password
            firestore.collection("accounts").document(docId)
                    .update("password", newPass)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Password reset successfully.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        showAdminLoginDialog();
                    });
        });
    }

    //to check the internet if available
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
