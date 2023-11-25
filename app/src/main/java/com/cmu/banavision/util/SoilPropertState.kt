package com.cmu.banavision.util

import com.cmu.banavision.network.SoilResponse

data class SoilPropertState(
    val soilPropertState: SoilResponse? = null,
    val isLoading : Boolean = false,
    val error : String = ""
)
