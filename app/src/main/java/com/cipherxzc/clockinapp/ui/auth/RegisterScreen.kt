package com.cipherxzc.clockinapp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.ui.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authState: AuthState,
    onSuccess: () -> Unit
) {
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onSuccess()
    }

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "注册", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("确认密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    when {
                        username.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入用户名", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        email.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入邮箱", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        password.isBlank() || confirm.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入密码", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        password != confirm -> {
                            android.widget.Toast.makeText(context, "两次输入的密码不一致", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        else -> onRegister(username, email.trim(), password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "注册中..." else "注册")
            }
            TextButton(onClick = onNavigateToLogin) {
                Text("已有账号？登录")
            }
        }
    }
}
