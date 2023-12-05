package com.cmu.banavision.network

data class ErrorInfo(
    val error: String? = "",
    val message: String? = "",
    val path: String? ="",
    val status: Int? = 0
)
