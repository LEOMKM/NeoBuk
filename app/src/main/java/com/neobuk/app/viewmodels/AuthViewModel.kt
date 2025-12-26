package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.models.BusinessCategory
import com.neobuk.app.data.models.PlanType
import com.neobuk.app.data.models.SubscriptionStatus
import com.neobuk.app.data.repositories.AuthRepository
import com.neobuk.app.data.repositories.Business
import com.neobuk.app.data.repositories.BusinessRepository
import com.neobuk.app.data.repositories.SubscriptionInfo
import com.neobuk.app.data.repositories.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Authentication State
 */
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(
        val userId: String,
        val business: Business?,
        val subscription: SubscriptionInfo?
    ) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

/**
 * Signup State (for multi-step wizard)
 */
sealed class SignupState {
    object Initial : SignupState()
    object Loading : SignupState()
    data class AccountCreated(val userId: String) : SignupState()
    data class BusinessCreated(val business: Business) : SignupState()
    data class SubscriptionCreated(val subscription: SubscriptionInfo) : SignupState()
    data class Complete(
        val userId: String,
        val business: Business,
        val subscription: SubscriptionInfo
    ) : SignupState()
    data class Error(val message: String, val step: String) : SignupState()
}

/**
 * AuthViewModel
 * Manages authentication state and coordinates signup/login flows
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Signup state (for tracking progress in wizard)
    private val _signupState = MutableStateFlow<SignupState>(SignupState.Initial)
    val signupState: StateFlow<SignupState> = _signupState.asStateFlow()
    
    // Subscription status (for UI to show banners, lock screens, etc.)
    val subscriptionStatus: StateFlow<SubscriptionStatus> = subscriptionRepository.effectiveStatus
    
    // Current business
    val currentBusiness: StateFlow<Business?> = businessRepository.currentBusiness
    
    // Current subscription
    val currentSubscription: StateFlow<SubscriptionInfo?> = subscriptionRepository.currentSubscription
    
    // Check if actions are allowed (subscription not locked)
    fun canPerformActions(): Boolean = subscriptionRepository.canPerformActions()
    
    // Get trial days remaining
    fun getTrialDaysRemaining(): Int = subscriptionRepository.getTrialDaysRemaining()
    
    init {
        // Check for existing session on startup
        checkExistingSession()
    }
    
    /**
     * Check if user has an existing session
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val hasSession = authRepository.checkSession()
                
                if (hasSession) {
                    val userId = authRepository.currentUserId
                    if (userId != null) {
                        // Fetch business and subscription
                        val business = businessRepository.fetchBusinessByOwner(userId)
                        val subscription = business?.let { 
                            subscriptionRepository.fetchSubscriptionByBusiness(it.id) 
                        }
                        
                        _authState.value = AuthState.Authenticated(
                            userId = userId,
                            business = business,
                            subscription = subscription
                        )
                    } else {
                        _authState.value = AuthState.LoggedOut
                    }
                } else {
                    _authState.value = AuthState.LoggedOut
                }
            } catch (e: Exception) {
                // If session check fails, just show logged out state
                _authState.value = AuthState.LoggedOut
            }
        }
    }
    
    /**
     * Login with email/phone and password
     */
    fun login(
        emailOrPhone: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.login(emailOrPhone, password)
            
            result.fold(
                onSuccess = { userId ->
                    // Fetch business and subscription
                    val business = businessRepository.fetchBusinessByOwner(userId)
                    val subscription = business?.let { 
                        subscriptionRepository.fetchSubscriptionByBusiness(it.id) 
                    }
                    
                    _authState.value = AuthState.Authenticated(
                        userId = userId,
                        business = business,
                        subscription = subscription
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                    onError(error.message ?: "Login failed")
                }
            )
        }
    }
    
    /**
     * Signup Step 1: Create account
     */
    fun signupCreateAccount(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            
            val result = authRepository.signUp(
                email = email,
                password = password,
                fullName = fullName,
                phone = phone
            )
            
            result.fold(
                onSuccess = { userId ->
                    _signupState.value = SignupState.AccountCreated(userId)
                    onSuccess()
                },
                onFailure = { error ->
                    _signupState.value = SignupState.Error(
                        message = error.message ?: "Account creation failed",
                        step = "account"
                    )
                    onError(error.message ?: "Account creation failed")
                }
            )
        }
    }
    
    /**
     * Signup Step 2: Create business
     */
    fun signupCreateBusiness(
        businessName: String,
        category: BusinessCategory,
        subtype: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId
            
            if (userId == null) {
                _signupState.value = SignupState.Error(
                    message = "User not authenticated",
                    step = "business"
                )
                onError("User not authenticated")
                return@launch
            }
            
            _signupState.value = SignupState.Loading
            
            val result = businessRepository.createBusiness(
                ownerUserId = userId,
                businessName = businessName,
                category = category,
                subtype = subtype
            )
            
            result.fold(
                onSuccess = { business ->
                    _signupState.value = SignupState.BusinessCreated(business)
                    onSuccess()
                },
                onFailure = { error ->
                    _signupState.value = SignupState.Error(
                        message = error.message ?: "Business creation failed",
                        step = "business"
                    )
                    onError(error.message ?: "Business creation failed")
                }
            )
        }
    }
    
    /**
     * Signup Step 3: Create subscription
     */
    fun signupCreateSubscription(
        planType: PlanType,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val business = businessRepository.currentBusiness.value
            
            if (business == null) {
                _signupState.value = SignupState.Error(
                    message = "Business not found",
                    step = "subscription"
                )
                onError("Business not found")
                return@launch
            }
            
            _signupState.value = SignupState.Loading
            
            val result = subscriptionRepository.createSubscription(
                businessId = business.id,
                planType = planType
            )
            
            result.fold(
                onSuccess = { subscription ->
                    val userId = authRepository.currentUserId ?: ""
                    
                    _signupState.value = SignupState.Complete(
                        userId = userId,
                        business = business,
                        subscription = subscription
                    )
                    
                    // Logout so user is taken to login screen
                    authRepository.logout()
                    _authState.value = AuthState.LoggedOut
                    
                    onSuccess()
                },
                onFailure = { error ->
                    _signupState.value = SignupState.Error(
                        message = error.message ?: "Subscription creation failed",
                        step = "subscription"
                    )
                    onError(error.message ?: "Subscription creation failed")
                }
            )
        }
    }
    
    /**
     * Complete signup - creates account, business, and subscription in one flow
     * This is the preferred method as it collects all data first before committing
     */
    fun signupComplete(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        businessName: String,
        category: BusinessCategory,
        subtype: String,
        planType: PlanType,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            
            // Step 1: Create account
            val authResult = authRepository.signUp(
                email = email,
                password = password,
                fullName = fullName,
                phone = phone
            )
            
            authResult.fold(
                onSuccess = { userId ->
                    // Step 2: Create business
                    val businessResult = businessRepository.createBusiness(
                        ownerUserId = userId,
                        businessName = businessName,
                        category = category,
                        subtype = subtype
                    )
                    
                    businessResult.fold(
                        onSuccess = { business ->
                            // Step 3: Create subscription
                            val subscriptionResult = subscriptionRepository.createSubscription(
                                businessId = business.id,
                                planType = planType
                            )
                            
                            subscriptionResult.fold(
                                onSuccess = { subscription ->
                                    _signupState.value = SignupState.Complete(
                                        userId = userId,
                                        business = business,
                                        subscription = subscription
                                    )
                                    
                                    // Logout so user is taken to login screen
                                    authRepository.logout()
                                    _authState.value = AuthState.LoggedOut
                                    
                                    onSuccess()
                                },
                                onFailure = { error ->
                                    _signupState.value = SignupState.Error(
                                        message = error.message ?: "Subscription creation failed",
                                        step = "subscription"
                                    )
                                    onError(error.message ?: "Subscription creation failed")
                                }
                            )
                        },
                        onFailure = { error ->
                            _signupState.value = SignupState.Error(
                                message = error.message ?: "Business creation failed",
                                step = "business"
                            )
                            onError(error.message ?: "Business creation failed")
                        }
                    )
                },
                onFailure = { error ->
                    _signupState.value = SignupState.Error(
                        message = error.message ?: "Account creation failed",
                        step = "account"
                    )
                    onError(error.message ?: "Account creation failed")
                }
            )
        }
    }
    
    /**
     * Logout
     */
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            businessRepository.clearCurrentBusiness()
            subscriptionRepository.clearCurrentSubscription()
            
            _authState.value = AuthState.LoggedOut
            _signupState.value = SignupState.Initial
            
            onComplete()
        }
    }
    
    /**
     * Reset signup state (for retry)
     */
    fun resetSignupState() {
        _signupState.value = SignupState.Initial
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
        if (_signupState.value is SignupState.Error) {
            _signupState.value = SignupState.Initial
        }
    }
}
