package com.example.cleansync.ui.booking

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BookingFormScreen(
    bookingViewModel: BookingViewModel,
    onBookingDone: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val addressSuggestions by bookingViewModel.addressSuggestions.collectAsState()
    val errorMessage = bookingViewModel.errorMessage

    val locationPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Manifest.permission.ACCESS_FINE_LOCATION
        else
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Use Current Location Button
        Button(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    bookingViewModel.useCurrentLocation(context)
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "Current Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name Input
        OutlinedTextField(
            value = bookingViewModel.name,
            onValueChange = { bookingViewModel.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Email Input
        OutlinedTextField(
            value = bookingViewModel.email,
            onValueChange = { bookingViewModel.email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Phone Number Input
        OutlinedTextField(
            value = bookingViewModel.phoneNumber,
            onValueChange = { bookingViewModel.phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Street Address Input
        OutlinedTextField(
            value = bookingViewModel.streetAddress,
            onValueChange = {
                bookingViewModel.streetAddress = it
                bookingViewModel.fetchAddressSuggestions(it)
            },
            label = { Text("Street Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Address Suggestions
        if (addressSuggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                addressSuggestions.forEach { suggestion ->
                    TextButton(onClick = {
                        bookingViewModel.streetAddress = suggestion
                    }) {
                        Text(suggestion, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Postal Code Input
        OutlinedTextField(
            value = bookingViewModel.postalCode,
            onValueChange = { bookingViewModel.postalCode = it },
            label = { Text("Postal Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // City Input
        OutlinedTextField(
            value = bookingViewModel.city,
            onValueChange = { bookingViewModel.city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Terms and Conditions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Checkbox(
                checked = bookingViewModel.acceptTerms,
                onCheckedChange = { bookingViewModel.acceptTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I accept the terms and conditions", fontSize = 14.sp)
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        // Confirm Booking Button
        Button(
            onClick = {
                bookingViewModel.saveBookingToFirestore(
                    onSuccess = {
                        onBookingDone()
                    },
                    onFailure = {
                        // Show an error message, log, etc.
                    }
                )
            },

            enabled = bookingViewModel.acceptTerms,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Confirm Booking")
        }
    }
}