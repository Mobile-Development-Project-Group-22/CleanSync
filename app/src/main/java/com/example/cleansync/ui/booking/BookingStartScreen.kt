package com.example.cleansync.ui.booking

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleansync.ai.CarpetImageAnalyzer
import com.example.cleansync.ui.booking.components.AIHelperButton
import com.example.cleansync.ui.booking.components.CarpetInputForm
import com.example.cleansync.ui.booking.components.DateAndHourPicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStartScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        bookingViewModel.resetBooking()
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showCouponField by remember { mutableStateOf(false) }
    var isAnalyzingImage by remember { mutableStateOf(false) }
    var analysisMessage by remember { mutableStateOf<String?>(null) }
    
    // AI Image Analyzer - Initialize safely
    val carpetAnalyzer = remember { 
        try {
            CarpetImageAnalyzer(context)
        } catch (e: Exception) {
            android.util.Log.e("BookingStart", "Failed to initialize CarpetImageAnalyzer", e)
            null
        }
    }
    
    // Handle image capture from AI helper
    val handleImageCapture: (Uri) -> Unit = { imageUri ->
        if (carpetAnalyzer != null) {
            coroutineScope.launch {
                isAnalyzingImage = true
                analysisMessage = "🤖 Analyzing carpet image..."
                
                try {
                    val result = carpetAnalyzer.analyzeCarpetImage(imageUri)
                    
                    if (result != null) {
                        // Check if it's actually a carpet
                        if (!result.isCarpet) {
                            // Not a carpet image
                            analysisMessage = "❌ ${result.errorMessage ?: "This is not a carpet. Please take a clear picture of a carpet or rug."}"
                        } else {
                            // Valid carpet - update the booking fields with AI results
                            bookingViewModel.length = result.length.toString()
                            bookingViewModel.width = result.width.toString()
                            bookingViewModel.fabricType = result.fabricType
                            
                            analysisMessage = "✅ Detected: ${result.fabricType} carpet (${result.confidence.toInt()}% confidence)"
                            
                            // Auto-calculate price
                            kotlinx.coroutines.delay(1000)
                            bookingViewModel.calculatePrice()
                            showCouponField = true
                        }
                    } else {
                        analysisMessage = "❌ Could not analyze image. Please enter manually."
                    }
                } catch (e: Exception) {
                    analysisMessage = "❌ Analysis failed: ${e.message}"
                    android.util.Log.e("BookingStart", "Image analysis failed", e)
                } finally {
                    isAnalyzingImage = false
                    // Clear message after 5 seconds
                    kotlinx.coroutines.delay(5000)
                    analysisMessage = null
                }
            }
        } else {
            analysisMessage = "❌ AI analyzer not available"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Book Carpet Cleaning",
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AI Analysis message
            analysisMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.startsWith("✅")) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Start Calculation Button
            if (!bookingViewModel.estimatedPriceCalculated) {
                Button(
                    onClick = { bookingViewModel.toggleInputFields() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Calculation",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Carpet Input Form
            AnimatedVisibility(visible = bookingViewModel.showInputFields && !bookingViewModel.estimatedPriceCalculated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Carpet Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
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
                }
            }

            // Price Card
            bookingViewModel.estimatedPrice?.let { price ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Price Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cleaning Price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cleaning Service",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "€${"%.2f".format(price)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Pickup & Delivery Fee
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pickup & Delivery",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "€10.00",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                        )
                        
                        // Total Price
                        bookingViewModel.totalPrice?.let { total ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Price",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "€${"%.2f".format(total)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Coupon Field
                if (showCouponField) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = "Coupon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Have a coupon code?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                OutlinedTextField(
                                    value = bookingViewModel.couponCode,
                                    onValueChange = { bookingViewModel.couponCode = it },
                                    label = { Text("Coupon Code") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    enabled = !bookingViewModel.couponApplied,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { bookingViewModel.applyCoupon() },
                                    enabled = !bookingViewModel.couponApplied && bookingViewModel.couponCode.isNotEmpty(),
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Apply")
                                }
                            }

                            bookingViewModel.couponMessage?.let { message ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
                    }
                }

                // Date & Time Selection
                if (bookingViewModel.selectedDateTime == null) {
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Select Date & Time",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (showDatePicker) {
                    DateAndHourPicker(
                        context = context,
                        bookingViewModel = bookingViewModel
                    )
                }
            }

            // Selected Date & Time Display
            bookingViewModel.selectedDateTime?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scheduled For",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bookingViewModel.formattedDateTime,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Button(
                    onClick = onBookingConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Continue to Booking Form",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onBookingCancelled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }

            bookingViewModel.errorMessage?.takeIf { bookingViewModel.estimatedPrice == null }?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Bottom padding for better scrolling
            Spacer(modifier = Modifier.height(80.dp))
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
}
