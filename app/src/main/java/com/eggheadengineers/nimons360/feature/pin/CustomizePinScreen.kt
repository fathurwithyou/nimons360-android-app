package com.eggheadengineers.nimons360.feature.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.core.pin.CustomPinDownloadService
import com.eggheadengineers.nimons360.ui.components.AppCard
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.PrimaryDark
import com.eggheadengineers.nimons360.ui.theme.PrimaryLight
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private data class PinSkin(
    val id: String,
    val displayName: String,
    val url: String?,
)

private val PIN_SKINS = listOf(
    PinSkin("avatar", "Avatar", null),
    PinSkin("redpin", "Pin Point", "https://mad.labpro.hmif.dev/assets/pin/redpin.png"),
    PinSkin("lizard", "Lizard", "https://mad.labpro.hmif.dev/assets/pin/lizard.png"),
    PinSkin("star", "Star", "https://mad.labpro.hmif.dev/assets/pin/star.png"),
    PinSkin("moon", "Moon", "https://mad.labpro.hmif.dev/assets/pin/moon.png"),
    PinSkin("smile", "Smile", "https://mad.labpro.hmif.dev/assets/pin/smile.png"),
)

@Composable
fun CustomizePinScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as? NimonsApplication
    val coroutineScope = rememberCoroutineScope()

    var userInitial by remember { mutableStateOf("U") }
    var selectedPinId by remember { mutableStateOf(app?.userPreferenceStore?.getSelectedPinId() ?: "avatar") }
    var downloadingIds by remember { mutableStateOf(setOf<String>()) }
    var downloadedIds by remember {
        mutableStateOf(
            PIN_SKINS.filter { skin ->
                skin.url == null || File(File(context.filesDir, "custom_pins"), "${skin.id}.png").exists()
            }.map { it.id }.toSet()
        )
    }

    LaunchedEffect(Unit) {
        val name = app?.sessionManager?.getUserName()?.trim()
        userInitial = name?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    }

    fun pinFileFor(skin: PinSkin): File? {
        if (skin.url == null) return null
        val f = File(File(context.filesDir, "custom_pins"), "${skin.id}.png")
        return if (f.exists()) f else null
    }

    fun selectPin(id: String) {
        selectedPinId = id
        app?.userPreferenceStore?.setSelectedPinId(id)
    }

    fun startDownload(skin: PinSkin) {
        val url = skin.url ?: return
        CustomPinDownloadService.start(context, skin.id, url)
        downloadingIds = downloadingIds + skin.id
        coroutineScope.launch {
            repeat(60) {
                delay(500L)
                val file = File(File(context.filesDir, "custom_pins"), "${skin.id}.png")
                if (file.exists()) {
                    downloadedIds = downloadedIds + skin.id
                    downloadingIds = downloadingIds - skin.id
                    return@launch
                }
            }
            downloadingIds = downloadingIds - skin.id
        }
    }

    val selectedSkin = PIN_SKINS.firstOrNull { it.id == selectedPinId } ?: PIN_SKINS.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppGrid.ScreenHorizontal)
            .padding(bottom = AppGrid.Space8),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        AppTopBar(
            title = "Customize Pin",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
        )

        // Current pin preview
        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppGrid.Space4),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
            ) {
                Text(
                    text = "YOUR CURRENT PIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                )
                if (selectedSkin.url == null) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userInitial,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    val file = pinFileFor(selectedSkin)
                    AsyncImage(
                        model = file ?: selectedSkin.url,
                        contentDescription = selectedSkin.displayName,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(
                    text = "Appears on your map",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Skin grid
        Text(
            text = "Choose a Skin",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        val rows = PIN_SKINS.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { skin ->
                        val isSelected = skin.id == selectedPinId
                        val isDownloaded = skin.id in downloadedIds
                        val isDownloading = skin.id in downloadingIds
                        PinSkinCell(
                            skin = skin,
                            isSelected = isSelected,
                            isDownloaded = isDownloaded,
                            isDownloading = isDownloading,
                            userInitial = userInitial,
                            pinFile = pinFileFor(skin),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when {
                                    isDownloaded -> selectPin(skin.id)
                                    !isDownloading -> startDownload(skin)
                                }
                            },
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PinSkinCell(
    skin: PinSkin,
    isSelected: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    userInitial: String,
    pinFile: File?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Surface(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryLight.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                modifier = Modifier.padding(AppGrid.Space2),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        skin.url == null -> {
                            // Avatar — always show initials circle
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Primary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = userInitial,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        isDownloading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = Primary,
                            )
                        }
                        isDownloaded -> {
                            AsyncImage(
                                model = pinFile ?: skin.url,
                                contentDescription = skin.displayName,
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        else -> {
                            // Not downloaded
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Download ${skin.displayName}",
                                modifier = Modifier.size(32.dp),
                                tint = TextSecondary,
                            )
                        }
                    }
                }
                Text(
                    text = skin.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDownloaded || skin.url == null) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            // Checkmark for selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .background(Primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
