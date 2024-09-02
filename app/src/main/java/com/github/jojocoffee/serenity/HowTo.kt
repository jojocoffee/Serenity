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
import androidx.compose.ui.unit.dp


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
