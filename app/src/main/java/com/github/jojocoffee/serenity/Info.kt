package com.github.jojocoffee.serenity

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp


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