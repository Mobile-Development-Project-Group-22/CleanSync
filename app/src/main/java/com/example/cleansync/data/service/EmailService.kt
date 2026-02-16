package com.example.cleansync.data.service

import com.example.cleansync.data.model.EmailRequest
import com.example.cleansync.data.model.EmailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


// Email service API
interface EmailService {
    @POST("v3/mail/send")
    suspend fun sendEmail(
        @Header("Authorization") authorization: String,
        @Body emailRequest: EmailRequest
    ): Response<Void>
}
