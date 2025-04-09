package com.example.cleansync.ui.booking



import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL

object GeoapifyApi {
    private const val API_KEY = "e920f1b37d124c6fad6c4ccc8cf75b8f"

    suspend fun getSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url =
            "https://api.geoapify.com/v1/geocode/search?text=$encoded&limit=5&filter=countrycode:fi&format=json&apiKey=$API_KEY"
        val response = URL(url).readText()
        val json = JSONObject(response)
        val features = json.getJSONArray("results")

        List(features.length()) {
            features.getJSONObject(it).getString("formatted")
        }
    }
}
