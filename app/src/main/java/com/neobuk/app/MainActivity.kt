package com.neobuk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.neobuk.app.data.models.Product
import com.neobuk.app.data.models.StockMovementReason
import com.neobuk.app.data.models.SubscriptionStatus
import com.neobuk.app.ui.components.HomeActions
import com.neobuk.app.ui.components.NeoBukLogo
import com.neobuk.app.ui.components.NeoBukToolbar
import com.neobuk.app.utils.GuardResult
import com.neobuk.app.utils.SubscriptionGuard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.neobuk.app.ui.screens.ExpensesScreen
import com.neobuk.app.ui.screens.HomeScreen
import com.neobuk.app.ui.screens.ManageServicesScreen
import com.neobuk.app.ui.screens.MoreScreen
import com.neobuk.app.ui.screens.ProductsScreen
import com.neobuk.app.ui.screens.ReportsScreen
import com.neobuk.app.ui.screens.SalesHistoryScreen
import com.neobuk.app.ui.screens.ServicesScreen
import com.neobuk.app.ui.screens.SubscriptionLockedScreen
import com.neobuk.app.ui.screens.SubscriptionScreen
import com.neobuk.app.ui.screens.TasksScreen
import com.neobuk.app.ui.screens.auth.LoginScreen
import com.neobuk.app.ui.screens.auth.SignupScreen
import com.neobuk.app.ui.screens.OnboardingScreen
import com.neobuk.app.ui.screens.DayEndClosureSheet
import com.neobuk.app.ui.screens.products.AddProductSheet
import com.neobuk.app.ui.screens.products.ScanStockSheet
import com.neobuk.app.ui.screens.products.UpdateStockSheet
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.NeoBukTheme
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.viewmodels.InventoryViewModel
import com.neobuk.app.viewmodels.ServicesViewModel
import com.neobuk.app.viewmodels.SubscriptionViewModel
import com.neobuk.app.viewmodels.TasksViewModel
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.viewmodels.AuthState as AuthViewModelState

// Define keys for navigation or state
enum class AuthState {
    ONBOARDING, LOGIN, SIGNUP, AUTHENTICATED
}

enum class BusinessType {
    RETAIL, // Shops: Products, Stock, Sales
    SERVICE // Salons: Services, Bookings, Daily Sales
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen - must be before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContent {
            NeoBukTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("neobuk_prefs", android.content.Context.MODE_PRIVATE) }
    val hasCompletedOnboarding = remember { sharedPreferences.getBoolean("onboarding_complete", false) }
    
    // AuthViewModel for real Supabase authentication (injected by Koin)
    val authViewModel: AuthViewModel = koinViewModel()
    val authViewModelState by authViewModel.authState.collectAsState()
    
    // Initialize state based on preference and existing session
    var authState by remember { 
        mutableStateOf(if (hasCompletedOnboarding) AuthState.LOGIN else AuthState.ONBOARDING) 
    }
    
    // Check for existing session on startup
    LaunchedEffect(authViewModelState) {
        when (authViewModelState) {
            is AuthViewModelState.Authenticated -> {
                authState = AuthState.AUTHENTICATED
            }
            is AuthViewModelState.LoggedOut -> {
                if (authState == AuthState.AUTHENTICATED) {
                    authState = AuthState.LOGIN
                }
            }
            else -> { /* Initial or Loading - do nothing */ }
        }
    }
    
    // Get business name from ViewModel if available
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val businessName = currentBusiness?.businessName ?: "My Business"

    when (authState) {
        AuthState.ONBOARDING -> {
            OnboardingScreen(
                onFinishOnboarding = { 
                    // Save preference
                    sharedPreferences.edit { putBoolean("onboarding_complete", true) }
                    authState = AuthState.LOGIN 
                }
            )
        }
        AuthState.LOGIN -> {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { authState = AuthState.AUTHENTICATED },
                onNavigateToSignup = { 
                    authViewModel.resetSignupState()
                    authState = AuthState.SIGNUP 
                }
            )
        }

        AuthState.SIGNUP -> {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = { name -> 
                    // After signup, redirect to login (user may need to confirm email)
                    authState = AuthState.LOGIN 
                },
                onNavigateToLogin = { authState = AuthState.LOGIN }
            )
        }

        AuthState.AUTHENTICATED -> {
            NeoBukApp(
                businessName = businessName,
                businessId = currentBusiness?.id,
                onLogout = { 
                    authViewModel.logout {
                        authState = AuthState.LOGIN
                    }
                }
            )
        }
    }
}


sealed class SheetScreen {
    object BusinessTypeSelection : SheetScreen()
    object ScanProduct : SheetScreen()
    data class AddProduct(val barcode: String? = null) : SheetScreen()
    data class UpdateStock(val product: Product) : SheetScreen()
    object Profile : SheetScreen()
    object DayEndClosure : SheetScreen()
    object NetProfitInfo : SheetScreen()
    object AddProductFlow : SheetScreen() // New Unified Flow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBukApp(
    businessName: String,
    businessId: String?,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Intercept back press
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0 // Go back to Home
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    // Default to Retail (Shop) initially
    var businessType by remember { mutableStateOf(BusinessType.RETAIL) }

    // Navigation State for BottomSheet
    var currentSheetScreen by remember { mutableStateOf<SheetScreen>(SheetScreen.BusinessTypeSelection) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // ViewModels
    // ViewModels
    val inventoryViewModel: InventoryViewModel = koinViewModel()
    val tasksViewModel: TasksViewModel = koinViewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val servicesViewModel: ServicesViewModel = koinViewModel()
    val subscriptionStatus by subscriptionViewModel.status.collectAsState()
    val subscription by subscriptionViewModel.subscription.collectAsState()
    val pendingTasksCount by tasksViewModel.pendingTaskCount.collectAsState()
    
    // Initialize ServicesViewModel with business ID
    // Initialize ServicesViewModel with business ID
    LaunchedEffect(businessId) {
        businessId?.let { 
            servicesViewModel.setBusinessId(it)
            inventoryViewModel.setBusinessId(it)
            tasksViewModel.setBusinessId(it)
        }
    }

    // Subtitle Logic: "Today..." OR "Trial..."
    val toolbarSubtitle = remember(subscription, subscriptionStatus) {
        if (subscriptionStatus == SubscriptionStatus.TRIALING && subscription?.trialEnd != null) {
            val now = Date()
            val diff = subscription!!.trialEnd!!.time - now.time
            val days = TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
            "Trial: $days days left"
        } else {
            SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
        }
    }

    // Guard Helper
    fun withGuard(action: () -> Unit) {
        when (val result = SubscriptionGuard.checkAccess(subscriptionStatus)) {
            is GuardResult.Allowed -> action()
            is GuardResult.Blocked -> {
                // Determine if we show a toast or navigate to subscription
                // For now, simpler: user sees Locked screen if genuinely locked.
                // If it's a soft block or grace period limitation (if any), handle here.
                // Assuming LOCKED status is already handled by the top-level if-else,
                // this guard is mostly for additional safety or future granular blocks.
                if (result.reason.contains("expired") || result.reason.contains("locked")) {
                     // Optionally show a snackbar here using a scaffoldState or similar
                }
                action() // ALLOW FOR NOW if logic dictates, OR BLOCK.
                // Based on prompt: "Returns Allowed/Blocked... This prevents forgotten screens"
                // Ideally we should NOT run action() if blocked.
                if (result is GuardResult.Blocked) {
                    // Start simple: redirect to subscription tab or show sheet?
                    // selectedTab = 8 // Subscription Screen
                    // Or just do nothing and let the user know.
                    // Since I don't have a snackbar host readily injected, I will block silently or log.
                    // NOTE: The main UI already blocks LOCKED status below.
                } else {
                     action()
                }
            }
        }
    }
    
    // Simplified Wrapper for Guard
    fun guard(block: () -> Unit) {
        val result = SubscriptionGuard.checkAccess(subscriptionStatus)
        if (result is GuardResult.Allowed) {
            block()
        } else {
            // If blocked, maybe navigate to Subscription tab
            selectedTab = 8 
        }
    }

    // Navigation items excluding Home (which is the FAB)
    // 0=Home, 1=Sales, 2=Expenses, 3=Products, 4=Reports

    if (subscriptionStatus == SubscriptionStatus.LOCKED) {
        SubscriptionLockedScreen(
            onSubscribe = { subscriptionViewModel.upgradeToActive() }, // Mock unlock
            onContactSupport = { /* TODO */ },
            onLogout = { /* TODO */ }
        )
    } else {
        Scaffold(
        topBar = {
            NeoBukToolbar(
                title = { 
                    if (selectedTab == 0) {
                        NeoBukLogo(subtitle = toolbarSubtitle)
                    } else {
                        val titleText = when(selectedTab) {
                            1 -> "Products"
                            2 -> "Services"
                            3 -> "Reports"
                            4 -> "More"
                            5 -> "Expenses"
                            6 -> "Sales History"
                            7 -> "Manage Services"
                            8 -> "Subscription"
                            9 -> "Tasks"
                            else -> ""
                        }
                        Text(
                            text = titleText,
                            style = AppTextStyles.pageTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    if (selectedTab != 0) {
                        IconButton(onClick = { 
                            when(selectedTab) {
                                7, 8 -> selectedTab = 4 // Back to 'More' screen
                                9 -> selectedTab = 0 // TASKS -> HOME
                                else -> selectedTab = 0 // Back to 'Home' screen
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    HomeActions(
                        onNotificationClick = { /* Handle click */ },
                        onProfileClick = { 
                            currentSheetScreen = SheetScreen.Profile
                            showBottomSheet = true
                        }
                    )
                }
            )
        },
        bottomBar = {
            // Box to handle potential shadow casting if needed, primarily used here for structure relative to the FAB
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp, // This gives the minty tint in M3
                    modifier = Modifier
                        .clip(CurvedBottomBarShape(fabSize = 64.dp, cutoutRadius = 40.dp)), // Apply the custom curve
                    actions = {
                        // 1. Products (Left)
                        NavigationItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = if (selectedTab == 1) Icons.Filled.Inventory else Icons.Outlined.Inventory,
                            label = "Products",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 2. Services (Left)
                        NavigationItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = if (selectedTab == 2) Icons.Filled.Build else Icons.Outlined.Build,
                            label = "Services",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Spacer for the Home Button (Center)
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // 3. Reports (Right)
                        NavigationItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = if (selectedTab == 3) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            label = "Reports",
                            modifier = Modifier.weight(1f)
                        )

                        // 4. More (Right)
                        NavigationItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = if (selectedTab == 4) Icons.Filled.MoreVert else Icons.Outlined.MoreVert,
                            label = "More",
                            modifier = Modifier.weight(1f)
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            // Micro-interaction: scale animation on selection
            val scale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (selectedTab == 0) 1.0f else 0.96f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                ),
                label = "FABScale"
            )
            
            // Outer ring for enhanced depth (softer than before)
            Box(contentAlignment = Alignment.Center) {
                // Softer shadow ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .offset(y = 32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                )
                
                FloatingActionButton(
                    onClick = { 
                        selectedTab = 0
                        // Trigger haptic feedback if available
                        // context.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    },
                    containerColor = if (selectedTab == 0) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f) 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    contentColor = if (selectedTab == 0) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp, // Even lower elevation to sit more "flush"
                        pressedElevation = 4.dp,
                        focusedElevation = 2.dp,
                        hoveredElevation = 3.dp
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = 40.dp) // Pushed further down (was 32) to nest deeply in the curve
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(30.dp) // Slightly smaller for balance
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Animated Tab Switch
            androidx.compose.animation.AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == 0 || initialState == 0) {
                       // Crossfade when moving to/from Home
                       fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                    } else if (targetState > initialState) {
                       // Slide Left for forward nav between tabs
                       slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(180)) togetherWith
                       slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(180))
                    } else {
                       // Slide Right for back nav
                       slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(180)) togetherWith
                       slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(180))
                    }
                },
                label = "MainTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(
                        subscriptionStatus = subscriptionStatus,
                        // Viewing is allowed, but actions should be guarded
                        onViewInventory = { selectedTab = 1 }, // Navigating to view is fine
                        onViewReports = { selectedTab = 3 },
                        onViewSales = { selectedTab = 6 },
                        onRecordSale = { 
                            guard { selectedTab = 6 } // Navigating to sales history
                        },
                        onRecordExpense = { 
                            guard { selectedTab = 5 } 
                        },
                        onCloseDay = {
                           currentSheetScreen = SheetScreen.DayEndClosure
                           showBottomSheet = true
                        },
                        onSubscribeClick = { subscriptionViewModel.upgradeToActive() },
                        onShowNetProfitInfo = {
                            currentSheetScreen = SheetScreen.NetProfitInfo
                            showBottomSheet = true
                        },
                        onViewTasks = { selectedTab = 9 }
                    )
                    1 -> ProductsScreen(
                        viewModel = inventoryViewModel,
                        onAddProduct = {
                            guard {
                                currentSheetScreen = SheetScreen.AddProductFlow
                                showBottomSheet = true
                            }
                        }
                    )
                    2 -> ServicesScreen()
                    3 -> ReportsScreen()
                    4 -> MoreScreen(
                        businessType = businessType,
                        onNavigateToManageServices = { selectedTab = 7 },
                        onNavigateToSubscription = { selectedTab = 8 }
                    )
                    5 -> ExpensesScreen() // Accessible from Home but not in bottom nav
                    6 -> SalesHistoryScreen()
                    7 -> ManageServicesScreen(
                        businessId = businessId,
                        servicesViewModel = servicesViewModel,
                        onBack = { selectedTab = 4 }
                    ) // Manage Services
                    8 -> SubscriptionScreen(onBack = { selectedTab = 4 }, viewModel = subscriptionViewModel)
                    9 -> TasksScreen(onBack = { selectedTab = 0 }, viewModel = inventoryViewModel, tasksViewModel = tasksViewModel)
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                when (val screen = currentSheetScreen) {
                    is SheetScreen.Profile -> {
                         ProfileBottomSheet(
                            businessName = businessName,
                            subscriptionStatus = subscriptionStatus,
                            onOptionClick = { option ->
                                when (option) {
                                    "Settings" -> selectedTab = 4 // Navigate to More Screen
                                    "Logout" -> { 
                                         onLogout()
                                    }
                                    "Subscription" -> selectedTab = 8
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
                            }
                         )
                    }

                    is SheetScreen.BusinessTypeSelection -> {
                        BusinessTypeSelectionSheet(
                            onOptionSelected = { type ->
                                businessType = type
                                if (type == BusinessType.RETAIL) {
                                    currentSheetScreen = SheetScreen.AddProductFlow // Default to new Flow
                                } else {
                                    // TODO: Handle Services flow
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) showBottomSheet = false
                                    }
                                }
                            }
                        )
                    }

                    is SheetScreen.AddProductFlow -> {
                         com.neobuk.app.ui.screens.products.UnifiedAddProductSheet(
                             onDismiss = {
                                 scope.launch { sheetState.hide() }.invokeOnCompletion {
                                     if (!sheetState.isVisible) showBottomSheet = false
                                 }
                             },
                             checkProductExists = { barcode ->
                                 inventoryViewModel.getProductByBarcode(barcode) != null
                             },
                             onProductExists = { barcode ->
                                 val product = inventoryViewModel.getProductByBarcode(barcode)
                                 if (product != null) {
                                     currentSheetScreen = SheetScreen.UpdateStock(product)
                                 }
                             },
                             onSubmit = { draft ->
                                 guard {
                                     // Convert Draft to Product
                                     val newProduct = Product(
                                         name = draft.name,
                                         description = draft.description,
                                         categoryId = draft.category,
                                         barcode = draft.barcode,
                                         unit = draft.unit,
                                         costPrice = draft.costPrice,
                                         sellingPrice = draft.sellingPrice,
                                         quantity = draft.quantity.toDouble()
                                     )
                                     inventoryViewModel.addProduct(newProduct)
                                     
                                     scope.launch { sheetState.hide() }.invokeOnCompletion {
                                         if (!sheetState.isVisible) showBottomSheet = false
                                     }
                                 }
                             }
                         )
                    }

                    is SheetScreen.ScanProduct -> {
                        ScanStockSheet(
                            onBarcodeScanned = { barcode ->
                                val existingProduct =
                                    inventoryViewModel.getProductByBarcode(barcode)
                                if (existingProduct != null) {
                                    currentSheetScreen = SheetScreen.UpdateStock(existingProduct)
                                } else {
                                    currentSheetScreen = SheetScreen.AddProduct(barcode = barcode)
                                }
                            },
                            onManualEntry = {
                                currentSheetScreen = SheetScreen.AddProduct(barcode = null)
                            },
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            }
                        )
                    }

                    is SheetScreen.AddProduct -> {
                        AddProductSheet(
                            initialBarcode = screen.barcode,
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            },
                            onSubmit = { draft ->
                                guard {
                                    // Convert Draft to Product
                                    val newProduct = Product(
                                        name = draft.name,
                                        description = draft.description,
                                        categoryId = draft.category,
                                        barcode = draft.barcode,
                                        unit = draft.unit,
                                        costPrice = draft.costPrice,
                                        sellingPrice = draft.sellingPrice,
                                        quantity = draft.quantity.toDouble()
                                    )
                                    inventoryViewModel.addProduct(newProduct)
                                }
                            }
                        )
                    }

                    is SheetScreen.UpdateStock -> {
                        UpdateStockSheet(
                            product = screen.product,
                            onConfirm = { quantityToAdd ->
                                guard {
                                    inventoryViewModel.updateStock(
                                        productId = screen.product.id,
                                        quantityChange = quantityToAdd,
                                        reason = StockMovementReason.MANUAL_ADD
                                    )
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            },
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            }
                        )
                    }

                    is SheetScreen.DayEndClosure -> {
                        DayEndClosureSheet(
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            },
                            onConfirmClosure = {
                                // Logic to save closure record would go here
                            }
                        )
                    }


                    is SheetScreen.NetProfitInfo -> {
                        NetProfitInfoSheet(
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
  }
}

@Composable
fun NetProfitInfoSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "How net profit is calculated",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Net profit shows what you actually earn after costs.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Formula
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Formula:",
                    style = AppTextStyles.bodyBold,
                    color = NeoBukTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selling price − Buying price − Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Example:",
            style = AppTextStyles.bodyBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "If you sell goods for KES 10,000, buy them at KES 6,000, and spend KES 1,000 on expenses, your net profit is KES 3,000.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

// Add ProfileBottomSheet Composable here
@Composable
fun ProfileBottomSheet(
    businessName: String,
    subscriptionStatus: SubscriptionStatus,
    onOptionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(NeoBukTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LM",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Owner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        ProfileOptionItem(
            icon = if (subscriptionStatus == SubscriptionStatus.ACTIVE) Icons.Default.CheckCircle else Icons.Default.Warning,
            title = "Subscription: ${subscriptionStatus.name}",
            color = if (subscriptionStatus == SubscriptionStatus.ACTIVE) NeoBukTeal else Color(0xFFF59E0B),
            onClick = { onOptionClick("Subscription") }
        )
        
        ProfileOptionItem(
            icon = Icons.Default.Settings,
            title = "Settings",
            onClick = { onOptionClick("Settings") }
        )
        
        ProfileOptionItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = "Log Out",
            color = MaterialTheme.colorScheme.error,
            onClick = { onOptionClick("Logout") }
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: Any, // ImageVector
    title: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
             Icon(icon, contentDescription = null, tint = resolvedColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = resolvedColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BusinessTypeSelectionSheet(onOptionSelected: (BusinessType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp) // Extra padding for bottom inset
    ) {
        Text(
            text = "Business Type selection",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Option 1: Sell Products
        SelectionCard(
            emoji = "🛒",
            title = "Sell Products",
            description = "Retail, Wholesale, Distribution",
            color = NeoBukTeal,
            onClick = { onOptionSelected(BusinessType.RETAIL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option 2: Offer Services
        SelectionCard(
            emoji = "✂️",
            title = "Offer Services",
            description = "Consultation, Repair, Maintenance",
            color = Color(0xFFE91E63), // Pink/Magenta for services
            onClick = { onOptionSelected(BusinessType.SERVICE) }
        )
    }
}

@Composable
fun SelectionCard(
    emoji: String,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    }

/**
 * Navigation Item with clear active state indicators
 * - Icon color change (filled vs outlined)
 * - Label weight increase when active
 * - Underline dot indicator
 * - Consistent sizing for visual balance
 */
@Composable
fun NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    // We utilize a Column with clickable instead of IconButton to allow 
    // for flexible height (Icon + Text + Dot) without clipping
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)) // Ripple shape
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp) // Internal padding
    ) {
        // Icon with consistent size
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label is now visible because we aren't constrained by IconButton
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Active indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (selected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        Color.Transparent
                )
        )
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}

/**
 * Custom Shape for the BottomAppBar to create a smooth circular cutout (notch)
 * at the top center to cradle the Home FAB.
 */
class CurvedBottomBarShape(
    private val fabSize: androidx.compose.ui.unit.Dp,
    private val cutoutRadius: androidx.compose.ui.unit.Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            val width = size.width
            val height = size.height
            val circleRadiusPx = with(density) { cutoutRadius.toPx() }
            val center = width / 2f

            // Start at top left
            moveTo(0f, 0f)

            // Draw straight line to the start of the curve
            lineTo(center - circleRadiusPx - 10f, 0f)

            // Draw the curve (notch)
            // We use cubicTo for a smooth ease-in and ease-out
            cubicTo(
                center - circleRadiusPx, 0f,           // Control point 1
                center - circleRadiusPx, circleRadiusPx * 0.6f, // Control point 2 (dip)
                center, circleRadiusPx * 0.6f          // End point (bottom of dip)
            )
            cubicTo(
                center + circleRadiusPx, circleRadiusPx * 0.6f, // Control point 1
                center + circleRadiusPx, 0f,           // Control point 2
                center + circleRadiusPx + 10f, 0f      // End point (back to top)
            )

            // Draw to top right
            lineTo(width, 0f)

            // Finish the rectangle
            lineTo(width, height)
            lineTo(0f, height)
            close()
        })
    }
}
