package com.arsham.dorom.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.components.scoreColor
import com.arsham.dorom.ui.theme.DataText
import java.time.LocalDate

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val reviews by container.reviewRepository.observeAllReviews().collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "History", onBack = onBack)

        if (reviews.isEmpty()) {
            EmptyState(icon = Icons.Filled.History, title = "No history yet", subtitle = "Finish your first planned day and it'll show up here.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(reviews) { review -> ReviewRow(review) }
        }
    }
}

@Composable
private fun ReviewRow(review: DailyReview) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(percent = review.percentage / 100f, size = 56.dp, strokeWidth = 6.dp, progressColor = scoreColor(review.score))
            Column(modifier = Modifier.weight(1f)) {
                Text(runCatching { LocalDate.parse(review.date) }.getOrNull()?.toString() ?: review.date, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${review.completedCount}/${review.plannedCount} tasks · score ${review.score}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (review.feedbackText.isNotBlank()) {
                    Text(review.feedbackText, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    review.actualWakeTime?.let { Text("Woke $it", style = DataText.small, color = MaterialTheme.colorScheme.primary) }
                    review.actualSleepTime?.let { Text("Slept $it", style = DataText.small, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}
