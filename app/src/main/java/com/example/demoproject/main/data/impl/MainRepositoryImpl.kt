package com.example.demoproject.main.data.impl

import android.content.Context
import com.example.demoproject.di.annotations.NoAuthApiService
import com.example.demoproject.main.data.response.MainResponseModel
import com.example.demoproject.main.repository.MainRepository
import com.example.demoproject.network.base.ApiManager
import com.example.demoproject.network.controllers.Resource
import com.example.demoproject.network.interceptor.AuthorizationInterceptor
import com.example.demoproject.network.service.AuthApiService
import com.example.demoproject.utils.safeFlowExtension.safeFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    deviceInterceptor: AuthorizationInterceptor,
    @NoAuthApiService private val deviceApiService: AuthApiService,
    @ApplicationContext private val context: Context
) : MainRepository, ApiManager(context, deviceInterceptor) {

    override suspend fun getProfile(): Flow<Resource<MainResponseModel>> {
        return safeFlow(context, dispatcher) {
            safeApiCall { deviceApiService.getProfile() }
        }
    }

}