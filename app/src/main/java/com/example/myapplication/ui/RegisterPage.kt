package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import com.google.firebase.auth.FirebaseAuthUserCollisionException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(navController: NavController, modifier: Modifier = Modifier) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRegistrationSuccessful by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Register Page") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("login") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done // 将转行键设置为“确认键”
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (email.isBlank() || password.isBlank()) {
                            dialogMessage = "邮箱和密码不能为空。"
                            isDialogVisible = true
                        } else {
                            registerUser(email, password) { success, message ->
                                isRegistrationSuccessful = success
                                dialogMessage = message
                                isDialogVisible = true
                            }
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        dialogMessage = "邮箱和密码不能为空。"
                        isDialogVisible = true
                    } else {
                        registerUser(email, password) { success, message ->
                            isRegistrationSuccessful = success
                            dialogMessage = message
                            isDialogVisible = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Register",
                    style = typography.titleMedium
                )
            }

        }

        if (isDialogVisible) {
            RegisterDialog(
                message = dialogMessage,
                onDismiss = {
                    isDialogVisible = false
                    if (isRegistrationSuccessful) {
                        navController.navigate("login")
                    }
                }
            )
        }
    }
}


fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onResult(true, "您已成功注册！")
        } else {
            val errorMessage = when (val exception = task.exception) {
                is FirebaseAuthUserCollisionException -> "该邮箱已被注册。"
                else -> "注册失败，请重试。原因：${exception?.localizedMessage ?: "未知错误"}"
            }
            onResult(false, errorMessage)
        }
    }
}


@Composable
fun RegisterDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (message == "您已成功注册！") "注册成功" else "注册失败") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确定")
            }
        },
        modifier = modifier
    )
}



