package com.s23010222.coconet.ui.farmer;

import com.s23010222.coconet.R;
import com.s23010222.coconet.ui.common.SettingsActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class FarmerProfileActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private TextView nameText, emailText, passwordText, contactText, locationText, farmNameText;
    private ImageView togglePasswordVisibility;
    private MaterialButton editProfileButton, settingsButton;
    private CircleImageView profileImage;
    private ImageView editProfileImage;

    private String loadedPassword = "";

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_profile);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.farmer_primary_dark));
        }

        profileImage = findViewById(R.id.farmer_profile_image);
        editProfileImage = findViewById(R.id.edit_profile_image);
        nameText = findViewById(R.id.farmer_name);
        emailText = findViewById(R.id.farmer_email);
        passwordText = findViewById(R.id.farmer_password);
        contactText = findViewById(R.id.farmer_contact);
        locationText = findViewById(R.id.farmer_location);
        farmNameText = findViewById(R.id.farmer_farm_name);
        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        editProfileButton = findViewById(R.id.edit_profile_button);
        settingsButton = findViewById(R.id.settings_button);

        setupActivityLaunchers();

        String farmerId = getIntent().getStringExtra("farmer_id");
        boolean isViewingOwnProfile = false;

        if (farmerId == null) {
            farmerId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("farmer_id", null);
            isViewingOwnProfile = true;
        } else {
            String currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("farmer_id", null);
            isViewingOwnProfile = farmerId.equals(currentUserId);
        }

        if (farmerId != null) {
            loadFarmerProfileFromFirestore(farmerId, isViewingOwnProfile);
        } else {
            Toast.makeText(this, "No farmer ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        editProfileImage.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        togglePasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        editProfileButton.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Edit Profile...", Toast.LENGTH_SHORT).show();
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(FarmerProfileActivity.this, SettingsActivity.class));
        });
    }

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        profileImage.setImageBitmap(imageBitmap);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri selectedImageUri = result.getData().getData();
                        profileImage.setImageURI(selectedImageUri);
                    }
                });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Profile Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePicture);
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }


    private void loadFarmerProfileFromFirestore(String farmerId, boolean isViewingOwnProfile) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(farmerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String password = documentSnapshot.getString("password");
                        String contact = documentSnapshot.getString("mobile");
                        String location = documentSnapshot.getString("city");
                        String farmName = documentSnapshot.getString("farmName");

                        updateProfileUI(name, email, password, contact, location, farmName, isViewingOwnProfile);
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileUI(String name, String email, String password, String contact, String location, String farmName, boolean isViewingOwnProfile) {
        nameText.setText(name != null ? name : "Unknown Farmer");
        emailText.setText(email != null ? email : "No email provided");

        if (isViewingOwnProfile) {
            loadedPassword = password != null ? password : "";
            passwordText.setText("••••••••");
            passwordText.setVisibility(View.VISIBLE);
            togglePasswordVisibility.setVisibility(View.VISIBLE);
        } else {
            passwordText.setVisibility(View.GONE);
            togglePasswordVisibility.setVisibility(View.GONE);
            View passwordContainer = (View) passwordText.getParent();
            if (passwordContainer != null) {
                passwordContainer.setVisibility(View.GONE);
            }
        }

        contactText.setText(contact != null && !contact.isEmpty() ? formatPhoneNumber(contact) : "No contact number");
        locationText.setText(location != null ? location : "Location not specified");
        farmNameText.setText(farmName != null && !farmName.isEmpty() ? farmName : "Farm name not set");

        if (!isViewingOwnProfile) {
            editProfileButton.setVisibility(View.GONE);
            settingsButton.setVisibility(View.GONE);
            editProfileImage.setVisibility(View.GONE);
        } else {
            editProfileButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            editProfileImage.setVisibility(View.VISIBLE);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() == 10) {
            return String.format("(%s) %s-%s",
                    phoneNumber.substring(0, 3),
                    phoneNumber.substring(3, 6),
                    phoneNumber.substring(6));
        } else if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        } else {
            return phoneNumber;
        }
    }

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            passwordText.setText(loadedPassword.isEmpty() ? "No password set" : loadedPassword);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
            togglePasswordVisibility.setColorFilter(ContextCompat.getColor(this, R.color.farmer_primary));
        } else {
            passwordText.setText("••••••••");
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            togglePasswordVisibility.setColorFilter(ContextCompat.getColor(this, R.color.gray_600));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
