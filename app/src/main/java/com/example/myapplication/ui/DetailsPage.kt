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
import androidx.navigation.NavController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsPage(
    navController: NavController,
    category: String,
    viewModel: BillViewModel = viewModel()
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var isMonthlyView by remember { mutableStateOf(true) } // 默认为月视图
    var sortByAmount by remember { mutableStateOf(true) } // 默认按金额排序

    var lineChartData by remember { mutableStateOf(emptyList<Float>()) }
    var barChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }
    var pieChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }

    var isIncomeSelected by remember{mutableStateOf(false)} // 控制收入或支出

    // 更新数据
    LaunchedEffect(selectedYear, selectedMonth, sortByAmount) {
        val bills = viewModel.getBillsForCategory(category, selectedYear, selectedMonth)

        lineChartData = viewModel.getMonthlyTotalsForCategory(category, selectedYear, viewModel.isIncomeSelected)

        barChartData = if (sortByAmount) {
            bills.sortedByDescending { it.amount }
        } else {
            bills.sortedBy { it.date }
        }.map { it.remarks to it.amount.toFloat() }

        pieChartData = viewModel.getPieChartDataForCategory(category, selectedYear, selectedMonth, viewModel.isIncomeSelected)
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
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isMonthlyView = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonthlyView) Color.Black else Color.White,
                        contentColor = if (isMonthlyView) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                ) {
                    Text("Month")
                }
                Button(
                    onClick = { isMonthlyView = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMonthlyView) Color.Black else Color.White,
                        contentColor = if (!isMonthlyView) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        )
                ) {
                    Text("Year")
                }
            }

            // 折线图
            DividerWithText("Trend")
            CustomLineChartWithAxis(dataPoints = lineChartData)

            // 排序切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { sortByAmount = true }) {
                    Text("Sort by Amount", color = if (sortByAmount) Color.Black else Color.Gray)
                }
                TextButton(onClick = { sortByAmount = false }) {
                    Text("Sort by Date", color = if (!sortByAmount) Color.Black else Color.Gray)
                }
            }

            // 柱状图
            DividerWithText("Breakdown")
            HorizontalBarChart(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f
            )

            // 饼图
            DividerWithText("Distribution")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PieChart(data = pieChartData)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(pieChartData.sortedByDescending { it.second }) { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, modifier = Modifier.weight(1f))
                            Text("${value.toInt()}%")
                        }
                    }
                }
            }
        }
    }
}
/*
@Composable
fun <T> DropdownMenuBox(label: String, items: List<T>, selectedItem: T, onItemSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black)
        ) {
            Text("$label: $selectedItem")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(onClick = {
                    onItemSelected(it)
                    expanded = false
                }, text = { Text(it.toString()) })
            }
        }
    }
}
 */

//折线图组件
@Composable
fun CustomLineChartWithAxis(dataPoints: List<Float>) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val tickInterval = 1000 // x 轴刻度间隔

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)) {
        val spacing = size.width / (dataPoints.size - 1)
        val maxHeight = size.height

        // 绘制 x 轴
        drawLine(
            color = Color.Black,
            start = Offset(0f, maxHeight),
            end = Offset(size.width, maxHeight),
            strokeWidth = 2f
        )
        // 绘制 x 轴刻度
        val tickCount = (maxValue / tickInterval).toInt()
        for (i in 0..tickCount) {
            val x = spacing * i
            drawLine(
                color = Color.Black,
                start = Offset(x, maxHeight),
                end = Offset(x, maxHeight + 10f),
                strokeWidth = 2f
            )
            // 显示刻度值
            drawContext.canvas.nativeCanvas.drawText(
                "${i * tickInterval}",
                x,
                maxHeight + 30f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                }
            )
        }

        // 绘制折线
        dataPoints.forEachIndexed { index, value ->
            if (index < dataPoints.size - 1) {
                val x1 = spacing * index
                val y1 = maxHeight - (value / maxValue * maxHeight)
                val x2 = spacing * (index + 1)
                val y2 = maxHeight - (dataPoints[index + 1] / maxValue * maxHeight)

                drawLine(
                    color = Color.Black,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }
        }
    }
}


//柱状图
@Composable
fun HorizontalBarChart(
    data: List<Pair<String, Float>>,
    maxValue: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        data.sortedByDescending { it.second }.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .weight(4f)
                        .height(20.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = value / maxValue)
                            .height(20.dp)
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}


//饼状图组件
@Composable
fun PieChart(data: List<Pair<String, Float>>) {
    val total = data.map { it.second }.sum()
    val angles = data.map { it.second / total * 360f }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        var startAngle = 0f

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.minDimension / 2f

        angles.forEachIndexed { index, sweepAngle ->
            // 绘制扇形
            drawArc(
                color = Color(0xFF000000 + (0xFFFFFF / data.size * index).toInt()),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
            )

            // 计算数值显示位置
            val angle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val x = (centerX + radius / 1.5 * kotlin.math.cos(angle)).toFloat()
            val y = (centerY + radius / 1.5 * kotlin.math.sin(angle)).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                "${data[index].second.toInt()}",
                x,
                y,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
            startAngle += sweepAngle
        }
    }
}


