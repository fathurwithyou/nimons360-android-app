package com.eggheadengineers.nimons360.feature.livestream

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.ui.PlayerView
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.ErrorColor
import com.eggheadengineers.nimons360.ui.theme.Surface
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    viewModel: ViewerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(state.stream?.hlsUrl) {
        val hlsUrl = state.stream?.hlsUrl ?: return@LaunchedEffect
        val mediaItem = MediaItem.fromUri(hlsUrl)
        val source = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(mediaItem)
        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            AppTopBar(
                title = state.stream?.broadcasterName ?: "Live stream",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                actions = {
                    if (state.stream != null && !state.isEnded) LiveBadgePill()
                },
            )
        },
    ) { innerPadding ->
        val videoSurface = @Composable { videoModifier: Modifier ->
            Surface(
                modifier = videoModifier,
                shape = RoundedCornerShape(AppGrid.CardRadius),
                color = Color.Black,
                border = BorderStroke(1.dp, Border.copy(alpha = 0.4f)),
            ) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    state.isEnded || state.stream == null -> EndedState()
                    else -> AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } },
                    )
                }
            }
        }

        val infoContent = @Composable {
            state.stream?.let { stream ->
                Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space1)) {
                    Text(stream.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("${stream.broadcasterName} is streaming live", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            if (state.error != null) {
                Text(state.error ?: "", style = MaterialTheme.typography.bodyMedium, color = ErrorColor)
                Spacer(Modifier.size(AppGrid.Space1))
                AppDarkButton(text = "Try again", onClick = { viewModel.load() }, modifier = Modifier.fillMaxWidth())
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                    .padding(AppGrid.Space4),
                horizontalArrangement = Arrangement.spacedBy(AppGrid.Space4),
            ) {
                videoSurface(Modifier.weight(1.4f).fillMaxHeight())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
                ) { infoContent() }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = AppGrid.ScreenHorizontal, vertical = AppGrid.Space4),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
            ) {
                videoSurface(Modifier.fillMaxWidth().height(320.dp))
                infoContent()
            }
        }
    }
}

@Composable
private fun LiveBadgePill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFC63D33))
            .padding(horizontal = AppGrid.Space2, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FiberManualRecord,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp),
        )
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Composable
private fun EndedState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppGrid.Space5),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space2),
        ) {
            Text(
                text = "Stream ended",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = "The broadcaster has stopped streaming.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}
