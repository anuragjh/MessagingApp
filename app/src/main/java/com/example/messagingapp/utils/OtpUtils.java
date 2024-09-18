package com.example.messagingapp.utils;

import android.util.Log;

import com.example.messagingapp.otphelpers.MailjetEmailRequest;
import com.example.messagingapp.otphelpers.MailjetEmailResponse;
import com.example.messagingapp.otphelpers.MailjetService;
import com.example.messagingapp.otphelpers.RetrofitClient;

import java.util.Collections;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpUtils {

    // Mailjet API credentials
    private static final String API_KEY = "c2afa51710f5a2b9e298019904d04a5a";
    private static final String API_SECRET = "2fdd24e938e0edf7173ed73ba3381efb";

    public static void sendOtpEmail(String email, final OtpCallback callback) {
        String otp = generateOtp();
        String otpValidity = "10 minutes"; // Example validity duration
        long validityPeriod = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minutes validity period

        String emailContent = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                + "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>"
                + "<h1 style='color: #007bff;'>Drift Chat</h1>"
                + "<p>Dear User,</p>"
                + "<p>We received a request to verify your email address. Please use the following OTP to complete the verification process:</p>"
                + "<h2 style='color: #007bff; font-size: 24px;'>" + otp + "</h2>"
                + "<p style='font-size: 16px;'>This OTP is valid for <strong>" + otpValidity + "</strong>. If you did not request this OTP, please ignore this email.</p>"
                + "<p>Thank you,<br>The Drift Chat Team</p>"
                + "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<footer style='font-size: 14px; color: #888;'>"
                + "<p>Drift Chat, Inc.<br>123 Chat Lane<br>Messaging City, MC 12345</p>"
                + "<p><a href='http://www.driftchat.com' style='color: #007bff; text-decoration: none;'>Visit our website</a></p>"
                + "</footer>"
                + "</div>"
                + "</body></html>";

        MailjetEmailRequest.Email fromEmail = new MailjetEmailRequest.Email("theamanjhaofficial@gmail.com");
        MailjetEmailRequest.Email toEmail = new MailjetEmailRequest.Email(email);

        MailjetEmailRequest.Message message = new MailjetEmailRequest.Message(
                fromEmail,
                Collections.singletonList(toEmail),
                "Your OTP Code",
                emailContent
        );

        MailjetEmailRequest request = new MailjetEmailRequest(Collections.singletonList(message));

        MailjetService service = RetrofitClient.getRetrofitInstance(API_KEY, API_SECRET).create(MailjetService.class);
        Call<MailjetEmailResponse> call = service.sendEmail(request);

        call.enqueue(new Callback<MailjetEmailResponse>() {
            @Override
            public void onResponse(Call<MailjetEmailResponse> call, Response<MailjetEmailResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("Mailjet", "Response: " + response.body().toString());
                    callback.onSuccess(otp, validityPeriod);
                } else {
                    Log.e("Mailjet", "Failed to send OTP: " + response.code() + " - " + response.message());
                    callback.onFailure("Failed to send OTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MailjetEmailResponse> call, Throwable t) {
                Log.e("Mailjet", "Error: " + t.getMessage());
                callback.onFailure("Error: " + t.getMessage());
            }
        });
    }

    // Generate a 6-digit OTP
    private static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public interface OtpCallback {
        void onSuccess(String otp, long validityPeriod);
        void onFailure(String errorMessage);
    }
}
