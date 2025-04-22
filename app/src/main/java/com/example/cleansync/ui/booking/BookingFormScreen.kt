package com.example.cleansync.ui.booking

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val addressSuggestions by bookingViewModel.addressSuggestions.collectAsState()
    val errors = bookingViewModel.fieldErrors

    val locationPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Manifest.permission.ACCESS_FINE_LOCATION
        else
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

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
            Icon(Icons.Filled.LocationOn, contentDescription = "Current Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Name", bookingViewModel.name, { bookingViewModel.name = it }, errors["name"])
        CustomTextField("Email", bookingViewModel.email, { bookingViewModel.email = it }, errors["email"], KeyboardType.Email)

        // PHONE NUMBER FIELD WITH FINLAND FLAG +358
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Phone Number")
            OutlinedTextField(
                value = bookingViewModel.phoneNumber,
                onValueChange = { bookingViewModel.phoneNumber = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errors["phone"] != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text(text = "🇫🇮 +358", fontSize = 14.sp)
                }
            )
            errors["phone"]?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }

        CustomTextField("Street Address", bookingViewModel.streetAddress, {
            bookingViewModel.streetAddress = it
            bookingViewModel.fetchAddressSuggestions(it)
        }, errors["address"])

        if (addressSuggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                addressSuggestions.forEach { suggestion ->
                    TextButton(onClick = {
                        bookingViewModel.streetAddress = suggestion
                    }) {
                        Text(suggestion)
                    }
                }
            }
        }

        CustomTextField("Postal Code", bookingViewModel.postalCode, { bookingViewModel.postalCode = it }, errors["postalCode"])
        CustomTextField("City", bookingViewModel.city, { bookingViewModel.city = it }, errors["city"])

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Checkbox(
                checked = bookingViewModel.acceptTerms,
                onCheckedChange = { bookingViewModel.acceptTerms = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I accept the terms and conditions", fontSize = 14.sp)
        }

        bookingViewModel.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                bookingViewModel.saveBookingToFirestore(
                    onSuccess = { onBookingDone() },
                    onFailure = {}
                )
            },
            enabled = bookingViewModel.acceptTerms,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Confirm Booking")
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}
