package com.vishnu.habittracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login/Signup form state
 */
data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val isLogin: Boolean = true,        // true = login form, false = signup form
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for authentication — ports auth.js UI logic.
 *
 * Maps to:
 * - auth.js onAuthStateChange (line 65) → authState Flow
 * - auth.js login() (line 256) → signInWithEmail()
 * - auth.js signup() (line 284) → signUp()
 * - auth.js signInWithGoogle() (line 226) → signInWithGoogle()
 * - auth.js logout() (line 307) → signOut()
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Auth state observation — drives navigation guard */
    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    /** Form state for login/signup screens */
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    // ── Form Actions ───────────────────────────────────────

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value.copy(password = password, errorMessage = null)
    }

    fun toggleForm() {
        _formState.value = _formState.value.copy(
            isLogin = !_formState.value.isLogin,
            errorMessage = null,
            successMessage = null
        )
    }

    // ── Auth Actions ───────────────────────────────────────

    /**
     * Google OAuth — opens browser for Google sign-in.
     * Equivalent to: auth.js signInWithGoogle() (line 226-254)
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.signInWithGoogle()
                // Auth state change will be observed via authState Flow
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Email/Password sign-in.
     * Equivalent to: auth.js login() (line 256-282)
     */
    fun signInWithEmail() {
        val state = _formState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _formState.value = state.copy(errorMessage = "Please enter email and password")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.signInWithEmail(state.email, state.password)
                _formState.value = _formState.value.copy(isLoading = false)
                // Auth state change will navigate to Dashboard automatically
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }

    /**
     * Create new account.
     * Equivalent to: auth.js signup() (line 284-305)
     */
    fun signUp() {
        val state = _formState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _formState.value = state.copy(errorMessage = "Please enter email and password")
            return
        }
        if (state.password.length < 6) {
            _formState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.signUp(state.email, state.password)
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    isLogin = true,
                    successMessage = "Account created! You can now login.",
                    email = "",
                    password = ""
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign up failed"
                )
            }
        }
    }

    /**
     * Sign out.
     * Equivalent to: auth.js logout() (line 307-332)
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                // Reset form state
                _formState.value = AuthFormState()
            } catch (e: Exception) {
                // Ignore "session missing" errors — user is already signed out
            }
        }
    }

    /** Get current user email for display */
    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUser()?.email
    }

    /** Get current user ID */
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
}
