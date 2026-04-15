package com.reciperoulette.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reciperoulette.data.FirebaseAuthRepository
import com.reciperoulette.data.FirestoreRepository
import com.reciperoulette.data.RecipeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FavoritesState {
    object Loading : FavoritesState()
    data class Success(val recipes: List<RecipeResult>) : FavoritesState()
    data class Error(val message: String) : FavoritesState()
    object NotSignedIn : FavoritesState()
}

class FavoritesViewModel : ViewModel() {

    private val firestoreRepo = FirestoreRepository()
    private val authRepo = FirebaseAuthRepository()

    private val _state = MutableStateFlow<FavoritesState>(FavoritesState.Loading)
    val state: StateFlow<FavoritesState> = _state

    // Track which tab is selected: "favorites" or "history"
    private val _selectedTab = MutableStateFlow("favorites")
    val selectedTab: StateFlow<String> = _selectedTab

    init {
        load()
    }

    fun load() {
        val uid = authRepo.currentUser?.uid
        if (uid == null) {
            _state.value = FavoritesState.NotSignedIn
            return
        }
        viewModelScope.launch {
            _state.value = FavoritesState.Loading
            val result = if (_selectedTab.value == "favorites") {
                firestoreRepo.fetchFavorites(uid)
            } else {
                firestoreRepo.fetchAllRecipes(uid)
            }
            _state.value = result.fold(
                onSuccess = { FavoritesState.Success(it) },
                onFailure = { FavoritesState.Error(it.message ?: "Failed to load") }
            )
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
        load()
    }

    fun removeFavorite(recipe: RecipeResult) {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreRepo.toggleFavorite(uid, recipe, false)
            load()
        }
    }
}
