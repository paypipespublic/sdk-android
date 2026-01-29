# PayPipes Android SDK

[![Version](https://img.shields.io/badge/version-1.0.3-blue.svg)](https://github.com/paypipespublic/punext-pms-sdk-android)
[![Platform](https://img.shields.io/badge/platform-Android%2010%2B-lightgrey.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9+-purple.svg)](https://kotlinlang.org)
![License](https://img.shields.io/badge/license-Proprietary-red.svg)

PayPipes SDK provides a seamless and secure payment processing solution for Android applications. Built with Jetpack Compose, this SDK handles card payment transactions, card storage, and 3D Secure authentication flows.

## Features

- 💳 **Card Payment Processing**: Complete payment flow with card validation
- 🔒 **Security First**: Device integrity checks, secure data handling, screen protection
- 🎨 **Customizable Theming**: Match your app's design system
- 📱 **Modern UI**: Built entirely with Jetpack Compose
- 🌍 **Localization**: Multi-language support
- ✅ **3D Secure**: Full support for 3DS authentication flows

## Requirements

- Android 10 (API level 29)+
- Kotlin 1.9+
- Gradle 8.0+
- Android Gradle Plugin 8.0+

## Installation

### Maven Repository

Add the Maven repository to your project's `build.gradle.kts` (or `build.gradle`):

```kotlin
repositories {
    maven {
        url = uri("https://github.com/paypipespublic/sdk-android/repository")
    }
    google()
    mavenCentral()
}
```

### Gradle Dependency

Add the PayPipes SDK dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.punext:paypipes:1.0.3")
}
```

### Manual Integration

1. Download the AAR file from the [Releases](https://github.com/paypipespublic/punext-pms-sdk-android/releases) page
2. Place the AAR file in your `libs` directory
3. Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/paypipes-1.0.3.aar"))
}
```

## Quick Start

### 1. Configure the SDK

```kotlin
import com.punext.paypipes.PayPipesUI
import com.punext.paypipes.model.Configuration
import com.punext.paypipes.model.Environment

val configuration = Configuration(
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    companyName = "Your Company",
    termsUrl = "https://yourcompany.com/terms",
    environment = Environment.PRODUCTION, // or Environment.SANDBOX
    isLoggingEnabled = false,
    language = null // Use system language, or SDKLanguage.ENGLISH, SDKLanguage.CZECH
)
```

### 2. Create a Transaction Intent

```kotlin
import com.punext.paypipes.model.CardTransaction
import com.punext.paypipes.model.Money
import com.punext.paypipes.model.CustomerDetails

// CustomerDetails is required
val customerDetails = CustomerDetails(
    firstName = "John",
    lastName = "Smith",
    email = "john.smith@example.com",
    address = null, // Optional
    phone = null, // Optional
    legalEntity = LegalEntity.PRIVATE, // or LegalEntity.BUSINESS
    referenceId = null // Optional: your unique customer identifier
)

val amount = Money(amount = BigDecimal("10.00"), currency = "USD")
val transaction = CardTransaction(
    orderId = UUID.randomUUID().toString(),
    customerDetails = customerDetails,
    amount = amount,
    flowType = FlowType.CARD_PAYMENT,
    billingAddressRequired = false,
    callbackUrl = null // Optional: URL for server callbacks
)

val intent = PayPipesUI.buildCardTransactionIntent(
    context = this,
    configuration = configuration,
    transaction = transaction
)
```

### 3. Launch the Payment Activity

```kotlin
startActivityForResult(intent, REQUEST_CODE_PAYMENT)
```

### 4. Handle the Result

```kotlin
// Using Activity Result API (recommended)
private val paymentLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    val transactionResult = CardTransactionActivity.extractResult(result.data)
    when (transactionResult) {
        is CardTransactionResult.Success -> {
            val transactionId = transactionResult.details.transactionId
            val customerToken = transactionResult.details.customerToken
            // Payment successful
        }
        is CardTransactionResult.Failure -> {
            val error = transactionResult.error
            // Partial data may be available
            transactionResult.transactionId?.let { /* transaction was created */ }
            transactionResult.customerToken?.let { /* customer was created */ }
        }
        null -> {
            // No result or cancelled
        }
    }
}

// Launch payment
paymentLauncher.launch(intent)
```

## Configuration

### Custom Theme

```kotlin
import com.punext.paypipes.theme.Theme
import com.punext.paypipes.theme.ThemeColors
import com.punext.paypipes.theme.ThemeFonts

val customTheme = Theme(
    colors = ThemeColors(
        screenBackgroundColor = Color(0xFFF5F5F5),
        buttonBackgroundColor = Color(0xFF1976D2),
        buttonTitleColor = Color.White
        // ... customize other colors
    ),
    fonts = ThemeFonts(
        buttonTitleFont = FontWeight.Bold
        // ... customize other fonts
    ),
    mode = ThemeMode.Auto // Auto, Light, or Dark
)

// Update theme at runtime
PayPipesUI.updateTheme(customTheme)
```

### Customer Details

**CustomerDetails is mandatory** for all transactions. The following fields are required:
- `firstName: String` - Customer's first name
- `lastName: String` - Customer's last name
- `email: String` - Customer's email address

The following fields are optional:
- `address: Address?` - Customer's billing address
- `phone: Phone?` - Customer's phone number
- `legalEntity: LegalEntity` - `PRIVATE` (default) or `BUSINESS`
- `referenceId: String?` - Your unique customer identifier (max 255 chars)

```kotlin
import com.punext.paypipes.model.CustomerDetails
import com.punext.paypipes.model.Address
import com.punext.paypipes.model.Phone

// Minimal required CustomerDetails
val minimalCustomerDetails = CustomerDetails(
    firstName = "John",
    lastName = "Smith",
    email = "john.smith@example.com"
)

// Complete CustomerDetails with all fields
val completeCustomerDetails = CustomerDetails(
    firstName = "John",
    lastName = "Smith",
    email = "john.smith@example.com",
    address = Address(
        street = "123 Main St",
        city = "New York",
        state = "NY",
        postCode = "10001",
        country = "US"
    ),
    phone = Phone(number = "1234567890", countryCode = "+1"),
    legalEntity = LegalEntity.PRIVATE,
    referenceId = "customer-123"
)

val transaction = CardTransaction(
    orderId = UUID.randomUUID().toString(),
    customerDetails = completeCustomerDetails,
    amount = amount,
    flowType = FlowType.CARD_PAYMENT,
    billingAddressRequired = true,
    callbackUrl = "https://yourserver.com/callback"
)
```

## Sample App

See the `SampleApp` directory for a complete example application demonstrating:
- Activity integration
- Theme customization
- Billing address handling
- Error handling
- Result processing

## API Reference

### PayPipesUI

The main entry point for the SDK.

#### Methods

- `buildCardTransactionIntent(context, configuration, transaction)` - Creates an Intent to launch the payment activity
- `getTransactionResult(intent)` - Extracts the transaction result from the activity result
- `updateTheme(theme)` - Updates the SDK theme at runtime

### CardTransaction

Represents a payment transaction.

#### Properties

- `orderId: String` - Unique order identifier
- `customerDetails: CustomerDetails` - **Required** customer information
- `amount: Money` - The transaction amount
- `flowType: FlowType` - Transaction type (`CARD_PAYMENT` or `CARD_STORAGE`)
- `billingAddressRequired: Boolean` - Whether billing address is required
- `callbackUrl: String?` - Optional URL for server callbacks

### CustomerDetails

Represents customer information for a transaction.

#### Required Properties

- `firstName: String` - Customer's first name
- `lastName: String` - Customer's last name
- `email: String` - Customer's email address

#### Optional Properties

- `address: Address?` - Customer's billing address
- `phone: Phone?` - Customer's phone number
- `legalEntity: LegalEntity` - `PRIVATE` (default) or `BUSINESS`
- `referenceId: String?` - Your unique customer identifier (max 255 chars)

### Configuration

SDK configuration settings.

#### Properties

- `clientId: String` - Your client ID
- `clientSecret: String` - Your client secret
- `companyName: String` - Displayed in payment form
- `termsUrl: String` - URL to your terms and conditions
- `environment: Environment` - `PRODUCTION` or `SANDBOX`
- `isLoggingEnabled: Boolean` - Enable SDK logging (default: `false`)
- `isScreenCaptureEnabled: Boolean` - Allow screenshots (default: `false`)
- `language: SDKLanguage?` - Override display language (`ENGLISH`, `CZECH`)

## Error Handling

The SDK provides detailed error information:

```kotlin
when (result) {
    is CardTransactionResult.Success -> {
        val transactionId = result.details.transactionId
        val customerToken = result.details.customerToken
        // Transaction successful
    }
    is CardTransactionResult.Failure -> {
        when (val error = result.error) {
            is CardTransactionError.CompromisedDevice -> {
                // Device is rooted/compromised
            }
            is CardTransactionError.Canceled -> {
                // User cancelled the transaction
            }
            is CardTransactionError.Declined -> {
                // Transaction was declined: error.declineCode
            }
            is CardTransactionError.NoSchemeForCurrency -> {
                // No payment scheme available for currency
            }
            is CardTransactionError.UnknownTransactionState -> {
                // Transaction state could not be determined
            }
            is CardTransactionError.ServerError -> {
                // Server-side error: error.serverError.message
            }
            is CardTransactionError.InvalidInput -> {
                // Input validation failed
            }
        }
        
        // Partial data may be available even on failure
        result.transactionId?.let { println("Transaction was created: $it") }
        result.customerToken?.let { println("Customer was created: $it") }
    }
}
```

## ProGuard Rules

If you're using ProGuard/R8, add these rules to your `proguard-rules.pro`:

```proguard
# PayPipes SDK
-keep class com.punext.paypipes.** { *; }
-dontwarn com.punext.paypipes.**
```

## Security

- **Device Integrity**: The SDK checks for compromised devices (root/jailbreak)
- **Secure Storage**: Sensitive data is handled securely
- **Screen Protection**: Screenshots are prevented during payment flows
- **Network Security**: All network requests use HTTPS with certificate pinning

## Support

For issues, questions, or feature requests, please contact:
- Email: pnemecek@purple-technology.com

## License

Proprietary - All rights reserved.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and changes.

