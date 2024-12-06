package com.example.myapplication.ui

import android.content.Context
import android.icu.util.Calendar
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BillItem(
    bill: Bill, onClick: () -> Unit,
    viewModel: BillViewModel = viewModel()
) {
    // 根据 bill.isIncome 决定使用的类别列表
    val categories = if (bill.isIncome) viewModel.incomeCategories else viewModel.expenditureCategories

    // 根据 bill.category 找到对应的图标资源 ID
    val categoryIcon = categories.find { it.first == bill.category }?.second ?: R.drawable.visibility_off

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
        ) {
            // 左侧图标
            Icon(
                painter = painterResource(id = categoryIcon),
                contentDescription = bill.category,
                modifier = Modifier
                    .size(40.dp) // 设置图标大小
                    .padding(end = 12.dp), // 与文本之间留间距
                tint = Color.Unspecified // 保持图标原始颜色
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 第一行：显示日期
                Text(
                    text = bill.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 第二行：备注
                    Text(
                        text = bill.remarks,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f) // 备注占满左侧
                    )
                    // 显示金额，右对齐
                    Text(
                        text = "₩${bill.amount.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (bill.isIncome) Color(0xFF004B2D) else Color(0xFF7D1D2F),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BillViewModel = viewModel() // 绑定 ViewModel
) {
    val bills = viewModel.bills // 从 ViewModel 获取账单数据
    val listState = rememberLazyListState() // 用于监听滚动状态
    var currentMonth by remember { mutableStateOf("12월") } // 用于显示在 AppBar 的月份
    var currentYear by remember { mutableStateOf("2024년") }
    val context = LocalContext.current

    val totalIncome by remember {
        derivedStateOf {
            bills
                .filter { it.isIncome }  // 只选择收入账单
                .filter { bill -> bill.date.split("-").getOrNull(1) == currentMonth.split("월").getOrNull(0) }  // 过滤出当前月份的账单
                .sumOf { it.amount }  // 计算总收入
        }
    }
    val totalExpense by remember {
        derivedStateOf {
            bills
                .filter { !it.isIncome }  // 只选择支出账单
                .filter { bill -> bill.date.split("-").getOrNull(1) == currentMonth.split("월").getOrNull(0) }  // 过滤出当前月份的账单
                .sumOf { it.amount }  // 计算总支出
        }
    }

    // 日期格式化器
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 检测滚动状态并动态更新 AppBar 的月份
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val visibleBill = bills.getOrNull(index) // 获取当前可见账单
                if (visibleBill != null) {
                    // 提取月份和年份
                    val dateParts = visibleBill.date.split("-")
                    val month = dateParts.getOrNull(1)?.toIntOrNull() // 提取月份
                    val year = dateParts.getOrNull(0) // 提取年份
                    // 更新月份和年份
                    currentMonth = if (month != null) "${month}월" else "未知月份"
                    currentYear = year?.let { "${it}년" } ?: "未知年份"
                }
            }
    }

    // 初始化时，直接根据第一条账单的月份来设置 currentMonth
    LaunchedEffect(bills) {
        bills.firstOrNull()?.let { firstBill ->
            val dateParts = firstBill.date.split("-")
            val month = dateParts.getOrNull(1)?.toIntOrNull()
            val year = dateParts.getOrNull(0)
            currentMonth = if (month != null) "${month}월" else "未知月份"
            currentYear = year?.let { "${it}년" } ?: "未知年份"
        }
    }
/*
    // 监听 currentMonth 变化并滚动到对应的账单
    LaunchedEffect(currentMonth) {
        // 找到 currentMonth 对应的最新账单
        val targetBill = bills
            .filter { bill ->
                val billMonth = bill.date.split("-").getOrNull(1)
                billMonth == currentMonth.split("월").getOrNull(0) // 比较月份
            }
            .sortedByDescending { bill ->
                LocalDate.parse(bill.date, dateFormatter)  // 按日期倒序排序，找到最新的账单
            }
            .firstOrNull()

        // 如果找到对应的账单，滚动到该账单
        targetBill?.let { bill ->
            val targetIndex = bills.indexOf(bill)  // 获取该账单的索引
            listState.animateScrollToItem(targetIndex)  // 滚动到该账单
        }
    }

 */

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween // 左右两边的内容分开
                    ) {
                        Column {
                            Text(
                                text = "$currentYear",
                                color = Color.Black,
                                style = TextStyle(fontSize = 16.sp)
                            )

                            Row {
                                Text(
                                    text = "$currentMonth",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 24.sp),
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        // 弹出年月选择器
                                        ShowMonthPicker(context
                                        ) { selectedMonth ->
                                            // 更新显示的月份
                                            currentMonth = "${selectedMonth.split("-")[1]}월"
                                            currentYear = "${selectedMonth.split("-")[0]}년"
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        modifier = Modifier.size(36.dp).offset(y = (-2).dp)
                                    )
                                }
                            }
                        }

                        // 右侧显示收入和支出的文本
                        Row() {
                            Column (horizontalAlignment = Alignment.End){
                                Text(
                                    text = "수입",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                )
                                Text(
                                    text = "₩${totalIncome.toInt()}",
                                    //color = Color(0xFF004B2D),
                                    style = TextStyle(fontSize = 24.sp),
                                )
                            }
                            Spacer(modifier = Modifier.width(40.dp))
                            Column (horizontalAlignment = Alignment.End){
                                Text(
                                    text = "지출",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                )
                                Text(
                                    text = "₩${totalExpense.toInt()}",
                                    //color = Color(0xFF7D1D2F),
                                    style = TextStyle(fontSize = 24.sp),
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    //containerColor = Color.White,// 背景颜色
                    titleContentColor = Color.Black // 标题文字颜色
                ),
                modifier = Modifier.fillMaxWidth().shadow(8.dp)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black // 黑色背景
            ) {
                Spacer(modifier = Modifier.width(60.dp)) // 间距

                // 图表导航按钮
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { navController.navigate("chart") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.chart),
                            contentDescription = "Graph",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "차트", // 图表的文本
                        color = Color.White
                    )
                }

                //Spacer(modifier = Modifier.width(30.dp)) // 间距

                IconButton(
                    onClick = { navController.navigate("addNewBill") },
                    modifier = Modifier
                        .size(200.dp) // 设置IconButton的大小
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(64.dp) // 设置图标大小
                            .border(2.dp, Color.Gray, shape = CircleShape) // 添加黑色描边，宽度为2dp，形状为圆形
                    )
                }


                //Spacer(modifier = Modifier.width(30.dp)) // 间距

                // 个人设置按钮
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { navController.navigate("my") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Setting",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "마이", // 个人的文本
                        color = Color.White
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding).padding(16.dp)
        ) {
            Column {
                // 账单列表
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bills.sortedByDescending {
                        LocalDate.parse(it.date, dateFormatter)
                    }) { bill ->
                        BillItem(
                            bill = bill,
                            onClick = {
                                navController.navigate("editBill/${bill.id}") // 传递账单的 ID
                            },
                            viewModel = viewModel() // 传递 ViewModel 给 BillItem
                        )
                    }
                }
            }
        }
    }
}

fun ShowMonthPicker(
    context: Context,
    onMonthSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // 使用 Holo Light 主题
        { _, selectedYear, selectedMonth, _ -> // 忽略 dayOfMonth
            val formattedMonth = String.format("%04d-%02d", selectedYear, selectedMonth + 1)
            onMonthSelected(formattedMonth) // 回调选中的年和月
        },
        year,
        month,
        1 // 设置默认日为 1
    )

    // 隐藏“日”选择器
    try {
        val daySpinnerId = context.resources.getIdentifier("android:id/day", null, null)
        val daySpinner = datePickerDialog.datePicker.findViewById<View>(daySpinnerId)
        daySpinner?.visibility = View.GONE
    } catch (e: Exception) {
        e.printStackTrace() // 打印异常信息，保持稳定性
    }

    datePickerDialog.show() // 显示日期选择器对话框
}



