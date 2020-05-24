package com.matt.socialmediaapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAD5HN2F4:APA91bHKuRD9_lUUPCpZ0GOk2aKTBeEBJ6F5PLpZjFpYcgDGNCQjnMMkWYzm2Zdf8Dk48Dj0rWz8LME_KJxrRds40hgRYDwAsQDmH0G1wQdz3kr_DGhqGOW5SkDYfPi2Hmxy7KQIrVtR"

    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
