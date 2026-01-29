# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2025-01-14

### Changed
- **BREAKING:** `CardTransactionDetails` now requires `customerToken` parameter
- **BREAKING:** `CardTransactionResult.Failure` now includes optional `transactionId` and `customerToken`
- **BREAKING:** `BillingInfo` renamed to `CustomerDetails`
- **BREAKING:** `CardTransaction.billingInfo` renamed to `CardTransaction.customerDetails`

### Added
- `customerToken` included in success results via `CardTransactionDetails`
- Partial transaction data (`transactionId`, `customerToken`) available in failure results
- `callbackUrl` parameter in `CardTransaction` for receiving transaction status callbacks
- `referenceId` parameter in `CustomerDetails` for customer unique identification (max 255 chars)
- `SDKLanguage` enum for explicit language selection (`ENGLISH`, `CZECH`)
- `language` parameter in `Configuration` to override SDK display language
- Language code sent to backend in purchase requests (ISO 639-1)

## [1.0.2] - 2025-01-09

### Changed
- Expiry date validation no longer enforces maximum year limit
- Card holder validation now respects legal entity (PRIVATE requires 2 names, BUSINESS allows 1 name)

### Added
- Customer legal entity

## [1.0.1] - 2025-12-10

### Changed
- Decline transaction note updated

## [1.0.0] - 2025-12-04

### Added
- Initial SDK release