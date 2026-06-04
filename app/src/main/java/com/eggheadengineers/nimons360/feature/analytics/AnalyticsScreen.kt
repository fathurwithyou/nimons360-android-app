package com.eggheadengineers.nimons360.feature.analytics

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eggheadengineers.nimons360.core.share.writeShareFile
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.ui.components.AppCard
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.Info
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = AppGrid.Space8),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        item {
            AppTopBar(
                title = "Analytics",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            ) {
                MetricCard("Monthly avg", formatKm(state.monthlyDistanceAverageKm), Modifier.weight(1f))
                MetricCard("Total distance", formatKm(state.totalDistanceKm), Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            ) {
                MetricCard("Daily avg", formatKm(state.dailyDistanceAverageKm), Modifier.weight(1f))
                MetricCard("Active days", state.activeDays.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            ) {
                MetricCard("Locations", state.locations.size.toString(), Modifier.weight(1f))
                MetricCard("Photos", state.photoCount.toString(), Modifier.weight(1f))
            }
        }
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                    Text(
                        text = "Daily distance graph",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = state.selectedMonthKey,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    DistanceGraph(state.selectedMonthDailyDistances)
                    Text(
                        text = "Each bar represents one day in the selected month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
        item {
            AppDarkButton(
                text = "Export CSV",
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
            Text(
                text = "Recent locations",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
        if (state.recentLocations.isEmpty()) {
            item {
                AppCard(tonal = true) {
                    Text(
                        text = "No marked locations yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            items(state.recentLocations, key = { it.id }) { location ->
                RecentLocationRow(location)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space1)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }
    }
}

@Composable
private fun DistanceGraph(values: List<DailyDistance>) {
    val lineColor = Info
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .padding(vertical = AppGrid.Space2)
            .background(Color.Transparent)
            .padding(vertical = AppGrid.Space4),
    ) {
        val max = values.maxOfOrNull { it.distanceKm }?.coerceAtLeast(0.1) ?: 0.1
        val gap = size.width / (values.size.coerceAtLeast(1) * 2f + 1f)
        values.ifEmpty {
            listOf(DailyDistance("", 1, "", 0.0))
        }.forEachIndexed { index, value ->
            val x = gap * (index * 2 + 1)
            val height = (size.height * (value.distanceKm / max)).toFloat().coerceAtLeast(8f)
            drawLine(
                color = lineColor,
                start = Offset(x, size.height),
                end = Offset(x, size.height - height),
                strokeWidth = gap.coerceAtMost(28f),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RecentLocationRow(location: FavoriteLocation) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = "Lat ${"%.5f".format(location.lat)}   Lon ${"%.5f".format(location.lng)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            HorizontalDivider(color = Border.copy(alpha = 0.28f))
            Text(
                text = "Updated ${formatDate(location.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(value))

private fun formatKm(value: Double): String = "${"%.2f".format(value)} km"
