package com.example.palayan.Helper.Validator;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.palayan.R;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class TextHelp {

    //live validation for required validation only
    public static void addValidation(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            String input = editText.getText() != null ? editText.getText().toString().trim() : "";
            if (!hasFocus && input.isEmpty()) {
                layout.setError(errorMessage); // kapag nawalan ng focus at empty
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();

                if (input.isEmpty()) {
                    layout.setError(errorMessage); // ito ‘yung kulang mo — live check sa empty field
                } else {
                    layout.setError(null);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }


    //live validation for letters, spaces and &
    public static void addLettersSpaceAnd(
            final TextInputLayout layout,
            final TextInputEditText editText,
            final String errorMessage
    ) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z& ]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s != null ? s.toString().trim() : "";
                if (!editText.isFocused()) return;

                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z& ]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }


    public static void addLetterOnly(
            final TextInputLayout layout,
            final TextInputEditText editText,
            final String errorMessage
    ) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z ]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s != null ? s.toString().trim() : "";
                if (!editText.isFocused()) return;

                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z ]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });
    }


    //live validation for alphanumeric and space
    public static void addAlphaNumericSpace(
            final TextInputLayout layout,
            final TextInputEditText editText,
            final String errorMessage
    ) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z0-9 ]+$")) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s != null ? s.toString().trim() : "";
                if (!editText.isFocused()) return; // Skip live validation kung wala sa focus

                if (input.isEmpty()) {
                    layout.setError("This field is required.");
                } else if (!input.matches("^[a-zA-Z0-9 ]+$")) {
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
            layout.setError("This field is required.");
            editText.requestFocus();
            return false;
        } else if (layout.getError() != null) {
            editText.requestFocus();
            return false;
        } else {
            layout.setError(null); // Clear any previous errors
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
            final CardView cvTwoReq, final ImageView ivTwoReq, final TextView tvTwoReq,
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
                Pattern number = Pattern.compile(".*[0-9].*");

                // At least 8 characters
                boolean lengthOk = password.length() >= 8;
                cvOneReq.setCardBackgroundColor(lengthOk ? activeColor : inactiveColor);
                tvOneReq.setTextColor(lengthOk ? activeTextColor : inactiveTextColor);
                ivOneReq.setColorFilter(lengthOk ? activeIconColor : inactiveIconColor);

                // At least 1 number ✅
                boolean hasNumber = number.matcher(password).matches();
                cvTwoReq.setCardBackgroundColor(hasNumber ? activeColor : inactiveColor);
                tvTwoReq.setTextColor(hasNumber ? activeTextColor : inactiveTextColor);
                ivTwoReq.setColorFilter(hasNumber ? activeIconColor : inactiveIconColor);

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

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String pass = txtPassword.getText() != null ? txtPassword.getText().toString() : "";
                String confirm = txtConfirm.getText() != null ? txtConfirm.getText().toString() : "";

                if (!confirm.isEmpty() && !pass.equals(confirm)) {
                    layoutConfirm.setError(errorMessage); // Show error if not matching
                } else {
                    layoutConfirm.setError(null); // Clear error if match or empty
                }
            }
        };

        txtPassword.addTextChangedListener(watcher);
        txtConfirm.addTextChangedListener(watcher);
    }

    public static void addAutoCompleteValidation(final TextInputLayout layout, final AutoCompleteTextView autoCompleteTextView, final String errorMessage) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (input.isEmpty()) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = autoCompleteTextView.getText() != null ? autoCompleteTextView.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            layout.setError(null);
        });
    }


    public interface OnUsernameCheckListener {
        void onCheckComplete(boolean exists);
    }

    public static void checkUsernameExists(FirebaseFirestore firestore, String username, OnUsernameCheckListener listener) {
        firestore.collection("accounts")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean exists = !querySnapshot.isEmpty();
                    listener.onCheckComplete(exists);
                })
                .addOnFailureListener(e -> {
                    listener.onCheckComplete(false);
                });
    }

    public static TextWatcher createUsernameLiveChecker(FirebaseFirestore firestore, TextInputLayout layoutUsername, String originalUsername, boolean isEditMode) {
        return new TextWatcher() {
            private Timer timer = new Timer();
            private final long DELAY = 100;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();

                String username = s.toString().trim();
                if (username.isEmpty()) return;

                // Don’t re-check if it’s the same as original (in edit mode)
                if (isEditMode && username.equals(originalUsername)) return;

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // UI thread para safe mag setError
                        new Handler(Looper.getMainLooper()).post(() -> {
                            checkUsernameExists(firestore, username, exists -> {
                                if (exists) {
                                    layoutUsername.setError("Username is already taken");
                                } else {
                                    layoutUsername.setError(null);
                                }
                            });
                        });
                    }
                }, DELAY);
            }
        };
    }

    // Shows the clear icon only when field is focused and has content
    public static void enableClearIcon(final TextInputLayout layout, final TextInputEditText editText, final String errorMessage) {
        layout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        // Listener kapag nag-focus o nawalan ng focus
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            String text = editText.getText().toString().trim();
            if (!hasFocus) {
                if (text.isEmpty()) {
                    layout.setError(errorMessage);
                    layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.dark_red)));
                } else {
                    layout.setError(null);
                    layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.green)));
                }
            }
        });

        // TextWatcher para sa live validation at icon color
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();

                if (text.isEmpty()) {
                    layout.setError(errorMessage);
                    layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.dark_red)));
                } else {
                    layout.setError(null);
                    layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.green)));
                }

                // Show or hide clear icon depending on text and focus
                if (text.isEmpty() || !editText.isFocused()) {
                    layout.setEndIconVisible(false);
                } else {
                    layout.setEndIconVisible(true);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Clear button click behavior
        layout.setEndIconOnClickListener(v -> {
            editText.setText("");
            layout.setError(errorMessage);
            layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.dark_red)));
        });

        // Optional: show/hide clear icon based on focus change too
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            String text = editText.getText().toString().trim();
            if (hasFocus && !text.isEmpty()) {
                layout.setEndIconVisible(true);
            } else {
                layout.setEndIconVisible(false);
            }

            if (text.isEmpty()) {
                layout.setError(errorMessage);
                layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.dark_red)));
            } else {
                layout.setError(null);
                layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(layout.getContext(), R.color.green)));
            }
        });
    }


}


