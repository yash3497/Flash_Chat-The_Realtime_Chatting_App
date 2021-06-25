package com.example.flashchat2.Notification;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAag-mPXc:APA91bGHeHw8thWrs0hQxPKeMX2Q413J95C-ySMVCepMRhLjlRVXStI-8UbZNePcL-iJCyqiwNxiqksNhENCyh5A-DcbnAAfPe30FHTZELFEtM2dSW9WRkO_qbbisMz_d-yFjNK9a2pj"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
