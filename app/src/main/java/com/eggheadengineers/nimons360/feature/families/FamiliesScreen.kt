package com.eggheadengineers.nimons360.feature.families

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.eggheadengineers.nimons360.R
import com.eggheadengineers.nimons360.databinding.ScreenFamiliesXmlBinding
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.applyBoundedRipple
import com.eggheadengineers.nimons360.ui.components.applyElasticPress
import com.eggheadengineers.nimons360.ui.components.renderFamilyRows
import com.eggheadengineers.nimons360.ui.components.setSkeletonVisible
import com.eggheadengineers.nimons360.ui.components.setTopInsetPadding
import com.eggheadengineers.nimons360.ui.components.showErrorAlert
import com.eggheadengineers.nimons360.ui.components.updateTextIfNeeded

@Composable
fun FamiliesScreen(
    viewModel: FamiliesViewModel,
    onFamilyClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onCreateFamilyClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarTopInset = WindowInsets.statusBars.getTop(density)
    val binding = remember(context) {
        ScreenFamiliesXmlBinding.inflate(LayoutInflater.from(context))
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showErrorAlert(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                binding.familiesRefresh.setOnRefreshListener { viewModel.load() }
                binding.familiesProfileButton.applyBoundedRipple(cornerRadiusDp = 16f)
                binding.familiesProfileButton.applyElasticPress()
                binding.familiesProfileButton.setOnClickListener { onProfileClick() }
                binding.familiesCreateButton.applyBoundedRipple(cornerRadiusDp = 12f, darkSurface = true)
                binding.familiesCreateButton.applyElasticPress()
                binding.familiesCreateButton.setOnClickListener { onCreateFamilyClick() }
                binding.familiesAllButton.applyBoundedRipple(cornerRadiusDp = 10f, darkSurface = true)
                binding.familiesAllButton.applyElasticPress()
                binding.familiesAllButton.setOnClickListener { viewModel.setFilter(FamiliesFilter.ALL) }
                binding.familiesMineButton.applyBoundedRipple(cornerRadiusDp = 10f)
                binding.familiesMineButton.applyElasticPress()
                binding.familiesMineButton.setOnClickListener { viewModel.setFilter(FamiliesFilter.MY_FAMILIES) }
                binding.familiesSearchInput.doAfterTextChanged {
                    viewModel.setSearch(it?.toString().orEmpty())
                }
                binding.root
            },
            update = {
                binding.familiesHeroContent.setTopInsetPadding(statusBarTopInset)

                val showSkeleton = state.isLoading && state.families.isEmpty()
                binding.familiesRefresh.isRefreshing = state.isLoading && !showSkeleton
                binding.familiesSearchInput.updateTextIfNeeded(state.searchQuery)

                // Skeleton placeholders
                binding.familiesSkeleton.setSkeletonVisible(showSkeleton)

                val selectedText = ContextCompat.getColor(binding.root.context, android.R.color.white)
                val defaultText = ContextCompat.getColor(binding.root.context, R.color.xml_text_primary)
                val allSelected = state.filter == FamiliesFilter.ALL
                val mineSelected = state.filter == FamiliesFilter.MY_FAMILIES

                binding.familiesAllButton.setBackgroundResource(
                    if (allSelected) R.drawable.bg_xml_filter_selected else R.drawable.bg_xml_filter_unselected
                )
                binding.familiesMineButton.setBackgroundResource(
                    if (mineSelected) R.drawable.bg_xml_filter_selected else R.drawable.bg_xml_filter_unselected
                )
                binding.familiesAllButton.setTextColor(if (allSelected) selectedText else defaultText)
                binding.familiesMineButton.setTextColor(if (mineSelected) selectedText else defaultText)

                binding.familiesResultCount.text = when {
                    showSkeleton -> "Loading..."
                    else -> "${state.displayedFamilies.size} results"
                }

                binding.familiesEmptyText.isVisible =
                    !showSkeleton && state.displayedFamilies.isEmpty()
                binding.familiesEmptyText.text = if (state.error != null) {
                    "Something went wrong. Pull to refresh or try again later."
                } else {
                    "No families found."
                }
                binding.familiesListContainer.isVisible =
                    !showSkeleton && state.displayedFamilies.isNotEmpty()

                renderFamilyRows(
                    inflater = LayoutInflater.from(binding.root.context),
                    container = binding.familiesListContainer,
                    families = state.displayedFamilies,
                    pinnedIds = state.pinnedIds,
                    pinMode = true,
                    onFamilyClick = { onFamilyClick(it.id) },
                    onPinClick = { family -> viewModel.togglePin(family) },
                )
            },
        )

        AppSnackbarHost(snackbarHostState)
    }
}
