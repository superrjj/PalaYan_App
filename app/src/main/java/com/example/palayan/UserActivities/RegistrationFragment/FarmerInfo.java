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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.example.palayan.R;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farmer_info, container, false);

        AutoCompleteTextView actProvince = view.findViewById(R.id.act_probinsya);
        AutoCompleteTextView actMunicipality = view.findViewById(R.id.act_municipality);
        AutoCompleteTextView actBarangay = view.findViewById(R.id.act_barangay);
        AutoCompleteTextView actGender = view.findViewById(R.id.act_kasarian);
        CheckBox chkTerms = view.findViewById(R.id.chkTerms);

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

        return view;
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

