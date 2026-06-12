package com.example.a212268_nazatulaini_lab1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Holds GPS state shared across the whole app.
 * Exposed as StateFlows so any composable can collect it.
 */
class LocationViewModel(app: Application) : AndroidViewModel(app) {

    private val helper = LocationHelper(app)

    // Raw coordinates — null until first successful fix
    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation

    // Human-readable place name for the header / profile
    private val _placeName = MutableStateFlow<String?>(null)
    val placeName: StateFlow<String?> = _placeName

    // Whether a fetch is in progress
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // One-line error message (null = no error)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Fetch current GPS position + reverse-geocode to a place name.
     * Safe to call multiple times; skips if already loading.
     */
    fun fetchLocation() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val loc = helper.getCurrentLocation()
            if (loc != null) {
                _userLocation.value = loc
                _placeName.value = helper.getPlaceName(loc.latitude, loc.longitude)
            } else {
                _error.value = "Could not get location. Make sure GPS is on."
            }
            _isLoading.value = false
        }
    }

    /**
     * Returns a formatted distance string from the user's current location
     * to the given target coordinates (parsed from strings like "3.1234, 101.5678").
     * Returns null if user location is unavailable or target can't be parsed.
     */
    fun distanceTo(targetCoordString: String): String? {
        val user = _userLocation.value ?: return null
        val parts = targetCoordString.split(",").map { it.trim().toDoubleOrNull() }
        if (parts.size < 2 || parts[0] == null || parts[1] == null) return null
        return helper.formatDistance(user.latitude, user.longitude, parts[0]!!, parts[1]!!)
    }

    class Factory(private val app: Application) : ViewModelProvider.AndroidViewModelFactory(app) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            LocationViewModel(app) as T
    }
}