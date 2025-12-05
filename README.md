# PayPipes Android SDK

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/paypipespublic/punext-pms-sdk-android)
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
    implementation("com.punext:paypipes:1.0.0")
}
```

### Manual Integration

1. Download the AAR file from the [Releases](https://github.com/paypipespublic/punext-pms-sdk-android/releases) page
2. Place the AAR file in your `libs` directory
3. Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/paypipes-1.0.0.aar"))
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
    environment = Environment.PRODUCTION // or Environment.SANDBOX
)
```

### 2. Create a Transaction Intent

```kotlin
import com.punext.paypipes.model.CardTransaction
import com.punext.paypipes.model.Money
import com.punext.paypipes.model.BillingInfo

// BillingInfo is required - create it with mandatory fields
val billingInfo = BillingInfo(
    firstName = "John",
    lastName = "Smith",
    email = "john.smith@example.com",
    address = null, // Optional
    phone = null // Optional
)

val amount = Money(amount = 10.00, currency = "USD")
val transaction = CardTransaction(
    amount = amount,
    orderId = UUID.randomUUID().toString(),
    billingInfo = billingInfo,
    flowType = FlowType.CARD_PAYMENT,
    billingAddressRequired = false
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
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == REQUEST_CODE_PAYMENT) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val result = PayPipesUI.getTransactionResult(data)
                when (result) {
                    is CardTransactionResult.Success -> {
                        val transactionId = result.transactionId
                        // Payment successful
                    }
                    is CardTransactionResult.Failure -> {
                        val error = result.error
                        // Handle error
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                // User cancelled
            }
        }
    }
}
```

## Configuration

### Custom Theme

```kotlin
import com.punext.paypipes.model.Theme

val customTheme = Theme(
    colors = Theme.Colors(
        primary = Color(0xFF1976D2),
        background = Color.White,
        // ... customize other colors
    ),
    fonts = Theme.Fonts(
        title = Font(24.sp, FontWeight.Bold),
        // ... customize other fonts
    )
)

val configuration = Configuration(
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    environment = Environment.PRODUCTION,
    theme = customTheme
)

// Update theme at runtime
PayPipesUI.updateTheme(customTheme)
```

### Billing Address

**BillingInfo is mandatory** for all transactions. The following fields are required:
- `firstName: String` - Customer's first name (required, cannot be blank)
- `lastName: String` - Customer's last name (required, cannot be blank)
- `email: String` - Customer's email address (required, cannot be blank)
- `address: Address?` - Optional billing address
- `phone: Phone?` - Optional phone number

```kotlin
import com.punext.paypipes.model.BillingInfo
import com.punext.paypipes.model.Address
import com.punext.paypipes.model.Phone

// Minimal required BillingInfo
val minimalBillingInfo = BillingInfo(
    firstName = "John",
    lastName = "Smith",
    email = "john.smith@example.com",
    address = null,
    phone = null
)

// Complete BillingInfo with address and phone
val completeBillingInfo = BillingInfo(
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
    phone = Phone(number = "1234567890", countryCode = "+1")
)

val transaction = CardTransaction(
    amount = amount,
    orderId = UUID.randomUUID().toString(),
    billingInfo = completeBillingInfo, // Required - cannot be null
    flowType = FlowType.CARD_PAYMENT,
    billingAddressRequired = true
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

- `amount: Money` - The transaction amount
- `orderId: String` - Unique order identifier (required, cannot be blank)
- `billingInfo: BillingInfo` - **Required** billing information (cannot be null)
- `flowType: FlowType` - Transaction type (`CARD_PAYMENT` or `CARD_STORAGE`)
- `billingAddressRequired: Boolean` - Whether billing address is required

### BillingInfo

Represents billing information for a customer.

#### Required Properties

- `firstName: String` - Customer's first name (required, cannot be blank)
- `lastName: String` - Customer's last name (required, cannot be blank)
- `email: String` - Customer's email address (required, cannot be blank)

#### Optional Properties

- `address: Address?` - Customer's billing address (optional)
- `phone: Phone?` - Customer's phone number (optional)

### Configuration

SDK configuration settings.

#### Properties

- `clientId: String` - Your client ID
- `clientSecret: String` - Your client secret
- `environment: Environment` - `PRODUCTION` or `SANDBOX`

## Error Handling

The SDK provides detailed error information:

```kotlin
when (result) {
    is CardTransactionResult.Success -> {
        val transactionId = result.transactionId
        // Transaction successful
    }
    is CardTransactionResult.Failure -> {
        when (result.error) {
            is CardTransactionError.Cancelled -> {
                // User cancelled the transaction
            }
            is CardTransactionError.NetworkError -> {
                // Network error occurred
            }
            is CardTransactionError.ValidationError -> {
                // Validation failed
            }
            is CardTransactionError.SecurityError -> {
                // Security check failed (e.g., rooted device)
            }
            else -> {
                // Other errors
            }
        }
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

