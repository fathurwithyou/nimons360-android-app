package com.eggheadengineers.nimons360.feature.profile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import coil.load
import com.eggheadengineers.nimons360.databinding.ScreenProfileXmlBinding
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSectionHeader
import com.eggheadengineers.nimons360.ui.components.AppSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.applyBorderlessRipple
import com.eggheadengineers.nimons360.ui.components.applyBoundedRipple
import com.eggheadengineers.nimons360.ui.components.applyElasticPress
import com.eggheadengineers.nimons360.ui.components.setTopInsetPadding
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.components.showSuccessAlert
import com.eggheadengineers.nimons360.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private fun studentIdFromEmail(email: String): String =
    email.substringBefore("@").ifBlank { "-" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignedOut: () -> Unit,
    onBack: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onCustomizePinClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarTopInset = WindowInsets.statusBars.getTop(density)
    val binding = remember(context) {
        ScreenProfileXmlBinding.inflate(LayoutInflater.from(context))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun uploadPhotoFromUri(uri: Uri?) {
        if (uri == null) return
        coroutineScope.launch {
            readProfilePhotoPayload(context, uri).fold(
                onSuccess = { payload ->
                    viewModel.uploadPhoto(payload.fileName, payload.bytes, payload.mediaType)
                },
                onFailure = { error ->
                    viewModel.showError(error.message ?: "Failed to read selected image")
                },
            )
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uploadPhotoFromUri(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            uploadPhotoFromUri(pendingCameraUri)
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = createProfileCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    LaunchedEffect(state.updateMessage) {
        state.updateMessage?.let {
            snackbarHostState.showSuccessAlert(
                title = "Profile updated",
                message = it,
            )
            viewModel.clearUpdateMessage()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showErrorAlert(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                binding.profileBackButton.applyBorderlessRipple()
                binding.profileBackButton.setOnClickListener { onBack() }
                binding.profileEditButton.applyBorderlessRipple()
                binding.profileEditButton.applyElasticPress()
                binding.profileEditButton.setOnClickListener { showEditSheet = true }
                binding.profilePhotoEditButton.applyBoundedRipple(cornerRadiusDp = 15f, darkSurface = true)
                binding.profilePhotoEditButton.applyElasticPress()
                binding.profilePhotoEditButton.setOnClickListener { showPhotoSheet = true }
                binding.profileAnalyticsButton.applyBoundedRipple(cornerRadiusDp = 12f)
                binding.profileAnalyticsButton.applyElasticPress()
                binding.profileAnalyticsButton.setOnClickListener { onAnalyticsClick() }
                binding.profileCustomizePinButton.applyBoundedRipple(cornerRadiusDp = 12f)
                binding.profileCustomizePinButton.applyElasticPress()
                binding.profileCustomizePinButton.setOnClickListener { onCustomizePinClick() }
                binding.profileSignOutButton.applyBoundedRipple(cornerRadiusDp = 12f, darkSurface = true)
                binding.profileSignOutButton.applyElasticPress()
                binding.profileSignOutButton.setOnClickListener { viewModel.signOut() }
                binding.root
            },
            update = {
                binding.profileTopBar.setTopInsetPadding(statusBarTopInset)
                val profile = state.profile
                binding.profileProgress.isVisible = state.isLoading
                binding.profileEditButton.isEnabled = profile != null

                if (profile != null) {
                    binding.profileAvatar.text = profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    binding.profileName.text = profile.name
                    binding.profileStudentId.text = studentIdFromEmail(profile.email)
                    binding.profileEmail.text = profile.email
                    val imageUrl = profile.profileImageUrl?.let(::resolveProfileImageUrl)
                    binding.profileAvatarImage.isVisible = !imageUrl.isNullOrBlank()
                    binding.profileAvatar.isVisible = imageUrl.isNullOrBlank()
                    if (!imageUrl.isNullOrBlank()) {
                        binding.profileAvatarImage.load(imageUrl) {
                            crossfade(true)
                        }
                    }
                } else {
                    binding.profileAvatar.text = "?"
                    binding.profileName.text = "-"
                    binding.profileStudentId.text = "-"
                    binding.profileEmail.text = "-"
                    binding.profileAvatarImage.isVisible = false
                    binding.profileAvatar.isVisible = true
                }

                binding.profileNotificationsSwitch.setOnCheckedChangeListener(null)
                binding.profileNotificationsSwitch.isChecked = state.notificationsEnabled
                binding.profileNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setNotificationsEnabled(isChecked)
                }

                binding.profileLocationSharingSwitch.setOnCheckedChangeListener(null)
                binding.profileLocationSharingSwitch.isChecked = state.locationSharingEnabled
                binding.profileLocationSharingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setLocationSharingEnabled(isChecked)
                }
            },
        )

        AppSnackbarHost(snackbarHostState)
    }

    if (showEditSheet) {
        EditProfileBottomSheet(
            currentName = state.profile?.name ?: "",
            onDismiss = { showEditSheet = false },
            onSave = { name ->
                viewModel.updateName(name)
                showEditSheet = false
            },
        )
    }

    if (showPhotoSheet) {
        ProfilePhotoBottomSheet(
            onDismiss = { showPhotoSheet = false },
            onPickGallery = {
                showPhotoSheet = false
                galleryLauncher.launch("image/*")
            },
            onTakePhoto = {
                showPhotoSheet = false
                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.CAMERA,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasCameraPermission) {
                    val uri = createProfileCameraUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by rememberSaveable(currentName) { mutableStateOf(currentName) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppGrid.ScreenHorizontal)
                .padding(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
        ) {
            AppSectionHeader(
                title = "Edit profile",
                subtitle = "Use a name the rest of the family can recognize quickly.",
            )
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                singleLine = true,
            )
            AppDarkButton(
                text = "Save",
                onClick = { onSave(name) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePhotoBottomSheet(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppGrid.ScreenHorizontal)
                .padding(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
        ) {
            AppSectionHeader(
                title = "Profile photo",
                subtitle = "Choose a PNG or JPEG image up to 500 KB.",
            )
            AppDarkButton(
                text = "Choose from gallery",
                onClick = onPickGallery,
                modifier = Modifier.fillMaxWidth(),
            )
            AppSecondaryButton(
                text = "Take photo",
                onClick = onTakePhoto,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private data class ProfilePhotoPayload(
    val fileName: String,
    val mediaType: String,
    val bytes: ByteArray,
)

private suspend fun readProfilePhotoPayload(
    context: Context,
    uri: Uri,
): Result<ProfilePhotoPayload> = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val mediaType = resolveImageMediaType(resolver.getType(uri), uri)
        require(mediaType == "image/png" || mediaType == "image/jpeg") {
            "Profile photo must be a PNG or JPEG image."
        }

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to open selected image.")
        require(bytes.size <= 500 * 1024) {
            "Profile photo must be 500 KB or smaller."
        }

        ProfilePhotoPayload(
            fileName = queryDisplayName(context, uri) ?: defaultProfilePhotoName(mediaType),
            mediaType = mediaType,
            bytes = bytes,
        )
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }?.takeIf { it.isNotBlank() }
}

private fun resolveImageMediaType(contentType: String?, uri: Uri): String {
    if (contentType == "image/png" || contentType == "image/jpeg") return contentType
    val path = uri.toString().lowercase()
    return when {
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        else -> contentType.orEmpty()
    }
}

private fun defaultProfilePhotoName(mediaType: String): String =
    if (mediaType == "image/png") "profile.png" else "profile.jpg"

private fun createProfileCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "profile_photos").also { it.mkdirs() }
    val file = File(directory, "profile_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

private fun resolveProfileImageUrl(value: String): String =
    if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "https://mad.labpro.hmif.dev/${value.removePrefix("/")}"
    }
