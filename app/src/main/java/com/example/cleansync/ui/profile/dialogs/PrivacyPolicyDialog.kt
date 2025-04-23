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


@Composable
fun PrivacyPolicyDialog(
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
                            text = "Privacy Policy",
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
                                    text = privacyPolicyText,
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

//Privacy policy text
val privacyPolicyText = """
    CleanSync values your privacy and is committed to protecting your personal information. This Privacy Policy explains how CleanSync collects, uses, and protects your information when you use our mobile app and services.

1. Information We Collect
When you use CleanSync, we may collect the following types of information:

Personal Information: Name, email address, phone number, and payment information.

Usage Data: Information about how you use our app, including device information, IP address, and app interaction data.

Payment Information: Payment details collected during transactions processed through our app.

2. How We Use Your Information
We use your personal information for the following purposes:

To provide and manage the carpet cleaning services.

To process payments securely.

To communicate with you about your orders and provide customer support.

To improve the app and enhance user experience.

To comply with legal and regulatory obligations.

3. Data Sharing
We do not sell or rent your personal information to third parties. We may share your information in the following circumstances:

Service Providers: We may share data with trusted third-party vendors who assist in the operation of the app (e.g., payment processors, delivery services).

Legal Compliance: We may disclose your information if required by law, such as in response to a subpoena or to comply with legal processes.

4. Data Retention
We retain your personal data only for as long as necessary to fulfill the purposes outlined in this Privacy Policy, including for legal, accounting, or reporting purposes.

5. Security
We use industry-standard security measures, including encryption and secure data storage, to protect your personal information. However, no method of transmission over the internet or electronic storage is 100% secure, and we cannot guarantee absolute security.

6. Your Rights
You have the right to:

Access and update your personal information through your CleanSync account settings.

Request the deletion of your personal data, subject to legal requirements.

Opt-out of marketing communications at any time.

7. Children's Privacy
Our services are not directed to children under the age of 16. We do not knowingly collect personal information from children. If you believe we have inadvertently collected such data, please contact us, and we will take steps to delete it.

8. Changes to This Privacy Policy
CleanSync reserves the right to update this Privacy Policy. Any changes will be posted in the app, and the updated policy will take effect immediately upon posting. Your continued use of the app after such updates signifies your acceptance of the revised policy.

9. Contact Us
If you have any questions or concerns about our Privacy Policy or data practices, please contact us at:contact@cleansync.com
""".trimIndent()

