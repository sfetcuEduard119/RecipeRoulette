package com.reciperoulette.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firestore operations for recipe history and favorites.
 *
 * Firestore structure:
 *   users/{uid}/recipes/{recipeId}
 *     - name: String
 *     - ingredients: List<String>
 *     - selectedIngredients: List<String>
 *     - steps: List<String>
 *     - chefTip: String
 *     - isFavorite: Boolean
 *     - timestamp: Long
 */
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun recipesCollection(uid: String) =
        db.collection("users").document(uid).collection("recipes")

    /** Save or overwrite a recipe for the given user. Uses recipe name as document ID. */
    suspend fun saveRecipe(uid: String, recipe: RecipeResult): Result<Unit> {
        return try {
            val data = mapOf(
                "name" to recipe.recipeName,
                "ingredients" to recipe.allIngredients,
                "selectedIngredients" to recipe.selectedIngredients,
                "steps" to recipe.steps,
                "chefTip" to recipe.chefTip,
                "isFavorite" to recipe.isFavorite,
                "timestamp" to System.currentTimeMillis()
            )
            // Use a sanitised recipe name as the document ID so duplicates overwrite
            val docId = recipe.recipeName.replace(Regex("[^A-Za-z0-9]"), "_").take(64)
            recipesCollection(uid).document(docId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Toggle the isFavorite flag on an existing recipe document. */
    suspend fun toggleFavorite(uid: String, recipe: RecipeResult, isFavorite: Boolean): Result<Unit> {
        return try {
            val docId = recipe.recipeName.replace(Regex("[^A-Za-z0-9]"), "_").take(64)
            recipesCollection(uid).document(docId)
                .update("isFavorite", isFavorite).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Fetch all saved recipes for a user, newest first. */
    suspend fun fetchAllRecipes(uid: String): Result<List<RecipeResult>> {
        return try {
            val snapshot = recipesCollection(uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    RecipeResult(
                        recipeName = doc.getString("name") ?: return@mapNotNull null,
                        allIngredients = doc.get("ingredients") as? List<String> ?: emptyList(),
                        selectedIngredients = doc.get("selectedIngredients") as? List<String> ?: emptyList(),
                        steps = doc.get("steps") as? List<String> ?: emptyList(),
                        chefTip = doc.getString("chefTip") ?: "",
                        isFavorite = doc.getBoolean("isFavorite") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Fetch only favorited recipes for a user. */
    suspend fun fetchFavorites(uid: String): Result<List<RecipeResult>> {
        return try {
            val snapshot = recipesCollection(uid)
                .whereEqualTo("isFavorite", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    RecipeResult(
                        recipeName = doc.getString("name") ?: return@mapNotNull null,
                        allIngredients = doc.get("ingredients") as? List<String> ?: emptyList(),
                        selectedIngredients = doc.get("selectedIngredients") as? List<String> ?: emptyList(),
                        steps = doc.get("steps") as? List<String> ?: emptyList(),
                        chefTip = doc.getString("chefTip") ?: "",
                        isFavorite = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
