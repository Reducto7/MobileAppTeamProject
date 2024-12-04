package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BillItem(bill: Bill, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                    text = "￥${"%.2f".format(bill.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (bill.isIncome) Color.Green else Color.Red,
                    textAlign = TextAlign.End
                )
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
    var currentMonth by remember { mutableStateOf("11月") } // 用于显示在 AppBar 的月份

    // 计算总收入和总支出
    val totalIncome by remember {
        derivedStateOf {
            bills.filter { it.isIncome }.sumOf { it.amount } // 计算总收入
        }
    }
    val totalExpense by remember {
        derivedStateOf {
            bills.filter { !it.isIncome }.sumOf { it.amount } // 计算总支出
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
                    val month = visibleBill.date.split("-").getOrNull(1)?.toIntOrNull() // 提取月份
                    currentMonth = if (month != null) "${month}月" else "未知月份" // 更新月份
                }
            }
    }

    // 初始化时，直接根据第一条账单的月份来设置 currentMonth
    LaunchedEffect(bills) {
        bills.firstOrNull()?.let { firstBill ->
            val month = firstBill.date.split("-").getOrNull(1)?.toIntOrNull()
            currentMonth = if (month != null) "${month}月" else "未知月份"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = currentMonth) }, // 显示动态更新的月份
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black, // 背景颜色
                    titleContentColor = Color.White // 标题文字颜色
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black // 黑色背景
            ) {
                Spacer(modifier = Modifier.width(60.dp)) // 间距
                // 加号按钮
                IconButton(onClick = { navController.navigate("addNewBill") }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White // 白色图标
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                // 图表导航按钮
                IconButton(onClick = { navController.navigate("chart") }) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Graph",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                // 个人设置按钮
                IconButton(onClick = { navController.navigate("mypage") }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Setting",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {
                // 本月总收入和总支出部分
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "本月总收入: ￥${"%.2f".format(totalIncome)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    Text(
                        text = "本月总支出: ￥${"%.2f".format(totalExpense)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }

                // 账单列表
                LazyColumn(
                    state = listState, // 绑定滚动状态
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 将账单按日期进行排序，确保使用 LocalDate 进行比较
                    items(bills.sortedByDescending {
                        LocalDate.parse(it.date, dateFormatter) // 将日期字符串转换为 LocalDate
                    }) { bill ->
                        BillItem(bill) {
                            // 点击账单项跳转编辑页面
                            navController.navigate("editBill/${bill.id}")
                        }
                    }
                }
            }
        }
    }
}


