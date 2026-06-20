package com.s23010222.coconet.ui.onboarding;

import com.s23010222.coconet.R;
import com.s23010222.coconet.ui.auth.CreateAccountActivity;
import com.s23010222.coconet.ui.auth.LoginActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class WelcomeActivity extends AppCompatActivity {

    private Button loginButton, signupButton;
    private ImageView googleIcon, xIcon, instagramIcon, facebookIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        googleIcon = findViewById(R.id.google_icon);
        xIcon = findViewById(R.id.x_icon);
        instagramIcon = findViewById(R.id.instagram_icon);
        facebookIcon = findViewById(R.id.facebook_icon);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener socialIconClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String platform = "";
                int id = v.getId();
                if (id == R.id.google_icon) platform = "Google";
                else if (id == R.id.x_icon) platform = "X";
                else if (id == R.id.instagram_icon) platform = "Instagram";
                else if (id == R.id.facebook_icon) platform = "Facebook";

                Toast.makeText(WelcomeActivity.this, "Login with " + platform, Toast.LENGTH_SHORT).show();
            }
        };

        googleIcon.setOnClickListener(socialIconClickListener);
        xIcon.setOnClickListener(socialIconClickListener);
        instagramIcon.setOnClickListener(socialIconClickListener);
        facebookIcon.setOnClickListener(socialIconClickListener);
    }

    private void selectLogin() {
        loginButton.setBackground(ContextCompat.getDrawable(this, R.drawable.login_button_background));
        loginButton.setTextColor(Color.WHITE);

        signupButton.setBackgroundColor(Color.TRANSPARENT);
        signupButton.setTextColor(Color.GRAY);
    }

    private void selectSignUp() {
        signupButton.setBackground(ContextCompat.getDrawable(this, R.drawable.login_button_background));
        signupButton.setTextColor(Color.WHITE);

        loginButton.setBackgroundColor(Color.TRANSPARENT);
        loginButton.setTextColor(Color.GRAY);
    }
}