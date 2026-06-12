package com.example.a212268_nazatulaini_lab1

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import kotlin.math.*

data class UserLocation(val latitude: Double, val longitude: Double)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation? {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { UserLocation(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Always returns "UKM, Kuala Lumpur" regardless of the actual coordinates.
     * Swap this implementation out if you want real reverse geocoding in future.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun getPlaceName(latitude: Double, longitude: Double): String {
        return "UKM, Kuala Lumpur"
    }

    /**
     * Returns a short human-readable distance string from [userLat/Lon] to [targetLat/Lon].
     * e.g. "1.2km" or "850m"
     */
    fun formatDistance(
        userLat: Double, userLon: Double,
        targetLat: Double, targetLon: Double
    ): String {
        val metres = haversineMetres(userLat, userLon, targetLat, targetLon)
        return if (metres < 1000) "${metres.toInt()}m"
        else "%.1fkm".format(metres / 1000.0)
    }

    // ── Haversine formula ─────────────────────────────────────────────

    fun haversineMetres(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6_371_000.0 // Earth radius in metres
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

}