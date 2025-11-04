package com.runningcity.data.location

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.runningcity.R
import com.runningcity.data.location.LocationBufferManager
import com.runningcity.data.network.LocationApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var api: LocationApiService
    private lateinit var fusedClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        startTracking()
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, "running_location")
            .setContentTitle("러닝시티")
            .setContentText("러닝 세션 동안 위치 추적 중 🏃‍♂️")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

    private fun startTracking() {
        val req = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000L
        ).setMinUpdateDistanceMeters(5f).build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper())
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (loc in result.locations) {
                serviceScope.launch {
                    LocationBufferManager.addLocation(loc)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(callback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
