package com.example.cleansync.ui.booking

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleansync.ai.CarpetImageAnalyzer
import com.example.cleansync.ui.booking.components.AIHelperButton
import com.example.cleansync.ui.booking.components.CarpetInputForm
import com.example.cleansync.ui.booking.components.DateAndHourPicker
import kotlinx.coroutines.launch

@Composable
fun BookingStartScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    LaunchedEffect(Unit) {
        bookingViewModel.resetBooking()
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCouponField by remember { mutableStateOf(false) }
    var isAnalyzingImage by remember { mutableStateOf(false) }
    var analysisMessage by remember { mutableStateOf<String?>(null) }
    
    // AI Image Analyzer
    val carpetAnalyzer = remember { CarpetImageAnalyzer(context) }
    
    // Handle image capture from AI helper
    val handleImageCapture: (Uri) -> Unit = { imageUri ->
        coroutineScope.launch {
            isAnalyzingImage = true
            analysisMessage = "🤖 Analyzing carpet image..."
            
            try {
                val result = carpetAnalyzer.analyzeCarpetImage(imageUri)
                
                if (result != null) {
                    // Update the booking fields with AI results
                    bookingViewModel.length = result.length.toString()
                    bookingViewModel.width = result.width.toString()
                    bookingViewModel.fabricType = result.fabricType
                    
                    analysisMessage = "✅ Detected: ${result.fabricType} carpet (${result.confidence.toInt()}% confidence)"
                    
                    // Auto-calculate price
                    kotlinx.coroutines.delay(1000)
                    bookingViewModel.calculatePrice()
                    showCouponField = true
                } else {
                    analysisMessage = "❌ Could not analyze image. Please enter manually."
                }
            } catch (e: Exception) {
                analysisMessage = "❌ Analysis failed: ${e.message}"
            } finally {
                isAnalyzingImage = false
                // Clear message after 5 seconds
                kotlinx.coroutines.delay(5000)
                analysisMessage = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Book Carpet Cleaning",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // AI Analysis message
            analysisMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (!bookingViewModel.estimatedPriceCalculated) {
                Button(onClick = { bookingViewModel.toggleInputFields() }) {
                    Text("Start Calculation")
                }
            }

        AnimatedVisibility(visible = bookingViewModel.showInputFields && !bookingViewModel.estimatedPriceCalculated) {
            CarpetInputForm(
                length = bookingViewModel.length,
                width = bookingViewModel.width,
                fabricType = bookingViewModel.fabricType,
                fabricTypes = bookingViewModel.fabricTypes,
                onLengthChange = { bookingViewModel.length = it },
                onWidthChange = { bookingViewModel.width = it },
                onFabricTypeChange = { bookingViewModel.fabricType = it },
                onCalculate = {
                    bookingViewModel.calculatePrice()
                    showCouponField = true
                }
            )
        }

        bookingViewModel.estimatedPrice?.let { price ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated Price",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "€${"%.2f".format(price)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // Pickup and Delivery Fee
            Text(
                text = "Pickup & Delivery Fee: €10.00",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            // Total Price
            bookingViewModel.totalPrice?.let { total ->
                Text(
                    text = "Total Price: €${"%.2f".format(total)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (showCouponField) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = bookingViewModel.couponCode,
                        onValueChange = { bookingViewModel.couponCode = it },
                        label = { Text("Coupon Code (optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !bookingViewModel.couponApplied
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { bookingViewModel.applyCoupon() },
                        enabled = !bookingViewModel.couponApplied
                    ) {
                        Text("Apply")
                    }
                }

                bookingViewModel.couponMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                bookingViewModel.errorMessage?.takeIf { bookingViewModel.estimatedPrice != null }?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (bookingViewModel.selectedDateTime == null) {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Select Date & Time")
                }
            }

            if (showDatePicker) {
                DateAndHourPicker(
                    context = context,
                    bookingViewModel = bookingViewModel
                )
            }
        }

        bookingViewModel.selectedDateTime?.let {
            Text(
                text = "Selected: ${bookingViewModel.formattedDateTime}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onBookingConfirmed,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Continue to Booking Form")
            }

            OutlinedButton(onClick = onBookingCancelled) {
                Text("Cancel")
            }
        }

        bookingViewModel.errorMessage?.takeIf { bookingViewModel.estimatedPrice == null }?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
    
    // AI Helper Button - Floating Action Button
    if (bookingViewModel.showInputFields && !bookingViewModel.estimatedPriceCalculated) {
        AIHelperButton(
            onImageCaptured = handleImageCapture,
            isAnalyzing = isAnalyzingImage
        )
    }
  }
}
