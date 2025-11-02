package com.runningcity.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {

    // 필요한 권한 목록 (우선순위 순서)
    private val requiredPermissions = buildList {
        // 1순위: 심박수 (가장 중요!)
        add(Manifest.permission.BODY_SENSORS)

        // 2순위: GPS
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // 3순위: 걸음수 (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // 4순위: 센서 고속 샘플링
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
        }

        // 5순위: 알림 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private var currentPermissionIndex = 0

    // 단일 권한 요청 launcher
    private val requestSinglePermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            println("🔍 Permission ${requiredPermissions[currentPermissionIndex].split(".").last()} = $granted")

            // 다음 권한으로 이동
            currentPermissionIndex++

            if (currentPermissionIndex < requiredPermissions.size) {
                // 다음 권한 요청
                requestNextPermission()
            } else {
                // 모든 권한 요청 완료
                val allGranted = hasAllPermissions()
                onPermissionResult?.invoke(allGranted)
                currentPermissionIndex = 0
            }
        }

    // 여러 권한 한번에 요청 launcher (백업용)
    private val requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            permissions.forEach { (permission, granted) ->
                println("🔍 Permission: ${permission.split(".").last()} = $granted")
            }
            onPermissionResult?.invoke(allGranted)
        }

    fun hasAllPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * 권한 요청 (순차적으로 하나씩)
     */
    fun requestPermissions(callback: (Boolean) -> Unit) {
        onPermissionResult = callback

        if (hasAllPermissions()) {
            callback(true)
            return
        }

        // 순차 요청 시작
        currentPermissionIndex = 0
        println("🚀 Starting sequential permission requests...")
        requestNextPermission()
    }

    /**
     * 권한 일괄 요청 (대안)
     */
    fun requestPermissionsAtOnce(callback: (Boolean) -> Unit) {
        onPermissionResult = callback

        if (hasAllPermissions()) {
            callback(true)
            return
        }

        println("🚀 Requesting all permissions at once...")
        requestMultiplePermissionsLauncher.launch(requiredPermissions.toTypedArray())
    }

    private fun requestNextPermission() {
        if (currentPermissionIndex >= requiredPermissions.size) {
            onPermissionResult?.invoke(hasAllPermissions())
            return
        }

        val permission = requiredPermissions[currentPermissionIndex]

        // 이미 허용된 권한은 건너뛰기
        if (hasPermission(permission)) {
            println("✅ ${permission.split(".").last()} already granted, skipping...")
            currentPermissionIndex++
            requestNextPermission()
            return
        }

        // 권한 요청
        println("❓ Requesting ${permission.split(".").last()}...")
        requestSinglePermissionLauncher.launch(permission)
    }

    fun getMissingPermissions(): List<String> {
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermissionDisplayName(permission: String): String {
        return when {
            permission.contains("BODY_SENSORS") -> "심박수 센서"
            permission.contains("FINE_LOCATION") -> "정확한 위치"
            permission.contains("COARSE_LOCATION") -> "대략적 위치"
            permission.contains("ACTIVITY_RECOGNITION") -> "걸음 수"
            permission.contains("HIGH_SAMPLING_RATE") -> "고속 센서"
            permission.contains("POST_NOTIFICATIONS") -> "알림"
            else -> permission.split(".").lastOrNull() ?: permission
        }
    }

    fun getSensorStatus(): SensorPermissionStatus {
        return SensorPermissionStatus(
            heartRate = hasPermission(Manifest.permission.BODY_SENSORS),
            gps = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
            steps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            } else true,
            notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else true
        )
    }

    fun createSettingsIntent(): android.content.Intent {
        return android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {
            data = android.net.Uri.fromParts("package", activity.packageName, null)
        }
    }
}

data class SensorPermissionStatus(
    val heartRate: Boolean,
    val gps: Boolean,
    val steps: Boolean,
    val notifications: Boolean
) {
    fun allEssentialGranted(): Boolean = heartRate && gps && steps
    fun getGrantedCount(): Int = listOf(heartRate, gps, steps, notifications).count { it }
    fun getTotalCount(): Int = 4
}