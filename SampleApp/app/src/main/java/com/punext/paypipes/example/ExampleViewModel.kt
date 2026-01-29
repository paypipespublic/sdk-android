package com.punext.paypipes.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.punext.paypipes.model.SDKLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the example app, managing UI state and business logic.
 */
class ExampleViewModel : ViewModel() {
    
    // UI State
    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()
    
    private val _amount = MutableStateFlow("10.00")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    private val _billingAddressRequired = MutableStateFlow(false)
    val billingAddressRequired: StateFlow<Boolean> = _billingAddressRequired.asStateFlow()
    
    private val _billingAddressProvided = MutableStateFlow(false)
    val billingAddressProvided: StateFlow<Boolean> = _billingAddressProvided.asStateFlow()
    
    private val _isBusinessCustomer = MutableStateFlow(false)
    val isBusinessCustomer: StateFlow<Boolean> = _isBusinessCustomer.asStateFlow()
    
    private val _isCustomThemeEnabled = MutableStateFlow(false)
    val isCustomThemeEnabled: StateFlow<Boolean> = _isCustomThemeEnabled.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow<SDKLanguage?>(null) // null = use system language
    val selectedLanguage: StateFlow<SDKLanguage?> = _selectedLanguage.asStateFlow()
    
    private val _firstName = MutableStateFlow("John")
    val firstName: StateFlow<String> = _firstName.asStateFlow()
    
    private val _lastName = MutableStateFlow("Smith")
    val lastName: StateFlow<String> = _lastName.asStateFlow()
    
    private val _email = MutableStateFlow("john.smith@example.com")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _referenceId = MutableStateFlow("")
    val referenceId: StateFlow<String> = _referenceId.asStateFlow()
    
    fun updateCurrency(currency: String) {
        _selectedCurrency.value = currency
    }
    
    fun updateAmount(amount: String) {
        _amount.value = amount
    }
    
    fun updateBillingAddressRequired(required: Boolean) {
        _billingAddressRequired.value = required
    }
    
    fun updateBillingAddressProvided(provided: Boolean) {
        _billingAddressProvided.value = provided
    }
    
    fun updateBusinessCustomer(isBusiness: Boolean) {
        _isBusinessCustomer.value = isBusiness
    }
    
    fun updateCustomThemeEnabled(enabled: Boolean) {
        _isCustomThemeEnabled.value = enabled
    }
    
    fun updateSelectedLanguage(language: SDKLanguage?) {
        _selectedLanguage.value = language
    }
    
    fun updateFirstName(firstName: String) {
        _firstName.value = firstName
    }
    
    fun updateLastName(lastName: String) {
        _lastName.value = lastName
    }
    
    fun updateEmail(email: String) {
        _email.value = email
    }
    
    fun updateReferenceId(referenceId: String) {
        _referenceId.value = referenceId
    }
}

