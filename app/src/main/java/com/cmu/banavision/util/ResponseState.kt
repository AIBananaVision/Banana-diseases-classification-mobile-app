package com.cmu.banavision.util

import android.net.Uri
import com.cmu.banavision.network.ApiResponse


data class ResponseState (
    val response: ApiResponse? = null,
    val uri: Uri? = null,
    val error: String? = null,
    val loading: Boolean = false
)