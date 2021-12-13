package com.example.videocallapp.network

import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap

interface ApiService {

    @POST("send")
    fun sendRemoteMessage(@HeaderMap headers :HashMap<String, String>, @Body remoteBody : String) : Call<String>
}