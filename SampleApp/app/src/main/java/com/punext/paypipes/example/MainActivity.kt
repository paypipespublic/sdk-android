package com.punext.paypipes.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punext.paypipes.PayPipesUI
import com.punext.paypipes.model.*
import com.punext.paypipes.theme.Theme
import com.punext.paypipes.theme.ThemeColors
import com.punext.paypipes.theme.ThemeFonts
import com.punext.paypipes.theme.ThemeSizes
import com.punext.paypipes.theme.ThemeMode
import com.punext.paypipes.ui.cardpayment.CardTransactionActivity
import java.math.BigDecimal
import com.punext.paypipes.PayPipes
import com.punext.paypipes.Version

/**
 * Example activity demonstrating PayPipes SDK integration.
 * 
 * This example demonstrates:
 * - ViewModel pattern for state management
 * - Proper error handling with user-friendly messages
 * - String resources for localization
 * - Snackbar for user feedback
 * - Accessibility support
 * - Theme customization
 * - Billing address configuration
 */
class MainActivity : ComponentActivity() {

    private val cardTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Always try to extract result first, as errors may be returned with different result codes
        val transactionResult = CardTransactionActivity.extractResult(result.data)
        
        if (transactionResult != null) {
            // We have a result (success or failure), handle it
            handleTransactionResult(transactionResult)
        } else {
            // No result in intent data, check result code
            when (result.resultCode) {
                RESULT_OK -> {
                    // RESULT_OK but no result data - this shouldn't happen, but handle gracefully
                    showToast(getString(R.string.no_result_received))
                }
                RESULT_CANCELED -> {
                    // User cancelled without a result
                    showToast(getString(R.string.transaction_cancelled))
                }
                else -> {
                    // Unknown result code
                    showToast(getString(R.string.no_result_received))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModelInstance: ExampleViewModel = viewModel()
                    
                    Scaffold { paddingValues ->
                        ExampleAppContent(
                            viewModel = viewModelInstance,
                            modifier = Modifier.padding(paddingValues),
                            onTestPayment = { startCardPayment(viewModelInstance) },
                            onTestCardStorage = { startCardStorage(viewModelInstance) },
                            onCustomThemeChanged = { enabled ->
                                viewModelInstance.updateCustomThemeEnabled(enabled)
                                if (enabled) {
                                    applyCustomTheme()
                                    showToast(getString(R.string.custom_theme_applied))
                                } else {
                                    resetTheme()
                                    showToast(getString(R.string.theme_reset))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startCardPayment(viewModel: ExampleViewModel) {
        try {
            val configuration = createConfiguration()
            val transaction = createPaymentTransaction(viewModel)
            PayPipes.initialize(configuration)
            val intent = PayPipesUI.buildCardTransactionIntent(
                context = this,
                configuration = configuration,
                transaction = transaction
            )
            cardTransactionLauncher.launch(intent)
        } catch (e: Exception) {
            showToast(getString(R.string.failed_to_start_payment, e.message ?: "Unknown error"))
        }
    }

    private fun startCardStorage(viewModel: ExampleViewModel) {
        try {
            val configuration = createConfiguration()
            val transaction = createCardStorageTransaction(viewModel)
            PayPipes.initialize(configuration)
            val intent = PayPipesUI.buildCardTransactionIntent(
                context = this,
                configuration = configuration,
                transaction = transaction
            )
            cardTransactionLauncher.launch(intent)
        } catch (e: Exception) {
            showToast(getString(R.string.failed_to_start_storage, e.message ?: "Unknown error"))
        }
    }

    private fun applyCustomTheme() {
        // Comprehensive custom theme demonstrating ALL available theme properties
        val customTheme = Theme(
            colors = ThemeColors(
                // Screen colors
                screenBackgroundColor = Color(0xFFF5F5F5), // Light gray background
                sectionTitleColor = Color(0xFF1A237E), // Deep indigo for section titles
                
                // Input field colors
                inputTitleColor = Color(0xFF424242), // Dark gray for input labels
                inputTextColor = Color(0xFF000000), // Black for input text
                inputErrorColor = Color(0xFFD32F2F), // Red for error messages
                inputFocusedBorderColor = Color(0xFF3F51B5), // Indigo for focused borders
                inputNormalBorderColor = Color(0xFFBDBDBD), // Light gray for normal borders
                inputErrorBorderColor = Color(0xFFD32F2F), // Red for error borders
                
                // Button colors
                buttonBackgroundColor = Color(0xFF3F51B5), // Indigo button background
                buttonTitleColor = Color(0xFFFFFFFF), // White button text
                
                // Checkbox colors
                checkboxTitleColor = Color(0xFF212121), // Almost black for checkbox labels
                checkboxCheckedColor = Color(0xFF3F51B5), // Indigo for checked state
                checkboxUnCheckedColor = Color(0xFF757575) // Medium gray for unchecked state
            ),
            fonts = ThemeFonts(
                sectionTitleFont = FontWeight.Bold, // Bold section titles
                inputTitleFont = FontWeight.Medium, // Medium weight for input labels
                inputTextFont = FontWeight.Normal, // Normal weight for input text
                inputErrorFont = FontWeight.Normal, // Normal weight for error messages
                buttonTitleFont = FontWeight.Bold, // Bold button text
                checkboxTitleFont = FontWeight.Normal // Normal weight for checkbox labels
            ),
            sizes = ThemeSizes(
                paddingsXXS = 4.dp, // Extra extra small padding
                paddingsXS = 8.dp, // Extra small padding
                paddingsS = 12.dp, // Small padding
                paddingsM = 20.dp, // Medium padding (increased from default 16)
                paddingsL = 40.dp, // Large padding (increased from default 32)
                cornersS = 8.dp, // Small corner radius
                cornersM = 16.dp, // Medium corner radius (increased from default 12)
                cornersXL = 28.dp // Extra large corner radius (increased from default 24)
            ),
            mode = ThemeMode.Auto, // Auto mode follows system dark/light setting
            darkColors = ThemeColors(
                // Dark mode colors - demonstrating all properties for dark theme
                screenBackgroundColor = Color(0xFF121212), // Dark background
                sectionTitleColor = Color(0xFF7986CB), // Light indigo for section titles
                inputTitleColor = Color(0xFFB0BEC5), // Light blue-gray for input labels
                inputTextColor = Color(0xFFFFFFFF), // White for input text
                inputErrorColor = Color(0xFFEF5350), // Light red for errors
                inputFocusedBorderColor = Color(0xFF7986CB), // Light indigo for focused borders
                inputNormalBorderColor = Color(0xFF424242), // Dark gray for normal borders
                inputErrorBorderColor = Color(0xFFEF5350), // Light red for error borders
                buttonBackgroundColor = Color(0xFF5C6BC0), // Medium indigo button
                buttonTitleColor = Color(0xFFFFFFFF), // White button text
                checkboxTitleColor = Color(0xFFE0E0E0), // Light gray for checkbox labels
                checkboxCheckedColor = Color(0xFF7986CB), // Light indigo for checked state
                checkboxUnCheckedColor = Color(0xFF757575) // Medium gray for unchecked state
            )
        )

        PayPipesUI.updateTheme(customTheme)
    }

    private fun resetTheme() {
        PayPipesUI.resetTheme()
    }

    private fun handleTransactionResult(result: CardTransactionResult?) {
        when (result) {
            is CardTransactionResult.Success -> {
                showToast(getString(R.string.payment_successful, result.details.transactionId))
            }

            is CardTransactionResult.Failure -> {
                val errorMessage = when (val error = result.error) {
                    is CardTransactionError.Declined -> getString(R.string.payment_declined, error.declineCode)
                    is CardTransactionError.Canceled -> getString(R.string.payment_cancelled)
                    is CardTransactionError.UnknownTransactionState -> getString(R.string.payment_unknown_state)
                    is CardTransactionError.ServerError -> getString(R.string.payment_server_error, error.serverError.message)
                    is CardTransactionError.CompromisedDevice -> getString(R.string.payment_compromised_device)
                    is CardTransactionError.NoSchemeForCurrency -> getString(R.string.payment_no_scheme)
                    is CardTransactionError.InvalidInput -> getString(R.string.payment_invalid_input)
                }
                showToast(getString(R.string.payment_failed, errorMessage))
            }

            null -> {
                showToast(getString(R.string.no_result_received))
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun createConfiguration(): Configuration {
        return Configuration(
            clientId = Credentials.clientId,
            clientSecret = Credentials.clientSecret,
            companyName = Credentials.companyName,
            termsUrl = Credentials.termsUrl,
            environment = Environment.SANDBOX,
            isLoggingEnabled = true,
            isScreenCaptureEnabled = true
        )
    }

    private fun createPaymentTransaction(viewModel: ExampleViewModel): CardTransaction {
        val amountValue = viewModel.amount.value
        val transactionAmount = try {
            BigDecimal(amountValue)
        } catch (e: Exception) {
            BigDecimal(Credentials.defaultAmount)
        }
        return CardTransaction(
            amount = Money(amount = transactionAmount, currency = viewModel.selectedCurrency.value),
            orderId = "order_${System.currentTimeMillis()}",
            billingInfo = createSampleBillingInfo(viewModel),
            flowType = FlowType.CARD_PAYMENT,
            billingAddressRequired = viewModel.billingAddressRequired.value
        )
    }

    private fun createCardStorageTransaction(viewModel: ExampleViewModel): CardTransaction {
        return CardTransaction(
            amount = Money.ZERO, // Zero amount for card storage
            orderId = "storage_${System.currentTimeMillis()}",
            billingInfo = createSampleBillingInfo(viewModel),
            flowType = FlowType.CARD_STORAGE,
            billingAddressRequired = viewModel.billingAddressRequired.value
        )
    }

    private fun createSampleBillingInfo(viewModel: ExampleViewModel): BillingInfo {
        return BillingInfo(
            firstName = viewModel.firstName.value,
            lastName = viewModel.lastName.value,
            email = viewModel.email.value,
            address = if (viewModel.billingAddressProvided.value) Credentials.sampleAddress else null,
            phone = Credentials.samplePhone
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExampleAppContent(
    viewModel: ExampleViewModel,
    modifier: Modifier = Modifier,
    onTestPayment: () -> Unit,
    onTestCardStorage: () -> Unit,
    onCustomThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val billingAddressRequired by viewModel.billingAddressRequired.collectAsState()
    val billingAddressProvided by viewModel.billingAddressProvided.collectAsState()
    val isCustomThemeEnabled by viewModel.isCustomThemeEnabled.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y != 0f) {
                    keyboardController?.hide()
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = context.getString(R.string.app_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ThemeConstants.colorDarkBlue
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = context.getString(R.string.app_description),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ThemeConstants.colorDarkGray
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Currency selector
        CurrencySelector(
            selectedCurrency = selectedCurrency,
            onCurrencyChange = { viewModel.updateCurrency(it) },
            amount = amount,
            onAmountChange = { viewModel.updateAmount(it) },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Billing Info inputs
        BillingInfoInputs(
            billingAddressRequired = billingAddressRequired,
            onBillingAddressRequiredChange = { viewModel.updateBillingAddressRequired(it) },
            billingAddressProvided = billingAddressProvided,
            onBillingAddressProvidedChange = { viewModel.updateBillingAddressProvided(it) },
            firstName = firstName,
            onFirstNameChange = { viewModel.updateFirstName(it) },
            lastName = lastName,
            onLastNameChange = { viewModel.updateLastName(it) },
            email = email,
            onEmailChange = { viewModel.updateEmail(it) },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Theming toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = context.getString(R.string.custom_theme_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ThemeConstants.colorDarkBlue
                        )
                    )
                    Text(
                        text = context.getString(R.string.custom_theme_description),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF757575)
                        )
                    )
                }
                Switch(
                    checked = isCustomThemeEnabled,
                    onCheckedChange = onCustomThemeChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2196F3),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }
        }

        // Payment buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.payment_flows_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ThemeConstants.colorDarkBlue
                    )
                )

                Button(
                    onClick = onTestPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = context.getString(R.string.accessibility_payment_button) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = context.getString(R.string.test_card_payment),
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onTestCardStorage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = context.getString(R.string.accessibility_storage_button) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ThemeConstants.colorDarkBlue
                    )
                ) {
                    Text(
                        text = context.getString(R.string.test_card_storage),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Features list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.features_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ThemeConstants.colorDarkBlue
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = context.getString(R.string.features_list),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ThemeConstants.colorDarkGray,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = context.getString(R.string.sdk_version, Version.MARKETING_VERSION),
            style = MaterialTheme.typography.bodySmall.copy(
                color = ThemeConstants.colorGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencies = listOf("EUR", "USD", "JPY")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val context = LocalContext.current
            
            Text(
                text = context.getString(R.string.amount_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ThemeConstants.colorDarkBlue
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text(context.getString(R.string.amount_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = context.getString(R.string.accessibility_amount_field) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeConstants.colorDarkBlue,
                    unfocusedBorderColor = ThemeConstants.colorLightGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .semantics { contentDescription = context.getString(R.string.accessibility_currency_dropdown) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                val currencyName = when (currency) {
                                    "EUR" -> "EUR - Euro"
                                    "USD" -> "USD - US Dollar"
                                    "JPY" -> "JPY - Japanese Yen"
                                    else -> currency
                                }
                                Text(currencyName)
                            },
                            onClick = {
                                onCurrencyChange(currency)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillingInfoInputs(
    billingAddressRequired: Boolean,
    onBillingAddressRequiredChange: (Boolean) -> Unit,
    billingAddressProvided: Boolean,
    onBillingAddressProvidedChange: (Boolean) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = context.getString(R.string.customer_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ThemeConstants.colorDarkBlue
                )
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text(context.getString(R.string.first_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeConstants.colorDarkBlue,
                    unfocusedBorderColor = ThemeConstants.colorLightGray
                )
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text(context.getString(R.string.last_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeConstants.colorDarkBlue,
                    unfocusedBorderColor = ThemeConstants.colorLightGray
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(context.getString(R.string.email_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeConstants.colorDarkBlue,
                    unfocusedBorderColor = ThemeConstants.colorLightGray
                )
            )

            Divider(color = Color(0xFFEEEEEE))

            // Required switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = context.getString(R.string.billing_address_required),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = ThemeConstants.colorDarkGray
                        )
                    )
                    Text(
                        text = context.getString(R.string.billing_address_required_description),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF757575)
                        )
                    )
                }
                Switch(
                    checked = billingAddressRequired,
                    onCheckedChange = onBillingAddressRequiredChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }

            Divider(color = Color(0xFFEEEEEE))

            // Provided switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = context.getString(R.string.billing_address_provided),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = ThemeConstants.colorDarkGray
                        )
                    )
                    Text(
                        text = context.getString(R.string.billing_address_provided_description),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF757575)
                        )
                    )
                }
                Switch(
                    checked = billingAddressProvided,
                    onCheckedChange = onBillingAddressProvidedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }
        }
    }
} 
// MARK: - Constants

private object Credentials {
    const val clientId = "[YOUR CLIENT ID]"
    const val clientSecret = "[YOUR CLIENT SECRET]"
    const val companyName = "[YOUR COMPANY NAME]"
    const val termsUrl = "https://www.paypipes.com"
    
    const val defaultAmount = "10.00"
    
    val sampleAddress = Address(
        street = "123 Fake Street",
        city = "Test City",
        state = "TS",
        postCode = "00000",
        country = "US"
    )
    val samplePhone = Phone(number = "777777777", countryCode = "+420")
}

private object ThemeConstants {
    val colorBlue = Color(0xFF2196F3)
    val colorRed = Color(0xFFF44336)
    val colorDarkBlue = Color(0xFF1976D2)
    val colorDarkGray = Color(0xFF424242)
    val colorVeryDarkGray = Color(0xFF212121)
    val colorLightGray = Color(0xFFBDBDBD)
    val colorWhite = Color(0xFFFFFFFF)
    val colorGray = Color(0xFF9E9E9E)
    val colorVeryLightGray = Color(0xFFFAFAFA)
}
