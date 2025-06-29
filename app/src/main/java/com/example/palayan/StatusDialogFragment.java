package com.example.palayan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class StatusDialogFragment extends DialogFragment {

    private Runnable onDismissCallback;

    public static StatusDialogFragment newInstance(
            String title,
            String message,
            int iconResId,
            int colorResId
    ) {
        StatusDialogFragment fragment = new StatusDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putInt("iconResId", iconResId);
        args.putInt("colorResId", colorResId);
        fragment.setArguments(args);
        return fragment;
    }

    public StatusDialogFragment setOnDismissListener(Runnable callback) {
        this.onDismissCallback = callback;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.status_dialog, null);

        TextView title = view.findViewById(R.id.status_title);
        TextView message = view.findViewById(R.id.status_message);
        ImageView icon = view.findViewById(R.id.status_icon);
        Button btnOkay = view.findViewById(R.id.btnOkay);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("title"));
            message.setText(args.getString("message"));
            icon.setImageResource(args.getInt("iconResId"));
            icon.setColorFilter(ContextCompat.getColor(requireContext(), args.getInt("colorResId")));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        Dialog dialog = builder.create();

        btnOkay.setOnClickListener(v -> {
            dialog.dismiss();
            if (onDismissCallback != null) {
                onDismissCallback.run();
            }
        });

        return dialog;
    }
}
