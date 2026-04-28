package com.s23010222.coconet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import android.content.ContentResolver;
import android.webkit.MimeTypeMap;

import okhttp3.*;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostAdActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etProductName, etDescription, etQuantity, etPrice, etMobileNumber;
    private Spinner spinnerAvailability, spinnerStockCondition;
    private Button btnPostAd;
    private LinearLayout mainImageContainer;
    private ImageView mainImageView;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int READ_MEDIA_IMAGES_PERMISSION_CODE = 101;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 102;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;

    private String selectedImageMimeType = "image/jpeg";
    private String selectedImageExtension = "jpg";
    private Bitmap.CompressFormat selectedCompressFormat = Bitmap.CompressFormat.JPEG;

    public interface OnUploadResultListener {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupSpinners();
        setupClickListeners();
        setupImageLaunchers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etProductName = findViewById(R.id.et_product_name);
        etDescription = findViewById(R.id.et_description);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        spinnerAvailability = findViewById(R.id.spinner_availability);
        spinnerStockCondition = findViewById(R.id.spinner_stock_condition);
        btnPostAd = findViewById(R.id.btn_post_ad);
        mainImageContainer = findViewById(R.id.main_image_container);
    }

    private void setupSpinners() {
        String[] availabilityOptions = {"Available Now", "Available in 1 week", "Available in 2 weeks", "Pre-order"};
        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availabilityOptions);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);

        String[] stockConditionOptions = {"New", "Good", "Fair", "Organic"};
        ArrayAdapter<String> stockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stockConditionOptions);
        stockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStockCondition.setAdapter(stockAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnPostAd.setOnClickListener(v -> postAdToFirestore());
        mainImageContainer.setOnClickListener(v -> showImagePickerDialog());
    }

    /** ========== IMAGE PICKER MERGED FROM 1ST CODE ========== **/
    private void setupImageLaunchers() {
<<<<<<< HEAD
        // Camera
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            setMainImage(bitmap, null);
                        }
                    }
                });

<<<<<<< HEAD
        // Gallery
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                setMainImage(bitmap, uri);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
    }

    private void openGallery() {
<<<<<<< HEAD
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.setType("image/*");
        galleryLauncher.launch(pickPhoto);
    }

    private void setMainImage(Bitmap bitmap, Uri uri) {
=======
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            handleAndroid14PlusGalleryAccess();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            handleAndroid13GalleryAccess();
        } else {
            handleLegacyGalleryAccess();
        }
    }

    private void handleAndroid14PlusGalleryAccess() {
        String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
        };

        boolean hasFullAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasPartialAccess = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
                        == PackageManager.PERMISSION_GRANTED;

        if (hasFullAccess || hasPartialAccess) {
            launchGalleryIntent();
        } else {
            ActivityCompat.requestPermissions(this, permissions, READ_MEDIA_IMAGES_PERMISSION_CODE);
        }
    }

    private void handleAndroid13GalleryAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    READ_MEDIA_IMAGES_PERMISSION_CODE);
        } else {
            launchGalleryIntent();
        }
    }

    private void handleLegacyGalleryAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
            launchGalleryIntent();
        }
    }

    private void launchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            galleryLauncher.launch(galleryIntent);
        } else {
            Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
                break;

            case READ_MEDIA_IMAGES_PERMISSION_CODE:
                boolean hasFullAccess = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean hasPartialAccess = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (hasFullAccess) {
                    Toast.makeText(this, "Full media access granted", Toast.LENGTH_SHORT).show();
                    launchGalleryIntent();
                } else if (hasPartialAccess) {
                    Toast.makeText(this, "Selected photos access granted", Toast.LENGTH_SHORT).show();
                    launchGalleryIntent();
                } else {
                    Toast.makeText(this, "Media access permission is required to select images", Toast.LENGTH_LONG).show();
                }
                break;

            case READ_EXTERNAL_STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchGalleryIntent();
                } else {
                    Toast.makeText(this, "Storage permission is required to access gallery", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setMainImage(Bitmap bitmap) {
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        mainImageContainer.removeAllViews();
        mainImageView = new ImageView(this);
        mainImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mainImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mainImageView.setImageBitmap(bitmap);
        mainImageContainer.addView(mainImageView);

        selectedBitmap = bitmap;
        selectedImageUri = uri;

        if (uri != null) {
            selectedImageMimeType = getMimeTypeFromUri(uri);
            selectedImageExtension = getExtensionFromMimeType(selectedImageMimeType);
            selectedCompressFormat = getCompressFormatFromMimeType(selectedImageMimeType);
        }
    }

    /** ========== POST AD LOGIC (FROM 2ND CODE) ========== **/
    private void postAdToFirestore() {
        String productName = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String availability = spinnerAvailability.getSelectedItem().toString();
        String stockCondition = spinnerStockCondition.getSelectedItem().toString();

        if (productName.isEmpty() || description.isEmpty() || quantity.isEmpty() ||
                price.isEmpty() || mobileNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentFarmerId = prefs.getString("farmer_id", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        if (currentFarmerId.isEmpty() || !isLoggedIn) {
            Toast.makeText(this, "Error: User not logged in. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        if (selectedBitmap != null) {
            uploadImageAndPost(productName, description, quantity, price, mobileNumber, availability, stockCondition, currentFarmerId);
        } else {
            savePostToFirestore(productName, description, quantity, price, mobileNumber, availability, stockCondition, currentFarmerId, null);
        }
    }

    private void uploadToCloudinary(Bitmap bitmap, OnUploadResultListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(selectedCompressFormat, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image." + selectedImageExtension,
                        RequestBody.create(imageBytes, MediaType.parse(selectedImageMimeType)))
                .addFormDataPart("upload_preset", "unsigned_preset")
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/drjizoz1s/image/upload")
                .post(requestBody)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> listener.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String imageUrl = json.getString("secure_url");
                        runOnUiThread(() -> listener.onSuccess(imageUrl));
                    } catch (Exception e) {
                        runOnUiThread(() -> listener.onFailure("Parse error: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> listener.onFailure("Upload failed: " + response.message()));
                }
            }
        });
    }

    private void uploadImageAndPost(String productName, String description, String quantity, String price, String mobileNumber, String availability, String stockCondition, String farmerId) {
        uploadToCloudinary(selectedBitmap, new OnUploadResultListener() {
            @Override
            public void onSuccess(String imageUrl) {
                savePostToFirestore(productName, description, quantity, price, mobileNumber, availability, stockCondition, farmerId, imageUrl);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PostAdActivity.this, "Failed to upload image: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePostToFirestore(String productName, String description, String quantity, String price, String mobileNumber, String availability, String stockCondition, String farmerId, String imageUrl) {
<<<<<<< HEAD
        // same logic from your 2nd code (with Notification + location check)
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        db.collection("users")
                .document(farmerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> postData = new HashMap<>();
                    postData.put("productName", productName);
                    postData.put("description", description);
                    postData.put("quantity", quantity);
                    postData.put("price", price);
                    postData.put("mobileNumber", mobileNumber);
                    postData.put("availability", availability);
                    postData.put("stockCondition", stockCondition);
                    postData.put("timestamp", System.currentTimeMillis());
                    postData.put("farmerId", farmerId);
                    postData.put("imageUrl", imageUrl != null ? imageUrl : "");

                    if (documentSnapshot.exists()) {
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");
                        String location = documentSnapshot.getString("city");
                        postData.put("latitude", latitude);
                        postData.put("longitude", longitude);
<<<<<<< HEAD
                        postData.put("location", location != null ? location : "");
=======

                        db.collection("farmer_posts")
                                .add(postData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(PostAdActivity.this, "Ad posted successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PostAdActivity.this, "Failed to post ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Map<String, Object> postData = new HashMap<>();
                        postData.put("productName", productName);
                        postData.put("description", description);
                        postData.put("quantity", quantity);
                        postData.put("price", price);
                        postData.put("mobileNumber", mobileNumber);
                        postData.put("availability", availability);
                        postData.put("stockCondition", stockCondition);
                        postData.put("timestamp", System.currentTimeMillis());
                        postData.put("farmerId", farmerId);
                        postData.put("imageUrl", imageUrl);
                        postData.put("location", "");

                        db.collection("farmer_posts")
                                .add(postData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(PostAdActivity.this, "Ad posted successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PostAdActivity.this, "Failed to post ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
                    }

                    db.collection("farmer_posts")
                            .add(postData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(PostAdActivity.this, "Ad posted successfully!", Toast.LENGTH_SHORT).show();
                                NotificationStorage.addNotification(
                                        PostAdActivity.this,
                                        new NotificationItem(
                                                "Ad Posted",
                                                "Your ad for '" + productName + "' has been posted successfully.",
                                                System.currentTimeMillis(),
                                                farmerId
                                        )
                                );
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PostAdActivity.this, "Failed to post ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private String getMimeTypeFromUri(Uri uri) {
        ContentResolver cr = getContentResolver();
        String mimeType = cr.getType(uri);
        return mimeType != null ? mimeType : "image/jpeg";
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) return "jpg";
<<<<<<< HEAD
        if (mimeType.contains("png")) return "png";
        if (mimeType.contains("webp")) return "webp";
=======
        if (mimeType.equals("image/png")) return "png";
        if (mimeType.equals("image/jpeg")) return "jpg";
        if (mimeType.equals("image/jpg")) return "jpg";
        if (mimeType.equals("image/webp")) return "webp";
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        return "jpg";
    }

    private Bitmap.CompressFormat getCompressFormatFromMimeType(String mimeType) {
        if (mimeType == null) return Bitmap.CompressFormat.JPEG;
<<<<<<< HEAD
        if (mimeType.contains("png")) return Bitmap.CompressFormat.PNG;
        if (mimeType.contains("webp")) return Bitmap.CompressFormat.WEBP;
=======
        if (mimeType.equals("image/png")) return Bitmap.CompressFormat.PNG;
        if (mimeType.equals("image/webp")) return Bitmap.CompressFormat.WEBP;
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        return Bitmap.CompressFormat.JPEG;
    }
}
