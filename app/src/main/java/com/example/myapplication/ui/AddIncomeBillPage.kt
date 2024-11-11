package com.example.teamproject.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeBillPage(
    navController: NavController
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Add Income Bill") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = {navController.navigate("addNewBill") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                            tint = Color.White,
                        )
                    }
                }
            )
        }
    ) {innerPadding ->
        var remark by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        val appViewModel: AppViewModel = viewModel()
        var selectedDate by remember { mutableStateOf(appViewModel.getTodayDate()) }
        val context = LocalContext.current

        // 定义类别选项列表
        val categories = listOf("Salary", "red envelope", "financial management", "rent", "dividends", "gifts", "other")
        var expanded by remember { mutableStateOf(false) } // 控制下拉菜单的显示状态
        var selectedCategory by remember { mutableStateOf(categories[0]) } // 保存用户选择的类别



        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box (modifier = Modifier.fillMaxWidth()){
                // 类别选择框
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { },
                    label = { Text("选择类别") },
                    readOnly = true,
                    trailingIcon = { // 在文本框末尾添加下拉图标
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "选择类别",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true } // 点击时展开菜单
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true } // 点击时展开菜单
                        .background(Color.Transparent) // 保持透明
                )

                // 下拉菜单
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }, // 点击外部时关闭菜单
                    modifier = Modifier.width(400.dp)//调节下拉菜单宽度
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category // 更新选中的类别
                                expanded = false // 关闭菜单
                            }
                        )
                    }
                }
            }

            // 输入备注
            OutlinedTextField(
                value = remark,
                onValueChange = {newText -> remark = newText},
                label = { Text("Enter Remarks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // 输入金额（只能输入数字）
            OutlinedTextField(
                value = amount,
                onValueChange = { newAmount ->
                    if (newAmount.all { it.isDigit() }) {  // 确保输入的内容是数字
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
                    onValueChange = { /* 禁止直接输入 */ },
                    label = { Text("Select Date") },
                    readOnly = true, // 设置为只读
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // 禁用编辑
                )
            }

            // 确认记录按钮
            Button(
                onClick = { /* 处理确认逻辑 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add")
            }
        }
    }


}


