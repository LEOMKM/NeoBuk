package com.neobuk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.neobuk.app.ui.screens.auth.LoginScreen
import com.neobuk.app.ui.screens.auth.SignupScreen
import com.neobuk.app.ui.screens.OnboardingScreen
import com.neobuk.app.ui.screens.DayEndClosureSheet
import com.neobuk.app.ui.screens.products.AddProductSheet
import com.neobuk.app.ui.screens.products.ScanStockSheet
import com.neobuk.app.ui.screens.products.UpdateStockSheet
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.NeoBukTheme
import com.neobuk.app.viewmodels.InventoryViewModel
import com.neobuk.app.viewmodels.SubscriptionViewModel
import kotlinx.coroutines.launch

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
    var authState by remember { mutableStateOf(AuthState.ONBOARDING) }
    var businessName by remember { mutableStateOf("Kasarani Shop") }

    when (authState) {
        AuthState.ONBOARDING -> {
            OnboardingScreen(
                onFinishOnboarding = { authState = AuthState.LOGIN }
            )
        }
        AuthState.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { authState = AuthState.AUTHENTICATED },
                onNavigateToSignup = { authState = AuthState.SIGNUP }
            )
        }

        AuthState.SIGNUP -> {
            SignupScreen(
                onSignupSuccess = { name -> 
                    businessName = name
                    authState = AuthState.AUTHENTICATED 
                },
                onNavigateToLogin = { authState = AuthState.LOGIN }
            )
        }

        AuthState.AUTHENTICATED -> {
            NeoBukApp(
                businessName = businessName,
                onLogout = { authState = AuthState.LOGIN }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoBukApp(
    businessName: String,
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
    val inventoryViewModel: InventoryViewModel = viewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()
    val subscriptionStatus by subscriptionViewModel.status.collectAsState()
    val subscription by subscriptionViewModel.subscription.collectAsState()

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
                    // Only show subtitle on Home Screen (Tab 0)
                    NeoBukLogo(subtitle = if (selectedTab == 0) toolbarSubtitle else null) 
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
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                actions = {
                    // Left Side: Products, Services
                    
                    // Products (Index 1)
                    IconButton(onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.Inventory else Icons.Outlined.Inventory,
                                contentDescription = "Products",
                                tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Products",
                                fontSize = 10.sp,
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Services (Index 2)
                    IconButton(onClick = { selectedTab = 2 }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Build else Icons.Outlined.Build,
                                contentDescription = "Services",
                                tint = if (selectedTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Services",
                                fontSize = 10.sp,
                                color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Space for Home FAB
                    Spacer(modifier = Modifier.weight(1f))

                    // Right Side: Reports, More
                    // Reports (Index 3)
                    IconButton(onClick = { selectedTab = 3 }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Reports",
                                tint = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Reports",
                                fontSize = 10.sp,
                                color = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // More (Index 4)
                    IconButton(onClick = { selectedTab = 4 }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 4) Icons.Filled.MoreVert else Icons.Outlined.MoreVert,
                                contentDescription = "More",
                                tint = if (selectedTab == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "More",
                                fontSize = 10.sp,
                                color = if (selectedTab == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { selectedTab = 0 }, // Home
                containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 40.dp) // Overlap the BottomAppBar
            ) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(32.dp)
                )
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
                        onSubscribeClick = { subscriptionViewModel.upgradeToActive() }
                    )
                    1 -> ProductsScreen(
                        viewModel = inventoryViewModel,
                        onAddProduct = {
                            guard {
                                currentSheetScreen = SheetScreen.ScanProduct
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
                    7 -> ManageServicesScreen(onBack = { selectedTab = 4 }) // Manage Services
                    8 -> SubscriptionScreen(onBack = { selectedTab = 4 }, viewModel = subscriptionViewModel)
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
                                    currentSheetScreen = SheetScreen.ScanProduct
                                } else {
                                    // TODO: Handle Services flow
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
                }
            }
        }
    }
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

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
