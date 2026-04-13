package com.eggheadengineers.nimons360.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.feature.auth.LoginScreen
import com.eggheadengineers.nimons360.feature.auth.LoginViewModel
import com.eggheadengineers.nimons360.feature.families.*
import com.eggheadengineers.nimons360.feature.home.HomeScreen
import com.eggheadengineers.nimons360.feature.home.HomeViewModel
import com.eggheadengineers.nimons360.feature.map.MapScreen
import com.eggheadengineers.nimons360.feature.map.MapViewModel
import com.eggheadengineers.nimons360.feature.profile.ProfileScreen
import com.eggheadengineers.nimons360.feature.profile.ProfileViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Map : Screen("map")
    data object Families : Screen("families")
    data object CreateFamily : Screen("create_family")
    data object Profile : Screen("profile")
    data object FamilyDetail : Screen("family_detail/{familyId}") {
        fun createRoute(familyId: String) = "family_detail/$familyId"
    }
}

@Composable
fun NimonsNavGraph(
    navController: NavHostController,
    startDestination: String,
    app: NimonsApplication,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Screen.Login.route) {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(app.authRepository))
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(app.familyRepository))
            HomeScreen(
                viewModel = vm,
                onFamilyClick = { navController.navigate(Screen.FamilyDetail.createRoute(it)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onCreateFamilyClick = { navController.navigate(Screen.CreateFamily.route) },
            )
        }

        composable(Screen.Map.route) {
            val vm: MapViewModel = viewModel(
                factory = MapViewModel.Factory(
                    app.presenceRepository, app.familyRepository,
                    app.favoriteLocationRepository, app.locationTracker,
                    app.orientationProvider, app.batteryProvider, app.connectivityObserver,
                )
            )
            MapScreen(viewModel = vm)
        }

        composable(Screen.Families.route) {
            val vm: FamiliesViewModel = viewModel(factory = FamiliesViewModel.Factory(app.familyRepository))
            FamiliesScreen(
                viewModel = vm,
                onFamilyClick = { navController.navigate(Screen.FamilyDetail.createRoute(it)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onCreateFamilyClick = { navController.navigate(Screen.CreateFamily.route) },
            )
        }

        composable(Screen.CreateFamily.route) {
            val vm: CreateFamilyViewModel = viewModel(factory = CreateFamilyViewModel.Factory(app.familyRepository))
            CreateFamilyScreen(
                viewModel = vm,
                onSuccess = { familyId ->
                    navController.navigate(Screen.FamilyDetail.createRoute(familyId)) {
                        popUpTo(Screen.CreateFamily.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(app.profileRepository, app.authRepository)
            )
            ProfileScreen(
                viewModel = vm,
                onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.FamilyDetail.route,
            arguments = listOf(navArgument("familyId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: return@composable
            val vm: FamilyDetailViewModel = viewModel(
                factory = FamilyDetailViewModel.Factory(familyId, app.familyRepository)
            )
            FamilyDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }    
}