package com.github.jojocoffee.serenity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.floor



fun startTimer(context: Context, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TimerReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("startTimer", "${timeInMillis} ms")
    val wakeUpTime = System.currentTimeMillis() + timeInMillis

    // Save wake-up time in SharedPreferences
    val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putLong("wake_up_time", wakeUpTime).apply()
    sharedPreferences.edit().remove("remaining_time").apply()
    Log.d("startTimer", "wakeUpTime set to: $wakeUpTime")

    // Set the alarm to go off after `timeInMillis` milliseconds
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        wakeUpTime,
        pendingIntent
    )
}

fun cancelAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TimerReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    alarmManager.cancel(pendingIntent)
    Log.d("cancelAlarm", "Alarm canceled")
}

fun pauseTimer(context: Context) {
    cancelAlarm(context)

    // Remove the wake-up time from SharedPreferences and set remaining-time
    val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    val wakeUpTime = sharedPreferences.getLong("wake_up_time", -1L)
    if (wakeUpTime != -1L) {
        val remainingTime = wakeUpTime - System.currentTimeMillis()
        if (remainingTime > 0) {
            sharedPreferences.edit().putLong("remaining_time", remainingTime).apply()
        }
    }
    sharedPreferences.edit().remove("wake_up_time").apply()
}

fun resumeTimer(context: Context) {
    val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    val remainingTime = sharedPreferences.getLong("remaining_time", -1L)

    if (remainingTime > 0) {
        startTimer(context, remainingTime)
    }
}

fun cancelTimer(context: Context) {
    cancelAlarm(context)

    val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("wake_up_time").apply()
    sharedPreferences.edit().remove("remaining_time").apply()
}

fun getRemainingTime(context: Context): Long {
    val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    val wakeUpTime = sharedPreferences.getLong("wake_up_time", -1L)
    val remainingPausedTime = sharedPreferences.getLong("remaining_time", -1L)

    return if (remainingPausedTime > 0) {
        remainingPausedTime
    } else if (wakeUpTime != -1L) {
        val currentTime = System.currentTimeMillis()
        Log.d("getRemainingTime", "Current time: $currentTime")
        val remainingTime = wakeUpTime - currentTime
        Log.d("getRemainingTime", "Remaining time: $remainingTime")
        remainingTime.coerceAtLeast(0) // Ensure non-negative value
    } else {
        0L
    }
}

@Composable
fun TimerContent(meditatedDays: List<String>, onTimerStarted: (String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current //!? Not sure if this should maybe be Application Context
        val sharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        val wakeUpTime = sharedPreferences.getLong("wake_up_time", -1L)
        var isTimerRunning by rememberSaveable { mutableStateOf(System.currentTimeMillis() < wakeUpTime) }
        val remainingPausedTime = sharedPreferences.getLong("remaining_time", -1L)
        var isTimerPaused by rememberSaveable { mutableStateOf(remainingPausedTime != -1L) }
        var currentSliderPosition by rememberSaveable { mutableFloatStateOf(15f) }


        Log.d("foo", "${isTimerRunning} ${isTimerPaused}")
        if (!isTimerRunning and !isTimerPaused) {
            TimeChoosingInterface(currentSliderPosition) { it ->
                Log.d("Slider", "${it}")
                currentSliderPosition = it
                Log.d("currentSliderPosition", "${currentSliderPosition}")
            }
        } else {
            RunningTimer(
                isTimerRunning,
                isTimerPaused,
                context
            ) {
                isTimerRunning = false
                isTimerPaused = false
                currentSliderPosition = 15f
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        StartAndRefresh(
            isTimerRunning,
            isTimerPaused,
            currentSliderPosition.toInt() * 60,
            { isTimerRunning = !isTimerRunning }, // startTimer: () -> Unit
            {
                isTimerPaused = !isTimerPaused
            }, //   pauseTimer: () -> Unit
            { isTimerPaused = !isTimerPaused}, // resumeTimer: () -> Unit
            onTimerStarted
        ) {
            // refresh slider
            currentSliderPosition = 15f
            isTimerRunning = false
            isTimerPaused = false
        }
    }

}


@Composable
fun TimeChoosingInterface(
    // modifier: Modifier = Modifier,
    currentSliderPosition: Float,
    onSliderChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${currentSliderPosition.toInt()} min",
                fontSize = 60.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Slider(
            value = currentSliderPosition,
            onValueChange = onSliderChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            steps = 59,
            valueRange = 0f..60f
        )
    }
}

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Play the sound when the alarm goes off
        val mediaPlayer = MediaPlayer.create(context, R.raw.bell)
        mediaPlayer.start()

        Log.d("TimerReceiver", "onReceive")
    }
}

@Composable
fun RunningTimer(
    isTimerRunning: Boolean,
    isTimerPaused: Boolean,
    context: Context,
    onTimeUp: () -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(getRemainingTime(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remainingTime = getRemainingTime(context);
            if (remainingTime <= 0 && isTimerRunning && !isTimerPaused) {
                onTimeUp()
                break
            }
        }
    }


    val remainingSeconds: Double = remainingTime / 1000.0
    val minutes: Int = floor(remainingSeconds / 60).toInt()
    val seconds: Int = floor(remainingSeconds % 60).toInt()
    Log.d("bar", "${minutes} ${seconds} ${remainingTime} ${remainingSeconds}")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Colon in the center
        Text(
            text = ":",
            fontSize = 80.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.Center)
        )

        // Minutes on the left side with padding to create space
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(end = 16.dp) // Add padding to create space
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = minutes.toString().padStart(2, '0'),
                fontSize = 80.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Seconds on the right side with padding to create space
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(start = 16.dp) // Add padding to create space
                .align(Alignment.CenterEnd)
        ) {
            Text(
                text = seconds.toString().padStart(2, '0'),
                fontSize = 80.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    // Sound cleanup
    /* DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    } */
}

@Composable
fun StartAndRefresh(
    isTimerStarted: Boolean,
    isTimerPaused: Boolean,
    timeSet: Int,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onTimerStarted: (String) -> Unit,
    onResetTimer: () -> Unit,
) {
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? = remember { null }
    Row(
        modifier = Modifier
    ) {
        IconButton(
            onClick = {
                /* When the Timer started, there are two options:
                1. the timer ist still running 2. the timer is paused.
                If the timer is paused, clicking on the left button should resume the countdown.
                But if the the timer is still running, the button should pause it.
                If the Timer didn't start, the button should start the timer*/
                if (isTimerStarted) {
                    if (isTimerPaused) {
                        resumeTimer(context)
                        onResumeTimer()  // Resume timer
                    } else {
                        pauseTimer(context)
                        onPauseTimer()  // Pause timer
                    }
                } else {
                    Log.d("timeSet", "${timeSet}")
                    startTimer(context, timeSet * 1000L) // Start timer
                    onStartTimer()
                    onTimerStarted(LocalDate.now().toString())
                    /* 🠗 bell sound */
                    mediaPlayer?.release() // Release any previous instance
                    mediaPlayer = MediaPlayer.create(context, R.raw.bell)
                    mediaPlayer?.start()
                }
            },
            modifier = Modifier.size(60.dp)
        ) {
            /* logic to decide the appearance of the left button icon */
            val whichIconToShow = when {
                !isTimerStarted || isTimerPaused -> painterResource(id = R.drawable.play_solid)// Icons.Filled.PlayArrow
                else -> painterResource(id = R.drawable.pause_solid)// Icons.Filled.Pause
            }
            Icon(
                painter = whichIconToShow,
                // imageVector = whichIconToShow,
                contentDescription = if (!isTimerStarted || isTimerPaused) "start" else "pause",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
            // Cleanup related to sound effect
            DisposableEffect(Unit) {
                onDispose {
                    mediaPlayer?.release()
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (isTimerStarted) {
            IconButton(
                onClick = {
                    onResetTimer()
                    cancelTimer(context)
                },
                modifier = Modifier.size(60.dp)
            ){
                Icon(
                    painter = painterResource(id = R.drawable.restart),
                    //imageVector = Icons.Rounded.Refresh,
                    contentDescription = "restart",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
