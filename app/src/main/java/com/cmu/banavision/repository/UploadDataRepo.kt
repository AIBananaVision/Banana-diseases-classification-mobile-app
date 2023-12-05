package com.cmu.banavision.repository

import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.util.ModelData
import kotlinx.coroutines.flow.Flow

interface UploadDataRepo {
    suspend fun uploadData(
        modelData: ModelData
    ): Flow<Resource<ApiResponse>>
}