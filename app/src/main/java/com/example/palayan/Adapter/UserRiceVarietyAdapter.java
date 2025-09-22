package com.example.palayan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.RiceVarietyInformation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserRiceVarietyAdapter extends RecyclerView.Adapter<UserRiceVarietyAdapter.UserViewHolder> {

    private List<RiceVariety> list;
    private Context context;
    private boolean isFavoritesPage;
    private Set<String> favoriteIds;
    private Map<String, String> documentIdMap; // Map variety name to document ID

    public UserRiceVarietyAdapter(List<RiceVariety> list, Context context, Set<String> favoriteIds, boolean isFavoritesPage) {
        this.list = list;
        this.context = context;
        this.favoriteIds = favoriteIds;
        this.isFavoritesPage = isFavoritesPage;
        this.documentIdMap = new HashMap<>();
    }

    // Add method to set document ID mapping
    public void setDocumentIdMap(Map<String, String> documentIdMap) {
        this.documentIdMap = documentIdMap;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_rice_seed_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        RiceVariety variety = list.get(position);
        holder.varietyName.setText(variety.varietyName != null ? variety.varietyName : "Unknown");
        holder.location.setText(variety.location != null ? variety.location : "Unknown");

        // Combine breeder origin and year release (or fallback text if null)
        String origin = variety.breederOrigin != null ? variety.breederOrigin : "Unknown";
        String year = variety.yearRelease != null ? variety.yearRelease : "N/A";
        holder.breederYear.setText(origin + ", " + year);

        // Make all variables used in lambda final
        final String deviceId = DeviceUtils.getDeviceId(context);
        final String deviceModel = android.os.Build.MODEL;
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final String varietyName = variety.varietyName != null ? variety.varietyName : "unknown_" + position;

        if (isFavoritesPage || (favoriteIds != null && favoriteIds.contains(varietyName))) {
            holder.ivFavorites.setImageResource(R.drawable.ic_fav_filled);
        } else {
            holder.ivFavorites.setImageResource(R.drawable.ic_fav_outline);
        }

        holder.ivFavorites.setOnClickListener(v -> {
            firestore.collection("rice_seed_favorites")
                    .whereEqualTo("deviceId", deviceId)
                    .whereEqualTo("rice_seed_id", varietyName)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            // Remove from favorites
                            for (QueryDocumentSnapshot doc : snapshot) {
                                firestore.collection("rice_seed_favorites").document(doc.getId()).delete();
                            }
                            holder.ivFavorites.setImageResource(R.drawable.ic_fav_outline);
                            if (favoriteIds != null) {
                                favoriteIds.remove(varietyName);
                            }
                        } else {
                            // Add to favorites
                            Map<String, Object> favorite = new HashMap<>();
                            favorite.put("deviceId", deviceId);
                            favorite.put("deviceModel", deviceModel);
                            favorite.put("rice_seed_id", varietyName);
                            firestore.collection("rice_seed_favorites").add(favorite);
                            holder.ivFavorites.setImageResource(R.drawable.ic_fav_filled);
                            if (favoriteIds != null) {
                                favoriteIds.add(varietyName);
                            }
                        }
                    });
        });

        // Go to RiceVarietyInformation Activity on item click
        holder.cvRiceDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, RiceVarietyInformation.class);

            // Use variety name as key to get document ID
            String documentId = documentIdMap.get(varietyName);


            if (documentId != null) {
                intent.putExtra("document_id", documentId);
                intent.putExtra("rice_seed_id", varietyName);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Error: No document ID found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView varietyName, location, breederYear;
        ImageView ivFavorites;
        CardView cvRiceDetails;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            varietyName = itemView.findViewById(R.id.txtVarietyName);
            location = itemView.findViewById(R.id.txtLocation);
            breederYear = itemView.findViewById(R.id.txtBreederYearRelease);
            ivFavorites = itemView.findViewById(R.id.ivFavorites);
            cvRiceDetails = itemView.findViewById(R.id.cvUserRiceView);
        }
    }
}