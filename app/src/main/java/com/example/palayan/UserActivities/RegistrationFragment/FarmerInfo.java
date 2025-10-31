package com.example.palayan.UserActivities.RegistrationFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.provider.Settings;
import android.content.Intent;
import android.os.Handler;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.palayan.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.palayan.UserActivities.UserDashboard;
import androidx.core.content.res.ResourcesCompat;

public class FarmerInfo extends Fragment {

    private static final String[] PROVINCES = new String[] { "Tarlac" };

    private static final String[] TARLAC_MUNICIPALITIES = new String[] {
            "Anao",
            "Bamban",
            "Camiling",
            "Capas",
            "Concepcion",
            "Gerona",
            "La Paz",
            "Mayantoc",
            "Moncada",
            "Paniqui",
            "Pura",
            "Ramos",
            "San Clemente",
            "San Jose",
            "San Manuel",
            "Santa Ignacia",
            "Tarlac City",
            "Victoria"
    };

    private static final String[] GENDERS = new String[] { "Lalake", "Babae" };

    // Terms text moved to strings resource (terms_conditions_tl)

    // PSGC API endpoints
    private static final String PSGC_BASE = "https://psgc.rootscratch.com";
    // PSGC ID for Tarlac Province
    private static final String TARLAC_PSGC_ID = "0369000000";

    // Cache and mapping for dynamic data
    private java.util.Map<String, String> municipalityNameToId = new java.util.HashMap<>();
    private java.util.List<String> dynamicMunicipalities = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farmer_info, container, false);

        AutoCompleteTextView actProvince = view.findViewById(R.id.act_probinsya);
        AutoCompleteTextView actMunicipality = view.findViewById(R.id.act_municipality);
        AutoCompleteTextView actBarangay = view.findViewById(R.id.act_barangay);
        AutoCompleteTextView actGender = view.findViewById(R.id.act_kasarian);
        CheckBox chkTerms = view.findViewById(R.id.chkTerms);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        TextInputEditText etFirst = view.findViewById(R.id.txtUnangPangalan);
        TextInputEditText etMiddle = view.findViewById(R.id.txtGitnangPangalan);
        TextInputEditText etLast = view.findViewById(R.id.txtApelyido);
        TextInputEditText etAge = view.findViewById(R.id.txtEdad);
        TextInputEditText etPhone = view.findViewById(R.id.txtTelepono);
        TextInputEditText etEmail = view.findViewById(R.id.txtEmailAddress);

        // Province adapter (Tarlac only)
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, PROVINCES);
        actProvince.setAdapter(provinceAdapter);
        actProvince.setOnItemClickListener((parent, v, position, id) -> {
            // Reset municipality and barangay when province changes
            actMunicipality.setText("");
            actBarangay.setText("");
            setupMunicipalities(actMunicipality, actBarangay);
        });

        // Initialize municipality and barangay lists
        setupMunicipalities(actMunicipality, actBarangay);

        // Gender adapter (Lalake/Babae)
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, GENDERS);
        actGender.setAdapter(genderAdapter);

        chkTerms.setOnClickListener(v -> {
            if (chkTerms.isChecked()) {
                showTermsDialog(chkTerms);
            }
        });

        btnRegister.setOnClickListener(v -> handleRegister(
                v,
                etFirst.getText().toString().trim(),
                etMiddle.getText().toString().trim(),
                etLast.getText().toString().trim(),
                etAge.getText().toString().trim(),
                actGender.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                actProvince.getText().toString().trim(),
                actMunicipality.getText().toString().trim(),
                actBarangay.getText().toString().trim(),
                chkTerms.isChecked()
        ));

        return view;
    }

    private void handleRegister(View anchor,
                                String first,
                                String middle,
                                String last,
                                String age,
                                String gender,
                                String phone,
                                String email,
                                String province,
                                String municipality,
                                String barangay,
                                boolean acceptedTerms) {
        // Basic validations
        if (!acceptedTerms) {
            showSnack(anchor, "Pakisundin ang tuntunin at kondisyon muna.", R.color.dark_red);
            return;
        }
        if (first.isEmpty() || last.isEmpty() || age.isEmpty() || gender.isEmpty() ||
                province.isEmpty() || municipality.isEmpty() || barangay.isEmpty()) {
            showSnack(anchor, "Pakipunan ang mga kinakailangang field.", R.color.dark_red);
            return;
        }

        int ageInt;
        try {
            ageInt = Integer.parseInt(age);
        } catch (NumberFormatException e) {
            showSnack(anchor, "Di wastong edad.", R.color.dark_red);
            return;
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        com.google.firebase.Timestamp nowTs = com.google.firebase.Timestamp.now();
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        data.put("first_name", first);
        data.put("middle_name", middle);
        data.put("last_name", last);
        data.put("age", ageInt);
        data.put("gender", gender);
        data.put("phone", phone);
        data.put("email", email);
        data.put("province", province);
        data.put("municipality", municipality);
        data.put("barangay", barangay);
        data.put("device_id", deviceId);
        data.put("created_at", nowTs);
        data.put("updated_at", null);

        FirebaseFirestore.getInstance()
                .collection("farmers")
                .document(deviceId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    showSnack(anchor, "Matagumpay na nairehistro.", R.color.green, 2000);
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(requireContext(), UserDashboard.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }, 2000);
                })
                .addOnFailureListener(err -> showSnack(anchor, "Hindi nairehistro: " + err.getMessage(), R.color.dark_red));
    }

    private void showSnack(View anchor, String message, int colorRes) {
        showSnack(anchor, message, colorRes, 3000);
    }

    private void showSnack(View anchor, String message, int colorRes, int durationMs) {
        Snackbar sb = Snackbar.make(anchor, message, Snackbar.LENGTH_INDEFINITE);
        sb.setDuration(durationMs);
        sb.setBackgroundTint(ContextCompat.getColor(requireContext(), colorRes));
        sb.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        TextView tv = sb.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) {
            android.graphics.Typeface tf = ResourcesCompat.getFont(requireContext(), R.font.poppins__regular);
            if (tf != null) tv.setTypeface(tf);
        }
        sb.show();
    }

    private void showTermsDialog(CheckBox chkTerms) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_terms_conditions, null, false);
        ScrollView scrollView = dialogView.findViewById(R.id.scrollTerms);
        Button btnAgree = dialogView.findViewById(R.id.btnAgree);
        Button btnDecline = dialogView.findViewById(R.id.btnDecline);

        androidx.appcompat.widget.AppCompatTextView tv = dialogView.findViewById(R.id.tvTerms);
        tv.setText(getString(R.string.terms_conditions_tl));

        btnAgree.setEnabled(false);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isScrollAtBottom(scrollView)) {
                btnAgree.setEnabled(true);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnAgree.setOnClickListener(v -> {
            chkTerms.setChecked(true);
            dialog.dismiss();
        });
        btnDecline.setOnClickListener(v -> {
            chkTerms.setChecked(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean isScrollAtBottom(ScrollView scrollView) {
        View child = scrollView.getChildAt(0);
        if (child == null) return false;
        int diff = child.getMeasuredHeight() - (scrollView.getScrollY() + scrollView.getHeight());
        return diff <= 0;
    }

    private void setupMunicipalities(AutoCompleteTextView actMunicipality, AutoCompleteTextView actBarangay) {
        ArrayAdapter<String> municipalitiesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, TARLAC_MUNICIPALITIES);
        actMunicipality.setAdapter(municipalitiesAdapter);
        actMunicipality.setOnItemClickListener((parent, v, position, id) -> {
            String selectedMunicipality = TARLAC_MUNICIPALITIES[position];
            actBarangay.setText("");
            setupBarangays(actBarangay, selectedMunicipality);
        });
    }

    private void setupBarangays(AutoCompleteTextView actBarangay, String municipality) {
        String[] barangays;
        switch (municipality) {
            case "Tarlac City":
                barangays = getResources().getStringArray(R.array.tarlac_city_barangays);
                break;
            case "Gerona":
                barangays = getResources().getStringArray(R.array.gerona_barangays);
                break;
            case "Concepcion":
                barangays = getResources().getStringArray(R.array.concepcion_barangays);
                break;
            case "La Paz":
                barangays = getResources().getStringArray(R.array.la_paz_barangays);
                break;
            case "San Jose":
                barangays = getResources().getStringArray(R.array.san_jose_barangays);
                break;
            case "Victoria":
                barangays = getResources().getStringArray(R.array.victoria_barangays);
                break;
            case "Santa Ignacia":
                barangays = getResources().getStringArray(R.array.santa_ignacia_barangays);
                break;
            case "Mayantoc":
                barangays = getResources().getStringArray(R.array.mayantoc_barangays);
                break;
            case "Camiling":
                barangays = getResources().getStringArray(R.array.camiling_barangays);
                break;
            case "San Clemente":
                barangays = getResources().getStringArray(R.array.san_clemente_barangays);
                break;
            case "San Manuel":
                barangays = getResources().getStringArray(R.array.san_manuel_barangays);
                break;
            case "Moncada":
                barangays = getResources().getStringArray(R.array.moncada_barangays);
                break;
            case "Paniqui":
                barangays = getResources().getStringArray(R.array.paniqui_barangays);
                break;
            case "Pura":
                barangays = getResources().getStringArray(R.array.pura_barangays);
                break;
            case "Anao":
                barangays = getResources().getStringArray(R.array.anao_barangays);
                break;
            case "Ramos":
                barangays = getResources().getStringArray(R.array.ramos_barangays);
                break;
            case "Capas":
                barangays = getResources().getStringArray(R.array.capas_barangays);
                break;
            case "Bamban":
                barangays = getResources().getStringArray(R.array.bamban_barangays);
                break;
            default:
                barangays = new String[] { };
        }

        ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, barangays);
        actBarangay.setAdapter(barangayAdapter);
    }
}

