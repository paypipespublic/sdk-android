# PayPipes SDK for Android

A secure, customizable payment SDK for Android applications.

## Requirements

- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 15 (API 35)
- **Language**: Kotlin

## Installation

Add the PayPipes SDK to your project via Maven:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        // ... other repositories
        maven {
            url = uri("https://your-maven-repo/repository")
        }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.punext:paypipes:1.0.3")
}
```

## Quick Start

### 1. Initialize Configuration

```kotlin
val configuration = Configuration(
    clientId = "your-client-id",
    clientSecret = "your-client-secret",
    companyName = "Your Company",
    termsUrl = "https://yourcompany.com/terms",
    environment = Environment.SANDBOX,  // Use Environment.PRODUCTION for live
    isLoggingEnabled = true  // Set to false in production
)
```

### 2. Create Customer Details

```kotlin
val customerDetails = CustomerDetails(
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
    phone = Phone(number = "5551234567", countryCode = "+1"),
    legalEntity = LegalEntity.PRIVATE,  // or LegalEntity.BUSINESS
    referenceId = "customer-123"  // Optional: your unique customer identifier
)
```

### 3. Create Transaction

```kotlin
val transaction = CardTransaction(
    orderId = "order_${System.currentTimeMillis()}",
    customerDetails = customerDetails,
    amount = Money(amount = BigDecimal("10.00"), currency = "USD"),
    flowType = FlowType.CARD_PAYMENT,  // or FlowType.CARD_STORAGE
    billingAddressRequired = false,
    callbackUrl = "https://yourserver.com/callback"  // Optional
)
```

### 4. Launch Payment UI

```kotlin
// Register for result
private val paymentLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    val transactionResult = CardTransactionActivity.extractResult(result.data)
    handleResult(transactionResult)
}

// Launch payment
PayPipes.initialize(configuration)
val intent = PayPipesUI.buildCardTransactionIntent(
    context = this,
    configuration = configuration,
    transaction = transaction
)
paymentLauncher.launch(intent)
```

### 5. Handle Results

```kotlin
private fun handleResult(result: CardTransactionResult?) {
    when (result) {
        is CardTransactionResult.Success -> {
            val transactionId = result.details.transactionId
            val customerToken = result.details.customerToken
            // Payment successful
        }
        is CardTransactionResult.Failure -> {
            when (val error = result.error) {
                is CardTransactionError.Declined -> {
                    // Payment declined: error.declineCode
                }
                is CardTransactionError.Canceled -> {
                    // User cancelled
                }
                is CardTransactionError.ServerError -> {
                    // Server error: error.serverError.message
                }
                // Handle other error types...
            }
            // Partial data may be available
            result.transactionId?.let { /* transaction was created */ }
            result.customerToken?.let { /* customer was created */ }
        }
        null -> {
            // No result received
        }
    }
}
```

## Configuration Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `String?` | * | Your client ID |
| `clientSecret` | `String?` | * | Your client secret |
| `accessToken` | `String?` | * | Pre-obtained OAuth token (alternative to clientId/clientSecret) |
| `companyName` | `String` | Yes | Displayed in payment form |
| `termsUrl` | `String` | Yes | URL to your terms and conditions |
| `environment` | `Environment` | Yes | `SANDBOX` or `PRODUCTION` |
| `isLoggingEnabled` | `Boolean` | No | Enable SDK logging (default: `false`) |
| `isScreenCaptureEnabled` | `Boolean` | No | Allow screenshots (default: `false`) |
| `language` | `SDKLanguage?` | No | Override display language |

\* Either `accessToken` OR both `clientId` and `clientSecret` must be provided.

## Transaction Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderId` | `String` | Yes | Unique order identifier |
| `customerDetails` | `CustomerDetails` | Yes | Customer information |
| `amount` | `Money` | Yes | Transaction amount and currency |
| `flowType` | `FlowType` | No | `CARD_PAYMENT` (default) or `CARD_STORAGE` |
| `billingAddressRequired` | `Boolean` | No | Require billing address (default: `false`) |
| `callbackUrl` | `String?` | No | Server callback URL for status updates |

## Customer Details

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `firstName` | `String` | Yes | Customer's first name |
| `lastName` | `String` | Yes | Customer's last name |
| `email` | `String` | Yes | Customer's email address |
| `address` | `Address?` | No | Billing address |
| `phone` | `Phone?` | No | Phone number |
| `legalEntity` | `LegalEntity` | No | `PRIVATE` (default) or `BUSINESS` |
| `referenceId` | `String?` | No | Your unique customer ID (max 255 chars) |

## Theming

Customize the SDK appearance:

```kotlin
val customTheme = Theme(
    colors = ThemeColors(
        screenBackgroundColor = Color(0xFFF5F5F5),
        buttonBackgroundColor = Color(0xFF3F51B5),
        buttonTitleColor = Color.White,
        // ... more color options
    ),
    fonts = ThemeFonts(
        buttonTitleFont = FontWeight.Bold,
        // ... more font options
    ),
    mode = ThemeMode.Auto  // Auto, Light, or Dark
)

PayPipesUI.updateTheme(customTheme)
```

## Localization

Supported languages: English (`ENGLISH`), Czech (`CZECH`)

```kotlin
val configuration = Configuration(
    // ...
    language = SDKLanguage.CZECH  // Override system language
)
```

## Error Types

| Error | Description |
|-------|-------------|
| `CompromisedDevice` | Device is rooted/compromised |
| `Canceled` | User cancelled the transaction |
| `Declined(declineCode)` | Transaction was declined |
| `NoSchemeForCurrency` | No payment scheme for currency |
| `UnknownTransactionState` | Transaction state undetermined |
| `ServerError(serverError)` | Server-side error |
| `InvalidInput` | Input validation failed |

## Security

- The SDK refuses to run on rooted/compromised devices
- Screen capture is disabled by default
- Card data is encrypted using JWE before transmission
- SSL certificate pinning is enforced

## Sample App

See the `SampleApp/` directory for a complete integration example.

## Support

For integration support, contact: pnemecek@purple-technology.com
