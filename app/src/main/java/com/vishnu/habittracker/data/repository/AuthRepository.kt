package com.vishnu.habittracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication states — mirrors the webapp's auth flow from auth.js
 */
sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val user: UserInfo) : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Repository wrapping Supabase Auth — ports auth.js functionality.
 *
 * Key features ported:
 * - Google OAuth with deep link redirect (auth.js line 226-254)
 * - Email/Password sign in (auth.js line 256-282)
 * - Sign up (auth.js line 284-305)
 * - Logout with local data cleanup (auth.js line 307-332)
 * - Auth state observation via Flow (auth.js line 65-110)
 */
@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    /**
     * Observe authentication state as a Flow.
     * Maps Supabase's SessionStatus to our AuthState sealed class.
     *
     * Equivalent to: client.auth.onAuthStateChange() in auth.js line 65
     */
    val authState: Flow<AuthState> = supabaseClient.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = supabaseClient.auth.currentUserOrNull()
                if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }
    }

    /** Current user ID (equivalent to auth.js getUserId()) */
    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    /** Current user info */
    fun getCurrentUser(): UserInfo? {
        return supabaseClient.auth.currentUserOrNull()
    }

    /**
     * Google OAuth sign-in.
     * Equivalent to: auth.js signInWithGoogle() (line 226-254)
     *
     * Uses deep link redirect: com.vishnu.habittracker://auth-callback
     * Supabase-kt handles the browser flow automatically.
     */
    suspend fun signInWithGoogle() {
        supabaseClient.auth.signInWith(Google)
    }

    /**
     * Email/Password sign-in.
     * Equivalent to: auth.js login() (line 256-282)
     */
    suspend fun signInWithEmail(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Create new account with email/password.
     * Equivalent to: auth.js signup() (line 284-305)
     */
    suspend fun signUp(email: String, password: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Sign out the current user.
     * Equivalent to: auth.js logout() (line 307-332)
     */
    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}
