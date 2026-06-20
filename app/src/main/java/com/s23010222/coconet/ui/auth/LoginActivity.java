package com.s23010222.coconet.ui.auth;

import com.s23010222.coconet.R;
import com.s23010222.coconet.ui.distributor.DistributorDashboardActivity;
import com.s23010222.coconet.ui.farmer.FarmerDashboardActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private TextInputEditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ImageView googleLogin, twitterLogin, instagramLogin, facebookLogin;
    private FrameLayout loadingOverlay;
    private LottieAnimationView loginLottie;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        usernameInputLayout = findViewById(R.id.username_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        googleLogin = findViewById(R.id.google_login);
        twitterLogin = findViewById(R.id.twitter_login);
        instagramLogin = findViewById(R.id.instagram_login);
        facebookLogin = findViewById(R.id.facebook_login);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loginLottie = findViewById(R.id.login_lottie);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        googleLogin.setOnClickListener(v -> Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show());
        twitterLogin.setOnClickListener(v -> Toast.makeText(this, "Twitter login clicked", Toast.LENGTH_SHORT).show());
        instagramLogin.setOnClickListener(v -> Toast.makeText(this, "Instagram login clicked", Toast.LENGTH_SHORT).show());
        facebookLogin.setOnClickListener(v -> Toast.makeText(this, "Facebook login clicked", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            focusView = passwordInput;
            cancel = true;
        } else if (password.length() < 4) {
            passwordInputLayout.setError("Password too short");
            focusView = passwordInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameInputLayout.setError("Username is required");
            focusView = usernameInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginButton.setVisibility(View.GONE);
            loadingOverlay.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loginButton.setVisibility(View.VISIBLE);
                loadingOverlay.setVisibility(View.GONE);
                loginButton.setEnabled(false);
                performLogin(username, password);
            }, 4000);
        }
    }

    private void performLogin(String username, String password) {
        db.collection("users")
                .whereEqualTo("email", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    loginButton.setText("Login");
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userId = document.getId();
                                String role = document.getString("role");
                                String city = document.getString("city");
                                String name = document.getString("username");
                                String email = document.getString("email");

                                saveUserDataToPreferences(userId, name, email, role, city);

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                Intent intent;
                                if ("Farmer".equalsIgnoreCase(role)) {
                                    intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
                                } else if ("Distributor".equalsIgnoreCase(role)) {
                                    intent = new Intent(LoginActivity.this, DistributorDashboardActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
                                }
                                intent.putExtra("username", name);
                                intent.putExtra("role", role);
                                intent.putExtra("city", city);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                            passwordInput.setText("");
                            passwordInput.requestFocus();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToPreferences(String userId, String username, String email, String role, String city) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save user ID based on role
        if ("Farmer".equalsIgnoreCase(role)) {
            editor.putString("farmer_id", userId);
            editor.putString("distributor_id", "");
        } else if ("Distributor".equalsIgnoreCase(role)) {
            editor.putString("distributor_id", userId);
            editor.putString("farmer_id", "");
        } else {
            editor.putString("farmer_id", userId);
            editor.putString("distributor_id", "");
        }

        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("role", role);
        editor.putString("city", city);
        editor.putBoolean("is_logged_in", true);

        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
