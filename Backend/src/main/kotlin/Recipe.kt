package com.Recipe

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val name: String,
    val description: String,
    val steps: List<String>,
    val time_minutes: Int
)