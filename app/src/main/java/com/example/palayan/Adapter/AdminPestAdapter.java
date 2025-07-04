package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.Pest;
import com.example.palayan.R;

import java.util.List;

public class AdminPestAdapter extends RecyclerView.Adapter<AdminPestAdapter.PestHolder> {
    private List<Pest> pestList;
    private Context context;

    public AdminPestAdapter(List<Pest> pestList, Context context) {
        this.pestList = pestList;
        this.context = context;
    }

    @NonNull
    @Override
    public AdminPestAdapter.PestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_pest_admin, viewGroup, false);
        return new PestHolder(view);
    }

    //For displaying information in list view
    @Override
    public void onBindViewHolder(@NonNull AdminPestAdapter.PestHolder pestHolder, int position) {

        Pest pest = pestList.get(position);
    }

    @Override
    public int getItemCount() {
        return pestList.size();
    }

    //For declaration
    public class PestHolder extends RecyclerView.ViewHolder {

        TextView tvPestName, tvSciName;
        ImageView pestImage, imgDelete, imgUpdate;


        public PestHolder(@NonNull View itemView) {
            super(itemView);

            tvPestName = itemView.findViewById(R.id.tvPestName);
            tvSciName = itemView.findViewById(R.id.tvScientificName);
            pestImage = itemView.findViewById(R.id.ivPestImage);
            imgDelete = itemView.findViewById(R.id.iv_delete);
            imgUpdate = itemView.findViewById(R.id.iv_edit);


        }
    }
}
