package com.eggheadengineers.nimons360.feature.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.core.media.ImagePayload
import com.eggheadengineers.nimons360.core.media.createCacheImageUri
import com.eggheadengineers.nimons360.core.media.readImagePayload
import com.eggheadengineers.nimons360.core.share.writeShareFile
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.model.FavoriteLocationPhotoInput
import com.eggheadengineers.nimons360.domain.model.MemberPresence
import com.eggheadengineers.nimons360.ui.components.AppCard
import com.eggheadengineers.nimons360.ui.components.AppDarkButton
import com.eggheadengineers.nimons360.ui.components.AppDestructiveButton
import com.eggheadengineers.nimons360.ui.components.AppFilterPill
import com.eggheadengineers.nimons360.ui.components.AppGrid
import com.eggheadengineers.nimons360.ui.components.AppSearchBar
import com.eggheadengineers.nimons360.ui.components.AppSecondaryButton
import com.eggheadengineers.nimons360.ui.components.AppSectionHeader
import com.eggheadengineers.nimons360.ui.components.AppTextField
import com.eggheadengineers.nimons360.ui.components.AvatarCircle
import com.eggheadengineers.nimons360.ui.theme.Background
import com.eggheadengineers.nimons360.ui.theme.Border
import com.eggheadengineers.nimons360.ui.theme.Info
import com.eggheadengineers.nimons360.ui.theme.Primary
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import com.eggheadengineers.nimons360.ui.theme.TextSecondary
import com.eggheadengineers.nimons360.ui.theme.Warning
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.CustomZoomButtonsController
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor
import com.eggheadengineers.nimons360.ui.theme.Surface as SurfaceColor

private sealed interface MapBottomPanelState {
    data object AddFavorite : MapBottomPanelState
    data class FavoriteDetail(val favoriteId: Long) : MapBottomPanelState
    data class EditFavorite(val favoriteId: Long) : MapBottomPanelState
    data class MemberDetail(val memberId: String) : MapBottomPanelState
    data object MyLocation : MapBottomPanelState
}

private enum class MapPhotoTarget {
    Add,
    Edit,
}

@Composable
fun MapScreen(viewModel: MapViewModel, onProfileClick: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var currentUserName by rememberSaveable { mutableStateOf("You") }
    var myCustomPinBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var selectedFavorite by remember { mutableStateOf<FavoriteLocation?>(null) }
    var selectedFavoriteSnapshot by remember { mutableStateOf<FavoriteLocation?>(null) }
    var selectedMemberSnapshot by remember { mutableStateOf<MemberPresence?>(null) }
    var editingFavorite by remember { mutableStateOf<FavoriteLocation?>(null) }
    var addPhotos by remember { mutableStateOf<List<ImagePayload>>(emptyList()) }
    var editPhotos by remember { mutableStateOf<List<ImagePayload>>(emptyList()) }
    var photoTarget by remember { mutableStateOf(MapPhotoTarget.Add) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraTarget by remember { mutableStateOf<MapPhotoTarget?>(null) }
    var notifyingMember by remember { mutableStateOf<MemberPresence?>(null) }
    var myLocationPanelVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    fun appendPhoto(uri: Uri?) {
        if (uri == null) return
        coroutineScope.launch {
            readImagePayload(context, uri).fold(
                onSuccess = { payload ->
                    when (photoTarget) {
                        MapPhotoTarget.Add -> addPhotos = addPhotos + payload
                        MapPhotoTarget.Edit -> editPhotos = editPhotos + payload
                    }
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message ?: "Failed to load image.", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> appendPhoto(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) appendPhoto(pendingCameraUri)
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val target = pendingCameraTarget ?: return@rememberLauncherForActivityResult
            pendingCameraTarget = null
            photoTarget = target
            val uri = createCacheImageUri(context, "marked_location_photos", "marked_location")
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            pendingCameraTarget = null
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    fun pickPhoto(target: MapPhotoTarget) {
        photoTarget = target
        galleryLauncher.launch("image/*")
    }

    fun takePhoto(target: MapPhotoTarget) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            photoTarget = target
            val uri = createCacheImageUri(context, "marked_location_photos", "marked_location")
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            pendingCameraTarget = target
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef.value?.onPause()
            mapViewRef.value?.onDetach()
        }
    }

    LaunchedEffect(context) {
        val app = context.applicationContext as? NimonsApplication
        val storedName = app?.sessionManager?.getUserName()?.trim().orEmpty()
        if (storedName.isNotBlank()) currentUserName = storedName

        val selectedPinId = app?.userPreferenceStore?.getSelectedPinId() ?: "avatar"
        if (selectedPinId != "avatar") {
            val pinFile = File(File(context.filesDir, "custom_pins"), "$selectedPinId.png")
            if (pinFile.exists()) {
                myCustomPinBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    android.graphics.BitmapFactory.decodeFile(pinFile.absolutePath)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onPermissionGranted()
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.onPermissionGranted()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            myLat = state.myLat,
            myLng = state.myLng,
            myRotation = state.myRotation,
            myCustomPinBitmap = myCustomPinBitmap,
            members = state.filteredMembers,
            favoriteLocations = state.favoriteLocations,
            onMarkerClick = {
                selectedMemberSnapshot = it
                viewModel.selectMember(it)
            },
            onFavoriteClick = {
                selectedFavorite = it
                selectedFavoriteSnapshot = it
            },
            onMapLongPress = { lat, lng -> viewModel.requestAddFavorite(lat, lng) },
            onMyLocationClick = {
                viewModel.selectMember(null)
                myLocationPanelVisible = true
            },
            onMapReady = { mapViewRef.value = it },
        )

        MapOverlayScrims()

        if (!isLandscape) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = AppGrid.ScreenHorizontal, top = AppGrid.Space3),
            ) {
                AvatarCircle(
                    initial = currentUserName.firstOrNull()?.uppercaseChar() ?: 'Y',
                    size = 40,
                    modifier = Modifier.clickable { onProfileClick() },
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(
                    start = AppGrid.ScreenHorizontal,
                    top = AppGrid.Space3,
                    end = AppGrid.ScreenHorizontal + if (isLandscape) 0.dp else 52.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        ) {
            AppSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search location or member...",
            )

            if (state.families.isNotEmpty() && state.hasLocationPermission) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
                ) {
                    item {
                        AppFilterPill(
                            text = "All Families",
                            selected = state.selectedFamilyIds.isEmpty(),
                            onClick = { viewModel.clearFamilyFilter() },
                        )
                    }
                    items(state.families, key = { it.id }) { family ->
                        AppFilterPill(
                            text = family.name,
                            selected = family.id in state.selectedFamilyIds,
                            onClick = { viewModel.toggleFamily(family.id) },
                        )
                    }
                }
            }
        }

        if (state.hasLocationPermission) {
            Column(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.BottomStart else Alignment.CenterEnd)
                    .padding(
                        start = if (isLandscape) AppGrid.ScreenHorizontal else 0.dp,
                        end = if (isLandscape) 0.dp else AppGrid.ScreenHorizontal,
                        bottom = if (isLandscape) AppGrid.Space6 else 0.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MapZoomControls(
                    onZoomIn = { mapViewRef.value?.controller?.zoomIn() },
                    onZoomOut = { mapViewRef.value?.controller?.zoomOut() },
                    compact = isLandscape,
                )
                MapMyLocationButton(
                    compact = isLandscape,
                    onClick = {
                        val lat = state.myLat
                        val lng = state.myLng
                        if (lat != 0.0 || lng != 0.0) {
                            mapViewRef.value?.let { mapView ->
                                if (mapView.zoomLevelDouble < 13.0) {
                                    mapView.controller.setZoom(15.0)
                                }
                                mapView.controller.setCenter(GeoPoint(lat, lng))
                            }
                        }
                    },
                )
            }

            if (state.selectedMemberId != null && state.selectedMember == null) {
                LaunchedEffect(Unit) { viewModel.selectMember(null) }
            }

            val panelState = when {
                state.pendingFavoriteLat != null -> {
                    myLocationPanelVisible = true
                    MapBottomPanelState.AddFavorite
                }
                editingFavorite != null -> {
                    myLocationPanelVisible = true
                    editingFavorite?.id?.let(MapBottomPanelState::EditFavorite) ?: MapBottomPanelState.MyLocation
                }
                selectedFavorite != null -> {
                    myLocationPanelVisible = true
                    selectedFavorite?.id?.let(MapBottomPanelState::FavoriteDetail) ?: MapBottomPanelState.MyLocation
                }
                state.selectedMemberId != null -> {
                    myLocationPanelVisible = true
                    state.selectedMemberId?.let(MapBottomPanelState::MemberDetail) ?: MapBottomPanelState.MyLocation
                }
                else -> MapBottomPanelState.MyLocation
            }

            AnimatedContent(
                targetState = panelState,
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = AppGrid.ScreenHorizontal, bottom = AppGrid.Space3)
                        .widthIn(max = 260.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = AppGrid.ScreenHorizontal)
                        .navigationBarsPadding()
                        .padding(bottom = AppGrid.Space3)
                },
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideInVertically(
                            animationSpec = tween(250),
                            initialOffsetY = { it / 4 },
                        ) + fadeIn(tween(200)),
                        initialContentExit = fadeOut(tween(150)),
                    )
                },
                label = "map_bottom_panel",
            ) { currentPanelState ->
                when (currentPanelState) {
                    MapBottomPanelState.AddFavorite -> {
                        val lat = state.pendingFavoriteLat ?: 0.0
                        val lng = state.pendingFavoriteLng ?: 0.0
                        AddFavoritePanel(
                            lat = lat,
                            lng = lng,
                            photos = addPhotos,
                            onAddPhotoFromGallery = { pickPhoto(MapPhotoTarget.Add) },
                            onTakePhoto = { takePhoto(MapPhotoTarget.Add) },
                            onNavigate = { openGoogleMapsNavigation(context, lat, lng) },
                            onConfirm = { name, description ->
                                viewModel.confirmAddFavorite(
                                    name = name,
                                    description = description,
                                    photos = addPhotos.map { it.toFavoritePhotoInput() },
                                )
                                addPhotos = emptyList()
                            },
                            onDismiss = {
                                addPhotos = emptyList()
                                viewModel.cancelAddFavorite()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is MapBottomPanelState.FavoriteDetail -> {
                        val favorite = selectedFavorite ?: selectedFavoriteSnapshot
                        if (favorite == null || favorite.id != currentPanelState.favoriteId) return@AnimatedContent
                        FavoriteDetailPanel(
                            favorite = favorite,
                            onNavigate = { openGoogleMapsNavigation(context, favorite.lat, favorite.lng) },
                            onEdit = {
                                editPhotos = emptyList()
                                editingFavorite = favorite
                            },
                            onDelete = {
                                viewModel.deleteFavorite(favorite.id)
                                selectedFavorite = null
                                selectedFavoriteSnapshot = null
                            },
                            onDismiss = { selectedFavorite = null },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is MapBottomPanelState.EditFavorite -> {
                        val favorite = editingFavorite ?: selectedFavoriteSnapshot
                        if (favorite == null || favorite.id != currentPanelState.favoriteId) return@AnimatedContent
                        EditFavoritePanel(
                            favorite = favorite,
                            photosToAdd = editPhotos,
                            onAddPhotoFromGallery = { pickPhoto(MapPhotoTarget.Edit) },
                            onTakePhoto = { takePhoto(MapPhotoTarget.Edit) },
                            onSave = { name, description ->
                                viewModel.updateFavorite(
                                    id = favorite.id,
                                    name = name,
                                    description = description,
                                    photosToAdd = editPhotos.map { it.toFavoritePhotoInput() },
                                )
                                editPhotos = emptyList()
                                editingFavorite = null
                                selectedFavorite = null
                            },
                            onDismiss = {
                                editPhotos = emptyList()
                                editingFavorite = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is MapBottomPanelState.MemberDetail -> {
                        val member = state.selectedMember ?: selectedMemberSnapshot
                        if (member == null || member.userId != currentPanelState.memberId) return@AnimatedContent
                        MemberDetailCard(
                            member = member,
                            onDismiss = { viewModel.selectMember(null) },
                            onNotify = { notifyingMember = member },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    MapBottomPanelState.MyLocation -> {
                        if (myLocationPanelVisible) {
                            MyLocationCard(
                                name = currentUserName,
                                lat = state.myLat,
                                lng = state.myLng,
                                rotation = state.myRotation,
                                battery = state.battery.level,
                                charging = state.battery.charging,
                                networkStatus = state.networkStatus,
                                onShareStory = {
                                    mapViewRef.value?.let { shareMapScreenshot(context, it) }
                                },
                                onMarkCurrentLocation = {
                                    viewModel.requestAddFavorite(state.myLat, state.myLng)
                                },
                                onDismiss = { myLocationPanelVisible = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        val notifyTarget = notifyingMember
        if (notifyTarget != null) {
            val commonFamilyId = state.families
                .firstOrNull { family -> family.members.any { it.id == notifyTarget.userId } }
                ?.id
            MapNotifyBottomSheet(
                member = notifyTarget,
                familyId = commonFamilyId,
                onDismiss = { notifyingMember = null },
            )
        }

        if (!state.hasLocationPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppGrid.ScreenHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    tonal = true,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space4)) {
                        AppSectionHeader(
                            title = "Location permission required",
                            subtitle = "Allow location access to see your position, keep family markers live, and use the shared map reliably.",
                        )
                        AppDarkButton(
                            text = "Grant location access",
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "You can change this later in system settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun MyLocationCard(
    name: String,
    lat: Double,
    lng: Double,
    rotation: Float,
    battery: Int,
    charging: Boolean,
    networkStatus: com.eggheadengineers.nimons360.core.network.NetworkStatus,
    onShareStory: () -> Unit,
    onMarkCurrentLocation: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MapInfoPanel(
        modifier = modifier,
        leadingInitial = name.firstOrNull()?.uppercaseChar() ?: 'Y',
        title = name,
        subtitle = "Your live location",
        onDismiss = onDismiss,
        rows = listOf(
            "Battery" to buildBatteryText(battery, charging),
            "Connection" to formatConnectivityLabel(
                when (networkStatus) {
                    com.eggheadengineers.nimons360.core.network.NetworkStatus.WIFI -> "wifi"
                    com.eggheadengineers.nimons360.core.network.NetworkStatus.MOBILE -> "mobile"
                    com.eggheadengineers.nimons360.core.network.NetworkStatus.OFFLINE -> "offline"
                }
            ),
            "Heading" to "${rotation.roundToInt()}°",
        ),
        footer = "Lat ${"%.4f".format(lat)}   Lon ${"%.4f".format(lng)}",
        actionItems = listOf(
            "Mark current location" to onMarkCurrentLocation,
            "Share story" to onShareStory,
        ),
    )
}

@Composable
private fun OsmMapView(
    myLat: Double,
    myLng: Double,
    myRotation: Float,
    myCustomPinBitmap: android.graphics.Bitmap?,
    members: Map<String, MemberPresence>,
    favoriteLocations: List<FavoriteLocation>,
    onMarkerClick: (MemberPresence) -> Unit,
    onFavoriteClick: (FavoriteLocation) -> Unit,
    onMapLongPress: (Double, Double) -> Unit,
    onMyLocationClick: () -> Unit,
    onMapReady: (MapView) -> Unit,
) {
    val myMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val memberMarkers = remember { mutableMapOf<String, Marker>() }
    val favoriteMarkers = remember { mutableMapOf<Long, Marker>() }
    val eventsOverlayRef = remember { mutableStateOf<MapEventsOverlay?>(null) }

    AndroidView(
        factory = { context ->
            val cfg = Configuration.getInstance()
            cfg.load(
                context.applicationContext,
                context.applicationContext.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE),
            )
            cfg.userAgentValue = "${context.packageName}/1.0 (Nimons360; Android)"
            MapView(context).also { mapView ->
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                mapView.controller.setZoom(15.0)

                mapView.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP -> {
                            v.performClick()
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false
                }

                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        if (p != null) onMapLongPress(p.latitude, p.longitude)
                        return p != null
                    }
                })
                mapView.overlays.add(eventsOverlay)
                eventsOverlayRef.value = eventsOverlay

                mapView.onResume()
                onMapReady(mapView)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            if (myLat != 0.0 || myLng != 0.0) {
                val point = GeoPoint(myLat, myLng)
                val icon = if (myCustomPinBitmap != null) {
                    val scaled = android.graphics.Bitmap.createScaledBitmap(myCustomPinBitmap, 96, 96, true)
                    BitmapDrawable(mapView.resources, scaled)
                } else {
                    makeArrowDrawable(mapView.resources, Primary.toArgb(), myRotation)
                }
                if (myMarkerRef.value == null) {
                    val marker = Marker(mapView).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        this.icon = icon
                        infoWindow = null
                        setOnMarkerClickListener { _, _ ->
                            onMyLocationClick()
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                    myMarkerRef.value = marker
                    mapView.controller.setCenter(point)
                } else {
                    myMarkerRef.value?.position = point
                    myMarkerRef.value?.icon = icon
                }
            }

            val staleMarkers = memberMarkers.keys - members.keys
            staleMarkers.forEach { id ->
                memberMarkers[id]?.let { marker -> mapView.overlays.remove(marker) }
                memberMarkers.remove(id)
            }

            members.forEach { (userId, presence) ->
                val point = GeoPoint(presence.lat, presence.lng)
                val icon = makeArrowDrawable(mapView.resources, Info.toArgb(), presence.rotation)
                val existing = memberMarkers[userId]
                if (existing != null) {
                    existing.position = point
                    existing.icon = icon
                    existing.title = presence.name
                    existing.setOnMarkerClickListener { _, _ ->
                        onMarkerClick(presence)
                        true
                    }
                } else {
                    val marker = Marker(mapView).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        this.icon = icon
                        title = presence.name
                        setOnMarkerClickListener { _, _ ->
                            onMarkerClick(presence)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                    memberMarkers[userId] = marker
                }
            }

            val currentFavIds = favoriteLocations.map { it.id }.toSet()
            val staleFavs = favoriteMarkers.keys - currentFavIds
            staleFavs.forEach { id ->
                favoriteMarkers[id]?.let { marker -> mapView.overlays.remove(marker) }
                favoriteMarkers.remove(id)
            }

            favoriteLocations.forEach { fav ->
                val point = GeoPoint(fav.lat, fav.lng)
                val existing = favoriteMarkers[fav.id]
                if (existing != null) {
                    existing.position = point
                    existing.title = fav.name
                    existing.setOnMarkerClickListener { _, _ ->
                        onFavoriteClick(fav)
                        true
                    }
                } else {
                    val capturedFav = fav
                    val marker = Marker(mapView).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        this.icon = makeStarDrawable(mapView.resources, Warning.toArgb())
                        title = capturedFav.name
                        setOnMarkerClickListener { _, _ ->
                            onFavoriteClick(capturedFav)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                    favoriteMarkers[fav.id] = marker
                }
            }

            mapView.invalidate()
        },
    )
}

@Composable
private fun MapZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val buttonSize = if (compact) 32.dp else 40.dp
    val buttonWidth = if (compact) 36.dp else 44.dp
    val iconSize = if (compact) 14.dp else 18.dp
    val cornerRadius = if (compact) 12.dp else 16.dp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.3f)),
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.width(buttonWidth)) {
            Box(
                modifier = Modifier
                    .size(width = buttonWidth, height = buttonSize)
                    .clickable(onClick = onZoomIn),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Zoom in", tint = TextPrimary, modifier = Modifier.size(iconSize))
            }
            HorizontalDivider(color = Border.copy(alpha = 0.22f))
            Box(
                modifier = Modifier
                    .size(width = buttonWidth, height = buttonSize)
                    .clickable(onClick = onZoomOut),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Zoom out", tint = TextPrimary, modifier = Modifier.size(iconSize))
            }
        }
    }
}

@Composable
private fun MapMyLocationButton(
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val buttonSize = if (compact) 36.dp else 44.dp
    val iconSize = if (compact) 14.dp else 18.dp
    val cornerRadius = if (compact) 12.dp else 16.dp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.3f)),
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier.size(buttonSize).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "My location", tint = TextPrimary, modifier = Modifier.size(iconSize))
        }
    }
}

private fun makeArrowDrawable(res: Resources, color: Int, rotationDeg: Float): BitmapDrawable {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, circlePaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, ringPaint)
    val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    val centerX = size / 2f
    val centerY = size / 2f
    canvas.save()
    canvas.rotate(rotationDeg, centerX, centerY)
    val path = Path().apply {
        moveTo(centerX, 8f)
        lineTo(centerX - 9f, centerY + 8f)
        lineTo(centerX + 9f, centerY + 8f)
        close()
    }
    canvas.drawPath(path, arrowPaint)
    canvas.restore()
    return BitmapDrawable(res, bitmap)
}

@Composable
private fun BoxScope.MapOverlayScrims() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Background.copy(alpha = 0.92f),
                        Background.copy(alpha = 0f),
                    ),
                )
            )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Background.copy(alpha = 0.88f),
                    ),
                )
            )
    )
}

@Composable
private fun MemberDetailCard(
    member: MemberPresence,
    onDismiss: () -> Unit,
    onNotify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MapInfoPanel(
        modifier = modifier.fillMaxWidth(),
        leadingInitial = member.name.firstOrNull()?.uppercaseChar() ?: '?',
        title = member.name,
        subtitle = member.email,
        onDismiss = onDismiss,
        actionText = "Notify",
        onActionClick = onNotify,
        rows = listOf(
            "Battery" to buildBatteryText(member.battery, member.charging),
            "Connection" to formatConnectivityLabel(member.internetStatus),
            "Updated" to formatLastSeen(member.lastSeen),
        ),
        footer = "Lat ${"%.4f".format(member.lat)}   Lon ${"%.4f".format(member.lng)}   ${member.rotation.roundToInt()}°",
    )
}

@Composable
private fun MapInfoPanel(
    leadingInitial: Char,
    title: String,
    subtitle: String,
    rows: List<Pair<String, String>>,
    footer: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionItems: List<Pair<String, () -> Unit>> = emptyList(),
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppGrid.Space3, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarCircle(
                    initial = leadingInitial,
                    size = 38,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppGrid.Space3)) {
                    val actions = if (actionItems.isNotEmpty()) {
                        actionItems
                    } else if (actionText != null && onActionClick != null) {
                        listOf(actionText to onActionClick)
                    } else {
                        emptyList()
                    }
                    actions.forEach { (text, action) ->
                        Text(
                            text = text,
                            modifier = Modifier.clickable(onClick = action),
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                        )
                    }
                    if (onDismiss != null) {
                        Text(
                            text = "Close",
                            modifier = Modifier.clickable(onClick = onDismiss),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }

            HorizontalDivider(color = Border.copy(alpha = 0.28f))

            rows.forEachIndexed { index, (label, value) ->
                MapInfoRow(
                    label = label,
                    value = value,
                )
                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        color = Border.copy(alpha = 0.18f),
                        modifier = Modifier.padding(start = 62.dp),
                    )
                }
            }

            HorizontalDivider(color = Border.copy(alpha = 0.28f))

            Text(
                text = footer,
                modifier = Modifier.padding(horizontal = AppGrid.Space3, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapNotifyBottomSheet(
    member: MemberPresence,
    familyId: String?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var message by rememberSaveable { mutableStateOf(mapDefaultGreetingMessage()) }
    var isSending by remember { mutableStateOf(false) }

    fun sendMessage() {
        if (message.isBlank() || familyId == null) return
        isSending = true
        coroutineScope.launch {
            val app = context.applicationContext as? NimonsApplication
            app?.notificationRepository?.sendGreeting(familyId, member.userId, message)
                ?.onSuccess {
                    Toast.makeText(context, "Greeting sent!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
                ?.onFailure {
                    Toast.makeText(context, "Failed to send greeting.", Toast.LENGTH_SHORT).show()
                }
            isSending = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = com.eggheadengineers.nimons360.ui.theme.Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppGrid.ScreenHorizontal)
                .padding(bottom = AppGrid.Space8),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space4),
        ) {
            AppSectionHeader(
                title = "Notify ${member.name}",
                subtitle = if (familyId == null) "No shared family found." else "Send a greeting message.",
            )
            AppTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                singleLine = false,
            )
            AppDarkButton(
                text = if (isSending) "Sending…" else "Send",
                onClick = { sendMessage() },
                modifier = Modifier.fillMaxWidth(),
                enabled = familyId != null && message.isNotBlank() && !isSending,
            )
        }
    }
}

@Composable
private fun MapInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppGrid.Space3, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )
    }
}

private fun buildBatteryText(level: Int, charging: Boolean): String =
    buildString {
        append(level.coerceAtLeast(0))
        append("%")
        if (charging) append(" ⚡")
    }

private fun formatConnectivityLabel(value: String): String = when (value.lowercase()) {
    "wifi" -> "Wi-Fi"
    "mobile" -> "Mobile"
    "offline" -> "Offline"
    else -> value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun mapDefaultGreetingMessage(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning!"
        in 12..17 -> "Good Afternoon!"
        else -> "Good Night!"
    }
}

private fun formatLastSeen(lastSeen: Long): String {
    val elapsedSeconds = ((System.currentTimeMillis() - lastSeen) / 1000L).coerceAtLeast(0L)
    return when {
        elapsedSeconds < 5 -> "Just now"
        elapsedSeconds < 60 -> "${elapsedSeconds}s ago"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ago"
        else -> "${elapsedSeconds / 3600}h ago"
    }
}

private fun makeStarDrawable(res: Resources, color: Int): BitmapDrawable {
    val size = 56
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, bgPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3.5f, ringPaint)
    val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    val cx = size / 2f
    val cy = size / 2f
    val outerR = 12f
    val innerR = 5.5f
    val path = Path()
    for (i in 0 until 5) {
        val outerAngle = Math.toRadians((i * 72 - 90).toDouble())
        val innerAngle = Math.toRadians((i * 72 + 36 - 90).toDouble())
        val ox = cx + outerR * Math.cos(outerAngle).toFloat()
        val oy = cy + outerR * Math.sin(outerAngle).toFloat()
        val ix = cx + innerR * Math.cos(innerAngle).toFloat()
        val iy = cy + innerR * Math.sin(innerAngle).toFloat()
        if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
        path.lineTo(ix, iy)
    }
    path.close()
    canvas.drawPath(path, starPaint)
    return BitmapDrawable(res, bitmap)
}

@Composable
private fun AddFavoritePanel(
    lat: Double,
    lng: Double,
    photos: List<ImagePayload>,
    onAddPhotoFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onNavigate: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppGrid.CardRadius),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(AppGrid.Space4),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Mark location",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Cancel",
                    modifier = Modifier.clickable(onClick = onDismiss),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            Text(
                text = "Lat ${"%.5f".format(lat)}   Lon ${"%.5f".format(lng)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. Home, School, Office") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (name.isNotBlank()) onConfirm(name.trim(), description.trim()) },
                ),
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Description") },
                singleLine = false,
            )

            PhotoAttachmentRow(
                existingPaths = emptyList(),
                newPhotos = photos,
                onAddPhotoFromGallery = onAddPhotoFromGallery,
                onTakePhoto = onTakePhoto,
            )

            AppSecondaryButton(
                text = "Open Google Maps",
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth(),
            )

            AppDarkButton(
                text = "Save marked location",
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FavoriteDetailPanel(
    favorite: FavoriteLocation,
    onNavigate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppGrid.CardRadius),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(AppGrid.Space4),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Close",
                    modifier = Modifier.clickable(onClick = onDismiss),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            Text(
                text = favorite.description.ifBlank { "No description added." },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )

            Text(
                text = "Lat ${"%.5f".format(favorite.lat)}   Lon ${"%.5f".format(favorite.lng)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            if (favorite.photoPaths.isNotEmpty()) {
                LocationPhotoStrip(paths = favorite.photoPaths)
            }

            AppSecondaryButton(
                text = "Open Google Maps",
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth(),
            )

            AppDarkButton(
                text = "Edit marked location",
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )

            AppDestructiveButton(
                text = "Delete marked location",
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EditFavoritePanel(
    favorite: FavoriteLocation,
    photosToAdd: List<ImagePayload>,
    onAddPhotoFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable(favorite.id) { mutableStateOf(favorite.name) }
    var description by rememberSaveable(favorite.id) { mutableStateOf(favorite.description) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppGrid.CardRadius),
        color = SurfaceColor.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(AppGrid.Space4),
            verticalArrangement = Arrangement.spacedBy(AppGrid.Space3),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Edit marked location",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Cancel",
                    modifier = Modifier.clickable(onClick = onDismiss),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Location name") },
                singleLine = true,
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Description") },
                singleLine = false,
            )

            PhotoAttachmentRow(
                existingPaths = favorite.photoPaths,
                newPhotos = photosToAdd,
                onAddPhotoFromGallery = onAddPhotoFromGallery,
                onTakePhoto = onTakePhoto,
            )

            AppDarkButton(
                text = "Save changes",
                onClick = { onSave(name.trim(), description.trim()) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PhotoAttachmentRow(
    existingPaths: List<String>,
    newPhotos: List<ImagePayload>,
    onAddPhotoFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
        Text(
            text = "${existingPaths.size + newPhotos.size} photos",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        if (existingPaths.isNotEmpty()) {
            LocationPhotoStrip(paths = existingPaths)
        }

        if (newPhotos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
                items(newPhotos) { photo ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceColor.copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 92.dp, height = 68.dp)
                                .padding(AppGrid.Space2),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = photo.fileName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
            AppSecondaryButton(
                text = "Gallery",
                onClick = onAddPhotoFromGallery,
                modifier = Modifier.weight(1f),
            )
            AppSecondaryButton(
                text = "Camera",
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LocationPhotoStrip(paths: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2)) {
        items(paths, key = { it }) { path ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceColor.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.34f)),
            ) {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Marked location photo",
                    modifier = Modifier.size(width = 92.dp, height = 68.dp),
                )
            }
        }
    }
}

private fun ImagePayload.toFavoritePhotoInput() = FavoriteLocationPhotoInput(
    fileName = fileName,
    bytes = bytes,
)

private fun openGoogleMapsNavigation(context: android.content.Context, lat: Double, lng: Double) {
    val uri = Uri.parse("google.navigation:q=$lat,$lng")
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"))
    context.startActivity(
        if (mapsIntent.resolveActivity(context.packageManager) != null) mapsIntent else fallbackIntent
    )
}

private fun shareMapScreenshot(context: android.content.Context, mapView: MapView) {
    if (mapView.width <= 0 || mapView.height <= 0) return
    val bitmap = Bitmap.createBitmap(mapView.width, mapView.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    mapView.draw(canvas)
    val bytes = ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.toByteArray()
    }
    val uri = writeShareFile(context, "nimons360-map-story.png", bytes)
    val storyIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setDataAndType(uri, "image/png")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra("interactive_asset_uri", uri)
        setPackage("com.instagram.android")
    }
    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Nimons360 live map")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (storyIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(storyIntent)
    } else {
        context.startActivity(Intent.createChooser(fallbackIntent, "Share map screenshot"))
    }
}
