package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillPage(navController: NavController, billId: Int?, modifier: Modifier = Modifier) {
    var isIncome by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val date by remember { mutableStateOf("2024-11-23") }
    var expanded by remember { mutableStateOf(false) }

    // 模拟根据 billId 加载数据
    LaunchedEffect(billId) {
        if (billId != null) {
            isIncome = false // 假设已存在的账单是支出类型
            category = ""
            remarks = ""
            amount = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "编辑账单") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            //

                            //
                            navController.popBackStack()
                        }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 收入和支出切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { isIncome = true },
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isIncome) Color.Black else Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Income",
                        color = if (isIncome) Color.White else Color.Black
                    )
                }

                OutlinedButton(
                    onClick = { isIncome = false },
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isIncome) Color.White else Color.Black,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Expenditure",
                        color = if (isIncome) Color.Black else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 类别选择框
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    label = { Text(if (isIncome) "Select Income Category" else "Select Expenditure Category") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "选择类别",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(400.dp)
                ) {
                    val categories = if (isIncome) {
                        listOf("Salary", "Red Envelope", "Financial Management", "Rent", "Dividends", "Gifts", "Other")
                    } else {
                        listOf("Dining", "Shopping", "Daily Use", "Transportation", "Sports", "Entertainment", "Accommodation", "Other")
                    }
                    categories.forEach { categoryItem ->
                        DropdownMenuItem(
                            text = { Text(categoryItem) },
                            onClick = {
                                category = categoryItem
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 备注输入框
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text(text = "Enter Remarks") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 金额输入框
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(text = "Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 日期选择器
            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text(text = "Select Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 添加按钮
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text(text = "Add")
            }
        }
    }
}
