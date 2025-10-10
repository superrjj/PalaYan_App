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

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.Disease;
import com.example.palayan.R;
import com.example.palayan.UserActivities.DiseaseDetails;

import java.util.List;

public class UserDiseaseAdapter extends RecyclerView.Adapter<UserDiseaseAdapter.DiseaseHolder> {

    private List<Disease> diseaseList;
    private Context context;

    public UserDiseaseAdapter(List<Disease> diseaseList, Context context) {
        this.diseaseList = diseaseList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserDiseaseAdapter.DiseaseHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_disease_user, viewGroup, false);
        return new DiseaseHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserDiseaseAdapter.DiseaseHolder diseaseHolder, int position) {

        Disease disease = diseaseList.get(position);
        diseaseHolder.tvDiseaseName.setText(disease.getName());
        diseaseHolder.tvScientificName.setText(disease.getScientificName());

        Glide.with(context)
                .load(disease.getMainImageUrl())
                .placeholder(R.drawable.loading_image)
                .into(diseaseHolder.ivDisease);

        diseaseHolder.cvDisease.setOnClickListener(v ->{
            Intent intent = new Intent(context, DiseaseDetails.class);
            intent.putExtra("disease_id", disease.getDocumentId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    public class DiseaseHolder extends RecyclerView.ViewHolder {

        TextView tvDiseaseName, tvScientificName;
        ImageView ivDisease;
        CardView cvDisease;

        public DiseaseHolder(@NonNull View itemView) {
            super(itemView);

            tvDiseaseName = itemView.findViewById(R.id.tvDiseaseName);
            tvScientificName = itemView.findViewById(R.id.tvScientificName);
            ivDisease = itemView.findViewById(R.id.ivDiseaseImage);
            cvDisease = itemView.findViewById(R.id.cvDiseaseView);

        }
    }
}