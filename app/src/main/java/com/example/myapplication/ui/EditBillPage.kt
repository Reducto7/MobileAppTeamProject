package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillPage(
    navController: NavController,
    id: Int?, // 接收 Int? 类型的 billId
    modifier: Modifier = Modifier,
    viewModel: BillViewModel = viewModel() // 获取 ViewModel 实例
) {
    // 定义状态变量
    var isIncome by remember { mutableStateOf(false) }
    var remark by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    // 获取当前日期（今天）
    val todayDate = viewModel.getTodayDate().split("-").map { it.toInt() }
    val todayYear = todayDate[0]
    val todayMonth = todayDate[1] - 1 // Month is zero-based in DatePickerDialog
    val todayDay = todayDate[2]

    // 获取当前 Context
    val context = LocalContext.current

    // 定义一个状态变量，标识是否已经初始化
    var isInitialized by remember { mutableStateOf(false) }

    viewModel.fetchBillById(id) { bill ->
        if (bill != null && !isInitialized) { // 只在未初始化时设置初始值
            isIncome = bill.isIncome
            selectedCategory = bill.category
            remark = bill.remarks
            amount = bill.amount.toString()
            selectedDate = bill.date
            isInitialized = true // 设置为已初始化
        } else if (bill == null) {
            println("未找到账单或发生错误")
        }
    }

// 根据 isIncome 判断选择收入或支出类别
    val categories = if (isIncome) viewModel.incomeCategories else viewModel.expenditureCategories


    val (initialYear, initialMonth, initialDay) = if (selectedDate.isNotEmpty()) {
        val initialDate = selectedDate.split("-").map { it.toInt() }
        Triple(initialDate[0], initialDate[1] - 1, initialDate[2]) // Month is zero-based in DatePickerDialog
    } else {
        // 默认日期，避免为空值导致崩溃
        Triple(2024, 3, 1)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "편집") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.fillMaxWidth().shadow(8.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
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
                        text = "지출",
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
                        text = "입금",
                        color = if (isIncome) Color.White else Color.Black
                    )
                }
            }

            // 类别选择
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),  // 每行固定 5 个图标
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                itemsIndexed(categories) {  index, (category, icon) ->
                    val isSelected = category == selectedCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)  // 为每个图标及其文本添加间距
                            .clickable(
                                indication = null,  // 去掉点击时的灰色背景效果
                                interactionSource = remember { MutableInteractionSource() }  // 必须提供一个interactionSource
                            ){
                                selectedCategory = category // 只点击图标时更改选中的类别
                            }
                    ) {
                        // 仅将图标设置为可点击并去掉点击效果的灰色背景
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = category,
                            tint = if (isSelected) Color.White else Color.Black,  // 改变图标颜色
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (isSelected) Color.Black else Color.Transparent,  // 如果选中，背景为黑色，否则透明
                                    shape = CircleShape
                                ).padding(8.dp)
                        )
                        Text(text = category)
                    }
                }
            }

            // 备注输入框
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("메모") },
                modifier = Modifier.fillMaxWidth()
            )

            // 金额输入框
            OutlinedTextField(
                value = amount,
                onValueChange = { newAmount ->
                    if (newAmount.all { it.isDigit() }) {
                        amount = newAmount
                    }
                },
                label = { Text("금액") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )


            // 日期选择器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                // 格式化日期为 yyyy-MM-dd，如果月份或日期是个位数，补零
                                val formattedDate = String.format(
                                    Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day
                                )
                                // 选择日期后更新 selectedDate
                                selectedDate = formattedDate
                            },
                            initialYear,
                            initialMonth,
                            initialDay
                        ).apply {
                            // 设置最大日期为今天
                            datePicker.maxDate = Calendar.getInstance().apply {
                                set(todayYear, todayMonth, todayDay) // 设置为今天
                            }.timeInMillis
                        }.show()
                    }
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("날짜") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 两按钮之间留间距
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.deleteBill(id)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f), // 均分宽度
                    shape = RoundedCornerShape(8.dp), // 设置圆角
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White, // 设置按钮背景色为白色
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "삭제", color = Color(0xFF7D1D2F)) // 文本颜色
                }

                Button(
                    onClick = {
                        if (id != null) {
                            val updatedBill = Bill(
                                id = id,
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
                    modifier = Modifier.weight(1f), // 均分宽度
                    shape = RoundedCornerShape(8.dp) // 设置圆角
                ) {
                    Text("저장")
                }

            }
        }
    }
}
