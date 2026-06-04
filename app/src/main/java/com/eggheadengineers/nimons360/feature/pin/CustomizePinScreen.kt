package com.eggheadengineers.nimons360.feature.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.core.pin.CustomPinDownloadService
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.Surface as AppSurface
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

    Scaffold(
        containerColor = AppSurface,
        topBar = {
            AppTopBar(
                title = "Customize Pin",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppSurface)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = AppGrid.ScreenHorizontal),
            contentPadding = PaddingValues(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space5),
        ) {
            item {
                CurrentPinSection(
                    skin = selectedSkin,
                    userInitial = userInitial,
                    pinFile = pinFileFor(selectedSkin),
                )
            }
            item {
                SectionHeader(
                    title = "Available pins",
                    subtitle = "Choose a downloaded pin or download a new one.",
                )
            }
            item {
                Column {
                    PIN_SKINS.forEachIndexed { index, skin ->
                        val isSelected = skin.id == selectedPinId
                        val isDownloaded = skin.id in downloadedIds
                        val isDownloading = skin.id in downloadingIds
                        PinSkinRow(
                            skin = skin,
                            isSelected = isSelected,
                            isDownloaded = isDownloaded,
                            isDownloading = isDownloading,
                            userInitial = userInitial,
                            pinFile = pinFileFor(skin),
                            onClick = {
                                when {
                                    isDownloaded -> selectPin(skin.id)
                                    !isDownloading -> startDownload(skin)
                                }
                            },
                        )
                        if (index != PIN_SKINS.lastIndex) {
                            HorizontalDivider(
                                color = Border.copy(alpha = 0.44f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentPinSection(
    skin: PinSkin,
    userInitial: String,
    pinFile: File?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space4)) {
        SectionHeader(
            title = "Current pin",
            subtitle = "This marker appears on your live map.",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppGrid.Space1),
            horizontalArrangement = Arrangement.spacedBy(AppGrid.Space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PinPreview(
                skin = skin,
                userInitial = userInitial,
                pinFile = pinFile,
                size = 72,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space1),
            ) {
                Text(
                    text = "Current map pin",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text = skin.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

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
private fun PinSkinRow(
    skin: PinSkin,
    isSelected: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    userInitial: String,
    pinFile: File?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppGrid.Space3),
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PinPreview(
            skin = skin,
            userInitial = userInitial,
            pinFile = if (isDownloaded) pinFile else null,
            size = 48,
            showPlaceholder = !isDownloaded && skin.url != null,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = skin.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = when {
                    isSelected -> "Selected"
                    isDownloaded -> "Ready to use"
                    isDownloading -> "Downloading"
                    else -> "Download required"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        when {
            isDownloading -> CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = Primary,
            )
            isSelected -> Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            !isDownloaded -> IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "Download ${skin.displayName}",
                    tint = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PinPreview(
    skin: PinSkin,
    userInitial: String,
    pinFile: File?,
    size: Int,
    showPlaceholder: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Border.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            skin.url == null -> {
                Surface(
                    modifier = Modifier.size((size * 0.72f).dp),
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
            showPlaceholder -> Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = null,
                modifier = Modifier.size((size * 0.42f).dp),
                tint = TextSecondary,
            )
            else -> AsyncImage(
                model = pinFile ?: skin.url,
                contentDescription = skin.displayName,
                modifier = Modifier.size((size * 0.72f).dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
