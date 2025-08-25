package com.s23010222.coconet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import com.airbnb.lottie.LottieAnimationView;

public class DistributorProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView helloText, usernameText;
    private LinearLayout viewProfileLayout, ordersLayout, settingsLayout, logoutLayout;
    private FrameLayout loadingOverlay;
    private LottieAnimationView loadingLottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_profile);

        initViews();
        setupClickListeners();
        setUserData(getUserName());
        setupLoadingOverlay();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        helloText = findViewById(R.id.hello_text);
        usernameText = findViewById(R.id.username_text);
        viewProfileLayout = findViewById(R.id.view_profile_layout);
        ordersLayout = findViewById(R.id.orders_layout);

        settingsLayout = findViewById(R.id.settings_layout);
        logoutLayout = findViewById(R.id.logout_layout);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingLottie = findViewById(R.id.loading_lottie);
    }

    private void setupClickListeners() {
        viewProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the distributor view profile activity
                Intent intent = new Intent(DistributorProfileActivity.this, DistributorViewProfileActivity.class);
                startActivity(intent);
            }
        });

        ordersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DistributorProfileActivity.this, DistributorOrdersActivity.class);
                startActivity(intent);
            }
        });



        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DistributorProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DistributorProfileActivity.this, "Profile image clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserData(String username) {
        usernameText.setText(username);
    }

    private String getUserName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "User");
    }

    private void setupLoadingOverlay() {
        // The loading overlay is already defined in the layout XML
        // Just set the animation file for the Lottie view
        if (loadingLottie != null) {
            loadingLottie.setAnimation(R.raw.loading);
        }
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            showLoading();
            new Handler().postDelayed(() -> {
                // Clear login state
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                hideLoading();
                Intent intent = new Intent(DistributorProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 4000); // 4 seconds, same as login
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            if (loadingLottie != null) loadingLottie.playAnimation();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            if (loadingLottie != null) loadingLottie.cancelAnimation();
        }
    }

    public void updateProfileImage(int resourceId) {
        profileImage.setImageResource(resourceId);
    }

    public void updateUsername(String username) {
        usernameText.setText(username);
    }
}
