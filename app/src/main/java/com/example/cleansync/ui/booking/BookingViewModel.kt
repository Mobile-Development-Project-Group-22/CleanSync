// BookingViewModel.kt

package com.example.cleansync.ui.booking

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookingViewModel : ViewModel() {

    var length by mutableStateOf("")
    var width by mutableStateOf("")
    var estimatedPrice by mutableStateOf<Float?>(null)

    var showInputFields by mutableStateOf(false)

    var selectedDateTime by mutableStateOf<LocalDateTime?>(null)

    val formattedDateTime: String
        get() = selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")) ?: ""

    fun toggleInputFields() {
        showInputFields = !showInputFields
    }

    fun calculatePrice() {
        val l = length.toFloatOrNull()
        val w = width.toFloatOrNull()

        if (l != null && w != null) {
            estimatedPrice = l * w * 4f
        } else {
            estimatedPrice = null
        }
    }

    fun resetBooking() {
        length = ""
        width = ""
        estimatedPrice = null
        selectedDateTime = null
        showInputFields = false
    }
}
