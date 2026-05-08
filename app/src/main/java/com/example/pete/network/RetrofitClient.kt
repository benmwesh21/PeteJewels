package com.example.pete.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // TODO: Replace with the Ngrok URL you get when you run `ngrok http 3000`
    // Example: "https://a1b2c3d4.ngrok-free.app/api/"
    // Make sure it ends with a slash!
    private const val BASE_URL = "https://simple-darkness-chewing.ngrok-free.dev/api/"

    val paymentApi: PaymentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApi::class.java)
    }
}
