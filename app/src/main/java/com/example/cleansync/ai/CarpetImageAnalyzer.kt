package com.example.cleansync.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class CarpetAnalysisResult(
    val length: Float,
    val width: Float,
    val fabricType: String,
    val confidence: Float
)

class CarpetImageAnalyzer(private val context: Context) {
    
    private val GEMINI_API_KEY = "AIzaSyBE3H5cA3WRkNGkq1_ybaiUiH0XLXdAGDQ"
    private val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$GEMINI_API_KEY"
    
    /**
     * Analyzes a carpet image using Google Gemini Vision API
     * Extracts dimensions and fabric type from the image
     */
    suspend fun analyzeCarpetImage(imageUri: Uri): CarpetAnalysisResult? {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CarpetAnalyzer", "Starting image analysis for URI: $imageUri")
                
                // Load and decode the image
                val bitmap = loadBitmapFromUri(imageUri)
                if (bitmap == null) {
                    android.util.Log.e("CarpetAnalyzer", "Failed to load bitmap from URI")
                    return@withContext null
                }
                android.util.Log.d("CarpetAnalyzer", "Bitmap loaded: ${bitmap.width}x${bitmap.height}")
                
                // Convert bitmap to base64
                val base64Image = bitmapToBase64(bitmap)
                android.util.Log.d("CarpetAnalyzer", "Image converted to base64, size: ${base64Image.length} chars")
                
                // Call Gemini Vision API
                val result = callGeminiVisionAPI(base64Image)
                
                if (result != null) {
                    android.util.Log.d("CarpetAnalyzer", "Analysis successful: $result")
                } else {
                    android.util.Log.e("CarpetAnalyzer", "API returned null result")
                }
                
                result
            } catch (e: Exception) {
                android.util.Log.e("CarpetAnalyzer", "Error during analysis: ${e.message}", e)
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Load bitmap from URI
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Resize if too large to reduce API payload
                if (bitmap.width > 1024 || bitmap.height > 1024) {
                    resizeBitmap(bitmap, 1024)
                } else {
                    bitmap
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Resize bitmap to max dimension while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    
    /**
     * Convert bitmap to base64 string for API transmission
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Call Google Gemini API to analyze the carpet image
     */
    private suspend fun callGeminiVisionAPI(base64Image: String): CarpetAnalysisResult? {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CarpetAnalyzer", "Calling Gemini API: $GEMINI_API_URL")
                
                val url = URL(GEMINI_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 30000 // 30 seconds
                connection.readTimeout = 30000
                
                // Create the prompt for Gemini
                val prompt = """
                    Analyze this carpet/rug image and provide:
                    1. Estimated length in meters (realistic estimate based on typical carpet sizes)
                    2. Estimated width in meters (realistic estimate based on typical carpet sizes)
                    3. Fabric type from these options: Wool, Cotton, Silk, Polyester, Nylon, Jute, Sisal, Shag, Persian/Oriental
                    
                    Respond ONLY with a valid JSON object in this exact format (no markdown, no extra text):
                    {"length": 2.5, "width": 1.8, "fabricType": "Wool", "confidence": 85}
                    
                    Use your best judgment for dimensions. If you cannot clearly see the carpet, provide reasonable estimates.
                    Confidence should be 60-95 based on image clarity.
                """.trimIndent()
                
                // Build JSON request
                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            })
                        })
                    })
                }
                
                android.util.Log.d("CarpetAnalyzer", "Sending request to Gemini API")
                
                // Send request
                connection.outputStream.use { os ->
                    os.write(jsonRequest.toString().toByteArray())
                }
                
                // Read response
                val responseCode = connection.responseCode
                android.util.Log.d("CarpetAnalyzer", "Response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    android.util.Log.d("CarpetAnalyzer", "API Response: $response")
                    parseGeminiResponse(response)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    android.util.Log.e("CarpetAnalyzer", "Gemini API Error: $responseCode - $errorResponse")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("CarpetAnalyzer", "Exception calling Gemini API: ${e.message}", e)
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Parse Gemini API response and extract carpet analysis results
     */
    private fun parseGeminiResponse(response: String): CarpetAnalysisResult? {
        return try {
            android.util.Log.d("CarpetAnalyzer", "Parsing response: $response")
            
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            
            if (candidates.length() > 0) {
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
                val parts = content.getJSONArray("parts")
                
                if (parts.length() > 0) {
                    val text = parts.getJSONObject(0).getString("text")
                    android.util.Log.d("CarpetAnalyzer", "Extracted text: $text")
                    
                    // Extract JSON from response (Gemini might wrap it in markdown)
                    val jsonText = text
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    
                    android.util.Log.d("CarpetAnalyzer", "Cleaned JSON: $jsonText")
                    
                    val resultJson = JSONObject(jsonText)
                    
                    val result = CarpetAnalysisResult(
                        length = resultJson.getDouble("length").toFloat(),
                        width = resultJson.getDouble("width").toFloat(),
                        fabricType = resultJson.getString("fabricType"),
                        confidence = resultJson.getDouble("confidence").toFloat()
                    )
                    
                    android.util.Log.d("CarpetAnalyzer", "Parsed result: $result")
                    result
                } else {
                    android.util.Log.e("CarpetAnalyzer", "No parts in response")
                    null
                }
            } else {
                android.util.Log.e("CarpetAnalyzer", "No candidates in response")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("CarpetAnalyzer", "Error parsing response: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}

/**
 * Extension function to get analysis result with loading state
 */
suspend fun CarpetImageAnalyzer.analyzeWithState(
    imageUri: Uri,
    onResult: (CarpetAnalysisResult?) -> Unit
) {
    val result = analyzeCarpetImage(imageUri)
    onResult(result)
}
