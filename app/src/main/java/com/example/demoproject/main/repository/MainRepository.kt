package com.example.demoproject.main.repository

import com.example.demoproject.main.data.response.MainResponseModel
import com.example.demoproject.network.controllers.Resource
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getProfile(): Flow<Resource<MainResponseModel>>
}