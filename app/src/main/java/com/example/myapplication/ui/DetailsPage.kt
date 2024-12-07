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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.net.URLEncoder
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

    // 滚动状态
    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    var lineChartData by remember { mutableStateOf(emptyList<Float>()) }
    var barChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }

    // 根据年份和月份获取当前月份的天数
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)

    // 更新数据
    LaunchedEffect(selectedYear, selectedMonth, isYearSelected, sortByAmount) {
        if (isYearSelected) {
            // 点击年份：显示每个月的金额总和
            lineChartData = viewModel.getMonthlyTotalsForCategory(category, selectedYear, isIncome)
            val bills = viewModel.getBillsForYearCategory(category, selectedYear, isIncome)
            println("Selected Year: $selectedYear, Bills: $bills")
            // 如果账单不为空，生成柱状图数据
            barChartData = if (bills.isNotEmpty()) {
                if (sortByAmount) {
                    bills.sortedByDescending { it.amount }
                } else {
                    bills.sortedBy { it.date }
                }.map { it.remarks to it.amount.toFloat() }
            } else {
                emptyList() // 如果账单为空，设置为空数据
            }
            println("BarChartData: $barChartData")
        } else {
            // 点击月份：显示每天的金额总和
            lineChartData =
                viewModel.getDailyCategoryTotals(selectedYear, selectedMonth, category, isIncome)
                    .take(getDaysInMonth(selectedYear, selectedMonth))
                    .map { it.toFloat() }
            val bills = viewModel.getBillsForCategory(category, selectedYear, selectedMonth, isIncome)

            // 如果账单不为空，生成柱状图数据
            barChartData = if (bills.isNotEmpty()) {
                if (sortByAmount) {
                    bills.sortedByDescending { it.amount }
                } else {
                    bills.sortedBy { it.date }
                }.map { it.remarks to it.amount.toFloat() }
            } else {
                emptyList() // 如果账单为空，设置为空数据
            }
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
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                selectedIndex = -1,
                onSelectedIndexChanged = {}
            )
            // 排序切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { sortByAmount = true }) {
                    Text("Sort by Amount", color = if (sortByAmount) Color.Black else Color.Gray)
                }
                TextButton(onClick = { /*TODO*/ }) {
                    Text("/")
                }
                TextButton(onClick = { sortByAmount = false }) {
                    Text("Sort by Date", color = if (!sortByAmount) Color.Black else Color.Gray)
                }
            }

            // 柱状图
            //DividerWithText("-")
            VerticalBarChartWithLabels(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f,
                onCategoryClick = { /*TODO*/ }
            )
        }
    }
}


//
@Composable
fun VerticalBarChartWithLabels(
    data: List<Pair<String, Float>>, // 数据：备注字段和金额
    maxValue: Float,                 // 最大值：用于确定横轴比例
    onCategoryClick: (String) -> Unit, // 点击事件，传递类别名称
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
        // 使用 LazyColumn 实现滚动
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 遍历数据并绘制每一项
            items(data.sortedByDescending { it.second }) { (label, value) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { onCategoryClick(label) }, // 点击事件
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 显示图标
                        val iconId = viewModel.incomeCategories.find { it.first == label }?.second
                            ?: viewModel.expenditureCategories.find { it.first == label }?.second

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

