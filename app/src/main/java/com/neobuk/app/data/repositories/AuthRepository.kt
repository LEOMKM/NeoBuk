package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Authentication Repository
 * Handles signup, login, logout, and session management via Supabase Auth
 */
import android.content.Context

/**
 * Authentication Repository
 * Handles signup, login, logout, and session management via Supabase Auth
 */
class AuthRepository(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    
    private val auth = supabaseClient.auth
    private val prefs = context.getSharedPreferences("neobuk_auth_prefs", Context.MODE_PRIVATE)
    
    // Current user state
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    // Auth state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Get current user ID (null if not logged in)
     */
    val currentUserId: String?
        get() = auth.currentUserOrNull()?.id
    
    /**
     * Save Remember Me preference
     */
    fun saveRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("remember_me", remember).apply()
    }
    
    private fun getRememberMe(): Boolean {
        return prefs.getBoolean("remember_me", true) // Default true for UX? Or false? "Remember me" usually defaults to false in UI, but if checked, we save true. If logic relies on it, let's default to false if not set. Wait, previously it was "always remember". Let's default to true to match previous behavior if key missing, OR false if strictly "Remember Me". Let's default to false if not set, requiring explicit user action. But existing users might lose session? No, key won't exist. Let's default true to be safe for existing sessions, but UI controls it.
        // Actually, for a "Remember Me" feature, default is usually false.
        // But let's say: if key doesn't exist, use true (legacy behavior).
        return prefs.getBoolean("remember_me", true) 
    }

    /**
     * Sign up a new user with email and password
     */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Result<String> {
        return try {
            _isLoading.value = true
            
            // ... (rest of signUp logic) ...
            
            // Use email if provided, otherwise generate from phone
            val authEmail = email.ifBlank { "${phone}@neobuk.app" }
            
            val result = auth.signUpWith(Email) {
                this.email = authEmail
                this.password = password
                this.data = buildJsonObject {
                    put("full_name", fullName)
                    put("phone", phone)
                }
            }
            
            val userId = result?.id ?: auth.currentUserOrNull()?.id
            
            if (userId != null) {
                if (auth.currentSessionOrNull() == null) {
                    try {
                        auth.signInWith(Email) {
                            this.email = authEmail
                            this.password = password
                        }
                    } catch (e: Exception) {
                         return Result.failure(Exception("Signup successful but login failed. If 'Confirm Email' is enabled in Supabase, please verify your email first."))
                    }
                }
                
                if (auth.currentSessionOrNull() != null) {
                    _currentUser.value = auth.currentUserOrNull()
                    _isAuthenticated.value = true
                    // Default remember me to true for new value driven signups? Or false? 
                    // Usually signup implies "log me in and keep me logged in".
                    saveRememberMe(true)
                    Result.success(userId)
                } else {
                     Result.failure(Exception("Signup successful but no session created. Please check your email for confirmation."))
                }
            } else {
                Result.failure(Exception("Signup requires email confirmation. Please check your email."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Signup failed: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Log in with email/phone and password
     */
    suspend fun login(
        emailOrPhone: String,
        password: String,
        rememberMe: Boolean // Added parameter
    ): Result<String> {
        return try {
            _isLoading.value = true
            
            val email = if (emailOrPhone.contains("@")) {
                emailOrPhone
            } else {
                "${emailOrPhone}@neobuk.app"
            }
            
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = auth.currentUserOrNull()?.id
            
            if (userId != null) {
                _currentUser.value = auth.currentUserOrNull()
                _isAuthenticated.value = true
                saveRememberMe(rememberMe) // Save preference
                Result.success(userId)
            } else {
                Result.failure(Exception("Login failed: Could not retrieve user ID"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Log out the current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            _currentUser.value = null
            _isAuthenticated.value = false
            // Do NOT clear remember_me pref here, user might want it remembered for next typing... 
            // Wait, "Remember Me" in this context usually means "Session Persistence".
            // If they explicitly logout, we should probably kill the session (done by signOut) 
            // AND maybe clear the 'remember_me' flag? 
            // If they logout, they are logged out. 'Remember Me' is for *session restoration* across app restarts.
            // If they manually logout, restoration should NOT happen.
            // But if they login again, they can check/uncheck it.
            // So logic in checkSession is key.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }
    
    /**
     * Check if there's an existing session
     */
    suspend fun checkSession(): Boolean {
        // First check our preference
        if (!getRememberMe()) {
            // User didn't want to be remembered. 
            // Even if Supabase has a cache, we ignore/clear it.
            if (auth.currentSessionOrNull() != null) {
                try {
                    auth.signOut() // Clear underlying session
                } catch (e: Exception) { /* ignore */ }
            }
            _isAuthenticated.value = false
            return false
        }

        return try {
            val user = auth.currentUserOrNull()
            _currentUser.value = user
            _isAuthenticated.value = user != null
            user != null
        } catch (e: Exception) {
            _isAuthenticated.value = false
            false
        }
    }
    
    /**
     * Refresh the current session
     */
    suspend fun refreshSession(): Result<Unit> {
        return try {
            auth.refreshCurrentSession()
            _currentUser.value = auth.currentUserOrNull()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Session refresh failed: ${e.message}"))
        }
    }
}

