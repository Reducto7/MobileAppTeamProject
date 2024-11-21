package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.remember
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavController, modifier: Modifier = Modifier, context: Context) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf("") }
    var autoLogin by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences("autoLoginPrefs", Context.MODE_PRIVATE)

    // Load saved email if auto-login is enabled
    LaunchedEffect(Unit) {
        if (sharedPreferences.getBoolean("autoLoginEnabled", false)) {
            email = sharedPreferences.getString("savedEmail", "") ?: ""
            autoLogin = true
        }
    }

    CenterAlignedTopAppBar(
        title = { Text("Login Page") }
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            //visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    dialogMessage = "邮箱和密码不能为空。"
                    isDialogVisible = true
                } else {
                    loginUser(email, password) { success, message ->
                        if (success) {
                            if (autoLogin) {
                                coroutineScope.launch {
                                    sharedPreferences.edit()
                                        .putBoolean("autoLoginEnabled", true)
                                        .putString("savedEmail", email)
                                        .apply()
                                }
                            } else {
                                coroutineScope.launch {
                                    sharedPreferences.edit()
                                        .putBoolean("autoLoginEnabled", false)
                                        .remove("savedEmail")
                                        .apply()
                                }
                            }
                            navController.navigate("main")
                        } else {
                            dialogMessage = message
                            isDialogVisible = true
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Login",
                style = typography.titleMedium
            )
        }


        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Login with Phone Number",
                style = typography.titleMedium
            )
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Login with E-mail",
                style = typography.titleMedium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = autoLogin,
                    onCheckedChange = { autoLogin = it }
                )
                Text("Auto-Login")
            }
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text(
                    text = "Register",
                    style = typography.titleMedium
                )
            }
        }
    }

    if (isDialogVisible) {
        LoginDialog(
            message = dialogMessage,
            onDismiss = { isDialogVisible = false }
        )
    }
}

fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onResult(true, "")
        } else {
            val errorMessage = when (val exception = task.exception) {
                is FirebaseAuthInvalidUserException -> "该账号未注册，请先注册。"
                is FirebaseAuthInvalidCredentialsException -> "邮箱或密码错误，请重试。"
                else -> "登录失败，请稍后重试。原因：${exception?.localizedMessage ?: "未知错误"}"
            }
            onResult(false, errorMessage)
        }
    }
}


@Composable
fun LoginDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "登录失败") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确定")
            }
        },
        modifier = modifier
    )
}
