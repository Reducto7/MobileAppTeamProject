package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPage(
    navController: NavController,
    viewModel: BillViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val accountName = currentUser?.email?.substringBefore("@") ?: "Guest" // 默认显示 "Guest" 如果用户未登录

    val totalRecords = viewModel.bills.size
    val totalDays = viewModel.getBillCountAndDays()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "마이") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().shadow(8.dp)
            )
        },
        content = { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 头像和账户名
                    HeaderSection(accountName)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 记账信息
                    AccountStatsSection(totalDays, totalRecords)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 功能按钮
                    FunctionButtonsSection(navController)
                }
            }
        }
    )
}


@Composable
fun HeaderSection(accountName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 使用默认头像
        Image(
            painter = painterResource(id = R.drawable.default_avatar),
            contentDescription = "photograph",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = accountName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            //color = Color.White // 设置字体颜色为白色
        )
    }
}

@Composable
fun AccountStatsSection(totalDays: Int, totalRecords: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "총 청구일 수：$totalDays",
            fontSize = 18.sp,
            //color = Color.White // 设置字体颜色为白色
        )
        Text(
            text = "총 항목 수：$totalRecords",
            fontSize = 18.sp,
            //color = Color.White // 设置字体颜色为白色
        )
    }
}

@Composable
fun FunctionButtonsSection(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = {
                // 退出账号并跳转到登录页面
                navController.navigate("login") {
                    popUpTo("my") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        ) {
            Text(text = "로그아웃")
        }
    }
}
