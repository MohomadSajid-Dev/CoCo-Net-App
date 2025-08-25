package com.s23010222.coconet;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocateFarmersActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView backButton;
    private TextView titleText;
    private EditText addressInput;
    private Button btnSearch, btnZoomIn, btnZoomOut, btnMyLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private int farmerCount = 0;
    private java.util.Map<Marker, String> markerToFarmerId = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_farmers);

        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        addressInput = findViewById(R.id.addressInput);
        btnSearch = findViewById(R.id.btnSearch);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        titleText.setText("Locate Farmers");

        backButton.setOnClickListener(v -> onBackPressed());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSearch.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            if (!address.isEmpty()) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocationName(address, 1);
                    if (!addressList.isEmpty()) {
                        Address loc = addressList.get(0);
                        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                        mMap.clear();
                        loadFarmerLocations();
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Searched Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Geocoder error", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter location to search", Toast.LENGTH_SHORT).show();
            }
        });

        btnZoomIn.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));
        btnZoomOut.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));

        btnMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
                        mMap.addMarker(new MarkerOptions()
                                .position(myLatLng)
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        LatLng sriLankaCenter = new LatLng(7.8731, 80.7718);
        loadFarmerLocations();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 7.5f));

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            String farmerId = markerToFarmerId.get(marker);
            if (farmerId != null) {
                // Open farmer profile
                android.content.Intent intent = new android.content.Intent(this, FarmerProfileActivity.class);
                intent.putExtra("farmer_id", farmerId);
                startActivity(intent);
                return true; // Consume the click event
            }
            return false; // Let default behavior handle other markers
        });
    }

    private void loadFarmerLocations() {
        farmerCount = 0;

        db.collection("users")
                .whereEqualTo("role", "Farmer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String username = document.getString("username");
                        String city = document.getString("city");

                        if (latitude != null && longitude != null && username != null) {
                            LatLng farmerLocation = new LatLng(latitude, longitude);
                            String farmerId = document.getId();

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(farmerLocation)
                                    .title(username + " (Farmer)")
                                    .snippet(city != null ? city : "Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(false)
                                    .visible(true);

                            Marker marker = mMap.addMarker(markerOptions);
                            if (marker != null) {
                                markerToFarmerId.put(marker, farmerId);
                            }
                            farmerCount++;

                            System.out.println("Added farmer marker: " + username + " at " + latitude + ", " + longitude);
                        }
                    }

                    if (farmerCount > 0) {
                        Toast.makeText(this, "Found " + farmerCount + " farmers with locations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No farmers with locations found in database", Toast.LENGTH_LONG).show();
                    }

                    loadFarmerPostsWithLocations();
                    loadCurrentUserLocation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading farmer locations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading farmers: " + e.getMessage());
                });
    }

    private void loadFarmerPostsWithLocations() {
        db.collection("farmer_posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int postCount = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String productName = document.getString("productName");
                        String location = document.getString("location");
                        String price = document.getString("price");

                        if (latitude != null && longitude != null && productName != null) {
                            LatLng postLocation = new LatLng(latitude, longitude);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(postLocation)
                                    .title(productName)
                                    .snippet("Price: " + (price != null ? price : "N/A") + " | Location: " + (location != null ? location : "N/A"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(false)
                                    .visible(true);

                            mMap.addMarker(markerOptions);
                            postCount++;

                            System.out.println("Added post marker: " + productName + " at " + latitude + ", " + longitude);
                        }
                    }

                    if (postCount > 0) {
                        Toast.makeText(this, "Loaded " + postCount + " farmer posts with locations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No farmer posts with locations found", Toast.LENGTH_SHORT).show();
                    }

                    int totalMarkers = farmerCount + postCount;
                    if (totalMarkers == 0) {
                        Toast.makeText(this, "No location data found. Farmers need to register with their locations first.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading farmer posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading posts: " + e.getMessage());
                });
    }

    private void loadCurrentUserLocation() {
        // Get current user ID from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("distributor_id", "");
        String role = prefs.getString("role", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        // Check if user is logged in and is a distributor
        if (currentUserId.isEmpty() || !isLoggedIn || !"Distributor".equalsIgnoreCase(role)) {
            System.out.println("Current user not logged in or not a distributor");
            return;
        }

        // Fetch current user's location from database
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");
                        String username = documentSnapshot.getString("username");
                        String city = documentSnapshot.getString("city");

                        if (latitude != null && longitude != null && username != null) {
                            LatLng userLocation = new LatLng(latitude, longitude);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(userLocation)
                                    .title(username + " (You)")
                                    .snippet(city != null ? city : "Your Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    .draggable(false)
                                    .visible(true);

                            mMap.addMarker(markerOptions);
                            System.out.println("Added current user marker: " + username + " at " + latitude + ", " + longitude);
                        } else {
                            System.out.println("Current user location data not available");
                        }
                    } else {
                        System.out.println("Current user document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error loading current user location: " + e.getMessage());
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
