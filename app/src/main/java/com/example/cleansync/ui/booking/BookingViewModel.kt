package com.example.cleansync.ui.booking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
//libraries for firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.cleansync.model.Booking
import android.util.Log

class BookingViewModel : ViewModel() {
//firebase reference
private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun saveBookingToFirestore(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val booking = Booking(
            userId = userId,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            streetAddress = streetAddress,
            postalCode = postalCode,
            city = city,
            length = length,
            width = width,
            estimatedPrice = estimatedPrice ?: 0f,
            bookingDateTime = formattedDateTime
        )

        db.collection("bookings")
            .add(booking)
            .addOnSuccessListener {
                Log.d("Firestore", "Booking saved successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving booking", e)
                onFailure(e)
            }
    }

    var length by mutableStateOf("")
    var width by mutableStateOf("")
    var estimatedPrice by mutableStateOf<Float?>(null)
    var showInputFields by mutableStateOf(false)
    var selectedDateTime by mutableStateOf<LocalDateTime?>(null)
    val formattedDateTime: String
        get() = selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")) ?: ""

    // Form fields
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var streetAddress by mutableStateOf("")
    var postalCode by mutableStateOf("")
    var city by mutableStateOf("")
    var acceptTerms by mutableStateOf(false)

    // Address autocomplete
    private val _addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    val addressSuggestions: StateFlow<List<String>> = _addressSuggestions

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun toggleInputFields() {
        showInputFields = !showInputFields
    }

    fun calculatePrice() {
        val l = length.toFloatOrNull()
        val w = width.toFloatOrNull()
        estimatedPrice = if (l != null && w != null) l * w * 4f else null
    }

    fun resetBooking() {
        length = ""
        width = ""
        estimatedPrice = null
        selectedDateTime = null
        showInputFields = false
    }

    fun fetchAddressSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val response = GeoapifyApi.getSuggestions(query)
                _addressSuggestions.value = response
            } catch (e: Exception) {
                _addressSuggestions.value = emptyList()
            }
        }
    }

    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun useCurrentLocation(context: Context) {
        if (!::fusedLocationClient.isInitialized) {
            initializeLocationClient(context)
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                addresses?.firstOrNull()?.let { addr ->
                    streetAddress = addr.getAddressLine(0) ?: ""
                    postalCode = addr.postalCode ?: ""
                    city = addr.locality ?: ""
                }
            }
        }
    }
}
