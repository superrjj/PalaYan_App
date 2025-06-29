package com.example.palayan;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class CustomDialogFragment extends DialogFragment {

    private DialogInterface.OnClickListener onConfirmListener;

    public static CustomDialogFragment newInstance(
            String title,
            String message,
            String sub_message,
            int iconResId,
            String confirmText,
            DialogInterface.OnClickListener onConfirm
    ) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("sub_message", sub_message);
        args.putInt("iconResId", iconResId);
        args.putString("confirmText", confirmText);
        fragment.setArguments(args);
        fragment.setOnConfirmListener(onConfirm);
        return fragment;
    }

    public void setOnConfirmListener(DialogInterface.OnClickListener listener) {
        this.onConfirmListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null);

        TextView title = view.findViewById(R.id.dialog_title);
        TextView message = view.findViewById(R.id.dialog_message);
        ImageView icon = view.findViewById(R.id.dialog_icon);
        Button cancel = view.findViewById(R.id.btnCancel);
        Button confirm = view.findViewById(R.id.btnConfirm);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("title"));
            message.setText(args.getString("message"));
            icon.setImageResource(args.getInt("iconResId"));
            confirm.setText(args.getString("confirmText"));
            confirm.setBackgroundColor(ContextCompat.getColor(requireContext(), args.getInt("confirmColorRes")));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            if (onConfirmListener != null) {
                onConfirmListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
            dialog.dismiss();
        });

        return dialog;
    }



}
