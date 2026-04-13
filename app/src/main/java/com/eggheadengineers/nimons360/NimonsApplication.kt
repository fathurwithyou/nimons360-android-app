package com.eggheadengineers.nimons360

import android.app.Application
import com.eggheadengineers.nimons360.core.battery.BatteryProvider
import com.eggheadengineers.nimons360.core.location.LocationTracker
import com.eggheadengineers.nimons360.core.network.ConnectivityObserver
import com.eggheadengineers.nimons360.core.sensor.OrientationProvider
import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.data.local.AppDatabase
import com.eggheadengineers.nimons360.data.network.NetworkModule
import com.eggheadengineers.nimons360.data.repository.*
import com.eggheadengineers.nimons360.domain.repository.*

class NimonsApplication : Application() {
    val sessionManager by lazy { 
        SessionManager(this) 
    }
    val apiService by lazy { 
        NetworkModule.provideApiService(sessionManager) 
    }
    val wsClient by lazy { 
        NetworkModule.provideWebSocketClient(sessionManager) 
    }
    val database by lazy { 
        AppDatabase.getInstance(this) 
    }
    val authRepository: AuthRepository by lazy { 
        provideAuthRepositoryImpl(apiService, sessionManager) 
    }
    val familyRepository: FamilyRepository by lazy { 
        provideFamilyRepositoryImpl(apiService) 
    }
    val profileRepository: ProfileRepository by lazy { 
        provideProfileRepositoryImpl(apiService) 
    }
    val presenceRepository: PresenceRepository by lazy { 
        providePresenceRepositoryImpl(wsClient) 
    }
    val favoriteLocationRepository: FavoriteLocationRepository by lazy {
        provideFavoriteLocationRepositoryImpl(apiService)
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
}