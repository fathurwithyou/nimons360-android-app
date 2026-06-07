package com.eggheadengineers.nimons360

import android.app.Application
import com.eggheadengineers.nimons360.core.battery.BatteryProvider
import com.eggheadengineers.nimons360.core.files.FavoriteLocationPhotoStore
import com.eggheadengineers.nimons360.core.location.LocationHistoryRecorder
import com.eggheadengineers.nimons360.core.location.LocationTracker
import com.eggheadengineers.nimons360.core.network.ConnectivityObserver
import com.eggheadengineers.nimons360.core.notifications.NotificationTokenSync
import com.eggheadengineers.nimons360.core.preferences.UserPreferenceStore
import com.eggheadengineers.nimons360.core.sensor.OrientationProvider
import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.data.local.AppDatabase
import com.eggheadengineers.nimons360.data.network.NetworkModule
import com.eggheadengineers.nimons360.data.repository.*
import com.eggheadengineers.nimons360.domain.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

class NimonsApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val sessionManager by lazy { 
        SessionManager(this) 
    }
    val apiService by lazy {
        NetworkModule.provideApiService(sessionManager)
    }
    val wsClient by lazy {
        NetworkModule.provideWsClient(sessionManager)
    }
    val liveStreamApi by lazy {
        NetworkModule.provideLiveStreamApi()
    }
    val liveWsClient by lazy {
        NetworkModule.provideLiveWsClient()
    }
    val database by lazy { 
        AppDatabase.getInstance(this) 
    }
    val authRepository: AuthRepository by lazy { 
        AuthRepositoryImpl(apiService, sessionManager) 
    }
    val familyRepository: FamilyRepository by lazy { 
        FamilyRepositoryImpl(apiService, database) 
    }
    val profileRepository: ProfileRepository by lazy { 
        ProfileRepositoryImpl(apiService) 
    }
    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(apiService)
    }
    val presenceRepository: PresenceRepository by lazy { 
        PresenceRepositoryImpl(wsClient, sessionManager) 
    }
    val favoriteLocationRepository: FavoriteLocationRepository by lazy {
        FavoriteLocationRepositoryImpl(database.favoriteLocationDao(), FavoriteLocationPhotoStore(this))
    }
    val locationHistoryRepository: LocationHistoryRepository by lazy {
        LocationHistoryRepositoryImpl(database.locationHistoryDao())
    }
    val locationHistoryRecorder by lazy {
        LocationHistoryRecorder(locationTracker, locationHistoryRepository, appScope)
    }
    val liveStreamRepository: LiveStreamRepository by lazy {
        LiveStreamRepositoryImpl(liveStreamApi, liveWsClient, sessionManager)
    }
    val connectivityObserver by lazy { 
        ConnectivityObserver(this) 
    }
    val locationTracker by lazy { 
        LocationTracker(this) 
    }
    val orientationProvider by lazy { 
        OrientationProvider(this) 
    }
    val batteryProvider by lazy { 
        BatteryProvider(this) 
    }
    val userPreferenceStore by lazy {
        UserPreferenceStore(this)
    }
    val notificationTokenSync by lazy {
        NotificationTokenSync(notificationRepository, userPreferenceStore)
    }
}
