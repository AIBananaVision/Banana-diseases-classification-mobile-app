package com.cmu.banavision.util

import com.cmu.banavision.network.ApiResponse

data class ResponseState (
    val response: ApiResponse? = null,
    val error: String? = null,
    val loading: Boolean = false
)