package com.anil.hidecontentcalcy;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private TextView displayText;
    private StringBuilder currentInput;
    private String SECRET_CODE;
    private double firstNumber = 0;
    private String operation = "";
    private boolean isNewOperation = true;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkStoragePermission();
        if (isFirstTimeLaunch()) {
            startPassCodeActivity();
            return;
        }

        initializeViews();
        setClickListeners();
        loadSavedPasscode();
//        handleSharedIntent();
    }


    private boolean isFirstTimeLaunch() {
        SharedPreferences prefs = getSharedPreferences("VaultPrefs", MODE_PRIVATE);
        return !prefs.contains("passcode");
    }

    private void startPassCodeActivity() {
        Intent intent = new Intent(MainActivity.this, PassCode.class);
        intent.putExtra("passcode_from_main", true);
        startActivity(intent);
    }

    private void initializeViews() {
        displayText = findViewById(R.id.display);
        currentInput = new StringBuilder();
    }

    private void setClickListeners() {
        setNumberClickListeners();
        setOperationClickListeners();
        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearCalculator());
    }

    private void setNumberClickListeners() {
        Button[] buttons = {
                findViewById(R.id.btn_0),
                findViewById(R.id.btn_1),
                findViewById(R.id.btn_2),
                findViewById(R.id.btn_3),
                findViewById(R.id.btn_4),
                findViewById(R.id.btn_5),
                findViewById(R.id.btn_6),
                findViewById(R.id.btn_7),
                findViewById(R.id.btn_8),
                findViewById(R.id.btn_9)
        };

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            final int finalI = i;
            button.setOnClickListener(v -> onNumberClick(finalI));
        }
    }

    private void setOperationClickListeners() {
        findViewById(R.id.btn_add).setOnClickListener(v -> onOperationClick("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> onOperationClick("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperationClick("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> onOperationClick("÷"));
    }

    private void loadSavedPasscode() {
        SharedPreferences sharedPreferences = getSharedPreferences("VaultPrefs", MODE_PRIVATE);
        SECRET_CODE = sharedPreferences.getString("passcode", "");
    }

//    private void handleSharedIntent() {
//        Intent intent = getIntent();
//        String action = intent.getAction();
//        Uri fileUri = null;
//
//        if (Intent.ACTION_SEND.equals(action)) {
//            fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
//            if (fileUri != null) {
//                handleSharedFile(fileUri);
//            }
//        }
//    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            checkLegacyStoragePermissions();
        } else {
            checkModernStoragePermissions();
        }
    }

    private void checkLegacyStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_CODE);
        } else {
            Log.d("checkStoragePermission", "Storage permissions already granted.");
        }
    }

    private void checkModernStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } else {
                Log.d("checkStoragePermission", "Manage External Storage permission already granted.");
            }
        } else {
            Log.d("checkStoragePermission", "External Storage permission check not required for Android versions < 30.");
        }
    }

    private void onNumberClick(int number) {
        if (isNewOperation) {
            currentInput.setLength(0);
            isNewOperation = false;
        }

        currentInput.append(number);
        displayText.setText(currentInput.toString());

        if (currentInput.toString().equals(SECRET_CODE)) {
            openVault();
        }
    }

    private void onOperationClick(String op) {
        if (currentInput.length() > 0) {
            firstNumber = Double.parseDouble(currentInput.toString());
            operation = op;
            isNewOperation = true;
        }
    }

    private void calculateResult() {
        if (currentInput.length() > 0 && !operation.isEmpty()) {
            double secondNumber = Double.parseDouble(currentInput.toString());
            double result = performCalculation(secondNumber);
            updateDisplay(result);
            operation = "";
            isNewOperation = true;
        }
    }

    private double performCalculation(double secondNumber) {
        double result = 0;
        switch (operation) {
            case "+":
                result = firstNumber + secondNumber;
                break;
            case "-":
                result = firstNumber - secondNumber;
                break;
            case "×":
                result = firstNumber * secondNumber;
                break;
            case "÷":
                if (secondNumber != 0) {
                    result = firstNumber / secondNumber;
                }
                break;
        }
        return result;
    }

    private void updateDisplay(double result) {
        currentInput.setLength(0);
        currentInput.append(result);
        displayText.setText(String.valueOf(result));
    }

    private void clearCalculator() {
        currentInput.setLength(0);
        operation = "";
        firstNumber = 0;
        displayText.setText("0");
        isNewOperation = true;
    }

//    private void handleSharedFile(Uri fileUri) {
//        Intent vaultIntent = new Intent(this, Vault.class);
//        vaultIntent.putExtra("FILE_URI", fileUri);
//        startActivity(vaultIntent);
//    }

    private void openVault() {
        Intent intent = new Intent(this, VaultFolders.class);
        startActivity(intent);
        clearCalculator();
    }
}