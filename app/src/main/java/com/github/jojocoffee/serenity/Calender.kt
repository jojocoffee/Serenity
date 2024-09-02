package com.github.jojocoffee.serenity

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    meditatedDays: List<String>,
    performDeleteAction: (String) -> Unit
) {
    Log.d("CalendarView", "$meditatedDays")
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    //val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        // DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        HorizontalCalendar(
            state = state,
            dayContent = { Day(it, meditatedDays, performDeleteAction) },
            monthHeader = {
                MonthNameTitle(it)
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            },
        )
    }
}


@Composable
fun Day(
    day: CalendarDay,
    meditatedDays: List<String>,
    performDeleteAction: (String) -> Unit
) {
    Log.d("Day", "${day.date}")
    var showDialog by remember { mutableStateOf(false) }
    val circleColor = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (meditatedDays.contains(day.date.toString())) {
            Text(
                text = day.date.dayOfMonth.toString(),
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            color = circleColor,
                            radius = this.size.maxDimension / 2
                        )
                    }
                    .padding(10.dp)
                    .clickable {
                        showDialog = true
                    }
            )
            if (showDialog) {
                Log.d("Day", "${day.date} Text Button Clicked")
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Calendar Entries") },
                    text = { Text("Delete entry from ${day.date}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                performDeleteAction(day.date.toString())
                                Log.d("Day", "${day.date} performed Delete Action")
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else {
            Text(
                text = day.date.dayOfMonth.toString()
            )
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    )
    {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)
                // text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
fun MonthNameTitle(calendarMonth: CalendarMonth) {
    val monthName = calendarMonth.yearMonth.month.toString() + " " + calendarMonth.yearMonth.year.toString()
    // val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Center
    )
    {
        Text(
            text = monthName
        )
    }
}

