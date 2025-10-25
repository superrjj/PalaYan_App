package com.example.palayan.UserActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.example.palayan.R;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private TextView tvTermsContent;
    private LinearLayout buttonContainer;
    private Button btnAccept, btnDecline;
    private boolean isFirstTime = false;
    private static final String PREFS_NAME = "PalaYanPrefs";
    private static final String KEY_FIRST_TIME = "first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_terms_and_conditions);

        // Check if this is first time launch
        isFirstTime = getIntent().getBooleanExtra("is_first_time", false);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (isFirstTime) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle("Welcome to PalaYan");
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Terms and Conditions");
            }
        }

        tvTermsContent = findViewById(R.id.tv_terms_content);
        buttonContainer = findViewById(R.id.button_container);
        btnAccept = findViewById(R.id.btn_accept);
        btnDecline = findViewById(R.id.btn_decline);

        loadTermsContent();
        setupButtons();
    }

    private void loadTermsContent() {
        String termsContent = getString(R.string.terms_and_conditions_content);
        tvTermsContent.setText(termsContent);
    }

    private void setupButtons() {
        if (isFirstTime) {
            // Show Accept/Decline buttons for first time users
            buttonContainer.setVisibility(LinearLayout.VISIBLE);
            
            btnAccept.setOnClickListener(v -> {
                // Mark as not first time anymore
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();
                
                // Go to UserDashboard
                Intent intent = new Intent(this, UserDashboard.class);
                startActivity(intent);
                finish();
            });
            
            btnDecline.setOnClickListener(v -> {
                // User declined terms, exit app
                finishAffinity();
            });
        } else {
            // Hide buttons for regular viewing
            buttonContainer.setVisibility(LinearLayout.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isFirstTime) {
            // Don't allow back press on first time - user must accept or decline
            return;
        }
        super.onBackPressed();
    }
}
