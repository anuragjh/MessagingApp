package com.example.messagingapp.services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class OtpService {

    private FirebaseFirestore db;

    public OtpService() {
        db = FirebaseFirestore.getInstance();
    }

    public void storeOtp(String email, String otp, long validityPeriod) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("validUntil", System.currentTimeMillis() + validityPeriod);

        db.collection("otp")
                .document(email)
                .set(otpData)
                .addOnSuccessListener(aVoid -> {
                    // OTP stored successfully
                    Log.d("OtpService", "OTP stored successfully.");
                })
                .addOnFailureListener(e -> {
                    // Failed to store OTP
                    Log.w("OtpService", "Error storing OTP", e);
                });
    }
}

