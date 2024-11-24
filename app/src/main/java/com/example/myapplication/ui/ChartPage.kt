package com.example.myapplication.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPage(
    navController: NavController
) {
    // 选中点的索引
    var selectedIndex by remember { mutableStateOf(-1) }

    var dataPoints by remember { mutableStateOf(listOf<Float>()) }
    var xAxisLabels by remember { mutableStateOf(listOf<String>()) }
    var isYearSelected by remember { mutableStateOf(false) } // 外部管理的状态
    var barChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }


    // 动态获取当前年份和月份
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonth = months[currentMonthIndex]


    // 模拟数据(折线图)
    val simulatedMonthData = mapOf(
        "October" to listOf(
            150f, 100f, 300f, 150f, 400f, 0f, 200f, 200f, 150f, 200f,
            500f, 100f, 0f, 150f, 200f, 250f, 100f, 200f, 150f, 100f,
            250f, 300f, 200f, 0f, 300f, 250f, 200f, 150f, 400f, 250f,
            100f
        ),
        "November" to listOf(
            250f, 100f, 200f, 150f, 400f, 250f, 50f, 200f, 150f, 100f,
            250f, 100f, 200f, 150f, 200f, 250f, 100f, 200f, 150f, 300f,
            250f, 100f, 150f, 150f, 300f,250f, 100f, 150f, 150f, 400f
        )
    )

    val simulatedYearData = mapOf(
        2023 to listOf(1500f, 1100f, 4200f, 500f, 3000f, 2600f, 400f, 2200f, 600f, 1200f, 1500f, 2800f),
        2024 to listOf(1000f, 1500f, 2300f, 800f, 3000f, 1800f, 2000f, 3800f, 500f, 1300f, 1200f, 2400f)
        // 添加其他年份数据
    )

    // 模拟数据（柱状图）
    val simulatedMonthBarData = mapOf(
        "October" to listOf("Food" to 300f, "Shopping" to 301f, "Rent" to 230f, "Daily" to 200f, "Transport" to 100f),
        "November" to listOf("Food" to 250f, "Shopping" to 220f, "Rent" to 100f, "Daily" to 150f, "Transport" to 200f)
    )

    val simulatedYearBarData = mapOf(
        2023 to listOf("Food" to 3600f, "Shopping" to 4200f, "Rent" to 4600f, "Daily" to 2400f, "Transport" to 3600f),
        2024 to listOf("Food" to 3200f, "Shopping" to 4800f, "Rent" to 4200f, "Daily" to 2200f, "Transport" to 3800f)
    )

    // 初始化默认值
    LaunchedEffect(Unit) {
        dataPoints = simulatedMonthData[currentMonth] ?: List(31) { 0f }
        barChartData = simulatedMonthBarData[currentMonth] ?: emptyList()
        xAxisLabels = generateXAxisLabels(dataPoints.size)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Chart") },
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
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //收入/支出选项
            IsIncome()

            // 时间选择器
            TimeSelection(
                onMonthYearSelected = { month, year ->
                    if (isYearSelected) {
                        val yearData = simulatedYearData[year] ?: List(12) { 0f }
                        dataPoints = yearData
                        barChartData = simulatedYearBarData[year] ?: emptyList()
                        xAxisLabels = generateXAxisLabelsForYear()
                    } else {
                        val monthData = simulatedMonthData[month] ?: List(31) { 0f }
                        dataPoints = monthData
                        barChartData = simulatedMonthBarData[month] ?: emptyList()
                        xAxisLabels = generateXAxisLabels(monthData.size)
                    }
                    selectedIndex = -1 // 重置选中状态
                },
                isYearSelected = isYearSelected,
                onSelectionChanged = { isYear ->
                    isYearSelected = isYear
                    if (isYear) {
                        dataPoints = simulatedYearData[currentYear] ?: List(12) { 0f }
                        xAxisLabels = generateXAxisLabelsForYear()
                    } else {
                        dataPoints = simulatedMonthData[currentMonth] ?: List(31) { 0f }
                        xAxisLabels = generateXAxisLabels(dataPoints.size)
                    }
                    selectedIndex = -1 // 重置选中状态
                },
                currentYear = currentYear,
                currentMonth = currentMonth
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Line Chart分隔线
            DividerWithText("Line Chart")

            // 折线图
            CustomLineChart(
                dataPoints = dataPoints,
                xAxisLabels = xAxisLabels,
                selectedIndex = selectedIndex,
                onSelectedIndexChanged = { newIndex ->
                    selectedIndex = newIndex
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bar Chart分割线
            DividerWithText("Bar Chart")

            // 横向柱状图
            HorizontalBarChart(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f
            )
        }
    }
}

@Composable
fun IsIncome(){
    var isIncome by remember { mutableStateOf(true) } // 控制是收入还是支出页面

    // 横向切换按钮
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = { isIncome = true },
            shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
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

        OutlinedButton(
            onClick = { isIncome = false },
            shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
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
    }
}

@Composable
fun TimeSelection(
    onMonthYearSelected: (String, Int) -> Unit,
    isYearSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    currentYear: Int,
    currentMonth: String
) {
    val years = (2020..2025).toList()
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    // 通知选择更改
    LaunchedEffect(selectedMonth, selectedYear) {
        onMonthYearSelected(selectedMonth, selectedYear)
    }

    LaunchedEffect(Unit) {
        if (isYearSelected) {
            yearListState.scrollToItem(years.indexOf(currentYear))
        } else {
            monthListState.scrollToItem(months.indexOf(currentMonth))
        }
    }

    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    onSelectionChanged(false) // 切换到月份模式
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isYearSelected) Color.White else Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            ) {
                Text(
                    text = "Month",
                    color = if (isYearSelected) Color.Black else Color.White
                )
            }

            Button(
                onClick = {
                    onSelectionChanged(true) // 切换到年份模式
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isYearSelected) Color.Black else Color.White,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
            ) {
                Text(
                    text = "Year",
                    color = if (isYearSelected) Color.White else Color.Black
                )
            }
        }

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
    }
}


@Composable
fun CustomLineChart(
    dataPoints: List<Float>,
    xAxisLabels: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit
) {
    val maxX = if (dataPoints.isNotEmpty()) dataPoints.size - 1 else 1
    val maxY = dataPoints.maxOrNull() ?: 0f
    val minY = 0f

    // 使用 OutlinedCard 包裹整个折线图
    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp) // 内边距，给折线图留出绘制空间
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
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
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
                    drawContext.canvas.nativeCanvas.drawText(
                        "${selectedIndex + 1} : ${dataPoints[selectedIndex].toInt()}",
                        x,
                        y - 16.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 40f
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
fun HorizontalBarChart(
    data: List<Pair<String, Float>>, // 数据格式：标签和数值
    maxValue: Float,                 // 数据中的最大值，用于计算比例
    modifier: Modifier = Modifier
) {
    // 将数据按数值降序排列
    val sortedData = data.sortedByDescending { it.second }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sortedData.forEach { (label, value) ->
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示标签
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // 横向柱状条
                    Box(
                        modifier = Modifier
                            .weight(4f)
                            .height(20.dp)
                            .background(Color.White) // 设置白色背景
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp)) // 保证圆角效果
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = value / maxValue) // 动态调整黑条宽度
                                .height(20.dp)
                                .background(Color.Black, RoundedCornerShape(10.dp))
                        )
                    }
                }
                // 数值显示在柱状图右上角
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = value.toInt().toString(),
                        //text = "${value.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}