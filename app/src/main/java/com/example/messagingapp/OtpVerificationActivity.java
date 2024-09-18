package com.example.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messagingapp.utils.OtpUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private TextView errorMessage;
    private Button verifyButton, resendOtpButton;
    private ProgressBar progressBar;
    private TextView resendOtpTextView;
    private CountDownTimer resendTimer;
    private long resendTimeoutMillis = 10 * 60 * 1000L; // 10 minutes
    private String userEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpBox1 = findViewById(R.id.otp_box_1);
        otpBox2 = findViewById(R.id.otp_box_2);
        otpBox3 = findViewById(R.id.otp_box_3);
        otpBox4 = findViewById(R.id.otp_box_4);
        otpBox5 = findViewById(R.id.otp_box_5);
        otpBox6 = findViewById(R.id.otp_box_6);
        errorMessage = findViewById(R.id.error_message);
        resendOtpButton = findViewById(R.id.resend_otp_button);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);
        verifyButton = findViewById(R.id.verify_button);
        progressBar = findViewById(R.id.progressBar);

        userEmail = getIntent().getStringExtra("user_email");

        verifyButton.setOnClickListener(v -> verifyOtp());
        resendOtpButton.setOnClickListener(v -> resendOtp());

        setupOtpInputListeners();

        startResendTimer();
    }

    private void setupOtpInputListeners() {
        otpBox1.addTextChangedListener(new OtpTextWatcher(otpBox1, otpBox2));
        otpBox2.addTextChangedListener(new OtpTextWatcher(otpBox2, otpBox1, otpBox3));
        otpBox3.addTextChangedListener(new OtpTextWatcher(otpBox3, otpBox2, otpBox4));
        otpBox4.addTextChangedListener(new OtpTextWatcher(otpBox4, otpBox3, otpBox5));
        otpBox5.addTextChangedListener(new OtpTextWatcher(otpBox5, otpBox4, otpBox6));
        otpBox6.addTextChangedListener(new OtpTextWatcher(otpBox6, otpBox5));
    }

    private class OtpTextWatcher implements TextWatcher {
        private EditText currentView, previousView, nextView;

        public OtpTextWatcher(EditText currentView, EditText previousView, EditText nextView) {
            this.currentView = currentView;
            this.previousView = previousView;
            this.nextView = nextView;
        }

        public OtpTextWatcher(EditText currentView, EditText previousView) {
            this(currentView, previousView, null);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus(); // Move to next box
            } else if (s.length() == 0 && previousView != null) {
                previousView.requestFocus(); // Move to previous box on delete
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void verifyOtp() {
        String otp = otpBox1.getText().toString().trim() +
                otpBox2.getText().toString().trim() +
                otpBox3.getText().toString().trim() +
                otpBox4.getText().toString().trim() +
                otpBox5.getText().toString().trim() +
                otpBox6.getText().toString().trim();

        if (otp.isEmpty() || otp.length() < 6) {
            errorMessage.setText("Please enter the complete OTP.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("otp").document(userEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        Map<String, Object> otpData = documentSnapshot.getData();
                        String storedOtp = (String) otpData.get("otp");
                        long validUntil = (long) otpData.get("validUntil");

                        if (storedOtp.equals(otp) && System.currentTimeMillis() <= validUntil) {
                            db.collection("otp").document(userEmail).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("OtpVerification", "OTP deleted successfully"))
                                    .addOnFailureListener(e -> Log.w("OtpVerification", "Error deleting OTP", e));

                            Intent intent = new Intent(OtpVerificationActivity.this, ProfileSetUp.class);
                            intent.putExtra("user_email", userEmail);
                            startActivity(intent);
                            finish();
                        } else {
                            errorMessage.setText("Invalid or expired OTP.");
                        }
                    } else {
                        errorMessage.setText("No OTP found for this email.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w("OtpVerification", "Error verifying OTP", e);
                    errorMessage.setText("Error verifying OTP. Please try again.");
                });
    }

    private void resendOtp() {
        OtpUtils.sendOtpEmail(userEmail, new OtpUtils.OtpCallback() {
            @Override
            public void onSuccess(String otp, long validityPeriod) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> otpData = new HashMap<>();
                otpData.put("otp", otp);
                otpData.put("validUntil", validityPeriod);

                db.collection("otp").document(userEmail).set(otpData)
                        .addOnSuccessListener(aVoid -> {
                            errorMessage.setText("OTP resent successfully.");
                            startResendTimer();
                        })
                        .addOnFailureListener(e -> errorMessage.setText("Error resending OTP. Please try again."));
            }

            @Override
            public void onFailure(String errorMessage) {
                OtpVerificationActivity.this.errorMessage.setText("Error: " + errorMessage);
            }
        });
    }

    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendOtpButton.setEnabled(false);

        resendTimer = new CountDownTimer(resendTimeoutMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                resendOtpTextView.setText(String.format("You can request a new OTP in %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                resendOtpButton.setEnabled(true);
                resendOtpTextView.setText("Request new OTP");
            }
        }.start();
    }
}
