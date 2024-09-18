package com.example.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

    private EditText otpInput;
    private TextView errorMessage;
    private Button verifyButton;
    private Button resendOtpButton;
    private ProgressBar progressBar;
    private TextView resendOtpTextView;
    private CountDownTimer resendTimer;
    private long otpValidityMillis = 10 * 60 * 1000L; // 10 minutes in milliseconds
    private long resendTimeoutMillis = 10 * 60 * 1000L; // 10 minutes in milliseconds
    private String userEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpInput = findViewById(R.id.otp_input);
        errorMessage = findViewById(R.id.error_message);
        resendOtpButton = findViewById(R.id.resend_otp_button);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);
        verifyButton = findViewById(R.id.verify_button);
        progressBar = findViewById(R.id.progressBar);

        userEmail = getIntent().getStringExtra("user_email");

        verifyButton.setOnClickListener(v -> verifyOtp());
        resendOtpButton.setOnClickListener(v -> resendOtp());

        startResendTimer();
    }

    private void verifyOtp() {
        String otp = otpInput.getText().toString().trim();

        if (otp.isEmpty()) {
            errorMessage.setText("Please enter the OTP.");
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
                            // Delete OTP document after successful verification
                            db.collection("otp").document(userEmail).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("OtpVerification", "OTP deleted successfully"))
                                    .addOnFailureListener(e -> Log.w("OtpVerification", "Error deleting OTP", e));

                            startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
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

            }
        });
    }

    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendOtpButton.setEnabled(false);

        resendTimer = new CountDownTimer(resendTimeoutMillis, 1000) { // 1 second interval
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
