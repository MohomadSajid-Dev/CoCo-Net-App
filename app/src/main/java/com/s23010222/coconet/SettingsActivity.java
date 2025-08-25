package com.s23010222.coconet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private static final int MAP_LOCATION_REQUEST_CODE = 2002;

    private Switch notificationsSwitch;
    private EditText currentPasswordEdit;
    private EditText newPasswordEdit;
    private EditText confirmPasswordEdit;
    private Button updatePasswordButton;
    private TextView currentCityText;
    private TextView newLocationText;
    private Button pickLocationButton;
    private Button saveLocationButton;
    private ProgressBar progressBar;
    private ImageView backButton;
    private EditText nameEdit;
    private EditText mobileEdit;
    private Button updateProfileButton;

    private String selectedLocationName;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsSwitch = findViewById(R.id.switch_notifications);
        currentPasswordEdit = findViewById(R.id.edit_current_password);
        newPasswordEdit = findViewById(R.id.edit_new_password);
        confirmPasswordEdit = findViewById(R.id.edit_confirm_password);
        updatePasswordButton = findViewById(R.id.btn_update_password);
        currentCityText = findViewById(R.id.text_current_city);
        newLocationText = findViewById(R.id.text_new_location);
        pickLocationButton = findViewById(R.id.btn_pick_location);
        saveLocationButton = findViewById(R.id.btn_save_location);
        progressBar = findViewById(R.id.progress);
        backButton = findViewById(R.id.btn_back);
        nameEdit = findViewById(R.id.edit_name);
        mobileEdit = findViewById(R.id.edit_mobile);
        updateProfileButton = findViewById(R.id.btn_update_profile);

        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize notification preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        notificationsSwitch.setChecked(enabled);
        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked);
                editor.apply();
            }
        });

        // Load current values from user prefs
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentCity = userPrefs.getString("city", "-");
        currentCityText.setText("Current: " + (currentCity != null ? currentCity : "-"));

        String currentName = userPrefs.getString("username", "");
        String currentMobile = userPrefs.getString("mobile", "");
        if (currentName != null) nameEdit.setText(currentName);
        if (currentMobile != null) mobileEdit.setText(currentMobile);

        pickLocationButton.setOnClickListener(v -> openMapForLocationSelection());
        saveLocationButton.setOnClickListener(v -> saveSelectedLocation());
        updatePasswordButton.setOnClickListener(v -> updatePassword());
        updateProfileButton.setOnClickListener(v -> updateProfile());
    }

    private void openMapForLocationSelection() {
        try {
            Intent intent = new Intent(this, MapLocationPickerActivity.class);
            startActivityForResult(intent, MAP_LOCATION_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening map. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLocationName = data.getStringExtra("selected_location");
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);
            if (selectedLocationName != null) {
                newLocationText.setText("(not selected)".equals(selectedLocationName) ? "New: (not selected)" : "New: " + selectedLocationName);
            }
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updatePasswordButton.setEnabled(!loading);
        saveLocationButton.setEnabled(!loading);
        pickLocationButton.setEnabled(!loading);
        updateProfileButton.setEnabled(!loading);
    }

    private void updatePassword() {
        String current = currentPasswordEdit.getText().toString().trim();
        String next = newPasswordEdit.getText().toString().trim();
        String confirm = confirmPasswordEdit.getText().toString().trim();

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!next.equals(confirm)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (next.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = userPrefs.getString("role", "Farmer");
        String userIdKey = "Farmer".equalsIgnoreCase(role) ? "farmer_id" : "distributor_id";
        String userId = userPrefs.getString(userIdKey, null);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String existing = doc.getString("password");
                    if (existing == null || !existing.equals(current)) {
                        setLoading(false);
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> update = new HashMap<>();
                    update.put("password", next);
                    db.collection("users").document(userId)
                            .update(update)
                            .addOnSuccessListener(v -> {
                                setLoading(false);
                                currentPasswordEdit.setText("");
                                newPasswordEdit.setText("");
                                confirmPasswordEdit.setText("");
                                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error verifying current password", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        String name = nameEdit.getText().toString().trim();
        String mobile = mobileEdit.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Name and mobile cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.length() < 9) {
            Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = userPrefs.getString("role", "Farmer");
        String userIdKey = "Farmer".equalsIgnoreCase(role) ? "farmer_id" : "distributor_id";
        String userId = userPrefs.getString(userIdKey, null);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> update = new HashMap<>();
        update.put("username", name);
        update.put("mobile", mobile);

        db.collection("users").document(userId)
                .update(update)
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    SharedPreferences.Editor editor = userPrefs.edit();
                    editor.putString("username", name);
                    editor.putString("mobile", mobile);
                    editor.apply();
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSelectedLocation() {
        if (selectedLocationName == null || selectedLocationName.isEmpty() || "(not selected)".equals(selectedLocationName)) {
            Toast.makeText(this, "Please pick a new location", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = userPrefs.getString("role", "Farmer");
        String userIdKey = "Farmer".equalsIgnoreCase(role) ? "farmer_id" : "distributor_id";
        String userId = userPrefs.getString(userIdKey, null);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> update = new HashMap<>();
        update.put("city", selectedLocationName);
        update.put("latitude", selectedLatitude);
        update.put("longitude", selectedLongitude);

        db.collection("users").document(userId)
                .update(update)
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    SharedPreferences.Editor editor = userPrefs.edit();
                    editor.putString("city", selectedLocationName);
                    editor.apply();
                    currentCityText.setText("Location: " + selectedLocationName);
                    Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show();
                });
    }
}