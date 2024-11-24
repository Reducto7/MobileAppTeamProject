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
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavController, modifier: Modifier = Modifier, context: Context) {
    // 添加 Google Sign-In 客户端配置
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.your_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task, context, navController)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Login Page") },
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

            // 邮箱登录部分
            EmailLoginSection(navController, context)

            //Spacer(modifier = Modifier.height(4.dp))

            // 灰线分隔
            DividerWithText("其他登录方式")

            Spacer(modifier = Modifier.height(4.dp))

            // Google 登录部分
            GoogleLoginSection(googleSignInClient, launcher)

            AnonymousLoginSection(navController)
        }
    }
}

@Composable
fun EmailLoginSection(navController: NavController, context: Context) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf("") }
    var autoLogin by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences("autoLoginPrefs", Context.MODE_PRIVATE)

    // 加载自动登录状态
    LaunchedEffect(Unit) {
        if (sharedPreferences.getBoolean("autoLoginEnabled", false)) {
            email = sharedPreferences.getString("savedEmail", "") ?: ""
            autoLogin = true
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login", style = typography.titleMedium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                Text(text = "Register", style = typography.titleMedium)
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

@Composable
fun DividerWithText(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = Color.Gray)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray,
            style = typography.bodyMedium
        )
        Divider(modifier = Modifier.weight(1f), color = Color.Gray)
    }
}

@Composable
fun GoogleLoginSection(
    googleSignInClient: GoogleSignInClient,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    OutlinedButton(
        onClick = {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Text(text = "Login with Google", style = typography.titleMedium)
    }
}

@Composable
fun AnonymousLoginSection(navController: NavController) {
    OutlinedButton(
        onClick = {
            performAnonymousLogin(navController)
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Text(text = "Login Anonymously", style = typography.titleMedium)
    }
}

fun performAnonymousLogin(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    auth.signInAnonymously()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AnonymousLogin", "signInAnonymously:success")
                navController.navigate("main")
            } else {
                Log.e("AnonymousLogin", "signInAnonymously:failure", task.exception)
            }
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

fun handleGoogleSignInResult(
    task: Task<GoogleSignInAccount>,
    context: Context,
    navController: NavController
) {
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken

        if (idToken != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = FirebaseAuth.getInstance()

            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    navController.navigate("main")
                } else {
                    Toast.makeText(
                        context,
                        "Firebase 登录失败: ${authTask.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "未能获取 Google ID Token", Toast.LENGTH_LONG).show()
        }
    } catch (e: ApiException) {
        Toast.makeText(context, "Google 登录失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

