package com.example.myapplication.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillPage(
    navController: NavController,
    billId: Int?, // 接收 Int? 类型的 billId
    modifier: Modifier = Modifier,
    viewModel: BillViewModel = viewModel() // 获取 ViewModel 实例
) {
    // 定义状态变量
    var isIncome by remember { mutableStateOf(false) }
    var remark by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    // 获取当前 Context
    val context = LocalContext.current

    // 页面加载时获取账单数据
    LaunchedEffect(billId) {
        if (billId != null) {
            val bill = viewModel.getBillById(billId) // 从 ViewModel 获取账单数据
            if (bill != null) {
                // 如果账单数据存在，加载上次编辑的数据
                isIncome = bill.isIncome
                remark = bill.remarks
                amount = bill.amount.toString()
                selectedDate = bill.date
                selectedCategory = bill.category
            } else {
                // 如果找不到账单数据，初始化为空状态
                isIncome = false
                remark = ""
                amount = ""
                selectedDate = ""
                selectedCategory = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Bill") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 收入/支出切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { isIncome = false },
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
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

                OutlinedButton(
                    onClick = { isIncome = true },
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
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
            }

            // 类别选择
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = if (isIncome) {
                    listOf("Salary", "Red Envelope", "Financial Management", "Rent", "Dividends", "Gifts", "Other")
                } else {
                    listOf("Dining", "Shopping", "Daily Use", "Transportation", "Sports", "Entertainment", "Accommodation", "Other")
                }
                itemsIndexed(categories) { _, category ->
                    val isSelected = category == selectedCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { selectedCategory = category }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = category,
                            tint = if (isSelected) Color.White else Color.Black,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (isSelected) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                        )
                        Text(text = category)
                    }
                }
            }

            // 备注输入框
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("Enter Remarks") },
                modifier = Modifier.fillMaxWidth()
            )

            // 金额输入框
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 日期选择器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(context, { _, year, month, day ->
                            selectedDate = "$year-${month + 1}-$day"
                        }, 2024, 10, 23).show()
                    }
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Select Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }

            // 保存按钮
            Button(
                onClick = {
                    if (billId != null) {
                        val updatedBill = Bill(
                            id = billId,
                            isIncome = isIncome,
                            category = selectedCategory,
                            remarks = remark,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = selectedDate
                        )
                        viewModel.updateBill(updatedBill)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
