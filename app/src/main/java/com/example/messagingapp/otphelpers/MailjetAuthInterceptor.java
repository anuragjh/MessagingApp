package com.example.messagingapp.otphelpers;

import okhttp3.Request;

import java.io.IOException;
import java.util.Base64;

public class MailjetAuthInterceptor implements okhttp3.Interceptor {
    private final String apiKey;
    private final String apiSecret;

    public MailjetAuthInterceptor(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String credentials = apiKey + ":" + apiSecret;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        Request requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build();

        return chain.proceed(requestWithAuth);
    }
}
