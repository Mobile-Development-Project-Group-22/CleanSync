package com.example.cleansync.ui.profile.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

//for terms and conditions
@Composable
fun TermsAndConditionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Cover 95% of the screen width
                    .fillMaxHeight(0.9f) // Cover 90% of the screen height
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = "Terms and Conditions",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Scrollable Content
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    text = termsAndConditionsParagraph,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Close Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

// Sample Terms and Conditions Paragraph
val termsAndConditionsParagraph = """
    These Terms and Conditions ("Terms") govern your use of the CleanSync mobile app and services ("Service"). By using CleanSync, you agree to comply with these Terms. If you do not agree to these Terms, please do not use the Service.

1. Services Provided
CleanSync offers carpet cleaning services through a mobile app, including pick-up, cleaning, and delivery of carpets. Additional services such as stain removal and expedited cleaning may be offered as premium services.

2. Account Registration
To use the CleanSync Service, you must create an account. You agree to provide accurate, complete, and current information during registration and keep your account information up to date.

3. Booking and Payment
You agree to book services through the app and provide accurate information regarding the size and condition of the carpet. You will be charged based on the size and type of cleaning required. Payments will be processed through secure payment methods available within the app.

4. Cancellations and Refunds
You may cancel or reschedule your booking up to 24 hours before the scheduled pick-up. Cancellations or changes made less than 24 hours in advance may incur a cancellation fee. If the service is unsatisfactory, please contact customer support within 24 hours of receiving your cleaned carpet for a resolution, which may include a partial or full refund, at CleanSync's discretion.

5. Customer Obligations
You agree to provide accurate information regarding the size and condition of your carpet. You are responsible for ensuring that the carpet is free from hazardous substances or items that may cause harm or damage during cleaning. CleanSync is not liable for any damage caused by undisclosed issues such as excessive wear, embedded dirt, or damage due to neglect.

6. Intellectual Property
CleanSync’s app and all related content, including trademarks, logos, and service marks, are owned by CleanSync and protected by copyright and intellectual property laws. You may not reproduce, distribute, or modify any of CleanSync’s content without prior written consent.

7. Limitation of Liability
CleanSync’s liability is limited to the amount paid for the specific service rendered. CleanSync is not responsible for any indirect, incidental, or consequential damages, including damage to property or loss of use.

8. Privacy
By using the Service, you agree to our Privacy Policy (see below). CleanSync will handle your personal data with care and in compliance with applicable privacy laws.

9. Amendments
CleanSync reserves the right to update these Terms at any time. Any changes will be effective once posted in the app. Continued use of the Service constitutes acceptance of the updated Terms.

10. Governing Law
These Terms are governed by and construed in accordance with the laws of Finland. Any disputes shall be resolved in the courts of Finland.
""".trimIndent()

