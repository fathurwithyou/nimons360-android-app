package com.eggheadengineers.nimons360.data.network

/**
 * Connection settings for the nimons360-live coordinator.
 * For local testing on a physical device, put your machine's LAN IP
 * (e.g. 192.168.1.42). The Android emulator can reach the host machine at 10.0.2.2.
 */
object LiveConfig {
    const val COORDINATOR_BASE_URL: String = "http://10.0.2.2:4000/"
    const val COORDINATOR_WS_URL: String = "ws://10.0.2.2:4000/ws/live-streams"
    const val API_KEY: String = ""
}
