package com.s23010222.coconet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapLocationPicker";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private Marker selectedLocationMarker;
    private LatLng selectedLocation;
    private String selectedLocationName;
    private Geocoder geocoder;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService;
    private Handler mainHandler;

    private MaterialButton confirmButton;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;
    private ImageButton precisionButton;
    private TextInputEditText searchEditText;
    private RecyclerView searchResultsRecycler;
    private MaterialCardView locationInfoCard;
    private TextView selectedLocationText;
    private CircularProgressIndicator loadingIndicator;
    private ImageButton closeButton;
    private ImageButton clearSelectionButton;

    private List<Address> searchResults;
    private SearchResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location_picker);

        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        searchResults = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupClickListeners();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        searchResultsRecycler = findViewById(R.id.search_results_recycler);
        confirmButton = findViewById(R.id.btn_confirm_location);
        zoomInButton = findViewById(R.id.btn_zoom_in);
        zoomOutButton = findViewById(R.id.btn_zoom_out);
        precisionButton = findViewById(R.id.btn_precision);
        locationInfoCard = findViewById(R.id.location_info_card);
        selectedLocationText = findViewById(R.id.selected_location_text);
        loadingIndicator = findViewById(R.id.loading_indicator);
        closeButton = findViewById(R.id.btn_close);
        clearSelectionButton = findViewById(R.id.btn_clear_selection);

        confirmButton.setEnabled(false);
        locationInfoCard.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter();
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecycler.setAdapter(adapter);
    }

    private void setupClickListeners() {
        confirmButton.setOnClickListener(v -> confirmLocationSelection());

        closeButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        clearSelectionButton.setOnClickListener(v -> clearSelection());
        precisionButton.setOnClickListener(v -> getCurrentLocation());

        zoomInButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Exception e) {
            Log.e(TAG, "Error applying map style: " + e.getMessage());
        }

        LatLng sriLanka = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 10));

        mMap.setOnMapClickListener(this::selectLocation);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        enableMyLocationIfPermitted();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchLocations(s.toString());
                } else {
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchLocations(String query) {
        showLoading(true);

        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", Sri Lanka", 8);

                mainHandler.post(() -> {
                    showLoading(false);
                    if (addresses != null && !addresses.isEmpty()) {
                        searchResults.clear();
                        searchResults.addAll(addresses);
                        adapter.notifyDataSetChanged();
                        searchResultsRecycler.setVisibility(View.VISIBLE);
                    } else {
                        clearSearchResults();
                        Toast.makeText(this, "No locations found for: " + query, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Log.e(TAG, "Search error: " + e.getMessage());
                    Toast.makeText(this, "Error searching locations", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void clearSearchResults() {
        searchResults.clear();
        adapter.notifyDataSetChanged();
        searchResultsRecycler.setVisibility(View.GONE);
    }

    private void selectSearchResult(Address address) {
        LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
        selectedLocationName = getLocationName(address);

        selectLocation(location);
        clearSearchResults();
        searchEditText.setText(selectedLocationName);

        searchEditText.clearFocus();
    }

    private void selectLocation(LatLng location) {
        if (selectedLocationMarker != null) {
            selectedLocationMarker.remove();
        }

        selectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Selected Location"));

        selectedLocation = location;

        if (selectedLocationName == null) {
            getAddressFromLocation(location);
        }

        updateLocationUI();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void updateLocationUI() {
        if (selectedLocationName != null) {
            selectedLocationText.setText(selectedLocationName);
            locationInfoCard.setVisibility(View.VISIBLE);
            confirmButton.setEnabled(true);

            if (selectedLocationMarker != null) {
                selectedLocationMarker.setTitle(selectedLocationName);
            }
        }
    }

    private void clearSelection() {
        if (selectedLocationMarker != null) {
            selectedLocationMarker.remove();
            selectedLocationMarker = null;
        }

        selectedLocation = null;
        selectedLocationName = null;

        locationInfoCard.setVisibility(View.GONE);
        confirmButton.setEnabled(false);
        searchEditText.setText("");
    }

    private void getAddressFromLocation(LatLng location) {
        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.latitude, location.longitude, 1);

                mainHandler.post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        selectedLocationName = getLocationName(address);
                        updateLocationUI();
                    } else {
                        selectedLocationName = "Unknown Location";
                        updateLocationUI();
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Geocoder error: " + e.getMessage());
                    selectedLocationName = "Unknown Location";
                    updateLocationUI();
                });
            }
        });
    }

    private String getLocationName(Address address) {
        String locationName = address.getLocality();
        if (locationName == null || locationName.isEmpty()) {
            locationName = address.getSubAdminArea();
        }
        if (locationName == null || locationName.isEmpty()) {
            locationName = address.getAdminArea();
        }
        if (locationName == null || locationName.isEmpty()) {
            locationName = address.getFeatureName();
        }
        if (locationName == null || locationName.isEmpty()) {
            locationName = address.getAddressLine(0);
        }
        return locationName != null ? locationName : "Unknown Location";
    }

    private String getFullAddress(Address address) {
        StringBuilder fullAddress = new StringBuilder();

        if (address.getFeatureName() != null) {
            fullAddress.append(address.getFeatureName()).append(", ");
        }
        if (address.getSubLocality() != null) {
            fullAddress.append(address.getSubLocality()).append(", ");
        }
        if (address.getAdminArea() != null) {
            fullAddress.append(address.getAdminArea());
        }

        String result = fullAddress.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result.isEmpty() ? "Address not available" : result;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        showLoading(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    showLoading(false);
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        selectLocation(currentLocation);
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void enableMyLocationIfPermitted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void confirmLocationSelection() {
        if (selectedLocation != null && selectedLocationName != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_location", selectedLocationName);
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationIfPermitted();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

        @NonNull
        @Override
        public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
            Address address = searchResults.get(position);
            holder.bind(address);
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder {
            private TextView locationName;
            private TextView locationAddress;

            public SearchResultViewHolder(@NonNull View itemView) {
                super(itemView);
                locationName = itemView.findViewById(R.id.location_name);
                locationAddress = itemView.findViewById(R.id.location_address);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        selectSearchResult(searchResults.get(position));
                    }
                });
            }

            public void bind(Address address) {
                locationName.setText(getLocationName(address));
                locationAddress.setText(getFullAddress(address));
            }
        }
    }
}
