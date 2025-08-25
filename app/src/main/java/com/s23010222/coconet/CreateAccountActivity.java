package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private static final int MAP_LOCATION_REQUEST_CODE = 1001;
    private static final String TAG = "CreateAccountActivity";
    private EditText cityEditText;
    private FirebaseFirestore db;

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        db = FirebaseFirestore.getInstance();
        setupViews();
    }

    private void setupViews() {
        cityEditText = findViewById(R.id.city_edit_text);
        RelativeLayout cityContainer = findViewById(R.id.city_container);

        cityContainer.setOnClickListener(v -> openMapForLocationSelection());
        cityEditText.setOnClickListener(v -> openMapForLocationSelection());

        cityEditText.setFocusable(false);
        cityEditText.setClickable(true);

        Spinner spinner = findViewById(R.id.who_are_you_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.who_you_are_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        ImageView nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> validateAndSave(spinner));
    }

    private void openMapForLocationSelection() {
        try {
            Intent intent = new Intent(this, MapLocationPickerActivity.class);
            startActivityForResult(intent, MAP_LOCATION_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Error opening map: " + e.getMessage());
            Toast.makeText(this, "Error opening map. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selected_location");
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);

            if (selectedLocation != null && !selectedLocation.isEmpty()) {
                cityEditText.setText(selectedLocation);
                selectedLatitude = latitude;
                selectedLongitude = longitude;
                Toast.makeText(this, "Location selected: " + selectedLocation, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Selected location: " + selectedLocation + " (" + latitude + ", " + longitude + ")");
            }
        }
    }

    private void validateAndSave(Spinner spinner) {
        EditText usernameEditText = findViewById(R.id.username_edit_text);
        EditText emailEditText = findViewById(R.id.email_edit_text);
        EditText passwordEditText = findViewById(R.id.password_edit_text);
        EditText mobileEditText = findViewById(R.id.mobile_edit_text);

        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String role = spinner.getSelectedItem().toString();
        String city = cityEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                mobile.isEmpty() || city.isEmpty() || role.equals("Who You Are")) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);
        user.put("mobile", mobile);
        user.put("role", role);
        user.put("city", city);
        user.put("latitude", selectedLatitude);
        user.put("longitude", selectedLongitude);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OTPVerificationActivity.class);
                    intent.putExtra("role", role);
                    intent.putExtra("city", city);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Firestore error", e);
                });
    }
}
