package com.eggheadengineers.nimons360.feature.livestream

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceHolder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppDestructiveButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.Surface
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.view.OpenGlView

private val RequiredPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcasterScreen(
    viewModel: BroadcasterViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasPermissions by remember {
        mutableStateOf(RequiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> hasPermissions = result.values.all { it } }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showErrorAlert(message = it)
            viewModel.clearError()
        }
    }

    val rtmpCameraHolder = remember { RtmpCameraHolder() }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Go live",
                navigationIcon = {
                    IconButton(onClick = {
                        rtmpCameraHolder.stop()
                        viewModel.requestStop(onBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                actions = {
                    if (state.status == BroadcastStatus.Live) {
                        LiveBadge()
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .padding(
                    horizontal = AppGrid.ScreenHorizontal,
                    vertical = AppGrid.Space4,
                ),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
        ) {
            CameraPreview(
                enabled = hasPermissions,
                holder = rtmpCameraHolder,
                onPermissionRequest = { permissionLauncher.launch(RequiredPermissions) },
                onBroadcasterError = viewModel::reportBroadcasterError,
            )

            AppTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Stream title") },
                placeholder = { Text("What are you showing?") },
                enabled = state.status == BroadcastStatus.Idle,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(AppGrid.Space1))

            when (state.status) {
                BroadcastStatus.Idle -> {
                    AppDarkButton(
                        text = "Start broadcasting",
                        onClick = {
                            if (!hasPermissions) {
                                permissionLauncher.launch(RequiredPermissions)
                                return@AppDarkButton
                            }
                            viewModel.requestStart { stream ->
                                val url = "${stream.rtmpUrl}/${stream.streamKey}"
                                rtmpCameraHolder.start(url) { message ->
                                    viewModel.reportBroadcasterError(message)
                                }
                            }
                        },
                        enabled = hasPermissions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!hasPermissions) {
                        Text(
                            text = "Camera and microphone access are required to broadcast.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }

                BroadcastStatus.Preparing -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Starting…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }

                BroadcastStatus.Live -> {
                    AppDestructiveButton(
                        text = "Stop broadcasting",
                        onClick = {
                            rtmpCameraHolder.stop()
                            viewModel.requestStop(onBack)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                BroadcastStatus.Stopping -> {
                    Text(
                        text = "Stopping…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            rtmpCameraHolder.release()
        }
    }
}

@Composable
private fun LiveBadge() {
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
private fun CameraPreview(
    enabled: Boolean,
    holder: RtmpCameraHolder,
    onPermissionRequest: () -> Unit,
    onBroadcasterError: (String) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(AppGrid.CardRadius),
        color = Color.Black,
        border = BorderStroke(1.dp, Border.copy(alpha = 0.4f)),
    ) {
        if (!enabled) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                    modifier = Modifier.padding(AppGrid.Space4),
                ) {
                    Text(
                        text = "Camera preview unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = "Grant camera and microphone access to start a live stream.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    AppDarkButton(
                        text = "Allow access",
                        onClick = onPermissionRequest,
                    )
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val view = OpenGlView(ctx)
                    holder.attach(view, onBroadcasterError)
                    view
                },
            )
        }
    }
}

/**
 * Bridges RTMP camera lifecycle to Compose. Lives as `remember`'d state so
 * it survives recomposition but is released on screen exit.
 */
class RtmpCameraHolder {
    private var view: OpenGlView? = null
    private var camera: RtmpCamera2? = null
    private var surfaceCallback: SurfaceHolder.Callback? = null

    fun attach(openGlView: OpenGlView, onBroadcasterError: (String) -> Unit) {
        detachCallback()
        this.view = openGlView
        val checker = object : ConnectChecker {
            override fun onConnectionStarted(url: String) {}
            override fun onConnectionSuccess() {}
            override fun onConnectionFailed(reason: String) {
                onBroadcasterError("Couldn't connect to the streaming server: $reason")
            }
            override fun onNewBitrate(bitrate: Long) {}
            override fun onDisconnect() {}
            override fun onAuthError() {
                onBroadcasterError("Streaming server rejected authentication.")
            }
            override fun onAuthSuccess() {}
        }
        val cam = RtmpCamera2(openGlView, checker)
        camera = cam

        val callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (!cam.isOnPreview) cam.startPreview()
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (cam.isStreaming) cam.stopStream()
                if (cam.isOnPreview) cam.stopPreview()
            }
        }
        openGlView.holder.addCallback(callback)
        surfaceCallback = callback
    }

    fun start(rtmpUrl: String, onError: (String) -> Unit) {
        val cam = camera ?: run {
            onError("Camera is not ready yet.")
            return
        }
        val prepared = try {
            cam.prepareAudio() && cam.prepareVideo(1280, 720, 2_500_000)
        } catch (t: Throwable) {
            onError("Couldn't prepare the camera: ${t.message ?: "unknown error"}")
            false
        }
        if (!prepared) {
            onError("This device doesn't support the required camera configuration.")
            return
        }
        cam.startStream(rtmpUrl)
    }

    fun stop() {
        camera?.let { cam ->
            if (cam.isStreaming) cam.stopStream()
        }
    }

    fun release() {
        camera?.let { cam ->
            if (cam.isStreaming) cam.stopStream()
            if (cam.isOnPreview) cam.stopPreview()
        }
        detachCallback()
        camera = null
        view = null
    }

    private fun detachCallback() {
        val cb = surfaceCallback
        val v = view
        if (cb != null && v != null) v.holder.removeCallback(cb)
        surfaceCallback = null
    }
}
