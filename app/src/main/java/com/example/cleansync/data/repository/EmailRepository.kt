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
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.sendgrid.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailService::class.java)
    }

    suspend fun sendConfirmationEmail(emailRequest: EmailRequest): Boolean {
        return try {
            Log.d("EmailRepository", "Attempting to send email...")
            Log.d("EmailRepository", "Recipient: ${emailRequest.personalizations}")
            
            val apiKey = BuildConfig.SENDGRID_API_KEY
            val response = emailService.sendEmail("Bearer $apiKey", emailRequest)
            
            Log.d("EmailRepository", "Response code: ${response.code()}")
            Log.d("EmailRepository", "Response message: ${response.message()}")
            
            if (response.isSuccessful) {
                Log.d("EmailRepository", "✅ Email sent successfully!")
                true
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("EmailRepository", "❌ Failed: ${response.code()}")
                Log.e("EmailRepository", "Error body: $errorBody")
                Log.e("EmailRepository", "Headers: ${response.headers()}")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailRepository", "❌ Exception occurred: ${e.message}", e)
            Log.e("EmailRepository", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
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
