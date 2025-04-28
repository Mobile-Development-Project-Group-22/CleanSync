package com.example.cleansync.ui.booking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Patterns
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.model.EmailRequest
import com.example.cleansync.data.repository.EmailRepository
import com.example.cleansync.model.Booking
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

import java.util.*

class BookingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    // Add a method to update the progress stage
    fun updateProgressStage(bookingId: String, newStage: String) {
        db.collection("bookings")
            .document(bookingId)
            .update("progressStage", newStage)
            .addOnSuccessListener {
                Log.d("BookingViewModel", "Progress updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("BookingViewModel", "Failed to update progress", e)
            }
    }
    private val emailRepository = EmailRepository()

    var isSendingEmail by mutableStateOf(false)
    var emailSentSuccess by mutableStateOf<Boolean?>(null)
    //coupon
    // Pickup and delivery fee
    private val pickupAndDeliveryFee = 10f
    var couponCode by mutableStateOf("")
    var couponApplied by mutableStateOf(false)
    var couponMessage by mutableStateOf<String?>(null)
    private var originalPrice: Float? = null
    val estimatedPriceCalculated: Boolean
        get() = estimatedPrice != null

    fun applyCoupon() {
        val code = couponCode.trim().uppercase()

        if (estimatedPrice == null) {
            errorMessage = "Calculate price before applying coupon"
            return
        }

        if (!couponApplied) {
            originalPrice = estimatedPrice // Save original to allow multiple applications
        }

        when (code) {
            "DISCOUNT10" -> {
                estimatedPrice = originalPrice?.times(0.9f)
                couponApplied = true
                couponMessage = "🎉 10% discount applied!"
                errorMessage = null
            }

            "JANNE" -> {
                estimatedPrice = originalPrice?.times(0.5f)
                couponApplied = true
                couponMessage = "🎉 50% discount applied!"
                errorMessage = null
            }

            else -> {
                errorMessage = "❌ Invalid coupon code"
                couponMessage = null
                couponApplied = false
                estimatedPrice = originalPrice
            }
        }
        calculateTotalPrice()
    }

    var length by mutableStateOf("")
    var width by mutableStateOf("")
    var estimatedPrice by mutableStateOf<Float?>(null)
    // To store total price including the pickup and delivery fee
    var totalPrice by mutableStateOf<Float?>(null)
    var showInputFields by mutableStateOf(false)
    var selectedDateTime by mutableStateOf<LocalDateTime?>(null)
    val formattedDateTime: String
        get() = selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")) ?: ""

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var streetAddress by mutableStateOf("")
    var postalCode by mutableStateOf("")
    var city by mutableStateOf("")
    var acceptTerms by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)
    var fieldErrors by mutableStateOf(mapOf<String, String?>())

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
        calculateTotalPrice()
    }
    // Recalculate the total price after including pickup and delivery fee
    private fun calculateTotalPrice() {
        totalPrice = if (estimatedPrice != null) {
            estimatedPrice!! + pickupAndDeliveryFee
        } else {
            null
        }
    }

    fun resetBooking() {
        length = ""
        width = ""
        estimatedPrice = null
        totalPrice = null
        selectedDateTime = null
        name = ""
        email = ""
        phoneNumber = ""
        streetAddress = ""
        postalCode = ""
        city = ""
        acceptTerms = false
        showInputFields = false
        errorMessage = null
        fieldErrors = emptyMap()
        // Reset coupon
        couponCode = ""
        couponApplied = false
        couponMessage = null
    }

    fun validateInputs(): Boolean {
        val errors = mutableMapOf<String, String?>()

        if (name.isBlank()) errors["name"] = "Name is required"
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) errors["email"] = "Invalid email"
        if (phoneNumber.isBlank()) errors["phone"] = "Phone number is required"
        if (streetAddress.isBlank()) errors["address"] = "Street address is required"
        if (postalCode.isBlank()) errors["postalCode"] = "Postal code is required"
        if (city.isBlank()) errors["city"] = "City is required"
        if (!acceptTerms) errorMessage = "You must accept terms to continue"
        else errorMessage = null

        fieldErrors = errors
        return errors.isEmpty()
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

    fun updateSelectedDateTime(dateTime: LocalDateTime) {
        selectedDateTime = dateTime
    }

    fun sendBookingConfirmationEmail() {
        val bookingDetails = """
        Booking Details:
        Name: $name
        Address: $streetAddress, $postalCode $city
        Date & Time: $formattedDateTime
        Estimated Price: €$estimatedPrice
        Pickup & Delivery Fee: €10.0
        Total Price: €$totalPrice
    """.trimIndent()

        val emailRequest = EmailRequest(
            personalizations = listOf(
                mapOf(
                    "to" to listOf(mapOf("email" to email)),
                    "subject" to "Booking Confirmation"
                )
            ),
            from = mapOf("email" to "t3shro00@students.oamk.fi"),
            content = listOf(
                mapOf("type" to "text/plain", "value" to "Thank you for your booking!\n\n$bookingDetails")
            )
        )

        viewModelScope.launch {
            isSendingEmail = true
            val result = emailRepository.sendConfirmationEmail(emailRequest)
            isSendingEmail = false
            if (result) {
                emailSentSuccess = true
            } else {
                errorMessage = "Failed to send confirmation email."
            }
        }
    }


    fun saveBookingToFirestore(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "You must be logged in to confirm a booking."
            return
        }

        if (!validateInputs()) return

        if (estimatedPrice == null) {
            errorMessage = "Please calculate the price first."
            return
        }

        val newBooking = Booking(
            userId = userId,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            streetAddress = streetAddress,
            postalCode = postalCode,
            city = city,
            length = length,
            width = width,
            estimatedPrice = estimatedPrice!!,
            totalPrice = totalPrice ?: (estimatedPrice!! + pickupAndDeliveryFee),
            bookingDateTime = formattedDateTime,
        )

        db.collection("bookings")
            .add(newBooking)
            .addOnSuccessListener { documentRef ->
                documentRef.update("id", documentRef.id)
                    .addOnSuccessListener {
                        errorMessage = null
                        sendBookingConfirmationEmail()
                        onSuccess()
                    }
                    .addOnFailureListener {
                        errorMessage = "Booking saved, but failed to update ID."
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                errorMessage = "Failed to save booking. Please try again."
                onFailure(it)
            }
    }
}
