package com.example.myapplication.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsPage(
    navController: NavHostController,
    category: String,
    isIncome: Boolean,
    viewModel: BillViewModel = viewModel()
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var isYearSelected by remember { mutableStateOf(false) } // 控制年份或月份模式
    var sortByAmount by remember { mutableStateOf(true) } // 默认按金额排序
    var sortByDate by remember { mutableStateOf(false) } // 按日期排序

    // 滚动状态
    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    var lineChartData by remember { mutableStateOf(emptyList<Float>()) }
    var barChartData by remember { mutableStateOf(emptyList<Triple<String, Float, String>>()) }

    // 选中点的索引
    var selectedIndex by remember { mutableStateOf(-1) }

    // 根据年份和月份获取当前月份的天数
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }
    val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)

    LaunchedEffect(selectedYear, selectedMonth, isYearSelected, sortByAmount, sortByDate) {
        val bills = if (isYearSelected) {
            // 获取指定年份的原始账单数据
            viewModel.getBillsForYearCategory(category, selectedYear, isIncome)
        } else {
            // 获取指定月份的原始账单数据
            viewModel.getBillsForCategory(category, selectedYear, selectedMonth, isIncome)
        }
        // 根据排序状态更新柱状图数据
        barChartData = if (bills.isNotEmpty()) {
            when {
                sortByAmount -> bills.sortedByDescending { it.amount }
                sortByDate -> {
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    bills.sortedBy { LocalDate.parse(it.date, dateFormatter) }
                }
                else -> bills
            }.map { Triple("${it.remarks}", it.amount.toFloat(), it.date) }
        } else {
            emptyList()
        }

        if (isYearSelected) {
            // 点击年份：显示每个月的金额总和
            lineChartData = viewModel.getMonthlyTotalsForCategory(category, selectedYear, isIncome)
        } else {
            // 点击月份：显示每天的金额总和
            lineChartData =
                viewModel.getDailyCategoryTotals(selectedYear, selectedMonth, category, isIncome)
                    .take(daysInMonth)
                    .map { it.toFloat() }
        }
    }

    // 滚动到当前年份或月份
    LaunchedEffect(Unit) {
        if (isYearSelected) {
            yearListState.scrollToItem(currentYear - 2020) // 假设年份范围是 2020 到当前年
        } else {
            monthListState.scrollToItem(currentMonth - 1)
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Details - $category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().shadow(8.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            // 年月选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isYearSelected = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isYearSelected) Color.White else Color.Black,
                        contentColor = if (isYearSelected) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = "월별",
                        color = if (isYearSelected) Color.Black else Color.White
                    )
                }
                Button(
                    onClick = { isYearSelected = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isYearSelected) Color.Black else Color.White,
                        contentColor = if (isYearSelected) Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = "년별",
                        color = if (isYearSelected) Color.White else Color.Black
                    )
                }
            }
            // 显示年份或月份列表
            if (isYearSelected) {
                LazyRow(state = yearListState) {
                    items((2020..currentYear).toList()) { year ->
                        Text(
                            text = year.toString(),
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { selectedYear = year },
                            color = if (selectedYear == year) Color.Black else Color.Gray
                        )
                    }
                }
            } else {
                LazyRow(state = monthListState) {
                    items((1..12).toList()) { month ->
                        Text(
                            text = "${month}월",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { selectedMonth = month },
                            color = if (selectedMonth == month) Color.Black else Color.Gray
                        )
                    }
                }
            }


            // 折线图
            //DividerWithText("-")
            CustomLineChart(
                isYearSelected = isYearSelected,
                dataPoints = lineChartData,
                xAxisLabels = if (isYearSelected) generateXAxisLabelsForYear() else generateXAxisLabels(
                    daysInMonth
                ),
                selectedIndex = selectedIndex,
                onSelectedIndexChanged = { newIndex ->
                    selectedIndex = newIndex
                }
            )

            // 中间统计信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                //horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    TextButton(onClick = {
                        sortByAmount = true;
                        sortByDate = false
                    },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .offset(x = (-16).dp)
                            .padding(start = 0.dp) // 将按钮左侧与图标对齐
                    ) {
                        Text("금액",
                            color = if (sortByAmount) Color.Black else Color.Gray,
                            style = TextStyle(fontSize = 30.sp)
                            )
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // 按钮间距
                    Text(
                        "|",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 45.sp),
                        modifier = Modifier.padding(horizontal = 4.dp).offset(x = (-20).dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 按钮间距
                    TextButton(onClick = {
                        sortByAmount = false;
                        sortByDate = true
                    },
                        modifier = Modifier.align(Alignment.CenterVertically).offset(x = (-30).dp)
                        ) {
                        Text(
                            "시간",
                            color = if (!sortByAmount) Color.Black else Color.Gray,
                            style = TextStyle(fontSize = 30.sp)
                        )
                    }
                }

                val totalAmount = barChartData.sumOf { it.second.toDouble() }.toInt()
                val averageAmount =
                    if (barChartData.isNotEmpty()) totalAmount / barChartData.size else 0
                Row {
                    Column {
                        Text(
                            text = "총",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                        Text(
                            text = "₩$totalAmount",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                    Column {
                        Text(
                            text = "평균",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                        Text(
                            text = "₩$averageAmount",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                    }
                }
            }


            // 柱状图
            VerticalBarChartWithLabels(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f,
                category = category
            )
        }
    }
}

//柱状图实现
@Composable
fun VerticalBarChartWithLabels(
    data: List<Triple<String, Float, String>>, // 数据：备注字段和金额
    maxValue: Float,                 // 最大值：用于确定横轴比例
    category: String,                // 传递类别名称
    viewModel: BillViewModel = viewModel() // 账单数据视图模型
) {
    // 整体外框
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(375.dp) // 设置黑色框的固定高度
            .pointerInput(Unit) { // 增加鼠标滚动支持
                detectTransformGestures { _, _, _, _ ->
                    // 支持鼠标滚动的相关逻辑（此处留空）
                }
            }
    ) {
        if (data.isEmpty()) {
            // 如果数据为空，显示“没有数据”文本
            Text(
                text = "데이터 없음",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp).offset(y = (-64).dp),
                color = Color.Gray,
                style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
        } else {
            // 使用 LazyColumn 实现滚动
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 遍历数据并绘制每一项
                items(data) { (label, value, date) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            //.clickable { onCategoryClick(label) }, // 点击事件
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 显示图标
                            val iconId = viewModel.incomeCategories.find { it.first == category }?.second
                                ?: viewModel.expenditureCategories.find { it.first == category }?.second

                            if (iconId != null) {
                                Icon(
                                    painter = painterResource(id = iconId),
                                    contentDescription = label,
                                    modifier = Modifier
                                        .size(44.dp) // 设置图标大小
                                        .padding(end = 8.dp)
                                        .offset(y = 4.dp),
                                    tint = Color.Unspecified // 使用原始颜色
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // 第一行：左侧为标签，右侧为金额
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 百分比计算
                                    val totalValue = data.sumOf { it.second.toDouble() } // 总和
                                    val percentage =
                                        if (totalValue > 0) (value / totalValue * 100).toInt() else 0

                                    // 标签
                                    Text(
                                        text = "$label   ${percentage}%",
                                        modifier = Modifier.weight(1f),
                                        color = Color.Black,
                                    )
                                    // 日期（灰色字体）
                                    Text(
                                        text = date, // 假设日期是传递的 `date` 字段
                                        color = Color.Gray,
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodySmall // 使用较小字体
                                    )

                                    // 金额
                                    Text(
                                        text = "₩${value.toInt()}",
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // 第二行：柱状图
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .background(Color.Transparent) // 设置透明背景
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = value / maxValue) // 动态调整黑条宽度
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(14.dp)) // 设置圆角，保证动态调整后两端为圆角
                                            .background(Color.Black)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


