package com.example.palayan.Dialog;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.ImageUploadAdapter;
import com.example.palayan.Helper.ImageUploadItem;
import com.example.palayan.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AllDiseaseRiceImagesSheet extends BottomSheetDialogFragment {

    private ArrayList<Uri> allImages;
    private ImageUploadAdapter adapter;

    public AllDiseaseRiceImagesSheet(ArrayList<Uri> images) {
        this.allImages = images;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_all_image_rice_disease, container, false);

        RecyclerView rvAllImages = view.findViewById(R.id.rvAllImages);
        rvAllImages.setLayoutManager(new GridLayoutManager(getContext(), 3));

        //Convert ArrayList<Uri> -> List<ImageUploadItem>
        List<ImageUploadItem> items = new ArrayList<>();
        for (Uri uri : allImages) {
            items.add(new ImageUploadItem(uri.toString(), false));
        }

        //Add a placeholder sa dulo kung gusto mo consistent behavior
        if (items.size() < 6) {
            items.add(new ImageUploadItem(null, true));
        }

        // Gamitin tamang constructor: (List<ImageUploadItem>, OnImageClickListener)
        adapter = new ImageUploadAdapter(items, clickedUri -> {
            // dito mo pwedeng i-handle kung may na-click na "tap to upload"
            // pero sa sheet, pwede mo ring i-disable o i-toast lang
        });

        rvAllImages.setAdapter(adapter);

        return view;
    }
}
