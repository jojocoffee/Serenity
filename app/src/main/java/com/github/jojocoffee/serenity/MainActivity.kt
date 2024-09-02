package com.github.jojocoffee.serenity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.jojocoffee.serenity.FileUtils.readListFromFile
import com.github.jojocoffee.serenity.FileUtils.saveListToFile
import com.github.jojocoffee.serenity.ui.theme.SerenityTheme


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

