package com.eggheadengineers.nimons360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.eggheadengineers.nimons360.core.network.NetworkStatus
import com.eggheadengineers.nimons360.core.network.UnauthorizedEvent
import com.eggheadengineers.nimons360.navigation.NimonsNavGraph
import com.eggheadengineers.nimons360.navigation.Screen
import com.eggheadengineers.nimons360.ui.components.AppConnectionStatusBox
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.StartupSplash
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.Surface
import com.eggheadengineers.nimons360.ui.theme.TextTertiary
import com.eggheadengineers.nimons360.ui.theme.Nimons360Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val screen: Screen,
)

private data class ConnectionBannerState(
    val connected: Boolean,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NimonsApplication
        val isLoggedIn = runBlocking { app.sessionManager.isLoggedIn() }
        val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
        setContent {
            Nimons360Theme {
                NimonsApp(app = app, startDestination = startDestination)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NimonsApp(app: NimonsApplication, startDestination: String) {
    val navController = rememberNavController()
    var showStartupSplash by rememberSaveable { mutableStateOf(true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val networkStatus by app.connectivityObserver.status.collectAsState()
    var connectionBannerState by remember { mutableStateOf<ConnectionBannerState?>(null) }
    var previousNetworkStatus by remember { mutableStateOf<NetworkStatus?>(null) }

    LaunchedEffect(networkStatus) {
        val previous = previousNetworkStatus
        if (previous == null) {
            previousNetworkStatus = networkStatus
            if (networkStatus == NetworkStatus.OFFLINE) {
                connectionBannerState = ConnectionBannerState(connected = false)
            }
            return@LaunchedEffect
        }

        when (networkStatus) {
            NetworkStatus.OFFLINE -> {
                connectionBannerState = ConnectionBannerState(connected = false)
            }

            NetworkStatus.WIFI, NetworkStatus.MOBILE -> {
                if (previous == NetworkStatus.OFFLINE) {
                    connectionBannerState = ConnectionBannerState(connected = true)
                    previousNetworkStatus = networkStatus
                    delay(2200)
                    if (connectionBannerState?.connected == true) {
                        connectionBannerState = null
                    }
                    return@LaunchedEffect
                }
            }
        }
        previousNetworkStatus = networkStatus
    }

    LaunchedEffect(Unit) {
        UnauthorizedEvent.events.collect {
            app.sessionManager.clearToken()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, Screen.Home),
        BottomNavItem("Map", Icons.Outlined.Map, Icons.Filled.Map, Screen.Map),
        BottomNavItem("Families", Icons.Outlined.People, Icons.Filled.People, Screen.Families),
    )

    val mainRoutes = bottomNavItems.map { it.screen.route }
    val showBottomNav = currentRoute in mainRoutes

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomNav) {
                Surface(
                    color = Surface,
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppGrid.Space8, vertical = AppGrid.Space4),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = navBackStackEntry?.destination?.hierarchy
                                ?.any { it.route == item.screen.route } == true
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) Primary else TextTertiary,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NimonsNavGraph(
                navController = navController,
                startDestination = startDestination,
                app = app,
                modifier = Modifier.padding(innerPadding),
            )

            AnimatedVisibility(
                visible = connectionBannerState != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = AppGrid.ScreenHorizontal,
                        end = AppGrid.ScreenHorizontal,
                        bottom = if (showBottomNav) 92.dp else AppGrid.ScreenHorizontal,
                    ),
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 },
                ) + fadeOut(),
            ) {
                val state = connectionBannerState
                if (state != null) {
                    AppConnectionStatusBox(
                        title = if (state.connected) "Connected again" else "Disconnected",
                        message = if (state.connected) {
                            "Internet connection restored."
                        } else {
                            "Please check your internet connection!"
                        },
                        connected = state.connected,
                    )
                }
            }

            if (showStartupSplash) {
                StartupSplash(
                    modifier = Modifier.align(Alignment.Center),
                    onFinished = { showStartupSplash = false },
                )
            }
        }
    }    
}