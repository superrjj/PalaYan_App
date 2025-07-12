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
import com.example.palayan.Helper.Pest;
import com.example.palayan.PestDetails;
import com.example.palayan.R;

import java.util.List;

public class UserPestAdapter extends RecyclerView.Adapter<UserPestAdapter.PestHolder> {

    private List<Pest> pestList;
    private Context context;

    public UserPestAdapter(List<Pest> pestList, Context context) {
        this.pestList = pestList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserPestAdapter.PestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_pest_user, viewGroup, false);
        return new PestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPestAdapter.PestHolder pestHolder, int position) {

        Pest pest = pestList.get(position);
        pestHolder.tvPestName.setText(pest.getPestName());
        pestHolder.tvScientificName.setText(pest.getScientificName());

        Glide.with(context)
                .load(pest.getImageUrl())
                .placeholder(R.drawable.loading_image)
                .into(pestHolder.ivPest);


        pestHolder.cvPest.setOnClickListener(v ->{
                Intent intent = new Intent(context, PestDetails.class);
                intent.putExtra("pest_id", pest.getPest_id());
                context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return pestList.size();
    }

    public class PestHolder extends RecyclerView.ViewHolder {

        TextView tvPestName, tvScientificName;
        ImageView ivPest;
        CardView cvPest;

        public PestHolder(@NonNull View itemView) {
            super(itemView);

            tvPestName = itemView.findViewById(R.id.tvPestName);
            tvScientificName = itemView.findViewById(R.id.tvScientificName);
            ivPest = itemView.findViewById(R.id.ivPestImage);
            cvPest = itemView.findViewById(R.id.cvPestView);

        }
    }
}
