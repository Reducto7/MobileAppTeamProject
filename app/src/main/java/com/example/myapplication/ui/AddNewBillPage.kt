package com.example.teamproject.ui

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
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
import com.example.myapplication.ui.Bill
import com.example.myapplication.ui.BillViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewBillPage(
    navController: NavController,
    viewModel: BillViewModel = viewModel()
) {
    var isIncome by remember { mutableStateOf(false) } // 控制是收入还是支出页面
    var remark by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(viewModel.getTodayDate()) }
    val context = LocalContext.current
    val initialDate = selectedDate.split("-").map { it.toInt() }
    val initialYear = initialDate[0]
    val initialMonth = initialDate[1] - 1 // Month is zero-based in DatePickerDialog
    val initialDay = initialDate[2]

// 根据 isIncome 判断选择收入或支出类别
    val categories = if (isIncome) viewModel.incomeCategories else viewModel.expenditureCategories

// 通过 selectedCategory 保存选中的类别
    var selectedCategory by remember { mutableStateOf("") }  // 获取类别名称

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "추가" ) },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // 横向切换按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        isIncome = false
                        selectedCategory = ""
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
                        text = "지출",
                        color = if (isIncome) Color.Black else Color.White
                    )
                }

                OutlinedButton(
                    onClick = {
                        isIncome = true
                        selectedCategory = ""
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
                        text = "입금",
                        color = if (isIncome) Color.White else Color.Black
                    )
                }


            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),  // 每行固定 4 个图标
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

            // 输入备注
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("메모") },
                modifier = Modifier
                    .fillMaxWidth()
            )

            // 输入金额（只能输入数字）
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
                        DatePickerDialog(context, { _, year, month, day ->
                            selectedDate = "$year-${month + 1}-$day"
                        },
                            initialYear,
                            initialMonth,
                            initialDay
                        ).show()
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
                        selectedDate = viewModel.getTodayDate()
                        navController.navigate("main")
                    } else {
                        // 处理非法输入，例如提示用户补全表单
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "추가")
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



