package com.example.palayan.UserActivities;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;

import com.example.palayan.R;

public class LoadingDialog {
    private final Dialog dialog;
    private final TextView tvMessage;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_loading, null));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        tvMessage = dialog.findViewById(R.id.tvMessage);

    }

    public void show(String message) {
        setMessage(message);
        if (!dialog.isShowing()) dialog.show();
    }

    public void setMessage(String message) {
        if (tvMessage != null && message != null) tvMessage.setText(message);
    }

    public void dismiss() {
        if (dialog.isShowing()) dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}