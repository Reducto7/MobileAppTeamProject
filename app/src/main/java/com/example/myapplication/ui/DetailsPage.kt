package com.example.myapplication.ui


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
import androidx.compose.material3.Divider
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
fun DetailsPage(
    navController: NavController,
    category: String
) {
    // 当前选中的维度（周、月、年）
    var selectedTab by remember { mutableStateOf("monthlyData") }

    // 样本数据
    //val weeklyData = listOf(100f, 150f, 120f, 180f, 200f, 130f, 160f) // 周折线图数据
    val monthlyData = listOf(4000f, 4500f, 3000f, 3500f, 3800f)       // 月折线图数据
    val yearlyData = listOf(48000f, 52000f, 60000f, 55000f, 58000f)  // 年折线图数据

    //val weeklyBarData = listOf("食品" to 150f, "购物" to 200f, "交通" to 100f)
    val monthlyBarData = listOf("食品" to 800f, "购物" to 1200f, "交通" to 600f)
    val yearlyBarData = listOf("食品" to 9600f, "购物" to 12000f, "交通" to 7800f)

    //val weeklyPieData = listOf("食品" to 40f, "购物" to 30f, "交通" to 30f)
    val monthlyPieData = listOf("食品" to 35f, "购物" to 40f, "交通" to 25f)
    val yearlyPieData = listOf("食品" to 50f, "购物" to 30f, "交通" to 20f)

    // 当前显示的数据
    val lineChartData = when (selectedTab) {
       // "Week" -> weeklyData
        "Month" -> monthlyData
        "Year" -> yearlyData
        else -> monthlyData
    }
    val barChartData = when (selectedTab) {
        //"Week" -> weeklyBarData
        "Month" -> monthlyBarData
        "Year" -> yearlyBarData
        else -> monthlyPieData
    }
    val pieChartData = when (selectedTab) {
        //"Week" -> weeklyPieData
        "Month" -> monthlyPieData
        "Year" -> yearlyPieData
        else -> monthlyPieData
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
            // 周/月/年 切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Month", "Year").forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tab) Color.Black else Color.Gray,
                            contentColor = if (selectedTab == tab) Color.White else Color.Black
                        )
                    ) {
                        Text(tab)
                    }
                }
            }

            // 折线图
            DividerWithText("Trend")
            CustomLineChartWithAxis(dataPoints = lineChartData)

            // 横向柱状图
            DividerWithText("Breakdown")
            HorizontalBarChart(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f
            )

            // 饼状图
            DividerWithText("Distribution")
            PieChart(data = pieChartData)
        }
    }
}




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