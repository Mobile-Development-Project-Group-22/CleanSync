package com.example.cleansync.data.repository

import android.util.Log
import com.example.cleansync.data.model.EmailRequest
import com.example.cleansync.data.model.EmailResponse
import com.example.cleansync.data.service.EmailService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import java.lang.Exception
import com.example.cleansync.BuildConfig

class EmailRepository {
    private val emailService: EmailService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.sendgrid.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailService::class.java)
    }

    suspend fun sendConfirmationEmail(emailRequest: EmailRequest): Boolean {
        return try {

            val response = emailService.sendEmail(emailRequest)
            if (response.isSuccessful) {
                Log.d("EmailRepository", "Email sent successfully!")
                true
            } else {
                Log.e("EmailRepository", "Failed: ${response.code()} - ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailRepository", "Exception: ${e.message}", e)
            false
        }
    }
    private fun logErrorResponse(response: Response<EmailResponse>) {
        val errorBody = response.errorBody()?.string()
        Log.e("EmailRepository", "Error: ${response.code()} - ${response.message()} - $errorBody")
    }


    // Log successful response body too to check the data structure
    private fun logResponse(response: Response<EmailResponse>) {
        val responseBody = response.body()
        Log.d("EmailRepository", "Response: $responseBody")
    }


}
