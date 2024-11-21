package com.example.myapplication.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamproject.ui.AppViewModel

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPage(
    navController: NavController
) {
    var isIncome by remember { mutableStateOf(true) } // 控制是收入还是支出页面
    var isYearSelected by remember { mutableStateOf(false) } // 控制选择的是年还是月

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    val years = (2000..2024).toList()
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(months[currentMonthIndex]) }

    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    // 使用 CoroutineScope
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isYearSelected) {
            yearListState.scrollToItem(years.indexOf(currentYear))
        } else {
            monthListState.scrollToItem(currentMonthIndex)
        }
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
                            contentDescription = "back",
                            tint = Color.Black,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 横向切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isIncome = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color.Black else Color.Gray
                    )
                ) {
                    Text(text = "Income")
                }

                Button(
                    onClick = { isIncome = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color.Gray else Color.Black
                    )
                ) {
                    Text(text = "Expenditure")
                }
            }

            // 年/月切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        isYearSelected = true
                        // 滚动到当前选择的年份
                        coroutineScope.launch {
                            yearListState.animateScrollToItem(years.indexOf(selectedYear))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYearSelected) Color.Black else Color.Gray
                    )
                ) {
                    Text(text = "Year")
                }

                Button(
                    onClick = {
                        isYearSelected = false
                        // 滚动到当前选择的月份
                        coroutineScope.launch {
                            monthListState.animateScrollToItem(months.indexOf(selectedMonth))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYearSelected) Color.Gray else Color.Black
                    )
                ) {
                    Text(text = "Month")
                }
            }

            // 滑动选择器
            if (isYearSelected) {
                LazyRow(
                    state = yearListState,
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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
            Text(text = "Expenditure Chart")
            CustomLineChart(dataPoints = listOf( 250f,100f, 200f, 150f, 400f, 250f,100f, 200f, 150f, 100f, 250f,100f, 200f, 150f, 200f, 250f,100f, 200f, 150f, 300f, 250f,100f, 200f, 150f, 300f, 250f,)) // 使用自定义折线图
        }
    }
}

@Composable
fun CustomLineChart(dataPoints: List<Float>) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Box(
        modifier = Modifier
            .height(150.dp) // 高度为 200dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .height(200.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val maxX = dataPoints.size - 1
                        val nearestIndex = dataPoints.indices.minByOrNull { index ->
                            val x = (index / maxX.toFloat()) * size.width
                            kotlin.math.abs(offset.x - x)
                        } ?: -1
                        selectedIndex = nearestIndex
                    }
                }
        ) {
            val maxX = dataPoints.size - 1
            val maxY = dataPoints.maxOrNull() ?: 0f
            val minY = dataPoints.minOrNull() ?: 0f
            val zeroY = size.height - ((0 - minY) / (maxY - minY) * size.height)

            // 绘制网格线
            for (i in 1..4) {
                val y = size.height * i / 5
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

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

            // 绘制选中点
            if (selectedIndex >= 0) {
                val x = (selectedIndex / maxX.toFloat()) * size.width
                val y =
                    size.height - ((dataPoints[selectedIndex] - minY) / (maxY - minY)) * size.height
                drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = Offset(x, y))
                drawContext.canvas.nativeCanvas.drawText(
                    "${dataPoints[selectedIndex]}",
                    x,
                    y - 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 32f
                    }
                )
            }
        }
    }
}

