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
import androidx.compose.material3.ExperimentalMaterial3Api
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
    val addressSuggestions by bookingViewModel.addressSuggestions.collectAsState()
    val errors = bookingViewModel.fieldErrors

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
            Icon(Icons.Filled.LocationOn, contentDescription = "Current Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Name", bookingViewModel.name, { bookingViewModel.name = it }, errors["name"])
        CustomTextField("Email", bookingViewModel.email, { bookingViewModel.email = it }, errors["email"], KeyboardType.Email)
        CustomTextField("Phone Number", bookingViewModel.phoneNumber, { bookingViewModel.phoneNumber = it }, errors["phone"], KeyboardType.Phone)
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
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
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
