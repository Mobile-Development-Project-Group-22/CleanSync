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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarpetInputForm(
    length: String,
    width: String,
    fabricType: String,
    fabricTypes: List<String>,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onFabricTypeChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    val maxCarpetSize = 50.0
    val coroutineScope = rememberCoroutineScope()

    // Error state and animation
    var showError by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    
    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    // Shake animation
    suspend fun shake() {
        offsetX.snapTo(0f)
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                -10f at 50
                10f at 100
                -8f at 150
                8f at 200
                -4f at 250
                4f at 300
                0f at 350
            }
        )
    }

    // Validation with feedback
    fun validateAndFilter(input: String, onChange: (String) -> Unit) {
        val cleaned = input.replace(',', '.').filterIndexed { index, c ->
            c.isDigit() || (c == '.' && input.indexOf('.') == index)
        }

        val number = cleaned.toDoubleOrNull()
        when {
            number == null -> onChange("")
            number > maxCarpetSize -> {
                onChange(maxCarpetSize.toString())
                showError = true
                coroutineScope.launch { shake() }
            }
            else -> {
                onChange(cleaned)
                showError = false
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = length,
            onValueChange = {
                if (it.isEmpty()) onLengthChange("") else validateAndFilter(it, onLengthChange)
            },
            label = { Text("Carpet Length (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = width,
            onValueChange = {
                if (it.isEmpty()) onWidthChange("") else validateAndFilter(it, onWidthChange)
            },
            label = { Text("Carpet Width (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Fabric Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = fabricType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fabric Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                fabricTypes.forEach { fabric ->
                    DropdownMenuItem(
                        text = { Text(fabric) },
                        onClick = {
                            onFabricTypeChange(fabric)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (showError) {
            Text(
                text = "Value can't be greater than 50",
                color = Color.Red,
                modifier = Modifier
                    .offset(x = offsetX.value.dp)
                    .padding(start = 8.dp)
            )
        }

        Button(onClick = onCalculate, modifier = Modifier.align(Alignment.End)) {
            Text("Calculate")
        }
    }
}



