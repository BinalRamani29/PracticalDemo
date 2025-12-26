package com.example.demoproject.utils.safeFlowExtension

import android.content.Context
import com.example.demoproject.R
import com.example.demoproject.network.controllers.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

inline fun <T> safeFlow(
    context: Context,
    dispatcherIo: CoroutineDispatcher,
    crossinline apiCall: suspend () -> Resource<T>
): Flow<Resource<T>> = flow {
    emit(Resource.Loading())
    val result = runCatching { apiCall() }

    emit(result.getOrElse {
        Resource.Error(it.message ?: context.getString(R.string.error_something_went_wrong))
    })
}.flowOn(dispatcherIo)