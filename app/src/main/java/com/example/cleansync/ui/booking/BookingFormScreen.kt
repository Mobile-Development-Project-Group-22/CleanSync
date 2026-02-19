package com.example.cleansync.ui.booking

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    bookingViewModel: BookingViewModel,
    onBookingDone: () -> Unit
) {
    val context = LocalContext.current
    val addressSuggestions by bookingViewModel.addressSuggestions.collectAsState()
    val errors = bookingViewModel.fieldErrors

    // Auto-fill name and email from Firebase Auth
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            if (bookingViewModel.name.isEmpty()) {
                bookingViewModel.name = user.displayName ?: ""
            }
            if (bookingViewModel.email.isEmpty()) {
                bookingViewModel.email = user.email ?: ""
            }
        }
    }

    val locationPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Manifest.permission.ACCESS_FINE_LOCATION
        else
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Booking Details",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location Button
            OutlinedButton(
                onClick = {
                    if (locationPermissionState.status.isGranted) {
                        bookingViewModel.useCurrentLocation(context)
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Current Location", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Use Current Location", style = MaterialTheme.typography.bodyMedium)
            }

            // All form fields in single card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CustomTextField("Name", bookingViewModel.name, { bookingViewModel.name = it }, errors["name"])
                    CustomTextField("Email", bookingViewModel.email, { bookingViewModel.email = it }, errors["email"], KeyboardType.Email)

                    // Phone Number Field
                    OutlinedTextField(
                        value = bookingViewModel.phoneNumber,
                        onValueChange = { newPhoneNumber ->
                            if (newPhoneNumber.length <= 9 && newPhoneNumber.all { it.isDigit() }) {
                                bookingViewModel.phoneNumber = newPhoneNumber
                            }
                            if (newPhoneNumber.length > 9) {
                                bookingViewModel.fieldErrors = bookingViewModel.fieldErrors + ("phone" to "Invalid number")
                            } else {
                                bookingViewModel.fieldErrors = bookingViewModel.fieldErrors - "phone"
                            }
                        },
                        label = { Text("Phone Number", style = MaterialTheme.typography.bodyMedium) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = bookingViewModel.fieldErrors["phone"] != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        prefix = {
                            Text(
                                text = "+358 ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingText = bookingViewModel.fieldErrors["phone"]?.let { errorText ->
                            { Text(text = errorText, style = MaterialTheme.typography.bodySmall) }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )

                    CustomTextField("Street Address", bookingViewModel.streetAddress, {
                        bookingViewModel.streetAddress = it
                        bookingViewModel.fetchAddressSuggestions(it)
                    }, errors["address"])

                    if (addressSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                addressSuggestions.take(3).forEach { suggestion ->
                                    TextButton(
                                        onClick = {
                                            bookingViewModel.streetAddress = suggestion
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            suggestion,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField("Postal Code", bookingViewModel.postalCode, { bookingViewModel.postalCode = it }, errors["postalCode"])
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField("City", bookingViewModel.city, { bookingViewModel.city = it }, errors["city"])
                        }
                    }
                }
            }

            // Terms and Confirmation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = bookingViewModel.acceptTerms,
                    onCheckedChange = { bookingViewModel.acceptTerms = it }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "I accept the terms and conditions",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            bookingViewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
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
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Booking", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = error != null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        supportingText = if (error != null) {
            { Text(text = error, style = MaterialTheme.typography.bodySmall) }
        } else null,
        shape = RoundedCornerShape(8.dp)
    )
}
