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
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
@Composable
fun CarpetInputForm(
    length: String,
    width: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    val maxCarpetSize = 50.0

    // Filters input: valid decimal number <= 50
    fun validateAndFilter(input: String): String {
        // Allow only digits and one optional dot
        val cleaned = input.replace(',', '.').filterIndexed { index, c ->
            c.isDigit() || (c == '.' && input.indexOf('.') == index)
        }

        val number = cleaned.toDoubleOrNull()
        return when {
            number == null -> ""
            number > maxCarpetSize -> maxCarpetSize.toString()
            else -> cleaned
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = length,
            onValueChange = {
                val filtered = validateAndFilter(it)
                if (filtered.isNotEmpty()) onLengthChange(filtered)
                else if (it.isEmpty()) onLengthChange("") // Allow clearing
            },
            label = { Text("Carpet Length (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = width,
            onValueChange = {
                val filtered = validateAndFilter(it)
                if (filtered.isNotEmpty()) onWidthChange(filtered)
                else if (it.isEmpty()) onWidthChange("")
            },
            label = { Text("Carpet Width (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(onClick = onCalculate, modifier = Modifier.align(Alignment.End)) {
            Text("Calculate")
        }
    }
}



