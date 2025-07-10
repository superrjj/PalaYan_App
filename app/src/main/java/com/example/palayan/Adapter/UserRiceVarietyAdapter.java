package com.example.palayan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.DeviceUtils;
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

    public UserRiceVarietyAdapter(List<RiceVariety> list, Context context, Set<String> favoriteIds, boolean isFavoritesPage) {
        this.list = list;
        this.context = context;
        this.favoriteIds = favoriteIds;
        this.isFavoritesPage = isFavoritesPage;
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
        holder.varietyName.setText(variety.varietyName);
        holder.location.setText(variety.location);

        // Combine breeder origin and year release (or fallback text if null)
        String origin = variety.breederOrigin != null ? variety.breederOrigin : "Unknown";
        String year = variety.yearRelease != null ? variety.yearRelease : "N/A";
        holder.breederYear.setText(origin + ", " + year);

        String deviceId = DeviceUtils.getDeviceId(context);
        String deviceModel = android.os.Build.MODEL;
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isFavoritesPage || (favoriteIds != null && favoriteIds.contains(variety.rice_seed_id))) {
            holder.ivFavorites.setImageResource(R.drawable.ic_fav_filled);
        } else {
            holder.ivFavorites.setImageResource(R.drawable.ic_fav_outline);
        }

        holder.ivFavorites.setOnClickListener(v -> {
            firestore.collection("rice_seed_favorites")
                    .whereEqualTo("deviceId", deviceId)
                    .whereEqualTo("rice_seed_id", variety.rice_seed_id)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            // Remove from favorites
                            for (QueryDocumentSnapshot doc : snapshot) {
                                firestore.collection("rice_seed_favorites").document(doc.getId()).delete();
                            }
                            holder.ivFavorites.setImageResource(R.drawable.ic_fav_outline);
                            if (favoriteIds != null) {
                                favoriteIds.remove(variety.rice_seed_id);
                            }
                        } else {
                            // Add to favorites
                            Map<String, Object> favorite = new HashMap<>();
                            favorite.put("deviceId", deviceId);
                            favorite.put("deviceModel", deviceModel);
                            favorite.put("rice_seed_id", variety.rice_seed_id);
                            firestore.collection("rice_seed_favorites").add(favorite);
                            holder.ivFavorites.setImageResource(R.drawable.ic_fav_filled);
                            if (favoriteIds != null) {
                                favoriteIds.add(variety.rice_seed_id);
                            }
                        }
                    });
        });

        // Go to RiceVarietyInformation Activity on item click
        holder.cvRiceDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, RiceVarietyInformation.class);
            intent.putExtra("rice_seed_id", variety.rice_seed_id);
            context.startActivity(intent);
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
