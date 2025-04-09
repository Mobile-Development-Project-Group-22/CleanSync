package com.example.cleansync.ui.booking

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BookingFormScreen(
    bookingViewModel: BookingViewModel = viewModel(),
    onBookingDone: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val addressSuggestions by bookingViewModel.addressSuggestions.collectAsState()

    val locationPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Manifest.permission.ACCESS_FINE_LOCATION
        else
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Button(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    bookingViewModel.useCurrentLocation(context)
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bookingViewModel.name,
            onValueChange = { bookingViewModel.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bookingViewModel.email,
            onValueChange = { bookingViewModel.email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bookingViewModel.phoneNumber,
            onValueChange = { bookingViewModel.phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bookingViewModel.streetAddress,
            onValueChange = {
                bookingViewModel.streetAddress = it
                bookingViewModel.fetchAddressSuggestions(it)
            },
            label = { Text("Street Address") },
            modifier = Modifier.fillMaxWidth()
        )

        addressSuggestions.forEach { suggestion ->
            TextButton(onClick = {
                bookingViewModel.streetAddress = suggestion
            }) {
                Text(suggestion)
            }
        }

        OutlinedTextField(
            value = bookingViewModel.postalCode,
            onValueChange = { bookingViewModel.postalCode = it },
            label = { Text("Postal Code") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bookingViewModel.city,
            onValueChange = { bookingViewModel.city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Checkbox(
                checked = bookingViewModel.acceptTerms,
                onCheckedChange = { bookingViewModel.acceptTerms = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I accept the terms and conditions")
        }

        Button(
            onClick = { onBookingDone() },
            enabled = bookingViewModel.acceptTerms,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Confirm Booking")
        }
    }
}
