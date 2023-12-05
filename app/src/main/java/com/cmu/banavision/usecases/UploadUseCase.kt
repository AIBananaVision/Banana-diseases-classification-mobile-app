package com.cmu.banavision.usecases

import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.repository.UploadDataRepo
import com.cmu.banavision.util.ModelData
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class UploadUseCase @Inject constructor(
    private val uploadData: UploadDataRepo
) {


    suspend operator fun invoke(modelData: ModelData): Resource<ApiResponse> {
        val result = MutableStateFlow<Resource<ApiResponse>>(Resource.Loading())
        uploadData.uploadData(modelData).collect { resource ->
            result.value = resource
        }
        return result.value
    }
}