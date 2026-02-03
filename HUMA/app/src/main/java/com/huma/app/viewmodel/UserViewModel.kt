package com.huma.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    fun login(username: String, password: String) {
        // Simulasi login offline
        _username.value = username
    }

    fun logout() {
        _username.value = null
    }
}
