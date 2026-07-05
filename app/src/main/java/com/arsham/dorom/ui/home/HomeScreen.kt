package com.arsham.dorom.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.TimeDirection
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.data.repository.MonthlyReport
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.LineChart
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.PressableSurface
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.scoreColor
import com.arsham.dorom.ui.mood.MoodPickerRow
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.BadgeGold
import com.arsham.dorom.ui.theme.BadgeViolet
import com.arsham.dorom.ui.theme.CardShapeLarge
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.pretty
import com.arsham.dorom.util.todayString
import com.arsham.dorom.util.yearMonthString
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val container = LocalAppContainer.current
    val today = todayString()

    val tasks by container.planRepository.observeTasks(today).collectAsStateWithLifecycle(initialValue = emptyList())
    val recentReviews by container.reviewRepository.observeRecentReviews(14).collectAsStateWithLifecycle(initialValue = emptyList())
    val markers by container.timeMarkerRepository.observeMarkers().collectAsStateWithLifecycle(initialValue = emptyList())
    val courses by container.courseRepository.observeCourses().collectAsStateWithLifecycle(initialValue = emptyList())
    val goals by container.goalsRepository.observeGoals().collectAsStateWithLifecycle(initialValue = emptyList())
    val financeReport by container.financeRepository.observeMonthlyReport(yearMonthString()).collectAsStateWithLifecycle(
        initialValue = MonthlyReport(0.0, 0.0, 0.0, emptyMap(), 50),
    )

    val completed = tasks.count { it.isDone }
    val plannedCount = tasks.size
    val progress = if (plannedCount == 0) 0f else completed.toFloat() / plannedCount
    val streak = computeStreak(recentReviews)
    val upcomingMarkers = markers.sortedBy { kotlin.math.abs(it.targetEpochMillis - System.currentTimeMillis()) }

    val snapshotPages = remember(financeReport, courses, upcomingMarkers, goals) {
        buildList {
            if (goals.isNotEmpty()) add(SnapshotPage.Goals(goals))
            add(SnapshotPage.Finance(financeReport))
            add(SnapshotPage.Track(courses))
            upcomingMarkers.forEach { add(SnapshotPage.Marker(it)) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            HeroCard(
                progress = progress,
                plannedCount = plannedCount,
                completed = completed,
                streak = streak,
                onSettings = { onNavigate(Routes.SETTINGS) },
                onClick = { onNavigate(Routes.PLAN) },
            )
        }

        item { MoodQuickLogCard(onViewReport = { onNavigate(Routes.MOOD_REPORT) }) }

        item { WeekGlanceCard(recentReviews) }

        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    val scores = recentReviews.reversed().map { it.score.toFloat() }
                    val avg = if (scores.isEmpty()) 0 else scores.average().toInt()
                    SectionHeader(title = "Score trend") {
                        if (scores.isNotEmpty()) {
                            Text("avg $avg", style = DataText.small, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (scores.size < 2) {
                        Text(
                            "Finish a couple of days and your trend shows up here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LineChart(values = scores, minValue = 0f, maxValue = 100f)
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader(title = "Snapshot")
                SnapshotCarousel(pages = snapshotPages, onNavigate = onNavigate)
            }
        }

        item { SectionHeader(title = "Quick links") }
        item {
            // Sized to fit every row exactly — no fixed height, so there's no separate inner
            // scroll fighting the page's own scroll (that's what was clipping the last tile).
            val rows = (quickLinks.size + 1) / 2
            val gridHeight = 130.dp * rows + 10.dp * (rows - 1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(gridHeight),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(quickLinks) { link ->
                    QuickLinkCard(link) { onNavigate(link.route) }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    progress: Float,
    plannedCount: Int,
    completed: Int,
    streak: Int,
    onSettings: () -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(CardShapeLarge)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CardShapeLarge),
    ) {
        HeroIllustration(modifier = Modifier.fillMaxSize().clip(CardShapeLarge))
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(greeting(), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        LocalDate.now().pretty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PressableSurface(onClick = onSettings) {
                    IconBadge(icon = Icons.Filled.Settings, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 40.dp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .doromClickable(onClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                ProgressRing(percent = progress, size = 88.dp)
                Column(modifier = Modifier.fillMaxWidth(0.62f)) {
                    Text("Today's plan", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (plannedCount == 0) "Nothing planned yet — set tonight's plan." else "$completed of $plannedCount tasks done",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (streak > 0) {
                        Text(
                            "🔥 $streak day streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = Sage,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 5 -> "Still up?"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Wind down"
    }
}

/**
 * A bold flat "sunrise over hills" scene confined to the right third of the card — solid fills,
 * no gradients. Sits behind the settings icon but stays clear of the left-aligned text/ring.
 */
@Composable
private fun HeroIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val sunCenterColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sunCx = w - 74.dp.toPx()
        val sunCy = 132.dp.toPx()
        val sunR = 40.dp.toPx()

        // sun rays
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble())
            val rInner = sunR * 1.15f
            val rOuter = sunR * 1.5f
            val start = Offset(sunCx + (rInner * kotlin.math.cos(angle)).toFloat(), sunCy + (rInner * kotlin.math.sin(angle)).toFloat())
            val end = Offset(sunCx + (rOuter * kotlin.math.cos(angle)).toFloat(), sunCy + (rOuter * kotlin.math.sin(angle)).toFloat())
            drawLine(color = primary.copy(alpha = 0.55f), start = start, end = end, strokeWidth = 3.dp.toPx())
        }
        // sun disc
        drawCircle(color = primary.copy(alpha = 0.85f), radius = sunR, center = Offset(sunCx, sunCy))
        drawCircle(color = sunCenterColor.copy(alpha = 0.5f), radius = sunR * 0.62f, center = Offset(sunCx, sunCy))

        // rolling hills, back to front, clipped to the card by drawing ovals whose caps peek above the bottom edge
        drawOval(color = secondary.copy(alpha = 0.30f), topLeft = Offset(w * 0.30f, h * 0.62f), size = Size(w * 0.55f, h * 0.75f))
        drawOval(color = secondary.copy(alpha = 0.50f), topLeft = Offset(w * 0.55f, h * 0.72f), size = Size(w * 0.60f, h * 0.75f))
        drawOval(color = primary.copy(alpha = 0.22f), topLeft = Offset(w * 0.68f, h * 0.80f), size = Size(w * 0.55f, h * 0.7f))
    }
}

@Composable
private fun WeekGlanceCard(recentReviews: List<DailyReview>) {
    val today = LocalDate.now()
    val byDate = recentReviews.associateBy { it.date }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = "This week")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 6 downTo 0) {
                    val date = today.minusDays(i.toLong())
                    val review = byDate[date.toString()]
                    val isToday = i == 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (review != null) scoreColor(review.score).copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant)
                                .then(
                                    if (isToday) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                                    else Modifier
                                ),
                        )
                        Text(
                            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodQuickLogCard(onViewReport: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var justLogged by remember { mutableStateOf<String?>(null) }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = "How are you feeling right now?") {
                Text(
                    "Full report",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.doromClickable(onViewReport),
                )
            }
            MoodPickerRow(
                modifier = Modifier.padding(top = 10.dp),
                onLog = { emoji, note ->
                    justLogged = emoji
                    scope.launch { container.moodRepository.logMood(emoji, note) }
                },
            )
            justLogged?.let {
                Text(
                    "Logged $it just now",
                    style = MaterialTheme.typography.labelMedium,
                    color = Sage,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

private data class QuickLink(val icon: ImageVector, val label: String, val subtitle: String, val route: String, val tint: Color)

private val quickLinks = listOf(
    QuickLink(Icons.Filled.Flag, "Goals", "Long-term wins", Routes.GOALS, BadgeBlue),
    QuickLink(Icons.Filled.FitnessCenter, "Gym", "Workout plan", Routes.TRACK_GYM, Terracotta),
    QuickLink(Icons.Filled.MusicNote, "Guitar", "Practice log", Routes.TRACK_GUITAR, BadgeViolet),
    QuickLink(Icons.Filled.Savings, "Finance", "This month", Routes.TRACK_FINANCE, Sage),
    QuickLink(Icons.Filled.Timelapse, "Markers", "Countdowns", Routes.TRACK_TIME_MARKERS, BadgeGold),
)

@Composable
private fun QuickLinkCard(link: QuickLink, onClick: () -> Unit) {
    val tinted = lerp(MaterialTheme.colorScheme.surface, link.tint, 0.09f)
    DoromCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        containerColor = tinted,
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            IconBadge(icon = link.icon, tint = link.tint)
            Column {
                Text(link.label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(link.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

private sealed class SnapshotPage {
    data class Goals(val goals: List<LongTermGoal>) : SnapshotPage()
    data class Finance(val report: MonthlyReport) : SnapshotPage()
    data class Track(val courses: List<Course>) : SnapshotPage()
    data class Marker(val marker: TimeMarker) : SnapshotPage()
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SnapshotCarousel(pages: List<SnapshotPage>, onNavigate: (String) -> Unit) {
    if (pages.isEmpty()) return
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pages.size })

    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 32.dp),
        pageSpacing = 10.dp,
    ) { page ->
        when (val p = pages[page]) {
            is SnapshotPage.Goals -> GoalsSnapshotCard(p.goals) { onNavigate(Routes.GOALS) }
            is SnapshotPage.Finance -> FinanceSnapshotCard(p.report) { onNavigate(Routes.TRACK_FINANCE) }
            is SnapshotPage.Track -> TrackSnapshotCard(p.courses) { onNavigate(Routes.TRACK) }
            is SnapshotPage.Marker -> MarkerSnapshotCard(p.marker) { onNavigate(Routes.TRACK_TIME_MARKERS) }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pages.size) { i ->
            val active = i == pagerState.currentPage
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (active) 7.dp else 5.dp)
                    .clip(CircleShape)
                    .then(
                        Modifier.background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        )
                    ),
            )
        }
    }
}

@Composable
private fun GoalsSnapshotCard(goals: List<LongTermGoal>, onClick: () -> Unit) {
    val active = goals.filter { it.progressPercent < 100 }.ifEmpty { goals }
    val topGoal = active.minByOrNull { it.targetDate ?: "9999-99-99" } ?: active.first()
    DoromCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ProgressRing(percent = topGoal.progressPercent / 100f, size = 64.dp, strokeWidth = 6.dp)
            Column {
                Text(
                    "${goals.size} long-term goal${if (goals.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(topGoal.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FinanceSnapshotCard(report: MonthlyReport, onClick: () -> Unit) {
    DoromCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ProgressRing(percent = report.spendingScore / 100f, size = 64.dp, strokeWidth = 6.dp)
            Column {
                Text("Finance this month", style = MaterialTheme.typography.titleMedium)
                Text(
                    "In ${report.income.toInt()} · Out ${report.expense.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrackSnapshotCard(courses: List<Course>, onClick: () -> Unit) {
    DoromCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(120.dp)) {
        if (courses.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconBadge(icon = Icons.Filled.Timelapse, tint = Terracotta)
                Text("Add a course to track your progress", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            val course = courses.first()
            val pct = if (course.totalLessons == 0) 0f else course.completedLessons.toFloat() / course.totalLessons
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ProgressRing(percent = pct, size = 64.dp, strokeWidth = 6.dp)
                Column {
                    Text(course.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        "${course.completedLessons}/${course.totalLessons} lessons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkerSnapshotCard(marker: TimeMarker, onClick: () -> Unit) {
    DoromCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(icon = Icons.Filled.Timelapse, tint = BadgeGold)
            Column {
                Text(marker.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                val now = System.currentTimeMillis()
                val days = ChronoUnit.DAYS.between(
                    Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate(),
                    Instant.ofEpochMilli(marker.targetEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate(),
                )
                val label = when {
                    marker.direction == TimeDirection.COUNTDOWN && days >= 0 -> "${days}d left"
                    else -> "${kotlin.math.abs(days)}d ago"
                }
                Text(label, style = DataText.large, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun computeStreak(reviews: List<DailyReview>): Int {
    if (reviews.isEmpty()) return 0
    val sorted = reviews.sortedByDescending { it.date }
    var streak = 0
    for (review in sorted) {
        if (review.percentage >= 50) streak++ else break
    }
    return streak
}
