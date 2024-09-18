package com.example.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messagingapp.services.OtpService;
import com.example.messagingapp.utils.OtpUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;
    private TextView otpStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        otpStatusTextView = findViewById(R.id.otpStatusTextView);

        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (!email.isEmpty()) {
                    OtpUtils.sendOtpEmail(email, new OtpUtils.OtpCallback() {
                        @Override
                        public void onSuccess(String otp, long validityPeriod) {
                            OtpService otpService = new OtpService();
                            otpService.storeOtp(email, otp, validityPeriod);
                            Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("user_email", email);
                            startActivity(intent);
                            otpStatusTextView.setText("OTP sent successfully. Please check your email.");
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            otpStatusTextView.setText(errorMessage);
                        }
                    });
                } else {
                    otpStatusTextView.setText("Please enter an email address.");
                }
            }
        });
    }
}
