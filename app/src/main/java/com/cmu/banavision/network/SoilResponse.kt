package com.cmu.banavision.network

data class SoilResponse(
    val type: String,
    val geometry: Geometry,
    val properties: SoilProperties,
    val query_time_s: Double
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

data class SoilProperties(
    val layers: List<Layer>
)

data class Layer(
    val name: String,
    val unit_measure: UnitMeasure,
    val depths: List<Depth>
)

data class UnitMeasure(
    val d_factor: Int,
    val mapped_units: String,
    val target_units: String,
    val uncertainty_unit: String
)

data class Depth(
    val range: Range,
    val label: String,
    val values: Values
)

data class Range(
    val top_depth: Int,
    val bottom_depth: Int,
    val unit_depth: String
)

data class Values(
    val mean: Int,
    val uncertainty: Int
)
