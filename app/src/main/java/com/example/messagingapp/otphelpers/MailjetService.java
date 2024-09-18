package com.example.messagingapp.otphelpers;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MailjetService {

    @POST("send") // Ensure the endpoint path is correct
    Call<MailjetEmailResponse> sendEmail(@Body MailjetEmailRequest request);
}
