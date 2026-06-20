package com.s23010222.coconet.ui.distributor;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class DistributorViewProfileActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private TextView nameText, emailText, passwordText, contactText, locationText, businessNameText;
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
        setContentView(R.layout.activity_distributor_view_profile);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.distributor_primary_dark));
        }

        // Bind views
        profileImage = findViewById(R.id.distributor_profile_image);
        editProfileImage = findViewById(R.id.edit_profile_image);
        nameText = findViewById(R.id.distributor_name);
        emailText = findViewById(R.id.distributor_email);
        passwordText = findViewById(R.id.distributor_password);
        contactText = findViewById(R.id.distributor_contact);
        locationText = findViewById(R.id.distributor_location);
        businessNameText = findViewById(R.id.distributor_business_name);
        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        editProfileButton = findViewById(R.id.edit_profile_button);
        settingsButton = findViewById(R.id.settings_button);

        setupActivityLaunchers();

        String distributorId = getIntent().getStringExtra("distributor_id");
        boolean isViewingOwnProfile = false;

        if (distributorId == null) {
            distributorId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("distributor_id", null);
            isViewingOwnProfile = true;
        } else {
            String currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("distributor_id", null);
            isViewingOwnProfile = distributorId.equals(currentUserId);
        }

        if (distributorId != null) {
            loadDistributorProfileFromFirestore(distributorId, isViewingOwnProfile);
        } else {
            Toast.makeText(this, "No distributor ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Profile image edit click
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
            startActivity(new android.content.Intent(DistributorViewProfileActivity.this, SettingsActivity.class));
        });

        View parentContact = (View) findViewById(R.id.distributor_contact).getParent();
        parentContact.setOnClickListener(v -> {
            Toast.makeText(this, "Calling...", Toast.LENGTH_SHORT).show();
        });

        View parentLocation = (View) findViewById(R.id.distributor_location).getParent();
        parentLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Maps...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupActivityLaunchers() {
        // Camera
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 201);
        } else {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePicture);
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }

    private void loadDistributorProfileFromFirestore(String distributorId, boolean isViewingOwnProfile) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(distributorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String password = documentSnapshot.getString("password");
                        String contact = documentSnapshot.getString("mobile");
                        String location = documentSnapshot.getString("city");
                        String businessName = documentSnapshot.getString("businessName");

                        updateProfileUI(name, email, password, contact, location, businessName, isViewingOwnProfile);
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileUI(String name, String email, String password, String contact, String location, String businessName, boolean isViewingOwnProfile) {
        nameText.setText(name != null ? name : "Unknown Distributor");
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
        businessNameText.setText(businessName != null && !businessName.isEmpty() ? businessName : "Business name not set");

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
            togglePasswordVisibility.setColorFilter(ContextCompat.getColor(this, R.color.distributor_primary));
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
