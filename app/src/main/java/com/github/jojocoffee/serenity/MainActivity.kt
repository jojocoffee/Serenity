package com.github.jojocoffee.serenity
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jojocoffee.serenity.FileUtils.readListFromFile
import com.github.jojocoffee.serenity.FileUtils.saveListToFile
import com.github.jojocoffee.serenity.ui.theme.SerenityTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SerenityTheme(
                dynamicColor = false
            ) {
                var selectedMenuEntry by remember { mutableStateOf("Timer") }
                var meditatedDays = remember { readListFromFile(this).toMutableStateList() }


                DisposableEffect(Unit) {
                    onDispose {
                        saveListToFile(this@MainActivity, meditatedDays)
                        Log.d("DisposableEffect", "I saved $meditatedDays to file")

                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SerenityTopAppBar(
                            onMenuItemClick = { item ->
                                selectedMenuEntry = item
                            }
                        )
                    },
                    bottomBar = {}
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        when (selectedMenuEntry) {
                            "Timer" -> TimerContent(meditatedDays) { newTimeStamp ->
                                if (!meditatedDays.contains(newTimeStamp)) {
                                    meditatedDays.add(newTimeStamp)
                                    saveListToFile(this@MainActivity, meditatedDays)
                                }
                            }
                            "HowTo" -> HowTo()
                            "Calendar" -> CalendarView(meditatedDays) { date ->
                                val uniqueDates = meditatedDays.toSet()
                                meditatedDays.clear()
                                meditatedDays.addAll(uniqueDates)
                                meditatedDays.remove(date)
                                saveListToFile(this@MainActivity, meditatedDays)
                            }

                            "About" -> Info()
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerenityTopAppBar (
    onMenuItemClick: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.lotus_icon),
                // imageVector = Icons.Filled.Spa,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(5.dp)
            )
        },
        title = {
            Text("Serenity")
        },
        actions = {
            IconButton(
                onClick = {isMenuExpanded = true}
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    // imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
            // Dropdown Menu (Menu Button right upper corner)
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier.width(200.dp)
            ) {
                DropdownMenuItem(
                    leadingIcon = {Icon(painter = painterResource(R.drawable.hourglass), contentDescription = "Stopwatch")},
                    text = { Text("Timer") },
                    onClick = {
                        onMenuItemClick("Timer")
                        isMenuExpanded = false
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {Icon(painter = painterResource(R.drawable.yoga), contentDescription = "Meditating person")},
                    text = { Text("How to meditate") },
                    onClick = {
                        onMenuItemClick("HowTo")
                        isMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {Icon(painter = painterResource(R.drawable.calendar), contentDescription = "Calendar")},
                    text = { Text("Calendar") },
                    onClick = {
                        onMenuItemClick("Calendar")
                        isMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {Icon(painter =  painterResource(R.drawable.info_circle), contentDescription = "Info Point")},
                    text = { Text("About") },
                    onClick = {
                        onMenuItemClick("About")
                        isMenuExpanded = false
                    }
                )
            }
        }
    )
}



@Composable
fun TimerContent(meditatedDays: List<String>, onTimerStarted: (String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isTimerStarted by rememberSaveable { mutableStateOf(false) }
        var isTimerPaused by rememberSaveable { mutableStateOf(false) }
        var currentSliderPosition by rememberSaveable { mutableFloatStateOf(15f) }

        if (!isTimerStarted and !isTimerPaused) {
            TimeChoosingInterface(currentSliderPosition) { currentSliderPosition = it }
        } else {
            RunningTimer(
                currentSliderPosition,
                isTimerStarted,
                isTimerPaused
            ) {
                isTimerStarted = false
                isTimerPaused = false
                currentSliderPosition = 15f

            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        StartAndRefresh(
            isTimerStarted,
            isTimerPaused,
            { isTimerStarted = !isTimerStarted }, // startTimer: () -> Unit
            { isTimerPaused = !isTimerPaused }, //   pauseTimer: () -> Unit
            { isTimerPaused = !isTimerPaused}, // resumeTimer: () -> Unit
            meditatedDays,
            onTimerStarted
        ) {
            // refresh slider
            currentSliderPosition = 15f
            isTimerStarted = false
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

@Composable
fun RunningTimer(
    currentSliderPosition: Float,
    isTimerStarted: Boolean,
    isTimerPaused: Boolean,
    onTimeUp: () -> Unit
) {
    var timeLeft by rememberSaveable { mutableIntStateOf(currentSliderPosition.toInt() * 60) }
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? = remember { null }

    LaunchedEffect(timeLeft, isTimerStarted, isTimerPaused) {
        while (timeLeft > 0 && !isTimerPaused) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) {
            mediaPlayer?.release() // Release any previous instance
            mediaPlayer = MediaPlayer.create(context, R.raw.bell)
            mediaPlayer?.start()
            onTimeUp()
        }
    }

    val minutes: Int = floor(timeLeft.toDouble() / 60).toInt()
    val seconds: Int = floor(((timeLeft.toDouble()) + 0.5) % 60).toInt()

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
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}


@Composable
fun StartAndRefresh(
    isTimerStarted: Boolean,
    isTimerPaused: Boolean,
    startTimer: () -> Unit,
    pauseTimer: () -> Unit,
    resumeTimer: () -> Unit,
    meditatedDays: List<String>,
    onTimerStarted: (String) -> Unit,
    refreshSlider: () -> Unit,
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
                        resumeTimer()  // Resume timer
                    } else {
                        pauseTimer()  // Pause timer
                    }
                } else {
                    startTimer() // Start timer
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
                onClick = refreshSlider,
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

@Composable
fun HowTo() {
    val howToText = listOf(
        "WHAT IS MEDITATION?",
        "Meditation is an ancient practice, thousands of years old, that helps calm and train your mind. It is a key element in many philosophies, religions, and spiritual traditions, but you don't need to subscribe to any of them to begin meditating and reaping its benefits. There are many ways to meditate, but the most basic form is simply sitting down and doing nothing—not even thinking—for a while, while staying attentive and awake. As you try this, you'll quickly realize that it's easier said than done. Thoughts will arise constantly. The practice here is to notice these thoughts without 'following' them. For example, if you suddenly remember that you forgot to do your laundry, simply notice that you forgot. Don’t start planning how you'll do it later, and don’t berate yourself for forgetting. Just notice the thought and let it go.",
        "PRACTICAL CONSIDERATIONS",
        "Most people prefer to find a quiet space for their practice. You can sit on a chair, your couch, a cushion, or even on the carpet. Many people choose to sit on a cushion, not only because of tradition but also because it helps them achieve a stable position that isn’t so comfortable that they might accidentally fall asleep. The important thing is that you can sit comfortably for a while in your chosen position, without needing to move too much or experiencing pain in your back or legs. Experiment to see what works best for you.\n" +
                "\n" +
                "While meditating, you can either close your eyes or focus on a point in the room. Many practitioners find it helpful to pay attention to their breath, without trying to alter it. A meditation timer can be useful, providing a clear starting and ending point for your practice. This way, you won’t feel the need to check the clock, as the timer will alert you when your session is over.",
        "TIPS FOR WHEN YOU STRUGGLE",
        "Meditation is not an easy practice. Thoughts and emotions will constantly arise, and it takes time and practice to manage them. Don't worry too much about it—meditation is not a competition. You'll benefit from the practice, even if your thoughts only slow down a little. If your mind drifts away from your practice, many people find it helpful to refocus on their breath; you could even count your breaths. Another method is to do a 'body scan': starting with your head, pay attention to each part of your body in turn. How does your neck feel today? What are your fingers touching? Don’t judge—just notice. Happy meditating!"
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(howToText) { text ->
            Text(
                text = text
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    meditatedDays: List<String>,
    performDeleteAction: (String) -> Unit
) {
    Log.d("CalendarViwe", "$meditatedDays")
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
                MonthNameTitle(it);
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



    @Composable
fun Info() {
        val handler = LocalUriHandler.current
        val infoText = listOf(
            "ABOUT SERENITY",
            "Serenity is a free and open-source app designed to support your meditation practice. It includes a timer, general guidance, and a calendar view to track your sessions. There are no ads or data collection. Your meditation sessions are stored solely on your device. Serenity comes with absolutely no warranty. The general guidance section is not medical advice. If you are unsure, please consult your doctor or therapist to determine if meditating is right for you. If you would like to provide feedback or contribute, you can visit this project on <a href=\"https://github.com\"> <u>Github</u></a>. If you enjoy the app, consider supporting me via <a href=\"https://ko-fi.com/jojo_codes\"><u>Ko-fi</u></a>.",
            "CREDITS",
            "• Menu and timer Icons by <a href=\"https://iconoir.com\"><u>Iconoir</u></a>",
            "• Calendar composable by <a href=\"https://github.com/kizitonwose/Calendar\"><u>Kizito Nwose</u></a>",
            "• Bell sound by <a href=\"http://www.freesound.org/people/isteak/\"><u>isteak</u></a>"
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(infoText) { text ->
                val t = AnnotatedString.fromHtml(
                    text,
                    linkInteractionListener = { annotation ->
                        val url = (annotation as? LinkAnnotation.Url) ?: return@fromHtml
                        handler.openUri(url.url)
                    }
                )
                Text(
                    text = t
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
}