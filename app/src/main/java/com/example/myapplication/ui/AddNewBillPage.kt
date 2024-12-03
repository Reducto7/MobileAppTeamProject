package com.example.teamproject.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import com.example.myapplication.ui.Bill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewBillPage(
    navController: NavController
) {
    var isIncome by remember { mutableStateOf(false) } // 控制是收入还是支出页面
    var remark by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val appViewModel: AppViewModel = viewModel()
    var selectedDate by remember { mutableStateOf(appViewModel.getTodayDate()) }
    val context = LocalContext.current

    // 定义类别选项列表
    val incomeCategories = listOf("Salary", "Red Envelope", "Financial Management", "Rent", "Dividends", "Gifts", "Other")
    val expenditureCategories = listOf("Dining", "Shopping", "Daily Use", "Transportation", "Sports", "Entertainment", "Accommodation", "Other")
    val categories = if (isIncome) incomeCategories else expenditureCategories
    var expanded by remember { mutableStateOf(false) } // 控制下拉菜单的显示状态
    var selectedCategory by remember { mutableStateOf(categories[0]) } // 保存用户选择的类别



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Add New Bill" ) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
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
            modifier = Modifier
                .padding(innerPadding)
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 横向切换按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        isIncome = false
                        selectedCategory = expenditureCategories[0]
                    },
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isIncome) Color.White else Color.Black, // 选中按钮为黑色
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
                    onClick = {
                        isIncome = true
                        selectedCategory = incomeCategories[0]
                    },
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isIncome) Color.Black else Color.White, // 选中按钮为黑色
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

            // 类别选择框
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
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

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                        .background(Color.Transparent)
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(400.dp)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 输入备注
            OutlinedTextField(
                value = remark,
                onValueChange = { newText -> remark = newText },
                label = { Text("Enter Remarks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // 输入金额（只能输入数字）
            OutlinedTextField(
                value = amount,
                onValueChange = { newAmount ->
                    if (newAmount.all { it.isDigit() }) {
                        amount = newAmount
                    }
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { appViewModel.showDatePicker(context) { date -> selectedDate = date } }
                    .padding(0.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // 确认记录按钮
            Button(
                onClick = {  // 检查输入是否合法
                    if (remark.isNotBlank() && amount.isNotBlank() && selectedDate.isNotBlank()) {
                        val id = System.currentTimeMillis().toInt() // 使用当前时间戳生成唯一 ID
                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0

                        // 调用上传函数
                        uploadBillToFirebase(
                            id = id,
                            isIncome = isIncome,
                            category = selectedCategory,
                            remarks = remark,
                            amount = parsedAmount,
                            date = selectedDate
                        )

                        // 清空表单内容并导航回主页面
                        remark = ""
                        amount = ""
                        selectedDate = appViewModel.getTodayDate()
                        navController.navigate("main")
                    } else {
                        // 处理非法输入，例如提示用户补全表单
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add")
            }
        }
    }
}

private fun uploadBillToFirebase(
    id: Int,
    isIncome: Boolean,
    category: String,
    remarks: String,
    amount: Double,
    date: String
) {
    val database = FirebaseDatabase.getInstance()

    val userId = FirebaseAuth.getInstance().currentUser?.uid // 获取当前用户的 UID
    if (userId != null) {
        val reference = database.getReference("bills").child(userId) // 将账单存储在当前用户的 UID 下

        // 将数据封装为 Bill 对象
        val newBill = Bill(
            id = id,
            isIncome = isIncome,
            category = category,
            remarks = remarks,
            amount = amount,
            date = date
        )

        // 上传数据
        reference.child(id.toString()).setValue(newBill)
            .addOnSuccessListener {
                // 数据上传成功处理，例如显示提示
            }
            .addOnFailureListener {
                // 数据上传失败处理，例如显示错误提示
            }
    } else {
        // 用户未登录的情况，处理错误
    }
}



