package com.example.palayan.Dialog;

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
import androidx.fragment.app.DialogFragment;

import com.example.palayan.R;

public class CustomDialogFragment extends DialogFragment {

    private DialogInterface.OnClickListener onConfirmListener;

    public static CustomDialogFragment newInstance(
            String title,
            String message,
            String subMessage,
            int iconResId,
            String confirmText,
            DialogInterface.OnClickListener onConfirm
    ) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("sub_message", subMessage);
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
        TextView subMessage = view.findViewById(R.id.dialog_sub_message);
        ImageView icon = view.findViewById(R.id.dialog_icon);
        Button cancel = view.findViewById(R.id.btnCancel);
        Button confirm = view.findViewById(R.id.btnConfirm);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("title"));
            message.setText(args.getString("message"));
            subMessage.setText(args.getString("sub_message"));
            icon.setImageResource(args.getInt("iconResId"));

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
