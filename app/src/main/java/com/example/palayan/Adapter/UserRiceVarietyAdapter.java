package com.example.palayan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.RiceVarietyInformation;

import java.util.List;

public class UserRiceVarietyAdapter extends RecyclerView.Adapter<UserRiceVarietyAdapter.UserViewHolder> {

    private List<RiceVariety> list;
    private Context context;

    public UserRiceVarietyAdapter(List<RiceVariety> list, Context context) {
        this.list = list;
        this.context = context;
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
        CardView cvRiceDetails;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            varietyName = itemView.findViewById(R.id.txtVarietyName);
            location = itemView.findViewById(R.id.txtLocation);
            breederYear = itemView.findViewById(R.id.txtBreederYearRelease);
            cvRiceDetails = itemView.findViewById(R.id.cvUserRiceView);
        }
    }
}
