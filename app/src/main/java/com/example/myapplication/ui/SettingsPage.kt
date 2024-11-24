package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsPage(navController: NavController, password: String = "12345678") {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val accountName = currentUser?.email?.substringBefore("@") ?: "Guest" // 默认显示 "Guest" 如果用户未登录

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "setting",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "账户名：$accountName", fontSize = 18.sp)
                Text(text = "密码：$password", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            ) {
                Text(text = "返回")
            }
        }
    }
}
