package com.reciperoulette.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reciperoulette.data.FirebaseAuthRepository
import com.reciperoulette.data.FirestoreRepository
import com.reciperoulette.data.RecipeResult
import com.reciperoulette.network.GeminiService
import com.reciperoulette.util.ShareUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecipeState {
    object Idle : RecipeState()
    object Loading : RecipeState()
    data class Success(val recipe: RecipeResult) : RecipeState()
    data class Error(val message: String) : RecipeState()
}

class RecipeViewModel : ViewModel() {

    private val geminiService = GeminiService
    private val firestoreRepo = FirestoreRepository()
    private val authRepo = FirebaseAuthRepository()

    private val _recipeState = MutableStateFlow<RecipeState>(RecipeState.Idle)
    val recipeState: StateFlow<RecipeState> = _recipeState

    // Feedback for save/favorite actions
    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap

    fun setCapturedBitmap(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
    }

    /** Called from CameraScreen after the photo is captured. */
    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _recipeState.value = RecipeState.Loading
            val result = geminiService.analyzeAndGenerateRecipe(bitmap)
            result.fold(
                onSuccess = { _recipeState.value = RecipeState.Success(it) },
                onFailure = { _recipeState.value = RecipeState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    /**
     * Save the current recipe to Firestore.
     * Silently skips if the user is not signed in.
     */
    fun saveCurrentRecipe() {
        val state = _recipeState.value as? RecipeState.Success ?: return
        val uid = authRepo.currentUser?.uid ?: run {
            _saveMessage.value = "Sign in to save recipes"
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.saveRecipe(uid, state.recipe)
            _saveMessage.value = if (result.isSuccess) "Recipe saved! 🎉" else "Save failed"
        }
    }

    /**
     * Toggle favorite on the current recipe and update Firestore.
     */
    fun toggleFavorite() {
        val state = _recipeState.value as? RecipeState.Success ?: return
        val uid = authRepo.currentUser?.uid ?: run {
            _saveMessage.value = "Sign in to favorite recipes"
            return
        }
        val updated = state.recipe.copy(isFavorite = !state.recipe.isFavorite)
        _recipeState.value = RecipeState.Success(updated)
        viewModelScope.launch {
            firestoreRepo.toggleFavorite(uid, updated, updated.isFavorite)
            // Also make sure the recipe document exists in Firestore
            firestoreRepo.saveRecipe(uid, updated)
        }
    }

    /** Share the current recipe via the Android share sheet. */
    fun shareRecipe(context: Context) {
        val state = _recipeState.value as? RecipeState.Success ?: return
        ShareUtils.shareRecipe(context, state.recipe)
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun reset() {
        _recipeState.value = RecipeState.Idle
    }
}
