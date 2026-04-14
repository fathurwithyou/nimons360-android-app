package com.eggheadengineers.nimons360.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggheadengineers.nimons360.ui.theme.Accent
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.ErrorContainer
import com.eggheadengineers.nimons360.ui.theme.ErrorColor
import com.eggheadengineers.nimons360.ui.theme.OnError
import com.eggheadengineers.nimons360.ui.theme.OnPrimary
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.Success
import com.eggheadengineers.nimons360.ui.theme.Surface
import com.eggheadengineers.nimons360.ui.theme.SurfaceContainerLow
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import com.eggheadengineers.nimons360.ui.theme.TextTertiary
import com.eggheadengineers.nimons360.ui.theme.CalSansFamily
import com.eggheadengineers.nimons360.ui.theme.ManropeFamily
import com.eggheadengineers.nimons360.ui.theme.SectionEyebrowStyle
import com.eggheadengineers.nimons360.ui.theme.UberMoveTextFamily

object AppGrid {
    val Base = 4.dp
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space10 = 40.dp

    val ScreenHorizontal = Space5
    val ScreenTop = Space4
    val ScreenBottom = Space8
    val SectionGap = Space6
    val CardPadding = Space4
    val CompactPadding = Space3
    val InlineGap = Space2
    val RelatedTextGap = Space1

    val CardRadius = 20.dp
    val FieldRadius = 14.dp
    val ButtonHeight = 52.dp
    val CompactButtonHeight = 48.dp
    val ListRowMinHeight = 56.dp
    val TopBarHeight = 56.dp
}

val AppScreenPadding = PaddingValues(
    start = AppGrid.ScreenHorizontal,
    top = AppGrid.ScreenTop,
    end = AppGrid.ScreenHorizontal,
    bottom = AppGrid.ScreenBottom,
)

enum class AppAlertTone {
    Success,
    Error,
    Info,
}

@Composable
fun AppConnectionStatusBox(
    title: String,
    message: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (connected) {
        Color(0xFF218A5A)
    } else {
        Color(0xEE161616)
    }
    val borderColor = Color.White.copy(alpha = if (connected) 0.16f else 0.09f)
    val timeColor = Color.White.copy(alpha = 0.74f)
    val messageColor = Color.White.copy(alpha = 0.88f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = AppGrid.Space4,
                vertical = AppGrid.Space4,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
            )
        }
    }
}

private data class AppSnackbarVisuals(
    override val message: String,
    val title: String,
    val tone: AppAlertTone,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

suspend fun SnackbarHostState.showSuccessAlert(
    title: String,
    message: String,
) {
    showSnackbar(
        AppSnackbarVisuals(
            title = title,
            message = message,
            tone = AppAlertTone.Success,
        )
    )
}

suspend fun SnackbarHostState.showErrorAlert(
    message: String,
    title: String = "Something went wrong",
) {
    showSnackbar(
        AppSnackbarVisuals(
            title = title,
            message = message,
            tone = AppAlertTone.Error,
            duration = SnackbarDuration.Long,
        )
    )
}

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        val visuals = data.visuals as? AppSnackbarVisuals
        val tone = visuals?.tone ?: AppAlertTone.Info
        val title = visuals?.title ?: "Notice"

        AppAlertSnackbar(
            title = title,
            message = data.visuals.message,
            tone = tone,
        )
    }
}

@Composable
private fun AppAlertSnackbar(
    title: String,
    message: String,
    tone: AppAlertTone,
) {
    val icon: ImageVector
    val iconTint: Color
    val iconBackground: Color
    val borderColor: Color

    when (tone) {
        AppAlertTone.Success -> {
            icon = Icons.Outlined.CheckCircle
            iconTint = Success
            iconBackground = Success.copy(alpha = 0.12f)
            borderColor = Success.copy(alpha = 0.2f)
        }

        AppAlertTone.Error -> {
            icon = Icons.Outlined.ErrorOutline
            iconTint = ErrorColor
            iconBackground = ErrorContainer
            borderColor = ErrorColor.copy(alpha = 0.2f)
        }

        AppAlertTone.Info -> {
            icon = Icons.Outlined.Info
            iconTint = Primary
            iconBackground = SurfaceContainerLow
            borderColor = Border.copy(alpha = 0.65f)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppGrid.ScreenHorizontal, vertical = AppGrid.Space2),
        shape = RoundedCornerShape(20.dp),
        color = Surface,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppGrid.Space4),
            horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBackground,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space1),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            subtitle?.let {
                Spacer(Modifier.height(AppGrid.Space1))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
        action?.let {
            Spacer(Modifier.width(AppGrid.Space3))
            Box(contentAlignment = Alignment.CenterEnd) { it() }
        }
    }
}

@Composable
fun AppEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextSecondary,
) {
    Text(
        text = text,
        modifier = modifier,
        style = SectionEyebrowStyle,
        color = color,
    )
}

@Composable
fun NimonsLogo(
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    nameFontSize: TextUnit = 48.sp,
    nameLineHeight: TextUnit = 36.sp,
    numberFontSize: TextUnit = 24.sp,
    numberLineHeight: TextUnit = 16.sp,
    numberOffsetY: Dp = (-20).dp,
    numberOffsetX: Dp = (-2).dp,
) {
    Column(modifier = modifier) {
        Text(
            text = "Nimons",
            style = TextStyle(
                fontFamily = CalSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = nameFontSize,
                lineHeight = nameLineHeight,
                letterSpacing = (-0.9).sp,
            ),
            color = color,
        )
        Text(
            text = "360",
            modifier = Modifier
                .align(Alignment.End)
                .offset(x = numberOffsetX, y = numberOffsetY),
            style = TextStyle(
                fontFamily = CalSansFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = numberFontSize,
                lineHeight = numberLineHeight,
                letterSpacing = (-0.7).sp,
            ),
            color = color,
        )
    }
}

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    transparent: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (transparent) Color.Transparent else Surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(AppGrid.TopBarHeight)
                .padding(horizontal = AppGrid.ScreenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigationIcon != null) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    navigationIcon()
                }
                Spacer(Modifier.width(AppGrid.Space2))
            }

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )

            if (actions != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = AppGrid.CardPadding,
    tonal: Boolean = false,
    borderColor: Color = Border.copy(alpha = 0.7f),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppGrid.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (tonal) SurfaceContainerLow else Surface,
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun AppSectionSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = AppGrid.CardRadius,
            bottomEnd = AppGrid.CardRadius,
        ),
        color = Surface,
        border = BorderStroke(1.dp, Border.copy(alpha = 0.45f)),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppGrid.Space5),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space5),
            content = content,
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") colors: TextFieldColors = defaultAppTextFieldColors(),
    supportingText: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var isFocused by remember { mutableStateOf(false) }

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            !enabled -> Border.copy(alpha = 0.2f)
            isFocused -> Primary.copy(alpha = 0.5f)
            else -> Border.copy(alpha = 0.35f)
        },
        animationSpec = tween(150),
        label = "fieldBorder",
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            !enabled -> TextTertiary
            isFocused -> TextPrimary
            else -> TextSecondary
        },
        animationSpec = tween(150),
        label = "labelColor",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space2),
    ) {
        if (label != null) {
            CompositionLocalProvider(LocalContentColor provides labelColor) {
                ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                    label()
                }
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(SurfaceContainerLow, RoundedCornerShape(AppGrid.FieldRadius))
                .border(1.dp, animatedBorderColor, RoundedCornerShape(AppGrid.FieldRadius))
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) TextPrimary else TextSecondary,
            ),
            cursorBrush = SolidColor(Primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AppGrid.Space4,
                            end = if (trailingIcon != null) AppGrid.Space1 else AppGrid.Space4,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                ) {
                    leadingIcon?.invoke()
                    prefix?.invoke()
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            CompositionLocalProvider(LocalContentColor provides TextSecondary) {
                                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                    placeholder()
                                }
                            }
                        }
                        innerTextField()
                    }
                    suffix?.invoke()
                    trailingIcon?.invoke()
                }
            },
        )

        if (supportingText != null) {
            CompositionLocalProvider(LocalContentColor provides TextSecondary) {
                ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                    supportingText()
                }
            }
        }
    }
}

@Composable
fun AppSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
            .border(1.dp, Border.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        cursorBrush = SolidColor(Primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
fun defaultAppTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = SurfaceContainerLow,
    unfocusedContainerColor = SurfaceContainerLow,
    disabledContainerColor = SurfaceContainerLow,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = Primary,
    focusedLabelColor = TextPrimary,
    unfocusedLabelColor = TextSecondary,
    disabledLabelColor = TextSecondary,
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = TextSecondary,
)

private data class SoftButtonPalette(
    val gradientTop: Color,
    val gradientBottom: Color,
    val pressedGradientTop: Color,
    val pressedGradientBottom: Color,
    val borderColor: Color,
    val innerBorderColor: Color,
    val highlightColor: Color,
    val shadowColor: Color,
    val contentColor: Color,
)

private fun darkButtonPalette(
    shadowAlpha: Float = 0.18f,
    highlightAlpha: Float = 0.15f,
    borderAlpha: Float = 1f,
): SoftButtonPalette = SoftButtonPalette(
    gradientTop = Color(0xFF222222),
    gradientBottom = Color(0xFF121212),
    pressedGradientTop = Color(0xFF1E1E1E),
    pressedGradientBottom = Color(0xFF121212),
    borderColor = Color(0xFF141414).copy(alpha = borderAlpha),
    innerBorderColor = Color.White.copy(alpha = 0.03f),
    highlightColor = Color.White.copy(alpha = highlightAlpha),
    shadowColor = Color.Black.copy(alpha = shadowAlpha),
    contentColor = OnPrimary,
)

private fun lightButtonPalette(
    shadowAlpha: Float = 0.05f,
): SoftButtonPalette = SoftButtonPalette(
    gradientTop = Color(0xFFF8F8F9),
    gradientBottom = Color(0xFFECECEE),
    pressedGradientTop = Color(0xFFF0F0F2),
    pressedGradientBottom = Color(0xFFE4E4E6),
    borderColor = Color(0xFFE1E2E4),
    innerBorderColor = Color.White.copy(alpha = 0.55f),
    highlightColor = Color.White.copy(alpha = 0.88f),
    shadowColor = Color.Black.copy(alpha = shadowAlpha),
    contentColor = TextPrimary,
)

@Composable
private fun SoftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = AppGrid.ButtonHeight,
    cornerRadius: Dp = 14.dp,
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 14.dp,
    gradientTop: Color,
    gradientBottom: Color,
    pressedGradientTop: Color,
    pressedGradientBottom: Color,
    borderColor: Color,
    innerBorderColor: Color,
    highlightColor: Color,
    shadowColor: Color,
    contentColor: Color,
    pressedScale: Float = 0.982f,
    restingElevation: Dp = 1.75.dp,
    pressedElevation: Dp = 0.5.dp,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "softScale",
    )

    val yOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled) with(density) { 1.dp.toPx() } else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "softYOffset",
    )

    val elevation by animateDpAsState(
        targetValue = when {
            !enabled -> 0.dp
            isPressed -> pressedElevation
            else -> restingElevation
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "softElevation",
    )

    val animTop by animateColorAsState(
        targetValue = if (isPressed && enabled) pressedGradientTop else gradientTop,
        animationSpec = tween(80),
        label = "softGradTop",
    )
    val animBottom by animateColorAsState(
        targetValue = if (isPressed && enabled) pressedGradientBottom else gradientBottom,
        animationSpec = tween(80),
        label = "softGradBottom",
    )

    val animHighlight by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            highlightColor.copy(alpha = highlightColor.alpha * 0.5f)
        } else {
            highlightColor
        },
        animationSpec = tween(80),
        label = "softHighlight",
    )

    val shape = RoundedCornerShape(cornerRadius)
    val cornerPx = with(density) { cornerRadius.toPx() }

    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationY = yOffset,
                alpha = if (enabled) 1f else 0.5f,
            )
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(listOf(animTop, animBottom)),
            )
            .border(1.dp, borderColor, shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .drawWithContent {
                drawContent()

                if (innerBorderColor.alpha > 0f) {
                    drawRoundRect(
                        color = innerBorderColor,
                        topLeft = Offset(1f, 1f),
                        size = Size(size.width - 2f, size.height - 2f),
                        cornerRadius = CornerRadius(
                            x = cornerPx - 1f,
                            y = cornerPx - 1f,
                        ),
                        style = Stroke(width = 1f),
                    )
                }

                if (animHighlight.alpha > 0f) {
                    drawLine(
                        color = animHighlight,
                        start = Offset(cornerPx, 1f),
                        end = Offset(size.width - cornerPx, 1f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = animHighlight.copy(alpha = animHighlight.alpha * 0.42f),
                        start = Offset(cornerPx + 2f, 3f),
                        end = Offset(size.width - cornerPx - 2f, 3f),
                        strokeWidth = 1.4f,
                        cap = StrokeCap.Round,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)) {
                content()
            }
        }
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    AppDarkButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
    )
}

@Composable
fun AppDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    val palette = darkButtonPalette()
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        cornerRadius = 12.dp,
        gradientTop = palette.gradientTop,
        gradientBottom = palette.gradientBottom,
        pressedGradientTop = palette.pressedGradientTop,
        pressedGradientBottom = palette.pressedGradientBottom,
        borderColor = palette.borderColor,
        innerBorderColor = palette.innerBorderColor,
        highlightColor = palette.highlightColor,
        shadowColor = palette.shadowColor,
        contentColor = palette.contentColor,
        pressedScale = 0.98f,
        restingElevation = 2.dp,
        pressedElevation = 0.5.dp,
    ) {
        content?.invoke() ?: Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.contentColor,
        )
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = lightButtonPalette()
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        gradientTop = palette.gradientTop,
        gradientBottom = palette.gradientBottom,
        pressedGradientTop = palette.pressedGradientTop,
        pressedGradientBottom = palette.pressedGradientBottom,
        borderColor = palette.borderColor,
        innerBorderColor = palette.innerBorderColor,
        highlightColor = palette.highlightColor,
        shadowColor = palette.shadowColor,
        contentColor = palette.contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.contentColor,
        )
    }
}

@Composable
fun AppDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        gradientTop = Color(0xFFD04040),
        gradientBottom = Color(0xFFB52E25),
        pressedGradientTop = Color(0xFFC03830),
        pressedGradientBottom = Color(0xFFA52820),
        borderColor = Color.White.copy(alpha = 0.08f),
        innerBorderColor = Color.White.copy(alpha = 0.08f),
        highlightColor = Color.White.copy(alpha = 0.12f),
        shadowColor = Color(0xFFC0362C).copy(alpha = 0.20f),
        contentColor = OnError,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = OnError,
        )
    }
}


@Composable
fun AppCompactPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppCompactDarkButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun AppCompactDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = darkButtonPalette(shadowAlpha = 0.18f, highlightAlpha = 0.15f)
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 42.dp,
        cornerRadius = 12.dp,
        horizontalPadding = 16.dp,
        verticalPadding = 0.dp,
        gradientTop = palette.gradientTop,
        gradientBottom = palette.gradientBottom,
        pressedGradientTop = palette.pressedGradientTop,
        pressedGradientBottom = palette.pressedGradientBottom,
        borderColor = palette.borderColor,
        innerBorderColor = palette.innerBorderColor,
        highlightColor = palette.highlightColor,
        shadowColor = palette.shadowColor,
        contentColor = palette.contentColor,
        pressedScale = 0.98f,
        restingElevation = 2.dp,
        pressedElevation = 0.5.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.contentColor,
        )
    }
}

@Composable
fun AppCompactSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = lightButtonPalette(shadowAlpha = 0.04f)
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 42.dp,
        cornerRadius = 12.dp,
        horizontalPadding = 16.dp,
        verticalPadding = 0.dp,
        gradientTop = palette.gradientTop,
        gradientBottom = palette.gradientBottom,
        pressedGradientTop = palette.pressedGradientTop,
        pressedGradientBottom = palette.pressedGradientBottom,
        borderColor = palette.borderColor,
        innerBorderColor = palette.innerBorderColor,
        highlightColor = palette.highlightColor,
        shadowColor = palette.shadowColor,
        contentColor = palette.contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.contentColor,
        )
    }
}


@Composable
fun AppFilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = if (selected) {
        darkButtonPalette(
            shadowAlpha = 0.14f,
            highlightAlpha = 0.12f,
            borderAlpha = 1f,
        )
    } else {
        lightButtonPalette(shadowAlpha = 0.03f)
    }
    SoftButton(
        onClick = onClick,
        modifier = modifier,
        minHeight = 32.dp,
        cornerRadius = 10.dp,
        horizontalPadding = 12.dp,
        verticalPadding = 0.dp,
        gradientTop = palette.gradientTop,
        gradientBottom = palette.gradientBottom,
        pressedGradientTop = palette.pressedGradientTop,
        pressedGradientBottom = palette.pressedGradientBottom,
        borderColor = if (selected) palette.borderColor else Border.copy(alpha = 0.7f),
        innerBorderColor = palette.innerBorderColor,
        highlightColor = palette.highlightColor,
        shadowColor = palette.shadowColor,
        contentColor = palette.contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = palette.contentColor,
        )
    }
}

/**
 * Marble/watercolor-inspired hero background using Canvas-drawn organic
 * grayscale shapes with subtle warm veins — shared across screens.
 */
@Composable
fun MarbleHeroBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(color = Color(0xFFF1ECE5))

        val vein1 = Path().apply {
            moveTo(w * 0.55f, 0f)
            cubicTo(w * 0.65f, h * 0.15f, w * 1.1f, h * 0.1f, w * 1.05f, h * 0.45f)
            cubicTo(w * 1.0f, h * 0.65f, w * 0.7f, h * 0.55f, w * 0.5f, h * 0.7f)
            cubicTo(w * 0.35f, h * 0.78f, w * 0.6f, h * 0.35f, w * 0.55f, 0f)
            close()
        }
        drawPath(
            path = vein1,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE2DDD6), Color(0xFFD7D0C6), Color(0xFFDDD7CF)),
                start = Offset(w * 0.5f, 0f),
                end = Offset(w, h * 0.6f),
            ),
            style = Fill,
        )

        val vein2 = Path().apply {
            moveTo(0f, h * 0.2f)
            cubicTo(w * 0.15f, h * 0.3f, w * 0.25f, h * 0.5f, w * 0.1f, h * 0.75f)
            cubicTo(w * 0.02f, h * 0.85f, -w * 0.05f, h * 0.6f, 0f, h * 0.2f)
            close()
        }
        drawPath(
            path = vein2,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE7E2DB), Color(0xFFDCD6CE)),
                start = Offset(0f, h * 0.2f),
                end = Offset(w * 0.2f, h * 0.8f),
            ),
            style = Fill,
        )

        val vein3 = Path().apply {
            moveTo(w * 0.7f, 0f)
            cubicTo(w * 0.8f, h * 0.08f, w * 0.9f, h * 0.2f, w * 0.85f, h * 0.35f)
            cubicTo(w * 0.82f, h * 0.42f, w * 0.75f, h * 0.25f, w * 0.7f, 0f)
            close()
        }
        drawPath(
            path = vein3,
            color = Color(0xFFD1C7B4).copy(alpha = 0.5f),
            style = Fill,
        )

        drawCircle(
            color = Color(0xFFE4DED5).copy(alpha = 0.55f),
            radius = w * 0.08f,
            center = Offset(w * 0.8f, h * 0.15f),
        )
        drawCircle(
            color = Color(0xFFE7E1D8).copy(alpha = 0.45f),
            radius = w * 0.06f,
            center = Offset(w * 0.25f, h * 0.6f),
        )
        drawCircle(
            color = Color(0xFFDED8D1).copy(alpha = 0.4f),
            radius = w * 0.1f,
            center = Offset(w * 0.6f, h * 0.75f),
        )
    }
}
