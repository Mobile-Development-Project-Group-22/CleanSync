package com.example.cleansync.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.model.Booking
import java.time.LocalDate

@Composable
fun CalendarView(
    bookingsByDate: Map<LocalDate, List<Booking>>,
    onDateClick: (LocalDate) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val today = remember { LocalDate.now() }

    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.withDayOfMonth(1)
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Monday = 0

    val dates = List(startDayOfWeek) { null } + List(daysInMonth) {
        selectedMonth.withDayOfMonth(it + 1)
    }

    val rows = dates.chunked(7).map { week ->
        if (week.size < 7) week + List(7 - week.size) { null } else week
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = selectedMonth.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() } + " ${selectedMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day Labels
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar Grid
        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date == null) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        } else {
                            val isBooked = bookingsByDate.containsKey(date)
                            val isToday = date == today
                            val isPast = date.isBefore(today)

                            val bgColor = when {
                                isToday -> MaterialTheme.colorScheme.primary
                                isBooked -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val textColor = when {
                                isToday -> MaterialTheme.colorScheme.onPrimary
                                isBooked -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val clickable = !isPast

                            Surface(
                                shape = CircleShape,
                                color = bgColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .let {
                                        if (clickable) {
                                            it.clickable { onDateClick(date) }
                                        } else {
                                            it // Disabled, no click
                                        }
                                    }
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )

                                    // Dot for upcoming bookings
                                    if (isBooked && !isPast) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .padding(top = 2.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}