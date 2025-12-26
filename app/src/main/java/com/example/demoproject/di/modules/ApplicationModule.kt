package com.example.demoproject.di.modules

import com.example.demoproject.di.annotations.NoAuthApiService
import com.example.demoproject.di.annotations.NoAuthOkHttpClient
import com.example.demoproject.di.annotations.NoAuthRetrofit
import com.example.demoproject.network.interceptor.NoAuthInterceptor
import com.example.demoproject.network.service.AuthApiConstants
import com.example.demoproject.network.service.AuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideDispatcherIO(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }


    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }


    @Provides
    @Singleton
    @NoAuthOkHttpClient
    fun providesNoAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        noAuthInterceptor: NoAuthInterceptor
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .cache(null)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(noAuthInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @NoAuthRetrofit
    fun providesNoAuthRetrofit(
        @NoAuthOkHttpClient loggingInterceptorClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder().client(loggingInterceptorClient)
            .baseUrl(AuthApiConstants.BASE_API).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @NoAuthApiService
    fun providesNoAuthApiService(@NoAuthRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}