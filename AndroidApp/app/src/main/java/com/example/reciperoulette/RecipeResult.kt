package com.reciperoulette.data

import kotlinx.serialization.Serializable

/**
 * Data model returned from a Gemini Vision call and stored in Firestore.
 *
 * Gemini is prompted to return JSON matching this structure:
 * {
 *   "recipeName": "...",
 *   "allIngredients": ["...", ...],
 *   "selectedIngredients": ["...", ...],
 *   "steps": ["...", ...],
 *   "chefTip": "..."
 * }
 */
@Serializable
data class RecipeResult(
    val recipeName: String,
    val allIngredients: List<String>,
    val selectedIngredients: List<String>,
    val steps: List<String>,
    val chefTip: String,
    // Not returned by Gemini — managed locally and in Firestore
    val isFavorite: Boolean = false
)
