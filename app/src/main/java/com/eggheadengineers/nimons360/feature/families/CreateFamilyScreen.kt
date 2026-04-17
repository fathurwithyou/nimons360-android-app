package com.eggheadengineers.nimons360.feature.families

import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.eggheadengineers.nimons360.R
import com.eggheadengineers.nimons360.databinding.ItemFamilyIconOptionBinding
import com.eggheadengineers.nimons360.databinding.ScreenCreateFamilyXmlBinding
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.applyBorderlessRipple
import com.eggheadengineers.nimons360.ui.components.applyBoundedRipple
import com.eggheadengineers.nimons360.ui.components.applyElasticPress
import com.eggheadengineers.nimons360.ui.components.loadUrl
import com.eggheadengineers.nimons360.ui.components.setBottomInsetPadding
import com.eggheadengineers.nimons360.ui.components.setTopInsetPadding
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.components.updateTextIfNeeded

private val FAMILY_ICONS = (1..8).map { "https://mad.labpro.hmif.dev/assets/family_icon_$it.png" }

private fun updateIconGridLayout(
    binding: ScreenCreateFamilyXmlBinding,
    iconBindings: List<ItemFamilyIconOptionBinding>,
) {
    val gridWidth = binding.createFamilyIconsGrid.width
    if (gridWidth <= 0 || iconBindings.isEmpty()) return

    val spacing = (12 * binding.root.resources.displayMetrics.density).toInt()
    val tileSize = ((gridWidth - (spacing * 4)) / 4).coerceAtLeast(0)

    iconBindings.forEachIndexed { index, iconBinding ->
        val params = iconBinding.root.layoutParams as GridLayout.LayoutParams
        if (params.width != tileSize || params.height != tileSize) {
            params.rowSpec = GridLayout.spec(index / 4, 1f)
            params.columnSpec = GridLayout.spec(index % 4, 1f)
            params.width = tileSize
            params.height = tileSize
            params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2)
            iconBinding.root.layoutParams = params
        }
    }
}

@Composable
fun CreateFamilyScreen(
    viewModel: CreateFamilyViewModel,
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarTopInset = WindowInsets.statusBars.getTop(density)
    val navigationBarBottomInset = WindowInsets.navigationBars.getBottom(density)
    val binding = remember(context) {
        ScreenCreateFamilyXmlBinding.inflate(LayoutInflater.from(context))
    }
    val iconBindings = remember { mutableStateListOf<ItemFamilyIconOptionBinding>() }
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateFamilyUiState.Success -> onSuccess(state.familyId)
            is CreateFamilyUiState.Error -> {
                snackbarHostState.showErrorAlert(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                binding.createFamilyBackButton.applyBorderlessRipple()
                binding.createFamilyBackButton.setOnClickListener { onBack() }
                binding.createFamilySubmitButton.applyBoundedRipple(cornerRadiusDp = 12f, darkSurface = true)
                binding.createFamilySubmitButton.applyElasticPress()
                binding.createFamilySubmitButton.setOnClickListener {
                    viewModel.createFamily(name, selectedIcon)
                }
                binding.createFamilyNameInput.doAfterTextChanged {
                    name = it?.toString().orEmpty()
                }

                if (iconBindings.isEmpty()) {
                    val fallbackSize = binding.root.resources.getDimensionPixelSize(R.dimen.family_icon_option_size)
                    FAMILY_ICONS.forEachIndexed { index, iconUrl ->
                        val iconBinding = ItemFamilyIconOptionBinding.inflate(
                            LayoutInflater.from(binding.root.context),
                            binding.createFamilyIconsGrid,
                            false,
                        )
                        val params = GridLayout.LayoutParams(
                            GridLayout.spec(index / 4, 1f),
                            GridLayout.spec(index % 4, 1f),
                        ).apply {
                            width = fallbackSize
                            height = fallbackSize
                            setMargins(6, 6, 6, 6)
                        }
                        iconBinding.root.layoutParams = params
                        iconBinding.iconOptionRoot.applyBoundedRipple(cornerRadiusDp = 18f)
                        iconBinding.iconOptionRoot.applyElasticPress()
                        iconBinding.iconOptionRoot.setOnClickListener { selectedIcon = iconUrl }
                        iconBinding.iconOptionImage.setImageResource(R.drawable.ic_xml_image)
                        iconBinding.iconOptionImage.loadUrl(iconUrl)
                        binding.createFamilyIconsGrid.addView(iconBinding.root)
                        iconBindings += iconBinding
                    }
                    binding.createFamilyIconsGrid.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                        updateIconGridLayout(binding, iconBindings)
                    }
                }

                binding.root
            },
            update = {
                binding.createFamilyTopBar.setTopInsetPadding(statusBarTopInset)
                binding.createFamilyBottomBar.setBottomInsetPadding(navigationBarBottomInset)
                binding.createFamilyNameInput.updateTextIfNeeded(name)

                binding.createFamilyPreviewName.text = name.ifBlank { "Your family name" }
                binding.createFamilyPreviewHint.text = if (selectedIcon.isBlank()) {
                    "Choose an icon to complete the family tile."
                } else {
                    "Selected icon will appear in lists and detail views."
                }

                if (selectedIcon.isBlank()) {
                    binding.createFamilyPreviewIcon.setImageResource(R.drawable.ic_xml_image)
                    binding.createFamilyPreviewIcon.setColorFilter(
                        ContextCompat.getColor(binding.root.context, R.color.xml_text_secondary)
                    )
                } else {
                    binding.createFamilyPreviewIcon.clearColorFilter()
                    binding.createFamilyPreviewIcon.loadUrl(selectedIcon)
                }

                val isLoading = uiState is CreateFamilyUiState.Loading
                binding.createFamilySubmitButton.isEnabled = !isLoading
                binding.createFamilySubmitButton.text = if (isLoading) "Creating..." else "Create family"
                updateIconGridLayout(binding, iconBindings)

                iconBindings.forEachIndexed { index, iconBinding ->
                    val iconUrl = FAMILY_ICONS[index]
                    val selected = iconUrl == selectedIcon
                    iconBinding.iconOptionRoot.setBackgroundResource(
                        if (selected) R.drawable.bg_xml_icon_selected else R.drawable.bg_xml_icon_unselected
                    )
                    iconBinding.iconOptionCheck.isVisible = selected
                }
            },
        )

        AppSnackbarHost(snackbarHostState)
    }
}
