package com.example.palayan.Helper.Validator;

import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TextHelp {

    //live validation for required validation only
    public static void addValidation(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
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

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }
        });
    }

    //live validation for letters, spaces and &
    public static void addLettersSpaceAnd(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (!text.matches("^[a-zA-Z& ]*$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    //live validation for alphanumeric and space
    public static void addAlphaNumericSpace(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (!text.matches("^[a-zA-Z0-9 ]*$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    //live validation for numbers only
    public static void addNumericOnly(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (!text.matches("^[0-9]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    //manual check for emptiness before submitting
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


    //validation for chip group with textView as error placeholder
    public static boolean validateChipGroup(ChipGroup chipGroup, TextView errorTextView, String errorMessage) {
        if (chipGroup.getCheckedChipIds().isEmpty()) {
            errorTextView.setText(errorMessage);
            errorTextView.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorTextView.setVisibility(View.GONE);
            return true;
        }
    }

    //auto hide error when a chip is selected
    public static void clearChipErrorOnSelect(ChipGroup chipGroup, TextView errorTextView) {
        if (!chipGroup.getCheckedChipIds().isEmpty()) {
            errorTextView.setVisibility(View.GONE);
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                errorTextView.setVisibility(View.GONE);
            }
        });
    }
}
