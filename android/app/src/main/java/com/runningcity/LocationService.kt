package com.runningcity

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class LocationService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val networkClient = NetworkClient()

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        startForeground(1, createNotification())
        startTracking()
    }

    private fun createNotification(): Notification {
        val channelId = "runningcity_location"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "RunningCity GPS Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RunningCity GPS 전송 중")
            .setContentText("위치 데이터를 서버로 전송하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    private fun startTracking() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(3) // 몇초간격으로 보낼지. 일단 3초로
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.requestLocationUpdates(request, locationCallback, null)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location? = result.lastLocation
            location?.let {
                serviceScope.launch {
                    networkClient.sendLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
