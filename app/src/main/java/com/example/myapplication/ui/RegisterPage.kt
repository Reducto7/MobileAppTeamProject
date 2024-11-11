package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(navController: NavController, modifier: Modifier = Modifier) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRegistrationSuccessful by rememberSaveable { mutableStateOf(false) }


    CenterAlignedTopAppBar(
        title = { Text("Register Page", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("login") }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
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
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                registerUser(email, password) { success ->
                    isRegistrationSuccessful = success
                    isDialogVisible = true
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
            isSuccessful = isRegistrationSuccessful,
            onDismiss = {
                isDialogVisible = false
                if (isRegistrationSuccessful) {
                    navController.navigate("login")
                }
            }
        )
    }

}


fun registerUser(email: String, password: String, onResult: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        onResult(task.isSuccessful)
    }
}


@Composable
fun RegisterDialog(
    isSuccessful: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = if (isSuccessful) "您已成功注册！" else "注册失败，请重试。"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isSuccessful) "注册成功" else "注册失败") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确定")
            }
        },
        modifier = modifier
    )
}



