package com.example.cleansync.data.model

data class EmailResponse(
    val success: Boolean,
    val message: String,
    val error: String? = null
)

data class EmailRequest(
    val personalizations: List<Map<String, Any>>,
    val from: Map<String, String>,
    val content: List<Map<String, String>>
)
