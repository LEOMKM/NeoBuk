package com.neobuk.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.data.models.BusinessCategory
import com.neobuk.app.data.models.PlanType
import com.neobuk.app.ui.components.NeoBukLogoLarge
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.ui.theme.NeoBukTeal
import com.neobuk.app.ui.theme.Tokens
import java.util.Date

enum class SignupStep {
    ACCOUNT_BASICS,
    BUSINESS_SETUP,
    SUBSCRIPTION_PLAN,
    PAYMENT,
    SUCCESS
}

// UI State holder for the wizard
data class SignupFormData(
    // Step 1
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,

    // Step 2
    val businessName: String = "",
    val businessCategory: BusinessCategory? = null,
    val businessSubtype: String = "",

    // Step 3
    val selectedPlan: PlanType? = null,

    // Step 4
    val paymentProvider: String? = null // "M-PESA", etc.
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignupScreen(
    onSignupSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var step by remember { mutableStateOf(SignupStep.ACCOUNT_BASICS) }
    var formData by remember { mutableStateOf(SignupFormData()) }
    var isProcessing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Tokens.HorizontalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header with Back Button (except on Step 1 and Success)
            Box(modifier = Modifier.fillMaxWidth()) {
                if (step != SignupStep.ACCOUNT_BASICS && step != SignupStep.SUCCESS) {
                    IconButton(
                        onClick = {
                            // Go back logic
                            step = when (step) {
                                SignupStep.BUSINESS_SETUP -> SignupStep.ACCOUNT_BASICS
                                SignupStep.SUBSCRIPTION_PLAN -> SignupStep.BUSINESS_SETUP
                                SignupStep.PAYMENT -> SignupStep.SUBSCRIPTION_PLAN
                                else -> SignupStep.ACCOUNT_BASICS
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                // Progress Indicator (Visual Bar)
                if (step != SignupStep.SUCCESS) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalSteps = 4
                        repeat(totalSteps) { index ->
                            val isActive = index <= step.ordinal
                            val isCurrent = index == step.ordinal
                            val color = if (isActive) NeoBukTeal else MaterialTheme.colorScheme.outlineVariant
                            Box(
                                modifier = Modifier
                                    .width(if (isCurrent) 24.dp else 12.dp) // highlight current step
                                    .fillMaxHeight()
                                    .background(color, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (step != SignupStep.SUCCESS) {
                NeoBukLogoLarge()
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        // Forward: Slide in from Right, Out to Left
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                         // Backward: Slide in from Left, Out to Right
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "SignupWizard"
            ) { currentStep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Tokens.CardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (currentStep) {
                            SignupStep.ACCOUNT_BASICS -> AccountBasicsStep(
                                formData = formData,
                                onUpdate = { formData = it },
                                onNext = { step = SignupStep.BUSINESS_SETUP },
                                onLoginClick = onNavigateToLogin
                            )
                            SignupStep.BUSINESS_SETUP -> BusinessSetupStep(
                                formData = formData,
                                onUpdate = { formData = it },
                                onNext = { step = SignupStep.SUBSCRIPTION_PLAN }
                            )
                            SignupStep.SUBSCRIPTION_PLAN -> SubscriptionPlanStep(
                                formData = formData,
                                onUpdate = { formData = it },
                                onNext = {
                                    if (formData.selectedPlan == PlanType.FREE_TRIAL) {
                                        // Skip payment for free trial -> Success
                                        // Here we would typically hit the API to create subscription
                                        step = SignupStep.SUCCESS
                                    } else {
                                        step = SignupStep.PAYMENT
                                    }
                                }
                            )
                            SignupStep.PAYMENT -> PaymentStep(
                                formData = formData,
                                onUpdate = { formData = it },
                                onPaymentSuccess = {
                                    step = SignupStep.SUCCESS
                                }
                            )
                            SignupStep.SUCCESS -> SuccessStep(
                                onContinue = { onSignupSuccess(formData.businessName) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ----------------------------------------------------------------------------------
// STEP 1: ACCOUNT BASICS
// ----------------------------------------------------------------------------------
@Composable
fun AccountBasicsStep(
    formData: SignupFormData,
    onUpdate: (SignupFormData) -> Unit,
    onNext: () -> Unit,
    onLoginClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isValid = formData.fullName.isNotBlank() && 
                  formData.phoneNumber.isNotBlank() && 
                  formData.password.isNotBlank() && 
                  formData.password == formData.confirmPassword &&
                  formData.termsAccepted

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Account Basics", style = AppTextStyles.pageTitle, color = MaterialTheme.colorScheme.onSurface)
        Text("Create your owner account", style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        SignupInput(
            label = "Full Name",
            value = formData.fullName,
            onValueChange = { onUpdate(formData.copy(fullName = it)) },
            placeholder = "e.g. John Doe"
        )
        Spacer(modifier = Modifier.height(16.dp))

        SignupInput(
            label = "Phone Number",
            value = formData.phoneNumber,
            onValueChange = { onUpdate(formData.copy(phoneNumber = it)) },
            placeholder = "e.g. 0712345678",
            keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(16.dp))

        SignupInput(
            label = "Email Address (Optional)",
            value = formData.email,
            onValueChange = { onUpdate(formData.copy(email = it)) },
            placeholder = "e.g. john@example.com",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password
        PasswordInput(
            label = "Password",
            value = formData.password,
            onValueChange = { onUpdate(formData.copy(password = it)) },
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )
        Spacer(modifier = Modifier.height(16.dp))

        PasswordInput(
            label = "Confirm Password",
            value = formData.confirmPassword,
            onValueChange = { onUpdate(formData.copy(confirmPassword = it)) },
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            isError = formData.confirmPassword.isNotEmpty() && formData.password != formData.confirmPassword
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Terms
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.fillMaxWidth().clickable { onUpdate(formData.copy(termsAccepted = !formData.termsAccepted)) }
        ) {
            Checkbox(
                checked = formData.termsAccepted,
                onCheckedChange = { onUpdate(formData.copy(termsAccepted = it)) },
                colors = CheckboxDefaults.colors(checkedColor = NeoBukTeal)
            )
            Text(
                "I accept the Terms & Conditions",
                style = AppTextStyles.body,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = isValid
        ) {
            Text("Next", style = AppTextStyles.buttonLarge)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", style = AppTextStyles.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Sign In", 
                style = AppTextStyles.bodyBold, 
                color = NeoBukTeal, 
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// STEP 2: BUSINESS SETUP
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusinessSetupStep(
    formData: SignupFormData,
    onUpdate: (SignupFormData) -> Unit,
    onNext: () -> Unit
) {
    val isValid = formData.businessName.isNotBlank() && formData.businessCategory != null && formData.businessSubtype.isNotBlank()
    
    val serviceTypes = listOf("Salon", "Kinyozi", "Barbershop", "Spa", "Cleaning", "Laundry", "Consultancy")
    val productTypes = listOf("Retail Shop", "Wholesale", "Hardware", "Chemist/Pharmacy", "Electronics", "Groceries")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Business Setup", style = AppTextStyles.pageTitle)
        Text("Tell us about your business", style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        SignupInput(
            label = "Business Name",
            value = formData.businessName,
            onValueChange = { onUpdate(formData.copy(businessName = it)) },
            placeholder = "e.g. Mama Njeri Salon"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("What kind of business is it?", style = AppTextStyles.bodyBold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Services Option
            SelectionCardSmall(
                title = "Services",
                icon = "✂️",
                selected = formData.businessCategory == BusinessCategory.SERVICES,
                onClick = { onUpdate(formData.copy(businessCategory = BusinessCategory.SERVICES, businessSubtype = "")) },
                modifier = Modifier.weight(1f)
            )
            // Products Option
            SelectionCardSmall(
                title = "Products",
                icon = "🛒",
                selected = formData.businessCategory == BusinessCategory.PRODUCTS,
                onClick = { onUpdate(formData.copy(businessCategory = BusinessCategory.PRODUCTS, businessSubtype = "")) },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (formData.businessCategory != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Select Category", 
                style = AppTextStyles.bodyBold, 
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtypes Chips
            val subtypes = if (formData.businessCategory == BusinessCategory.SERVICES) serviceTypes else productTypes
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                subtypes.forEach { type ->
                    val isSelected = formData.businessSubtype == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdate(formData.copy(businessSubtype = type)) },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeoBukTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                // Custom handling could be added
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = isValid
        ) {
            Text("Next", style = AppTextStyles.buttonLarge)
        }
    }
}

@Composable
fun SelectionCardSmall(
    title: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NeoBukTeal.copy(alpha = 0.1f) else Color(0xFFF8FAFC)
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, NeoBukTeal) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = AppTextStyles.bodyBold)
        }
    }
}

// ----------------------------------------------------------------------------------
// STEP 3: SUBSCRIPTION PLAN
// ----------------------------------------------------------------------------------
@Composable
fun SubscriptionPlanStep(
    formData: SignupFormData,
    onUpdate: (SignupFormData) -> Unit,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Choose a Plan", style = AppTextStyles.pageTitle)
        Text("Fair pricing for your business", style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Free Trial
        PlanCard(
            title = "1 Month Free Trial",
            price = "KES 0",
            subtitle = "No charge today.",
            selected = formData.selectedPlan == PlanType.FREE_TRIAL,
            onClick = { onUpdate(formData.copy(selectedPlan = PlanType.FREE_TRIAL)) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Monthly
        PlanCard(
            title = "Monthly Plan",
            price = "KES 249",
            subtitle = "Billed monthly",
            selected = formData.selectedPlan == PlanType.MONTHLY,
            onClick = { onUpdate(formData.copy(selectedPlan = PlanType.MONTHLY)) }
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        // Yearly
        PlanCard(
            title = "Yearly Plan",
            price = "KES 2,490",
            subtitle = "Save 2 months (KES 207/mo)",
            selected = formData.selectedPlan == PlanType.YEARLY,
            badge = "BEST VALUE",
            onClick = { onUpdate(formData.copy(selectedPlan = PlanType.YEARLY)) }
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal),
            enabled = formData.selectedPlan != null
        ) {
            val buttonText = if (formData.selectedPlan == PlanType.FREE_TRIAL) "Start Free Trial" else "Continue to Payment"
            Text(buttonText, style = AppTextStyles.buttonLarge)
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NeoBukTeal.copy(alpha = 0.05f) else Color.White
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, NeoBukTeal) 
                 else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = NeoBukTeal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (badge != null) {
                    Text(
                        text = badge,
                        style = AppTextStyles.caption,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(title, style = AppTextStyles.bodyBold)
                Text(subtitle, style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(price, style = AppTextStyles.pageTitle, fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------------------------------------
// STEP 4: PAYMENT
// ----------------------------------------------------------------------------------
@Composable
fun PaymentStep(
    formData: SignupFormData,
    onUpdate: (SignupFormData) -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    
    // Simulate amount
    val amount = if (formData.selectedPlan == PlanType.YEARLY) "KES 2,490" else "KES 249"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Complete Payment", style = AppTextStyles.pageTitle)
        Text("Secure payment for $amount", style = AppTextStyles.secondary, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Info about prompt
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoBukTeal.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            PaddingValues(8.dp)
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = NeoBukTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prompt will be sent to ${formData.phoneNumber}",
                    style = AppTextStyles.caption,
                    color = Tokens.TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Select Payment Method", style = AppTextStyles.bodyBold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))

        // Payment Methods List
        PaymentMethodButton("M-PESA", "Pay with M-PESA", Color(0xFF4CAF50)) {
            isProcessing = true
            // Mock delay
            java.util.Timer().schedule(object : java.util.TimerTask() {
                override fun run() {
                    onPaymentSuccess()
                }
            }, 2000)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PaymentMethodButton("Paystack", "Pay with Card", Color(0xFF1E88E5)) {
            // Mock
            isProcessing = true
             java.util.Timer().schedule(object : java.util.TimerTask() {
                override fun run() {
                    onPaymentSuccess()
                }
            }, 2000)
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = NeoBukTeal)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Processing Payment...", style = AppTextStyles.secondary)
        }
    }
}

@Composable
fun PaymentMethodButton(name: String, text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // In real app, put logo here
            Text(name, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text)
        }
    }
}

// ----------------------------------------------------------------------------------
// STEP 5: SUCCESS
// ----------------------------------------------------------------------------------
@Composable
fun SuccessStep(onContinue: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = NeoBukTeal,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("You're all set!", style = AppTextStyles.pageTitle)
        Text(
            "Your business account has been created successfully.",
            style = AppTextStyles.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeoBukTeal)
        ) {
            Text("Go to Home", style = AppTextStyles.buttonLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

// ----------------------------------------------------------------------------------
// SHARED COMPONENTS
// ----------------------------------------------------------------------------------

@Composable
fun SignupInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = AppTextStyles.bodyBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Tokens.InputRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeoBukTeal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = NeoBukTeal
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
        )
    }
}

@Composable
fun PasswordInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
         Text(
            text = label,
            style = AppTextStyles.bodyBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Tokens.InputRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color.Red else NeoBukTeal,
                unfocusedBorderColor = if (isError) Color.Red else MaterialTheme.colorScheme.outline,
                cursorColor = NeoBukTeal
            ),
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            isError = isError
        )
        if (isError) {
             Text("Passwords do not match", style = AppTextStyles.caption, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
