package com.eggheadengineers.nimons360.feature.profile

import android.view.LayoutInflater
import android.view.View
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import com.eggheadengineers.nimons360.databinding.ScreenProfileXmlBinding
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSectionHeader
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.applyBorderlessRipple
import com.eggheadengineers.nimons360.ui.components.applyBoundedRipple
import com.eggheadengineers.nimons360.ui.components.applyElasticPress
import com.eggheadengineers.nimons360.ui.components.setTopInsetPadding
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.components.showSuccessAlert
import com.eggheadengineers.nimons360.ui.theme.Surface

private fun studentIdFromEmail(email: String): String =
    email.substringBefore("@").ifBlank { "-" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignedOut: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarTopInset = WindowInsets.statusBars.getTop(density)
    val binding = remember(context) {
        ScreenProfileXmlBinding.inflate(LayoutInflater.from(context))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditSheet by remember { mutableStateOf(false) }

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
                } else {
                    binding.profileAvatar.text = "?"
                    binding.profileName.text = "-"
                    binding.profileStudentId.text = "-"
                    binding.profileEmail.text = "-"
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
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
