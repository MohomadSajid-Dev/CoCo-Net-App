package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4;
    private TextView resendOtp, resendLink;
    private Button signUpButton;
    private String phoneNumber;
    private String correctOtp = "1234";
    private CountDownTimer resendTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        phoneNumber = getIntent().getStringExtra("phone_number");

        initViews();
        setupOtpInputs();
        setupClickListeners();
        startResendTimer();
    }

    private void initViews() {
        otp1 = findViewById(R.id.otp_1);
        otp2 = findViewById(R.id.otp_2);
        otp3 = findViewById(R.id.otp_3);
        otp4 = findViewById(R.id.otp_4);
        resendOtp = findViewById(R.id.resend_otp);
        resendLink = findViewById(R.id.resend_link);
        signUpButton = findViewById(R.id.sign_up_button);
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new GenericTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new GenericTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new GenericTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new GenericTextWatcher(otp4, null));

        otp2.setOnKeyListener(new GenericKeyListener(otp2, otp1));
        otp3.setOnKeyListener(new GenericKeyListener(otp3, otp2));
        otp4.setOnKeyListener(new GenericKeyListener(otp4, otp3));
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp();
            }
        });

        resendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canResend) {
                    resendOtp();
                } else {
                    Toast.makeText(OTPVerificationActivity.this,
                            "Please wait before requesting another OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyOtp() {
        String enteredOtp = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString();

        if (enteredOtp.length() != 4) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpButton.setText("Verifying...");
        signUpButton.setEnabled(false);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (enteredOtp.equals(correctOtp)) {
                    Toast.makeText(OTPVerificationActivity.this,
                            "OTP verified successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(OTPVerificationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(OTPVerificationActivity.this,
                            "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    clearOtpFields();
                }

                signUpButton.setText("Sign-up");
                signUpButton.setEnabled(true);
            }
        }, 2000);
    }

    private void resendOtp() {
        Toast.makeText(this, "OTP resent successfully!", Toast.LENGTH_SHORT).show();
        correctOtp = generateRandomOtp();
        clearOtpFields();
        startResendTimer();
    }

    private void clearOtpFields() {
        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp1.requestFocus();
    }

    private String generateRandomOtp() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    private void startResendTimer() {
        canResend = false;
        resendLink.setTextColor(getResources().getColor(android.R.color.darker_gray));

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendLink.setText("Resend (" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                canResend = true;
                resendLink.setText("Resend");
                resendLink.setTextColor(0xFF085099);
            }
        }.start();
    }

    private class GenericTextWatcher implements TextWatcher {
        private EditText currentView;
        private EditText nextView;

        GenericTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.length() == 1 && nextView != null) {
                nextView.requestFocus();
            } else if (text.length() == 1 && nextView == null) {
                currentView.clearFocus();
                verifyOtp();
            }
        }
    }

    private class GenericKeyListener implements View.OnKeyListener {
        private EditText currentView;
        private EditText previousView;

        GenericKeyListener(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    currentView.getText().toString().isEmpty() &&
                    previousView != null) {
                previousView.requestFocus();
                previousView.setText("");
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        super.onBackPressed();
    }
}
