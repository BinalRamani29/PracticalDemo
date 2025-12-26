package com.example.demoproject.main.data.di

import com.example.demoproject.main.data.impl.MainRepositoryImpl
import com.example.demoproject.main.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MainRepositoryModule {

    @Binds
    abstract fun bindMainRepository(hubSettingRepositoryImpl: MainRepositoryImpl): MainRepository
}