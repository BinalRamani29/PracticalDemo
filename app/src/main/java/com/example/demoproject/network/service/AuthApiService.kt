package com.example.demoproject.network.service

import com.example.demoproject.main.data.response.MainResponseModel
import com.example.demoproject.network.service.AuthApiConstants.GET_PROFILE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @GET(GET_PROFILE)
    suspend fun getProfile(
    ): Response<MainResponseModel>
}