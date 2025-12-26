package com.example.demoproject.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoproject.main.data.response.MainResponseModel
import com.example.demoproject.main.repository.MainRepository
import com.example.demoproject.network.controllers.Resource
import com.example.demoproject.utils.helpers.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
) : ViewModel() {
    val hubListApiResponse = SingleLiveEvent<Resource<MainResponseModel>>()


    fun getProfileData() {
        viewModelScope.launch {
            mainRepository.getProfile().collectLatest {
                hubListApiResponse.value = it
            }
        }
    }
}