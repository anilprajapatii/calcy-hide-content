package com.anil.hidecontentcalcy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PassCode extends AppCompatActivity {
    private static final int MIN_PASSCODE_LENGTH = 3;
    private static final int MAX_PASSCODE_LENGTH = 6;

    private EditText passcodeEditText;
    private Button submitButton;
    private boolean isFromMainActivity;
    private boolean isFromVaultFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passcode);

        initializeViews();
        handleIntent();
        setSubmitButtonListener();
    }

    private void initializeViews() {
        passcodeEditText = findViewById(R.id.passcodeEditText);
        submitButton = findViewById(R.id.submitButton);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        isFromMainActivity = intent.getBooleanExtra("passcode_from_main", false);
        isFromVaultFolder = intent.getBooleanExtra("passcode_from_vault_folder", false);
    }

    private void setSubmitButtonListener() {
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String passcode = passcodeEditText.getText().toString().trim();

        if (!isValidPasscode(passcode)) {
            showInvalidPasscodeToast();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("VaultPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isFromMainActivity) {
            savePasscode(editor, passcode);
        } else if (isFromVaultFolder) {
            updatePasscode(sharedPreferences, editor, passcode);
        }

        editor.apply();
        navigateToNextActivity();
        finish();
    }

    private boolean isValidPasscode(String passcode) {
        return passcode.length() >= MIN_PASSCODE_LENGTH && passcode.length() <= MAX_PASSCODE_LENGTH;
    }

    private void showInvalidPasscodeToast() {
        Toast.makeText(this, "Passcode must be between 3 and 6 digits", Toast.LENGTH_SHORT).show();
    }

    private void savePasscode(SharedPreferences.Editor editor, String passcode) {
        editor.putString("passcode", passcode);
        Toast.makeText(this, "Passcode saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updatePasscode(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, String passcode) {
        String existingPasscode = sharedPreferences.getString("passcode", "");
        if (existingPasscode.isEmpty()) {
            Toast.makeText(this, "No existing passcode to change.", Toast.LENGTH_SHORT).show();
            return;
        }
        editor.putString("passcode", passcode);
        Toast.makeText(this, "Passcode changed successfully!", Toast.LENGTH_SHORT).show();
    }

    private void navigateToNextActivity() {
        if (isFromMainActivity) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (isFromVaultFolder) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}