// components/CarpetInputForm.kt
package com.example.cleansync.ui.booking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
@Composable
fun CarpetInputForm(
    length: String,
    width: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = length,
            onValueChange = onLengthChange,
            label = { Text("Carpet Length (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = width,
            onValueChange = onWidthChange,
            label = { Text("Carpet Width (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = onCalculate, modifier = Modifier.align(Alignment.End)) {
            Text("Calculate")
        }
    }
}
