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
class AuthRepository(private val supabaseClient: SupabaseClient) {
    
    private val auth = supabaseClient.auth
    
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
     * Sign up a new user with email and password
     * 
     * @param email User's email (can be empty, phone will be used as identifier)
     * @param password Password
     * @param fullName User's full name
     * @param phone User's phone number
     * @return Result with user ID on success, or exception on failure
     */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Result<String> {
        return try {
            _isLoading.value = true
            
            // Use email if provided, otherwise generate from phone
            val authEmail = email.ifBlank { "${phone}@neobuk.app" }
            
            // Sign up with Supabase Auth - this returns the user info
            val result = auth.signUpWith(Email) {
                this.email = authEmail
                this.password = password
                // Pass metadata to be used by trigger to create user profile
                this.data = buildJsonObject {
                    put("full_name", fullName)
                    put("phone", phone)
                }
            }
            
            // The result contains user info - extract the ID
            // In Supabase SDK v3.x, signUpWith returns the user directly if auto-confirm is enabled
            // or returns with identities if email confirmation is required
            val userId = result?.id ?: auth.currentUserOrNull()?.id
            
            if (userId != null) {
                // Check if we have an active session
                if (auth.currentSessionOrNull() == null) {
                    try {
                        // Attempt immediate login
                        auth.signInWith(Email) {
                            this.email = authEmail
                            this.password = password
                        }
                    } catch (e: Exception) {
                        // If login fails, force specific error
                         return Result.failure(Exception("Signup successful but login failed. If 'Confirm Email' is enabled in Supabase, please verify your email first."))
                    }
                }
                
                // Double check session
                if (auth.currentSessionOrNull() != null) {
                    _currentUser.value = auth.currentUserOrNull()
                    _isAuthenticated.value = true
                    Result.success(userId)
                } else {
                     Result.failure(Exception("Signup successful but no session created. Please check your email for confirmation."))
                }
            } else {
                // This might happen if email confirmation is required and no user object returned
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
     * 
     * @param emailOrPhone Email or phone number
     * @param password Password
     * @return Result with user ID on success, or exception on failure
     */
    suspend fun login(
        emailOrPhone: String,
        password: String
    ): Result<String> {
        return try {
            _isLoading.value = true
            
            // Determine if input is email or phone
            val email = if (emailOrPhone.contains("@")) {
                emailOrPhone
            } else {
                // Phone number - use our generated email format
                "${emailOrPhone}@neobuk.app"
            }
            
            // Sign in with Supabase Auth
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Get the user ID
            val userId = auth.currentUserOrNull()?.id
            
            if (userId != null) {
                _currentUser.value = auth.currentUserOrNull()
                _isAuthenticated.value = true
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }
    
    /**
     * Check if there's an existing session
     * Call this on app startup to restore session
     */
    suspend fun checkSession(): Boolean {
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

