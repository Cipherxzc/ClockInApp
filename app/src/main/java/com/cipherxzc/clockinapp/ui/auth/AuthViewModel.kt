package com.cipherxzc.clockinapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    object Success : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    private val _logoutState = MutableStateFlow<AuthState>(AuthState.Idle)
    val logoutState: StateFlow<AuthState> = _logoutState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _logoutState.value = AuthState.Idle
                    _loginState.value = AuthState.Success
                }
                .addOnFailureListener { e ->
                    _loginState.value = AuthState.Error(e.message ?: "登录失败")
                }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    _logoutState.value = AuthState.Idle
                    // 注册成功后更新 displayName
                    val firebaseUser = result.user
                    if (firebaseUser != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                        firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    _registerState.value = AuthState.Success
                                } else {
                                    _registerState.value =
                                        AuthState.Error(task.exception?.message ?: "更新用户名失败")
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    _registerState.value = AuthState.Error(e.message ?: "注册失败")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _logoutState.value = AuthState.Loading
                auth.signOut()
                _loginState.value = AuthState.Idle
                _registerState.value = AuthState.Idle
                _logoutState.value = AuthState.Success
            } catch (e: Exception) {
                _logoutState.value = AuthState.Error(e.message ?: "登出失败")
            }
        }
    }

    fun currentUser() = auth.currentUser
}
