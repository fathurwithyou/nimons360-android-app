package com.eggheadengineers.nimons360.feature.pin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.eggheadengineers.nimons360.core.pin.CustomPinDownloadService
import com.eggheadengineers.nimons360.ui.components.AppCard
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppTopBar
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import java.io.File

@Composable
fun CustomizePinScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val pinFile = File(File(context.filesDir, "custom_pins"), CustomPinDownloadService.PIN_FILE_NAME)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppGrid.ScreenHorizontal)
            .padding(bottom = AppGrid.Space4),
        verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
    ) {
        AppTopBar(
            title = "Customize pin",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
        )
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                Text(
                    text = "Download Nimons pin",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "The download runs in a foreground service and reports progress in the notification shade.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
        AppDarkButton(
            text = "Download pin",
            onClick = { CustomPinDownloadService.start(context) },
            modifier = Modifier.fillMaxWidth(),
        )
        AppSecondaryButton(
            text = "Share downloaded pin",
            onClick = {
                if (!pinFile.exists()) return@AppSecondaryButton
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pinFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share custom pin"))
            },
            enabled = pinFile.exists(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
