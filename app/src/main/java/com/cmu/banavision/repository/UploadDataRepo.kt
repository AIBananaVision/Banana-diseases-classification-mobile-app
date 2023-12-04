package com.cmu.banavision.repository

import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.util.ModelData

interface UploadDataRepo {
    suspend fun uploadData(
        modelData: ModelData
    ):Resource<ApiResponse>
}