package com.eggheadengineers.nimons360.feature.analytics

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eggheadengineers.nimons360.core.share.writeShareFile
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(horizontal = AppGrid.ScreenHorizontal),
        contentPadding = PaddingValues(bottom = AppGrid.Space8),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        item {
            AnalyticsHeader(onBack)
        }
        item {
            MonthlyHighlightSection(
                monthLabel = monthLabel(state.selectedMonthKey),
                monthlyDistanceKm = state.selectedMonthTotalKm,
                dailyAverageKm = state.dailyDistanceAverageKm,
                activeDays = state.activeDays,
            )
        }
        item {
            MetricList(
                rows = listOf(
                    MetricRowModel(Icons.Outlined.Route, "Total distance", formatKm(state.totalDistanceKm)),
                    MetricRowModel(Icons.Outlined.Timeline, "Monthly distance avg", formatKm(state.monthlyDistanceAverageKm)),
                    MetricRowModel(Icons.Outlined.CalendarMonth, "Daily distance avg", formatKm(state.dailyDistanceAverageKm)),
                    MetricRowModel(Icons.Outlined.LocalFireDepartment, "Active days", state.activeDays.toString()),
                    MetricRowModel(Icons.Outlined.LocationOn, "Marked locations", state.locations.size.toString()),
                    MetricRowModel(Icons.Outlined.PhotoLibrary, "Photos", state.photoCount.toString()),
                ),
            )
        }
        item {
            DistanceGraphSection(
                monthLabel = monthLabel(state.selectedMonthKey),
                values = state.selectedMonthDailyDistances,
            )
        }
        item {
            AppDarkButton(
                text = "Export to CSV",
                onClick = {
                    val uri = writeShareFile(
                        context = context,
                        fileName = "nimons360-analytics.csv",
                        bytes = analyticsCsv(state).toByteArray(StandardCharsets.UTF_8),
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export analytics CSV"))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            SectionHeader(
                title = "Recent locations",
                subtitle = "Latest marked places from your local history.",
            )
        }
        if (state.recentLocations.isEmpty()) {
            item {
                Text(
                    text = "No marked locations yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = AppGrid.Space3),
                )
            }
        } else {
            item {
                Column {
                    state.recentLocations.forEachIndexed { index, location ->
                        RecentLocationRow(location)
                        if (index != state.recentLocations.lastIndex) {
                            HorizontalDivider(color = Border.copy(alpha = 0.44f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppGrid.Space2, bottom = AppGrid.Space1),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
    }
}

@Composable
private fun MonthlyHighlightSection(
    monthLabel: String,
    monthlyDistanceKm: Double,
    dailyAverageKm: Double,
    activeDays: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        SectionHeader(
            title = "This month",
            subtitle = monthLabel,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppGrid.Space1),
            horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space1),
            ) {
                Text(
                    text = monthLabel.uppercase(Locale.US),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Text(
                    text = formatKm(monthlyDistanceKm),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${formatKm(dailyAverageKm)}/day • $activeDays active days",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Border.copy(alpha = 0.22f)),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timeline,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricList(rows: List<MetricRowModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
        SectionHeader(
            title = "Overview",
            subtitle = "Distance and activity summary.",
        )
        Column {
            rows.forEachIndexed { index, row ->
                MetricListRow(row)
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = Border.copy(alpha = 0.44f))
                }
            }
        }
    }
}

@Composable
private fun MetricListRow(row: MetricRowModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppGrid.Space3),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Border.copy(alpha = 0.20f)),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = row.value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private data class MetricRowModel(
    val icon: ImageVector,
    val label: String,
    val value: String,
)

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun DistanceGraphSection(
    monthLabel: String,
    values: List<DailyDistance>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
        SectionHeader(
            title = "Daily distance",
            subtitle = monthLabel,
        )
        DistanceGraph(values)
        Text(
            text = "Each bin represents one day in the selected month.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun DistanceGraph(values: List<DailyDistance>) {
    val bins = values.ifEmpty { listOf(DailyDistance("", 1, "", 0.0)) }
    val active = TextPrimary
    val inactive = Border.copy(alpha = 0.30f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(top = AppGrid.Space2, bottom = AppGrid.Space1),
    ) {
        val max = bins.maxOfOrNull { it.distanceKm }?.coerceAtLeast(0.1) ?: 0.1
        val gap = 3.dp.toPx()
        val binWidth = ((size.width - gap * (bins.size - 1)) / bins.size.coerceAtLeast(1)).coerceAtLeast(4.dp.toPx())
        val baseline = size.height - 22.dp.toPx()
        val minHeight = 10.dp.toPx()
        bins.forEachIndexed { index, value ->
            val ratio = (value.distanceKm / max).toFloat().coerceIn(0f, 1f)
            val height = if (value.distanceKm > 0.0) {
                minHeight + (baseline - minHeight) * ratio
            } else {
                minHeight
            }
            val left = index * (binWidth + gap)
            val top = baseline - height
            drawRoundRect(
                color = if (value.distanceKm > 0.0) active else inactive,
                topLeft = Offset(left, top),
                size = Size(binWidth, height),
                cornerRadius = CornerRadius(binWidth / 2f, binWidth / 2f),
            )
        }
        listOf(1, 5, 10, 15, 20, 25, bins.size).distinct().forEach { day ->
            if (day in 1..bins.size) {
                val x = (day - 1) * (binWidth + gap) + binWidth / 2f
                drawLine(
                    color = Border.copy(alpha = 0.42f),
                    start = Offset(x, baseline + 7.dp.toPx()),
                    end = Offset(x, baseline + 10.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
    }
}

@Composable
private fun RecentLocationRow(location: FavoriteLocation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppGrid.Space3),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(TextPrimary),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = "${"%.5f".format(location.lat)}, ${"%.5f".format(location.lng)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(
            text = formatDate(location.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("dd MMM, HH:mm", Locale.US).format(Date(value))

private fun formatKm(value: Double): String = "${"%.2f".format(value)} km"

private fun monthLabel(value: String): String =
    runCatching {
        val parser = SimpleDateFormat("yyyy-MM", Locale.US)
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.US)
        formatter.format(parser.parse(value) ?: Date())
    }.getOrElse { value }
