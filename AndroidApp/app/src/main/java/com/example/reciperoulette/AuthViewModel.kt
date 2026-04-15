package com.reciperoulette.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.reciperoulette.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object SignedOut : AuthState()
}

class AuthViewModel : ViewModel() {

    private val authRepo = FirebaseAuthRepository()

    private val _authState = MutableStateFlow<AuthState>(
        if (authRepo.isLoggedIn) AuthState.Success(authRepo.currentUser!!)
        else AuthState.SignedOut
    )
    val authState: StateFlow<AuthState> = _authState

    val currentUser: FirebaseUser?
        get() = authRepo.currentUser

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepo.signUp(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Sign-up failed") }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepo.signIn(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Sign-in failed") }
            )
        }
    }

    fun signOut() {
        authRepo.signOut()
        _authState.value = AuthState.SignedOut
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.SignedOut
        }
    }
}
