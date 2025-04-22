package com.example.cleansync.data.service

import com.example.cleansync.data.model.EmailRequest
import com.example.cleansync.data.model.EmailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


// Email service API
interface EmailService {
    @Headers("Authorization: Bearer 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")
    @POST("v3/mail/send")
    suspend fun sendEmail(@Body emailRequest: EmailRequest): Response<Void>
}

//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