// app/src/main/java/com/example/a212268_nazatulaini_lab1/ProfileViewModel.kt
// PURPOSE: ViewModel for ProfileScreen — reads/writes UserProfileEntity via Room.

package com.example.a212268_nazatulaini_lab1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a212268_nazatulaini_lab1.data.ReServeRepository
import com.example.a212268_nazatulaini_lab1.data.UserProfileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ReServeRepository) : ViewModel() {

    // Live profile from Room — null on first launch
    val profile: StateFlow<UserProfileEntity?> =
        repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun saveProfile(
        displayName: String,
        bio: String,
        location: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    id          = 1,
                    displayName = displayName,
                    bio         = bio,
                    location    = location,
                    phoneNumber = phoneNumber,
                    joinDate    = profile.value?.joinDate
                        ?: java.text.SimpleDateFormat(
                            "MMM yyyy",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())
                )
            )
        }
    }

    class Factory(private val repository: ReServeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(repository) as T
    }
}