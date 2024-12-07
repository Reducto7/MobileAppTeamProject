package com.example.myapplication.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
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
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder
import java.util.Calendar


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPage(
    navController: NavController,
    viewModel: BillViewModel = viewModel()
) {
    val months = listOf(
        "1월", "2월", "3월", "4월", "5월", "6월",
        "7월", "8월", "9월", "10월", "11월", "12월"
    )
    val years = (2020..2024).toList()

    // 当前年份和月份
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    // 选中点的索引
    var selectedIndex by remember { mutableStateOf(-1) }

    var dataPoints by remember { mutableStateOf(listOf<Double>()) }
    var xAxisLabels by remember { mutableStateOf(listOf<String>()) }
    var isYearSelected by remember { mutableStateOf(false) } // 外部管理的状态
    var barChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }

    var selectedYear by remember { mutableStateOf(currentYear) } // 默认选中当前年份
    var selectedMonth by remember { mutableStateOf(months[currentMonth - 1]) } // 默认选中当前月份

    var selectedOption by remember { mutableStateOf("지출 차트") } // 动态标题内容
    var isDropdownExpanded by remember { mutableStateOf(false) } // 控制下拉框显示


    LaunchedEffect(isYearSelected, selectedYear, selectedMonth) {
        if (isYearSelected) {
            // 按年份模式加载数据
            val yearlyData = viewModel.getYearlyCategoryIncomeExpenditure(
                selectedYear?: currentYear,
                viewModel.isIncomeSelected
            )
            barChartData = yearlyData.map { (category, total) -> category to total.toFloat() }
            dataPoints = viewModel.getMonthlyIncomeExpenditure(selectedYear, viewModel.isIncomeSelected).map { it }
            xAxisLabels = generateXAxisLabelsForYear()
        } else {
            // 按月份模式加载数据
            val monthlyIndex = months.indexOf(selectedMonth) + 1 // 将月份转换为数字
            val monthlyData = viewModel.getMonthlyCategoryIncomeExpenditure(
                selectedYear?: currentYear,
                monthlyIndex,
                viewModel.isIncomeSelected
            )
            barChartData = monthlyData.map { (category, total) -> category to total.toFloat() }
            dataPoints = viewModel.getDailyIncomeExpenditure(selectedYear, monthlyIndex,viewModel.isIncomeSelected).map { it }
            xAxisLabels = generateXAxisLabels(dataPoints.size)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 返回按钮
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { isDropdownExpanded = !isDropdownExpanded }),
                        contentAlignment = Alignment.Center // 标题内容居中
                    ) {
                        Text(text = selectedOption)
                    }
                },
                actions = {
                    // 将按钮放在 actions 区域，自动靠右对齐
                    IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("지출 차트") },
                            onClick = {
                                // 更新逻辑为收入图表
                                selectedOption = "지출 차트" // 更新标题
                                viewModel.isIncomeSelected = false
                                if (isYearSelected) {
                                    // 按年份模式加载数据
                                    val yearlyData = viewModel.getYearlyCategoryIncomeExpenditure(
                                        selectedYear?: currentYear,
                                        viewModel.isIncomeSelected
                                    )
                                    barChartData = yearlyData.map { (category, total) -> category to total.toFloat() }
                                    dataPoints = viewModel.getMonthlyIncomeExpenditure(selectedYear, viewModel.isIncomeSelected).map { it }
                                    xAxisLabels = generateXAxisLabelsForYear()
                                } else {
                                    // 按月份模式加载数据
                                    val monthlyIndex = months.indexOf(selectedMonth) + 1 // 将月份转换为数字
                                    val monthlyData = viewModel.getMonthlyCategoryIncomeExpenditure(
                                        selectedYear?: currentYear,
                                        monthlyIndex,
                                        viewModel.isIncomeSelected
                                    )
                                    barChartData = monthlyData.map { (category, total) -> category to total.toFloat() }
                                    dataPoints = viewModel.getDailyIncomeExpenditure(selectedYear, monthlyIndex,viewModel.isIncomeSelected).map { it }
                                    xAxisLabels = generateXAxisLabels(dataPoints.size)
                                }
                                isDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("수입 차트") },
                            onClick = {
                                // 更新逻辑为支出图表
                                selectedOption = "수입 차트" // 更新标题
                                viewModel.isIncomeSelected = true
                                if (isYearSelected) {
                                    // 按年份模式加载数据
                                    val yearlyData = viewModel.getYearlyCategoryIncomeExpenditure(
                                        selectedYear?: currentYear,
                                        viewModel.isIncomeSelected
                                    )
                                    barChartData = yearlyData.map { (category, total) -> category to total.toFloat() }
                                    dataPoints = viewModel.getMonthlyIncomeExpenditure(selectedYear, viewModel.isIncomeSelected).map { it }
                                    xAxisLabels = generateXAxisLabelsForYear()
                                } else {
                                    // 按月份模式加载数据
                                    val monthlyIndex = months.indexOf(selectedMonth) + 1 // 将月份转换为数字
                                    val monthlyData = viewModel.getMonthlyCategoryIncomeExpenditure(
                                        selectedYear?: currentYear,
                                        monthlyIndex,
                                        viewModel.isIncomeSelected
                                    )
                                    barChartData = monthlyData.map { (category, total) -> category to total.toFloat() }
                                    dataPoints = viewModel.getDailyIncomeExpenditure(selectedYear, monthlyIndex,viewModel.isIncomeSelected).map { it }
                                    xAxisLabels = generateXAxisLabels(dataPoints.size)
                                }
                                isDropdownExpanded = false
                            }
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
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            //滚动视图到12月
            val yearListState = rememberLazyListState()
            val monthListState = rememberLazyListState()

            //滚动视图到12月
            LaunchedEffect(Unit) {
                if (isYearSelected) {
                    yearListState.scrollToItem(years.indexOf(selectedYear))
                } else {
                    monthListState.scrollToItem(months.indexOf("12월"))
                }

            }

            // 切换按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        isYearSelected = false // 切换到月份模式
                        //val monthData = viewModel.monthData[selectedMonth]?.map { it.toDouble() } ?: List(31) { 0.0 }
                        //dataPoints = monthData
                        //xAxisLabels = generateXAxisLabels(monthData.size)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isYearSelected) Color.White else Color.Black,
                        contentColor = Color.White
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
                    onClick = {
                        isYearSelected = true // 切换到年份模式
                        //val yearData = viewModel.yearData[selectedYear]?.map { it.toDouble() } ?: List(12) { 0.0 }
                        //dataPoints = yearData
                        //xAxisLabels = generateXAxisLabelsForYear()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isYearSelected) Color.Black else Color.White,
                        contentColor = Color.White
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

            // 显示月份或年份列表
            if (isYearSelected) {
                LazyRow(state = yearListState) {
                    items(years) { year ->
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
                    items(months) { month ->
                        Text(
                            text = month,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { selectedMonth = month },
                            color = if (selectedMonth == month) Color.Black else Color.Gray
                        )
                    }
                }
            }

            // 折线图
            CustomLineChart(
                isYearSelected = isYearSelected,
                dataPoints = dataPoints.map { it.toFloat() },
                xAxisLabels = xAxisLabels,
                selectedIndex = selectedIndex,
                onSelectedIndexChanged = { newIndex ->
                    selectedIndex = newIndex
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                text = "순위",
                modifier = Modifier.weight(1f), // 让这个文本靠左对齐
                style = TextStyle(fontSize = 32.sp)
            )
                // 获取总和
                val totalSum = dataPoints.sum().toInt()
                // 获取平均值
                val average = dataPoints.average().toInt()

                Row {
                    Column {
                        Text(
                            text = "총",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                        Text(
                            text = "₩${totalSum}",
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
                            text = "₩${average}",
                            modifier = Modifier.align(Alignment.End), // 让这个文本靠左对齐
                            //color = Color.Gray
                        )
                    }
                }

            }


            // 横向柱状图
            HorizontalBarChartWithClick(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f,
                onCategoryClick = { category ->
                    val isIncome = viewModel.isIncomeSelected
                    navController.navigate("details/${URLEncoder.encode(category, "UTF-8")}/$isIncome")
                }
            )
        }
    }
}


//折线图实现
@Composable
fun CustomLineChart(
    isYearSelected : Boolean,
    dataPoints: List<Float>,
    xAxisLabels: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit
) {
    val maxX = if (dataPoints.isNotEmpty()) dataPoints.size - 1 else 1
    val maxY = dataPoints.maxOrNull() ?: 0f
    val minY = 0f

    Box(
        modifier = Modifier
            .padding(4.dp) // 内边距，给折线图留出绘制空间
            .height(150.dp) // 设置卡片内容高度，确保横坐标有足够空间
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints) { // 监听 dataPoints 的变化，重新计算点击逻辑
                    detectTapGestures { offset ->
                        val nearestIndex = dataPoints.indices.minByOrNull { index ->
                            val x = (index / maxX.toFloat()) * size.width
                            kotlin.math.abs(offset.x - x)
                        } ?: -1

                        if (nearestIndex in dataPoints.indices) {
                            onSelectedIndexChanged(nearestIndex)
                        }
                    }
                }
        ) {
            // 计算横坐标需要的额外高度
            val xAxisPadding = 40.dp.toPx()

            // 绘制网格线
            val averageValue = if (dataPoints.isNotEmpty()) dataPoints.average().toFloat() else 0f
            val averageY =
                size.height - xAxisPadding - ((averageValue - minY) / (maxY - minY)) * (size.height - xAxisPadding)

            // 零刻度线
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height - xAxisPadding),
                end = Offset(size.width, size.height - xAxisPadding),
                strokeWidth = 1.dp.toPx()
            )

            // 平均值线（灰色虚线）
            drawLine(
                color = Color.Gray,
                start = Offset(0f, averageY),
                end = Offset(size.width, averageY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )

            // 绘制折线图
            val path = Path().apply {
                dataPoints.forEachIndexed { index, value ->
                    val x = (index / maxX.toFloat()) * size.width
                    val y = size.height - xAxisPadding - ((value - minY) / (maxY - minY)) * (size.height - xAxisPadding)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 绘制横坐标
            xAxisLabels.forEachIndexed { index, label ->
                if (label.isNotEmpty()) { // 仅绘制非空标签
                    val x = (index.toFloat() / maxX) * size.width
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        size.height - 10.dp.toPx(), // 确保标签位于 Canvas 的底部
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // 绘制所有点
            dataPoints.forEachIndexed { index, value ->
                val x = (index / maxX.toFloat()) * size.width
                val y = size.height - xAxisPadding - ((value - minY) / (maxY - minY)) * (size.height - xAxisPadding)

                drawCircle(
                    color = Color.Black,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // 绘制选中点
            if (selectedIndex >= 0 && selectedIndex < dataPoints.size) {
                val x = (selectedIndex / maxX.toFloat()) * size.width
                val y =
                    size.height - xAxisPadding - ((dataPoints[selectedIndex] - minY) / (maxY - minY)) * (size.height - xAxisPadding)
                // 画选择的点
                drawCircle(color = Color.Black, radius = 3.dp.toPx(), center = Offset(x, y))
                // 显示选择的数据
                if(isYearSelected){drawContext.canvas.nativeCanvas.drawText(
                    "${selectedIndex + 1}월  ₩${dataPoints[selectedIndex].toInt()}",
                    x,
                    y - 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 46f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
                } else {
                    drawContext.canvas.nativeCanvas.drawText(
                        "${selectedIndex + 1}일  ₩${dataPoints[selectedIndex].toInt()}",
                        x,
                        y - 16.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 46f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

            }
        }
    }
}

fun generateXAxisLabels(dataSize: Int): List<String> {
    // 指定要显示标签的索引
    val indices = listOf(0, 4, 9, 14, 19, 24, dataSize - 1)
    return List(dataSize) { index ->
        if (index in indices) String.format("%02d", index + 1) else ""
    }
}

fun generateXAxisLabelsForYear(): List<String> {
    return (1..12).map { String.format("%02d", it) }
}

//横向柱状图实现
@Composable
fun HorizontalBarChartWithClick(
    data: List<Pair<String, Float>>, // 数据格式：标签和数值
    maxValue: Float,                 // 数据中的最大值，用于计算比例
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit, // 点击事件，传递类别名称
    viewModel: BillViewModel = viewModel()
) {

    // 整体外框
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(375.dp) // 设置黑色框的固定高度
            //.background(Color.White)
            //.border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
            //.padding(8.dp)
            .pointerInput(Unit) { // 增加鼠标滚动支持
                detectTransformGestures { _, _, zoom, _ ->
                    // 支持鼠标滚动的相关逻辑
                }
            }
    ) {
        // 使用 LazyColumn 实现滚动
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
                                    .offset(y = (4).dp),
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
                                val percentage = if (totalValue > 0) (value / totalValue * 100).toInt() else 0

                                // 标签
                                Text(
                                    text = "${label}   ${percentage}%",
                                    modifier = Modifier.weight(1f),
                                    color = Color.Black,
                                )


                                // 金额
                                Text(
                                    text = value.toInt().toString(),
                                    color = Color.Black,
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = TextStyle(fontSize = 18.sp)
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

