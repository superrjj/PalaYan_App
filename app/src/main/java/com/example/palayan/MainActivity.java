package com.example.palayan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.palayan.UserActivities.TermsAndConditionsActivity;
import com.example.palayan.UserActivities.UserDashboard;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private static final String PREFS_NAME = "PalaYanPrefs";
    private static final String KEY_FIRST_TIME = "first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        //for offline
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


        progressBar = findViewById(R.id.progressBar);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if it's the first time launching the app
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);
                
                Intent intent;
                if (isFirstTime) {
                    // First time - show Terms and Conditions
                    intent = new Intent(MainActivity.this, TermsAndConditionsActivity.class);
                    intent.putExtra("is_first_time", true);
                } else {
                    // Not first time - go directly to UserDashboard
                    intent = new Intent(MainActivity.this, UserDashboard.class);
                }
                startActivity(intent);
                finish();
            }
        }, 2000);

    }
}