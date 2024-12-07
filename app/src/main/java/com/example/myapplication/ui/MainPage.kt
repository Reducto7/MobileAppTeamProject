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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.mutableIntStateOf
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
    bill: Bill?, // 修改为可为空的类型
    onClick: () -> Unit,
    viewModel: BillViewModel = viewModel()
) {
    // 如果账单为 null，显示空账单提示
    if (bill == null) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 显示一个空的占位图标
                Icon(
                    painter = painterResource(id = R.drawable.visibility),
                    contentDescription = "Empty Bill",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "空账单", // 显示空账单的提示文字
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    } else {
        // 正常显示账单内容
        val categories = if (bill.isIncome) viewModel.incomeCategories else viewModel.expenditureCategories
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = categoryIcon),
                    contentDescription = bill.category,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp),
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
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
                        Text(
                            text = bill.remarks,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
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
    val today = viewModel.getTodayDate()
    var currentMonth by remember { mutableStateOf("${today.split("-")[1]}월") }
    var currentYear by remember { mutableStateOf("${today.split("-")[0]}년") }
    val context = LocalContext.current
    var firstVisibleBillId by remember { mutableIntStateOf(0) }

    // 日期格式化器
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val sortedBills = bills.sortedByDescending {
        LocalDate.parse(it.date, dateFormatter)
    }
    // 输出排序后第一条账单的 id
    val firstBillId = sortedBills.firstOrNull()?.id
    println("排序后第一条账单的 ID: $firstBillId")

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
                                        ShowMonthPicker(context, currentYear, currentMonth) { selectedMonth ->
                                            // 更新显示的月份
                                            currentMonth = "${selectedMonth.split("-")[1]}월"
                                            currentYear = "${selectedMonth.split("-")[0]}년"
                                        }
                                    }
                                ){
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
                    items(sortedBills) { bill ->
                        BillItem(
                            bill = bill,
                            onClick = {
                                navController.navigate("editBill/${bill.id}") // 传递账单的 ID
                            },
                            viewModel = viewModel() // 传递 ViewModel 给 BillItem
                        )
                    }
                }

                // 滚动到特定月份或年份
                suspend fun scrollToBillByDate(
                    targetYear: String?,
                    targetMonth: String?,
                    sortedBills: List<Bill>,
                    listState: LazyListState
                ) {
                    val targetIndex = sortedBills.indexOfFirst { bill ->
                        val dateParts = bill.date.split("-")
                        val year = dateParts.getOrNull(0)
                        val month = dateParts.getOrNull(1)?.toIntOrNull()?.toString()
                        (targetYear == null || year == targetYear) && (targetMonth == null || month == targetMonth)
                    }

                    if (targetIndex >= 0) {
                        listState.scrollToItem(targetIndex)
                    }else {
                        // 滚动到最底部
                        //listState.scrollToItem(sortedBills.lastIndex)
                    }
                }

// 监听年份和月份变化，自动滚动到对应账单
                LaunchedEffect(currentYear, currentMonth) {
                    val year = currentYear.takeIf { it != "未知年份" }?.removeSuffix("년")
                    val month = currentMonth.takeIf { it != "未知月份" }?.removeSuffix("월")
                    scrollToBillByDate(year, month, sortedBills, listState)
                }

// 动态监听滚动，更新当前可视账单的年份和月份
                LaunchedEffect(listState, sortedBills) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { index ->
                            val visibleBill = sortedBills.getOrNull(index)
                            if (visibleBill != null) {
                                val dateParts = visibleBill.date.split("-")
                                val month = dateParts.getOrNull(1)?.toIntOrNull() // 提取月份
                                val year = dateParts.getOrNull(0) // 提取年份
                                firstVisibleBillId = visibleBill.id
                                currentMonth = if (month != null) "${month}월" else "未知月份"
                                currentYear = year?.let { "${it}년" } ?: "未知年份"
                            }
                        }
                }
            }
        }
    }
}

fun ShowMonthPicker(
    context: Context,
    initialYear: String,
    initialMonth: String,
    onMonthSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    // Set initial year and month
    val year = initialYear.split("년")[0].toInt()
    val month = initialMonth.split("월")[0].toInt() - 1 // month is 0-based

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

    // Hide the day selector
    try {
        val daySpinnerId = context.resources.getIdentifier("android:id/day", null, null)
        val daySpinner = datePickerDialog.datePicker.findViewById<View>(daySpinnerId)
        daySpinner?.visibility = View.GONE
    } catch (e: Exception) {
        e.printStackTrace() // Print exception for stability
    }

    datePickerDialog.show() // Show the date picker dialog
}



