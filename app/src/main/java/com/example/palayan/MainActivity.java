package com.example.palayan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.palayan.UserActivities.FarmerRegistration;
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
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                FirebaseFirestore.getInstance()
                        .collection("farmers")
                        .document(deviceId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            Class<?> next = doc.exists() ? com.example.palayan.UserActivities.UserDashboard.class : FarmerRegistration.class;
                            Intent intent = new Intent(MainActivity.this, next);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Intent intent = new Intent(MainActivity.this, FarmerRegistration.class);
                            startActivity(intent);
                            finish();
                        });
            }
        }, 2000);

    }
}