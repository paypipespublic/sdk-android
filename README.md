# Android SDK - Internal Developer Guide

This document provides an internal overview of the Android SDK, covering its architecture, key components, development practices, and project structure. It is intended for developers working on the SDK itself.

## Core Objectives & Design Philosophy

*   **Seamless Integration**: Provide a drop-in UI solution that handles the complexity of payment processing.
*   **Modern Android Development**: Built entirely with **Jetpack Compose** and **Kotlin Coroutines** for a modern, reactive codebase.
*   **Security First**: Implements strict security measures including device integrity checks (root detection), secure string handling, and preventing screen capture in sensitive flows.
*   **Themability**: A robust theming system allowing the SDK to adapt to the host application's design system.

## Architecture Overview

The SDK follows a Clean Architecture approach with MVVM (Model-View-ViewModel) presentation pattern.

*   **UI Layer (`ui/`)**:
    *   Built 100% with **Jetpack Compose**.
    *   **Activity**: `CardTransactionActivity` is the single entry point for the UI flow. It handles the window configuration and hosts the Compose content.
    *   **Screens**: `CardTransactionScreen` is the main composable defining the layout.
    *   **ViewModel**: `CardTransactionViewModel` manages the state (`CardTransactionUiState`), handles business logic, input validation, and communicates with the data layer.
    *   **State**: UI state is modeled as sealed classes/interfaces for reactivity.

*   **Domain/Model Layer (`model/`)**:
    *   Contains pure data classes (`CardTransaction`, `Configuration`, `Money`, `CustomerDetails`).
    *   Defines core business rules and validation logic.

### Localization

The SDK supports multiple languages with an optional explicit language override:

```kotlin
val configuration = Configuration(
    // ... other params ...
    language = SDKLanguage.CZECH  // Force Czech language
)
```

#### Supported Languages

| Language | Enum Value | ISO 639-1 |
|----------|------------|-----------|
| English | `SDKLanguage.ENGLISH` | `en` |
| Czech | `SDKLanguage.CZECH` | `cs` |

#### Language Resolution

1. If `language` is explicitly set in `Configuration`, use that language
2. Otherwise, check if the system language is supported
3. Fall back to English if system language is not supported

The resolved language is:
*   Used for all SDK UI strings via locale-wrapped Context
*   Sent to the backend via the `language` parameter in API requests

### Transaction Configuration

`CardTransaction` supports the following parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderId` | `String` | Yes | Unique identifier for the order |
| `customerDetails` | `CustomerDetails` | Yes | Customer details including billing information |
| `amount` | `Money` | Yes | Transaction amount and currency |
| `flowType` | `FlowType` | No | Transaction flow type (default: `CARD_PAYMENT`) |
| `billingAddressRequired` | `Boolean` | No | Whether billing address is required (default: `false`) |
| `callbackUrl` | `String?` | No | URL for receiving transaction status callbacks |

#### Customer Details

`CustomerDetails` contains the customer information for the transaction:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `firstName` | `String` | Yes | Customer's first name |
| `lastName` | `String` | Yes | Customer's last name |
| `email` | `String` | Yes | Customer's email address |
| `address` | `Address?` | No | Customer's billing address |
| `phone` | `Phone?` | No | Customer's phone number |
| `legalEntity` | `LegalEntity` | No | `PRIVATE` (default) or `BUSINESS` |
| `referenceId` | `String?` | No | Unique customer identifier (max 255 chars) |

#### Callback URL

The `callbackUrl` parameter allows you to specify a URL where the backend will send transaction status updates:

```kotlin
val transaction = CardTransaction(
    orderId = "order_123",
    customerDetails = customerDetails,
    amount = Money(amount = BigDecimal("10.00"), currency = "USD"),
    flowType = FlowType.CARD_PAYMENT,
    callbackUrl = "https://example.com/callback"
)
```

**Validation Rules:**
- Maximum length: 2048 characters
- Must use `http://` or `https://` scheme
- Invalid URLs will throw an `IllegalArgumentException` at construction time

*   **Data/Network Layer (`network/`)**:
    *   **ApiManager**: The high-level facade for network operations.
    *   **NetworkService**: Low-level HTTP client using `OkHttp`. Note: Retrofit is *not* used to minimize library footprint.
    *   **Entities**: DTOs for JSON parsing (using `Gson`).

*   **Security Layer (`security/`)**:
    *   `DeviceIntegrity`: Checks for rooted/compromised devices.
    *   `SecureString`: Wrapper for sensitive data (like CVV) to minimize memory footprint and exposure.

## Key Systems Deep Dive

### Theming System (`theme/`, `ui/theme/`)

*   The SDK uses a custom `Theme` object that mirrors Material Design concepts but remains independent.
*   `ThemeManager` is a thread-safe singleton that holds the current configuration.
*   `PayPipesTheme` composable acts as the theme provider, bridging the SDK's `Theme` to Jetpack Compose's `MaterialTheme`.
*   Supports Light/Dark modes and Auto switching.

### Localization (`localization/`)

*   `LocalizationManager`: A custom manager to handle string resources dynamically.
*   Allows the SDK to use its own localized strings independent of the host app's configuration, while still supporting standard Android resource qualifiers.

### Form Validation (`ui/cardpayment/validation/`)

*   Validation logic is decoupled into separate Validator classes (`CardNumberValidator`, `CardExpiryValidator`, etc.).
*   `FormValidationManager` orchestrates the validation of the entire form state.
*   Validation happens in real-time as the user types, updating the `CardFormData` state in the ViewModel.

### Result Handling

The SDK uses a sealed class for transaction outcomes:

```kotlin
sealed class CardTransactionResult : Parcelable {
    data class Success(val details: CardTransactionDetails) : CardTransactionResult()
    data class Failure(
        val error: CardTransactionError,
        val transactionId: String? = null,
        val customerToken: String? = null
    ) : CardTransactionResult()
}
```

#### Success Result

`CardTransactionDetails` contains:
*   `transactionId: String` - The unique transaction identifier
*   `customerToken: String` - The customer token for future transactions

#### Failure Result

`CardTransactionResult.Failure` wraps the error with optional partial data:
*   `error: CardTransactionError` - The specific error that occurred
*   `transactionId: String?` - Transaction ID if one was created before failure
*   `customerToken: String?` - Customer token if one was created before failure

This design allows integrators to access partial transaction data even when an error occurs (e.g., when a transaction was created but later declined).

#### Usage Example

```kotlin
val transactionResult = CardTransactionActivity.extractResult(result.data)

when (transactionResult) {
    is CardTransactionResult.Success -> {
        val details = transactionResult.details
        println("Transaction ID: ${details.transactionId}")
        println("Customer Token: ${details.customerToken}")
    }
    is CardTransactionResult.Failure -> {
        println("Error: ${transactionResult.error}")
        // Access partial data if available
        transactionResult.transactionId?.let { println("Partial Transaction ID: $it") }
        transactionResult.customerToken?.let { println("Partial Customer Token: $it") }
    }
    null -> {
        println("No result received")
    }
}
```

#### Error Types (`CardTransactionError`)

| Error | Description |
|-------|-------------|
| `CompromisedDevice` | Device is rooted/compromised |
| `Canceled` | User cancelled the transaction |
| `Declined(declineCode)` | Transaction was declined |
| `NoSchemeForCurrency` | No payment scheme available for currency |
| `UnknownTransactionState` | Transaction state could not be determined |
| `ServerError(serverError)` | Server-side error occurred |
| `InvalidInput` | Input validation failed |

## Project Structure

*   **`paypipes-sdk`**: The core library module.
    *   `src/main/java/com/punext/paypipes/`: Source code.
    *   `src/main/res/`: Resources (strings, drawables, themes).
*   **`example-app`**: A consumer application module.
    *   Used for testing and demonstrating SDK integration.
    *   Contains example usage of `PayPipesUI`.

## Development Guidelines

### Build & Dependencies

The project uses Gradle Kotlin DSL (`.kts`).

*   **Minimum SDK**: 29 (Android 10)
*   **Compile SDK**: 35
*   **Jetpack Compose**: Used for all UI.
*   **Coroutines**: Used for asynchronous tasks.
*   **OkHttp**: Used for networking.
*   **Gson**: Used for JSON serialization.

**Note**: We strictly limit external dependencies to keep the SDK lightweight. Avoid adding new libraries (like Retrofit, Dagger/Hilt) to the SDK module unless absolutely necessary.

### API Design

*   **`PayPipesUI`**: The main public entry point.
    *   `buildCardTransactionIntent`: Creates the Intent to launch the SDK.
    *   `updateTheme`: Allows runtime theme updates.
*   **Result Handling**: The SDK returns results via standard Android Activity results (`Activity.RESULT_OK`, `Activity.RESULT_CANCELED`) with a `CardTransactionResult` Parcelable extra.

### Security Best Practices

*   **Sensitive Data**: Never log full card numbers or CVV. Use `SecureString` where possible.
*   **Root Detection**: The SDK checks for root access on initialization and will refuse to run on compromised devices.
*   **Screen Protection**: `FLAG_SECURE` is applied to the window to prevent screenshots/recording in production environments.

## Key Contacts & Maintainers

*   Pavel Nemecek, pnemecek@purple-technology.com

---
*This document is for internal use. Information may change as the SDK evolves.*
