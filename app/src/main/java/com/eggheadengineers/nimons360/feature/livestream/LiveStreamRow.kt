package com.eggheadengineers.nimons360.feature.livestream

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eggheadengineers.nimons360.domain.model.LiveStream
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.PrimaryLight
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary

@Composable
fun LiveStreamsSection(
    streams: List<LiveStream>,
    onStreamClick: (LiveStream) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (streams.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Videocam,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Live now",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
        Column {
            streams.forEachIndexed { index, stream ->
                LiveStreamRow(stream = stream, onClick = { onStreamClick(stream) })
                if (index != streams.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = AppGrid.Space2),
                        color = Border.copy(alpha = 0.25f),
                    )
                }
            }
        }
        HorizontalDivider(color = Border.copy(alpha = 0.3f))
    }
}

@Composable
private fun LiveStreamRow(
    stream: LiveStream,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppGrid.Space1),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = PrimaryLight,
            border = BorderStroke(1.dp, Border.copy(alpha = 0.22f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AppGrid.Space2),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stream.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stream.broadcasterName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LivePill()
    }
}

@Composable
private fun LivePill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFC63D33))
            .padding(horizontal = AppGrid.Space2, vertical = 4.dp)
            .height(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FiberManualRecord,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(8.dp),
        )
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}
