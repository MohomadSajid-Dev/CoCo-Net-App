package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class GetstartActivity extends AppCompatActivity {

    Button btnToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_getstart);

        btnToday = findViewById(R.id.btn_today);

        Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        btnToday.startAnimation(buttonAnim);

        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GetstartActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
