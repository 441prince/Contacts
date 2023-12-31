package com.prince.contacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.viewpager.widget.ViewPager
import com.prince.contacts.models.ContactRepository
import com.prince.contacts.models.Profile
import com.prince.contacts.models.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    // Define a LiveData to trigger navigation
    private val navigateToNewActivity = MutableLiveData<Boolean>()
    private val _selectedProfile = MutableLiveData<Profile?>()
    val selectedProfile: LiveData<Profile?> get() = _selectedProfile

    fun getAllProfiles() = liveData {
        //insertContact(Contact(6, "123456", "Joel", R.drawable.filledheart))
        profileRepository.allProfiles.collect {
            emit(it)
        }
    }

    // Method to select a profile
    fun selectProfile(profileId: Long, viewPager: ViewPager) {
        Log.d("ProfileViewModel", "selectProfile called with profileId: $profileId")
        viewModelScope.launch {
            /*// Deselect the currently selected profile (if any)
            _selectedProfile.value?.let {
                it.isSelected = false
                repository.update(it)
            }

            // Select the new profile
            val profile = repository.getProfileById(profileId)
            profile?.isSelected = true
            _selectedProfile.value = profile

            // Update the database
            repository.update(profile!!)*/
            Log.d("ProfileViewModel", "inside: $profileId")
            profileRepository.selectProfile(profileId)
            viewPager.adapter?.notifyDataSetChanged()
        }
    }

    fun addDefaultProfile(defaultProfile: Profile) = viewModelScope.launch {
        // Ensure a default profile is available in the database
        profileRepository.checkAndInsertDefaultProfile(defaultProfile)
    }

    suspend fun insert(profile: Profile) {
        profileRepository.insert(profile)
    }

    suspend fun update(profile: Profile) {
        profileRepository.update(profile)
    }

    suspend fun delete(profileId: Long) {
        profileRepository.deleteProfileById(profileId)
        contactRepository.deleteContactsByProfileId(profileId)
    }

    fun getNavigateToNewActivity(): LiveData<Boolean>? {
        return navigateToNewActivity
    }

    fun onPlusButtonClick() {
        navigateToNewActivity.value = true
    }

}

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val contactRepository: ContactRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(profileRepository, contactRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}