package com.eggheadengineers.nimons360.feature.home

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.databinding.ScreenHomeXmlBinding
import com.eggheadengineers.nimons360.ui.components.AppSnackbarHost
import com.eggheadengineers.nimons360.ui.components.applyBoundedRipple
import com.eggheadengineers.nimons360.ui.components.applyElasticPress
import com.eggheadengineers.nimons360.ui.components.bindProfileImageButton
import com.eggheadengineers.nimons360.ui.components.renderFamilyRows
import com.eggheadengineers.nimons360.ui.components.setSkeletonVisible
import com.eggheadengineers.nimons360.ui.components.setTopInsetPadding
import com.eggheadengineers.nimons360.ui.components.showErrorAlert

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFamilyClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onCreateFamilyClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarTopInset = WindowInsets.statusBars.getTop(density)
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    val binding = remember(context) {
        ScreenHomeXmlBinding.inflate(LayoutInflater.from(context))
    }
    val snackbarHostState = remember { 
        SnackbarHostState() 
    }

    LaunchedEffect(state.error) {
        state.error?.let { 
            snackbarHostState.showErrorAlert(it) 
        }
    }

    LaunchedEffect(Unit) {
        val app = context.applicationContext as? NimonsApplication
        app?.sessionManager?.observeProfileImageUrl()?.collect { url ->
            profileImageUrl = url
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                binding.homeRefresh.setOnRefreshListener { viewModel.load() }
                binding.homeProfileButton.applyBoundedRipple(cornerRadiusDp = 16f)
                binding.homeProfileButton.applyElasticPress()
                binding.homeProfileButton.setOnClickListener { onProfileClick() }
                binding.homeCreateFamilyButton.applyBoundedRipple(cornerRadiusDp = 12f)
                binding.homeCreateFamilyButton.applyElasticPress()
                binding.homeCreateFamilyButton.setOnClickListener { onCreateFamilyClick() }
                binding.root
            },
            update = {
                binding.homeHeroContent.setTopInsetPadding(statusBarTopInset)
                binding.homeProfileButton.bindProfileImageButton(profileImageUrl)
                val showSkeleton = state.isLoading && state.myFamilies.isEmpty() && state.discoverFamilies.isEmpty()
                binding.homeRefresh.isRefreshing = state.isLoading && !showSkeleton
                binding.homeMyFamiliesSkeleton.setSkeletonVisible(showSkeleton)
                binding.homeDiscoverFamiliesSkeleton.setSkeletonVisible(showSkeleton)

                val hasError = state.error != null

                binding.homeMyFamiliesMeta.text = when {
                    showSkeleton -> "Loading..."
                    hasError && state.myFamilies.isEmpty() -> "Couldn't load your families."
                    state.myFamilies.isEmpty() -> "No joined families yet."
                    else -> "${state.myFamilies.size} active groups"
                }
                binding.homeDiscoverFamiliesMeta.text = when {
                    showSkeleton -> "Loading..."
                    hasError && state.discoverFamilies.isEmpty() -> "Couldn't load suggestions."
                    state.discoverFamilies.isEmpty() -> "No suggestions available."
                    else -> "${state.discoverFamilies.size} groups available"
                }

                binding.homeMyFamiliesEmpty.isVisible = !showSkeleton && state.myFamilies.isEmpty()
                binding.homeMyFamiliesEmpty.text = if (hasError) {
                    "Something went wrong. Pull to refresh or try again later."
                } else {
                    "You have not joined any families yet."
                }
                binding.homeMyFamiliesContainer.isVisible = !showSkeleton && state.myFamilies.isNotEmpty()
                binding.homeDiscoverFamiliesEmpty.isVisible = !showSkeleton && state.discoverFamilies.isEmpty()
                binding.homeDiscoverFamiliesEmpty.text = if (hasError) {
                    "Something went wrong. Pull to refresh or try again later."
                } else {
                    "No families available to discover right now."
                }
                binding.homeDiscoverFamiliesContainer.isVisible = !showSkeleton && state.discoverFamilies.isNotEmpty()

                renderFamilyRows(
                    inflater = LayoutInflater.from(binding.root.context),
                    container = binding.homeMyFamiliesContainer,
                    families = state.myFamilies,
                    onFamilyClick = { onFamilyClick(it.id) },
                )
                renderFamilyRows(
                    inflater = LayoutInflater.from(binding.root.context),
                    container = binding.homeDiscoverFamiliesContainer,
                    families = state.discoverFamilies,
                    onFamilyClick = { onFamilyClick(it.id) },
                )
            },
        )

        AppSnackbarHost(snackbarHostState)
    }    
}
