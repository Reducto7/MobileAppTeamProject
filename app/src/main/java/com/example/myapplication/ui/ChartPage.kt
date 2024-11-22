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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
    var dataPoints by remember { mutableStateOf(listOf<Float>()) }
    var xAxisLabels by remember { mutableStateOf(listOf<String>()) }

    // 模拟数据
    val simulatedData = mapOf(
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
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IsIncome()

            TimeSelection { month, year ->
                // 更新数据和横坐标标签
                val selectedData = simulatedData[month] ?: List(31) { 0f }
                dataPoints = selectedData
                xAxisLabels = generateXAxisLabels(selectedData.size)
            }


            CustomLineChart(dataPoints, xAxisLabels)

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
fun TimeSelection(onMonthYearSelected: (String, Int) -> Unit) {
    var isYearSelected by remember { mutableStateOf(false) } // 控制选择的是年还是月

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    val years = (2020..2025).toList()
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(months[currentMonthIndex]) }

    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    //通知选择更改
    LaunchedEffect(selectedMonth, selectedYear) {
        onMonthYearSelected(selectedMonth, selectedYear)
    }

    // 使用 CoroutineScope
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isYearSelected) {
            yearListState.scrollToItem(years.indexOf(currentYear))
        } else {
            monthListState.scrollToItem(currentMonthIndex)
        }
    }

    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
    ) {
        // 年/月切换按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent) // 去掉背景色
                .padding(4.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Month 按钮
            Button(
                onClick = {
                    isYearSelected = false
                    coroutineScope.launch {
                        monthListState.animateScrollToItem(months.indexOf(selectedMonth))
                    }
                },
                border = BorderStroke(2.dp,Color(0xFFF5F2FA)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isYearSelected) Color(0xFFFFFBFE) else Color.Black, // 选中按钮为黑色
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f), // 等宽按钮
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            ) {
                Text(
                    text = "Month",
                    color = if (isYearSelected) Color.Black else Color(0xFFFFFBFE)
                )
            }

            // Year 按钮
            Button(
                onClick = {
                    isYearSelected = true
                    coroutineScope.launch {
                        yearListState.animateScrollToItem(years.indexOf(selectedYear))
                    }
                },
                border = BorderStroke(2.dp,Color(0xFFF5F2FA)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isYearSelected) Color.Black else Color(0xFFFFFBFE), // 选中按钮为黑色
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f), // 等宽按钮
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
            ) {
                Text(
                    text = "Year",
                    color = if (isYearSelected) Color.White else Color.Black
                )
            }
        }

        // 滑动选择器
        if (isYearSelected) {
            LazyRow(
                state = yearListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp) // 内部间距
                    .border(1.dp, Color(0xFFF5F2FA), RoundedCornerShape(8.dp)) // 添加边框
                    .clip(RoundedCornerShape(8.dp)) // 确保内容适配圆角
                    .background(Color(0xFFFFFBFE)), // 设置背景颜色
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
            LazyRow(
                state = monthListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp) // 内部间距
                    .border(1.dp, Color(0xFFF5F2FA), RoundedCornerShape(8.dp)) // 添加边框
                    .clip(RoundedCornerShape(8.dp)) // 确保内容适配圆角
                    .background(Color(0xFFFFFBFE)), // 设置背景颜色
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
fun CustomLineChart(dataPoints: List<Float>, xAxisLabels: List<String>) {
    var selectedIndex by remember { mutableStateOf(-1) } // 选中点的索引

    val maxX = dataPoints.size - 1
    val maxY = dataPoints.maxOrNull() ?: 0f
    val minY = 0f


    Box(
        modifier = Modifier
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // 检测最近点
                        val nearestIndex = dataPoints.indices.minByOrNull { index ->
                            val x = (index / maxX.toFloat()) * size.width
                            kotlin.math.abs(offset.x - x)
                        } ?: -1
                        selectedIndex = nearestIndex
                    }
                }
        ) {

            // 绘制网格线
            val averageValue = if (dataPoints.isNotEmpty()) dataPoints.average().toFloat() else 0f
            val averageY = size.height - ((averageValue - minY) / (maxY - minY)) * size.height

            // 零刻度线
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )

            // 平均值线（灰色虚线）
            drawLine(
                color = Color.Gray,
                start = Offset(0f, averageY),
                end = Offset(size.width, averageY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // 虚线效果
            )


            // 绘制折线图
            val path = Path().apply {
                dataPoints.forEachIndexed { index, value ->
                    val x = (index / maxX.toFloat()) * size.width
                    val y = size.height - ((value - minY) / (maxY - minY)) * size.height
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            //绘制横坐标
            xAxisLabels.forEachIndexed { index, label ->
                if (label.isNotEmpty()) { // 仅绘制非空标签
                    val x = (index.toFloat() / maxX) * size.width
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        size.height + 20.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // 绘制所有点
            dataPoints.forEachIndexed { index, value ->
                val x = (index / maxX.toFloat()) * size.width
                val y = size.height - ((value - minY) / (maxY - minY)) * size.height

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
            if (selectedIndex >= 0) {
                val x = (selectedIndex / maxX.toFloat()) * size.width
                val y =
                    size.height - ((dataPoints[selectedIndex] - minY) / (maxY - minY)) * size.height
                //画选择的点
                drawCircle(color = Color.Black, radius = 3.dp.toPx(), center = Offset(x, y))
                // 显示选择的数据
                drawContext.canvas.nativeCanvas.drawText(
                    "Day${selectedIndex + 1} : ${dataPoints[selectedIndex].toInt()}",
                    x,
                    y - 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
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
