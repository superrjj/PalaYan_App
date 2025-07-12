package com.example.palayan.Helper.Validator;

import android.text.TextWatcher;
import android.text.Editable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TextHelp {


    public static void addValidation(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {

        //para sa mga blank fields
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        // Clear error while typing
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
        });
    }

    //if empty the fields
    public static boolean isFilled(TextInputLayout layout, TextInputEditText editText, String errorMessage) {
        String input = editText.getText() != null ? editText.getText().toString().trim() : "";

        if (input.isEmpty()) {
            layout.setError(errorMessage);
            editText.requestFocus();
            return false;
        } else {
            layout.setError(null);
            return true;
        }
    }
}
