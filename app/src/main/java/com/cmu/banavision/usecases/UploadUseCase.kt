package com.cmu.banavision.usecases

import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.repository.UploadDataRepo
import com.cmu.banavision.util.ModelData
import javax.inject.Inject

class UploadUseCase @Inject constructor(
    private val uploadData: UploadDataRepo
) {

    suspend operator fun invoke(modelData: ModelData): Resource<ApiResponse> {
        return uploadData.uploadData(modelData)
    }
}