package com.example.pete.network

import retrofit2.http.Body
import retrofit2.http.POST

data class StkPushRequest(
    val phoneNumber: String,
    val amount: String,
    val userId: String,
    val itemsJson: String
)

data class StkPushResponse(
    val success: Boolean,
    val orderId: String?,
    val message: String?
)

interface PaymentApi {
    @POST("triggerStkPush")
    suspend fun triggerStkPush(@Body request: StkPushRequest): StkPushResponse
}
