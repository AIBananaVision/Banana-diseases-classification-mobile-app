package com.cmu.banavision.util

data class Report(
    val predictedClass: String = "",
    val recommendations: List<String> = listOf(),
)