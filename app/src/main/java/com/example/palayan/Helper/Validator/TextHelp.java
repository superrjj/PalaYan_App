package com.example.palayan.Helper.Validator;

import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

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

    public static void addLetterOnly(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (!text.matches("^[a-zA-Z ]*$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
    public static void addChipValidation(ChipGroup chipGroup, TextView errorTextView, String errorMessage) {
        // Initial check
        if (chipGroup.getCheckedChipIds().isEmpty()) {
            errorTextView.setText(errorMessage);
            errorTextView.setVisibility(View.VISIBLE);
        } else {
            errorTextView.setVisibility(View.GONE);
        }

        // Listen for changes in chip selection
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                errorTextView.setText(errorMessage);
                errorTextView.setVisibility(View.VISIBLE);
            } else {
                errorTextView.setVisibility(View.GONE);
            }
        });
    }

    public static void addPasswordRequirementsValidation(
            final TextInputEditText passwordEditText,
            final TextInputEditText usernameEditText,
            final CardView cvOneReq, final ImageView ivOneReq, final TextView tvOneReq,
            final CardView cvThreeReq, final ImageView ivThreeReq, final TextView tvThreeReq,
            final CardView cvFourReq, final ImageView ivFourReq, final TextView tvFourReq,
            final CardView cvFiveReq, final ImageView ivFiveReq, final TextView tvFiveReq,
            final int activeColor, final int inactiveColor, final int activeTextColor, final int inactiveTextColor,
            final int activeIconColor, final int inactiveIconColor
    ) {
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";

                Pattern uppercase = Pattern.compile(".*[A-Z].*");
                Pattern lowercase = Pattern.compile(".*[a-z].*");
                Pattern symbol = Pattern.compile(".*[^a-zA-Z0-9].*");

                // At least 8 characters
                boolean lengthOk = password.length() >= 8;
                cvOneReq.setCardBackgroundColor(lengthOk ? activeColor : inactiveColor);
                tvOneReq.setTextColor(lengthOk ? activeTextColor : inactiveTextColor);
                ivOneReq.setColorFilter(lengthOk ? activeIconColor : inactiveIconColor);

                // At least 1 uppercase
                boolean hasUppercase = uppercase.matcher(password).matches();
                cvThreeReq.setCardBackgroundColor(hasUppercase ? activeColor : inactiveColor);
                tvThreeReq.setTextColor(hasUppercase ? activeTextColor : inactiveTextColor);
                ivThreeReq.setColorFilter(hasUppercase ? activeIconColor : inactiveIconColor);

                // At least 1 lowercase
                boolean hasLowercase = lowercase.matcher(password).matches();
                cvFourReq.setCardBackgroundColor(hasLowercase ? activeColor : inactiveColor);
                tvFourReq.setTextColor(hasLowercase ? activeTextColor : inactiveTextColor);
                ivFourReq.setColorFilter(hasLowercase ? activeIconColor : inactiveIconColor);

                // At least 1 symbol
                boolean hasSymbol = symbol.matcher(password).matches();
                cvFiveReq.setCardBackgroundColor(hasSymbol ? activeColor : inactiveColor);
                tvFiveReq.setTextColor(hasSymbol ? activeTextColor : inactiveTextColor);
                ivFiveReq.setColorFilter(hasSymbol ? activeIconColor : inactiveIconColor);
            }
        });
    }

    //live validation for confirm password
    public static void addConfirmPasswordValidation(
            final TextInputLayout layoutConfirm,
            final TextInputEditText txtPassword,
            final TextInputEditText txtConfirm,
            final String errorMessage) {

        txtConfirm.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String pass = txtPassword.getText() != null ? txtPassword.getText().toString() : "";
                String confirm = txtConfirm.getText() != null ? txtConfirm.getText().toString() : "";

                if (!confirm.equals(pass)) {
                    layoutConfirm.setError(errorMessage);
                } else {
                    layoutConfirm.setError(null);
                }
            }
        });

        // Para ma-trigger din kapag password ang nabago
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String pass = txtPassword.getText() != null ? txtPassword.getText().toString() : "";
                String confirm = txtConfirm.getText() != null ? txtConfirm.getText().toString() : "";

                if (!confirm.equals(pass)) {
                    layoutConfirm.setError(errorMessage);
                } else {
                    layoutConfirm.setError(null);
                }
            }
        });
    }


}
