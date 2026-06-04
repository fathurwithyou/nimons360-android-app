package com.eggheadengineers.nimons360.feature.auth

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.focus.FocusManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.eggheadengineers.nimons360.R
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.NimonsLogo
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.defaultAppTextFieldColors
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.CalSansFamily
import com.eggheadengineers.nimons360.ui.theme.OnPrimary
import com.eggheadengineers.nimons360.ui.theme.Surface as SurfaceColor
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun sanitizeEmailInput(value: String): String =
    value
        .filterNot(Char::isWhitespace)
        .lowercase()

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val submitLogin = {
        focusManager.clearFocus()
        viewModel.login(email, password)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> onLoginSuccess()
            is LoginUiState.Error -> {
                snackbarHostState.showErrorAlert(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LoginVideoBackground(modifier = Modifier.fillMaxSize())

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.32f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.40f),
                                Color.Black.copy(alpha = 0.74f),
                            ),
                        )
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                            center = Offset.Zero,
                            radius = 900f,
                        )
                    ),
            )

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(AppGrid.ScreenHorizontal),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            NimonsLogo(
                                color = Color.White,
                                nameFontSize = 20.sp,
                                nameLineHeight = 31.sp,
                                numberFontSize = 10.sp,
                                numberLineHeight = 13.sp,
                                numberOffsetY = (-8).dp,
                            )
                            Text(
                                text = "See where life is moving.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CalSansFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Italic,
                                ),
                                color = Color.White,
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
                        color = SurfaceColor.copy(alpha = 0.985f),
                        border = BorderStroke(1.dp, Border.copy(alpha = 0.42f)),
                        shadowElevation = 10.dp,
                    ) {
                        LoginFormContent(
                            email = email,
                            password = password,
                            passwordVisible = passwordVisible,
                            uiState = uiState,
                            focusManager = focusManager,
                            onEmailChange = { email = sanitizeEmailInput(it) },
                            onPasswordChange = { password = it },
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            onSubmit = submitLogin,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(end = AppGrid.ScreenHorizontal),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    NimonsLogo(
                        color = Color.White,
                        nameFontSize = 20.sp,
                        nameLineHeight = 31.sp,
                        numberFontSize = 10.sp,
                        numberLineHeight = 13.sp,
                        numberOffsetY = (-8).dp,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            top = 108.dp,
                            start = AppGrid.ScreenHorizontal,
                            end = AppGrid.ScreenHorizontal,
                        ),
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 292.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "See where life is moving.",
                            modifier = Modifier.widthIn(max = 292.dp),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CalSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "Stay connected",
                            modifier = Modifier
                                .padding(start = 28.dp)
                                .widthIn(max = 220.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = CalSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = Color.White.copy(alpha = 0.94f),
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "without losing calm.",
                            modifier = Modifier
                                .padding(start = 56.dp)
                                .widthIn(max = 168.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Start,
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = SurfaceColor.copy(alpha = 0.985f),
                    border = BorderStroke(1.dp, Border.copy(alpha = 0.42f)),
                    shadowElevation = 10.dp,
                ) {
                    LoginFormContent(
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        uiState = uiState,
                        focusManager = focusManager,
                        onEmailChange = { email = sanitizeEmailInput(it) },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onSubmit = submitLogin,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginFormContent(
    email: String,
    password: String,
    passwordVisible: Boolean,
    uiState: LoginUiState,
    focusManager: FocusManager,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = AppGrid.ScreenHorizontal,
                vertical = AppGrid.Space6,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space1),
        ) {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Sign in with your STEI email and password.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            colors = defaultAppTextFieldColors(),
        )

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextPrimary,
                    )
                }
            },
            colors = defaultAppTextFieldColors(),
        )

        AppDarkButton(
            text = "Sign in",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading,
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(AppGrid.Space5),
                    color = OnPrimary,
                    strokeWidth = AppGrid.Base / 2,
                )
            } else {
                Text("Sign in", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LoginVideoBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var hasRenderedFirstFrame by remember { mutableStateOf(false) }
    val placeholderAlpha = remember { Animatable(1f) }
    val videoBytes by produceState<ByteArray?>(initialValue = null, context.applicationContext) {
        value = withContext(Dispatchers.IO) {
            context.applicationContext.resources
                .openRawResource(R.raw.login_page_video)
                .use { input -> input.readBytes() }
        }
    }
    val player = remember(context.applicationContext, videoBytes) {
        videoBytes?.let { bytes ->
            val dataSourceFactory = DataSource.Factory {
                ByteArrayDataSource(bytes)
            }
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.EMPTY))

            ExoPlayer.Builder(context.applicationContext).build().apply {
                setMediaSource(mediaSource)
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                prepare()
            }
        }
    }

    DisposableEffect(player) {
        if (player == null) {
            onDispose { }
        } else {
            val listener = object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    if (!hasRenderedFirstFrame) {
                        hasRenderedFirstFrame = true
                        scope.launch {
                            placeholderAlpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 420),
                            )
                        }
                    }
                }
            }
            player.addListener(listener)
            onDispose {
                player.removeListener(listener)
            }
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        if (player == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> player.play()
                    Lifecycle.Event.ON_STOP -> player.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                playerViewRef?.player = null
                player.playWhenReady = false
                player.stop()
                player.clearMediaItems()
                player.release()
                playerViewRef = null
            }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        if (player != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        keepScreenOn = false
                        this.player = player
                        playerViewRef = this
                    }
                },
                update = { view ->
                    view.player = player
                    playerViewRef = view
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = placeholderAlpha.value }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF66625C),
                            Color(0xFF474A4E),
                            Color(0xFF1A1A1A),
                        ),
                    ),
                ),
        )
    }
}
