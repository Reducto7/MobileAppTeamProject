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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class ChartViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("bills")
    private val _bills = mutableStateListOf<Bill>()
    val bills: List<Bill> get() = _bills

    val monthData: MutableMap<String, List<Double>> = mutableMapOf()
    val yearData: MutableMap<Int, List<Double>> = mutableMapOf()

    var isIncomeSelected by mutableStateOf(false) // 控制收入或支出

    init {
        fetchBillsFromDatabase()
    }

    private fun fetchBillsFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _bills.clear()
                    snapshot.children.mapNotNull { it.toBill() }.let { _bills.addAll(it) }
                    println("加载的账单数据: $_bills")
                    updateData() // 数据加载完成后更新 monthData
                }

                override fun onCancelled(error: DatabaseError) {
                    println("数据加载失败: ${error.message}")
                }
            })
        }
    }
   fun updateData() {
       monthData["1월"] = getDailyIncomeExpenditure(2024, 1, isIncomeSelected)
       monthData["2월"] = getDailyIncomeExpenditure(2024, 2, isIncomeSelected)
       monthData["3월"] = getDailyIncomeExpenditure(2024, 3, isIncomeSelected)
       monthData["4월"] = getDailyIncomeExpenditure(2024, 4, isIncomeSelected)
       monthData["5월"] = getDailyIncomeExpenditure(2024, 5, isIncomeSelected)
       monthData["6월"] = getDailyIncomeExpenditure(2024, 6, isIncomeSelected)
       monthData["7월"] = getDailyIncomeExpenditure(2024, 7, isIncomeSelected)
       monthData["8월"] = getDailyIncomeExpenditure(2024, 8, isIncomeSelected)
       monthData["9월"] = getDailyIncomeExpenditure(2024, 9, isIncomeSelected)
       monthData["10월"] = getDailyIncomeExpenditure(2024, 10, isIncomeSelected)
       monthData["11월"] = getDailyIncomeExpenditure(2024, 11, isIncomeSelected)
       monthData["12월"] = getDailyIncomeExpenditure(2024, 12, isIncomeSelected)


        // 更新年数据
        yearData[2020] = getMonthlyIncomeExpenditure(2020, isIncomeSelected)
        yearData[2021] = getMonthlyIncomeExpenditure(2021, isIncomeSelected)
        yearData[2022] = getMonthlyIncomeExpenditure(2022, isIncomeSelected)
        yearData[2023] = getMonthlyIncomeExpenditure(2023, isIncomeSelected)
        yearData[2024] = getMonthlyIncomeExpenditure(2024, isIncomeSelected)
    }


    private fun DataSnapshot.toBill(): Bill? {
        return try {
            val id = child("id").getValue(Int::class.java) ?: 0
            val isIncome = child("income").getValue(Boolean::class.java) ?: false
            val category = child("category").getValue(String::class.java) ?: ""
            val remarks = child("remarks").getValue(String::class.java) ?: ""
            val amount = child("amount").getValue(Double::class.java) ?: 0.0
            val date = child("date").getValue(String::class.java) ?: ""

            Bill(id, isIncome, category, remarks, amount, date)
        } catch (e: Exception) {
            println("数据解析失败: $e")
            null
        }
    }

    // 计算某月每天的收入或支出总和
    fun getDailyIncomeExpenditure(year: Int, month: Int, isIncome: Boolean): List<Double> {
        val filteredBills = _bills.filter {
            val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    billDate[1] == month &&
                    it.isIncome == isIncome
        }

        val daysInMonth = when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        val dailyTotals = MutableList(daysInMonth) { 0.0 }

        filteredBills.forEach {
            val day = it.date.split("-")[2].toIntOrNull() ?: 1
            if (day in 1..daysInMonth) dailyTotals[day - 1] += it.amount
        }

        return dailyTotals
    }

    // 计算某年每月的收入或支出总和
    fun getMonthlyIncomeExpenditure(year: Int, isIncome: Boolean): List<Double> {
        val filteredBills = _bills.filter {
            val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    it.isIncome == isIncome
        }

        val monthlyTotals = MutableList(12) { 0.0 }

        filteredBills.forEach {
            val month = it.date.split("-")[1].toIntOrNull() ?: 1
            if (month in 1..12) monthlyTotals[month - 1] += it.amount
        }

        return monthlyTotals
    }

    // 判断是否为闰年
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPage(
    navController: NavController,
    viewModel: ChartViewModel = viewModel()
) {
    // 选中点的索引
    var selectedIndex by remember { mutableStateOf(-1) }

    var dataPoints by remember { mutableStateOf(listOf<Double>()) }
    var xAxisLabels by remember { mutableStateOf(listOf<String>()) }
    var isYearSelected by remember { mutableStateOf(false) } // 外部管理的状态
    var barChartData by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }

    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<String?>(null) } // 默认没有选择月份

    val months = listOf(
        "1월", "2월", "3월", "4월", "5월", "6월",
        "7월", "8월", "9월", "10월", "11월", "12월"
    )

    val years = (2020..2024).toList()

    var selectedOption by remember { mutableStateOf("지출 차트") } // 动态标题内容
    var isDropdownExpanded by remember { mutableStateOf(false) } // 控制下拉框显示

    // 初始化默认值
    LaunchedEffect(viewModel.isIncomeSelected) {
        // 当收入/支出选项变化时，更新数据
        if (isYearSelected) {
            val yearData = viewModel.yearData[selectedYear]?.map { it.toDouble() } ?: List(12) { 0.0 }
            dataPoints = yearData
            xAxisLabels = generateXAxisLabelsForYear()
        } else {
            val monthData = viewModel.monthData[selectedMonth]?.map { it.toDouble() } ?: List(31) { 0.0 }
            dataPoints = monthData
            xAxisLabels = generateXAxisLabels(monthData.size)
        }
    }

    // 模拟数据（柱状图）
    val simulatedMonthBarData = mapOf(
        "October" to listOf("Food" to 300f, "Shopping" to 301f, "Rent" to 230f, "Daily" to 200f, "Transport" to 100f, "Stocks" to 150f, "Utilities" to 180f, "Healthcare" to 210f, "Education" to 120f, "Entertainment" to 250f, "Others" to 90f),
        "November" to listOf("Food" to 250f, "Shopping" to 220f, "Rent" to 100f, "Daily" to 150f, "Transport" to 200f, "Stocks" to 150f, "Utilities" to 170f, "Healthcare" to 190f, "Education" to 140f, "Entertainment" to 230f, "Others" to 80f)
    )
    val simulatedYearBarData = mapOf(
        2023 to listOf("Food" to 3600f, "Shopping" to 4200f, "Rent" to 4600f, "Daily" to 2400f, "Transport" to 3600f, "Stocks" to 1500f, "Utilities" to 2000f, "Healthcare" to 2200f, "Education" to 1800f, "Entertainment" to 2700f, "Others" to 1300f),
        2024 to listOf("Food" to 3200f, "Shopping" to 4800f, "Rent" to 4200f, "Daily" to 2200f, "Transport" to 3800f, "Stocks" to 1500f, "Utilities" to 2100f, "Healthcare" to 2300f, "Education" to 1900f, "Entertainment" to 2600f, "Others" to 1400f)
    )

    // 初始化默认值
    LaunchedEffect(Unit) {
        dataPoints = List(31) { 0.0 } // 空的折线图数据
        xAxisLabels = generateXAxisLabels(dataPoints.size)
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
                                // 更新逻辑为支出图表
                                selectedOption = "지출 차트" // 更新标题
                                viewModel.isIncomeSelected = false
                                viewModel.updateData()  // 更新数据
                                isDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("수입 차트") },
                            onClick = {
                                // 更新逻辑为收入图表
                                selectedOption = "수입 차트" // 更新标题
                                viewModel.isIncomeSelected = true
                                viewModel.updateData()  // 更新数据

                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            //verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 时间选择器
            val yearListState = rememberLazyListState()
            val monthListState = rememberLazyListState()

            // 通知选择更改
            LaunchedEffect(selectedMonth, selectedYear) {
                if (isYearSelected) {
                    val yearData = viewModel.yearData[selectedYear]?.map { it.toDouble() } ?: List(12) { 0.0 }
                    dataPoints = yearData
                    xAxisLabels = generateXAxisLabelsForYear()
                } else {
                    val monthData = viewModel.monthData[selectedMonth]?.map { it.toDouble() } ?: List(31) { 0.0 }
                    dataPoints = monthData
                    xAxisLabels = generateXAxisLabels(monthData.size)
                }
                selectedIndex = -1 // 重置选中状态
            }

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
                        val monthData = viewModel.monthData[selectedMonth]?.map { it.toDouble() } ?: List(31) { 0.0 }
                        dataPoints = monthData
                        xAxisLabels = generateXAxisLabels(monthData.size)
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
                        text = "당년   월별",
                        color = if (isYearSelected) Color.Black else Color.White
                    )
                }

                Button(
                    onClick = {
                        isYearSelected = true // 切换到年份模式
                        val yearData = viewModel.yearData[selectedYear]?.map { it.toDouble() } ?: List(12) { 0.0 }
                        dataPoints = yearData
                        xAxisLabels = generateXAxisLabelsForYear()
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
                        text = "각년   년별",
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

            // Line Chart分隔线
            //DividerWithText("Chart Table")

            // 折线图
            CustomLineChart(
                dataPoints = dataPoints.map { it.toFloat() },
                xAxisLabels = xAxisLabels,
                selectedIndex = selectedIndex,
                onSelectedIndexChanged = { newIndex ->
                    selectedIndex = newIndex
                },
                isYearSelected = isYearSelected
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 横向柱状图
            HorizontalBarChartWithClick(
                data = barChartData,
                maxValue = barChartData.maxOfOrNull { it.second } ?: 1f,
                onCategoryClick = { category ->
                    navController.navigate("details/$category")
                }
            )
        }
    }
}



@Composable
fun CustomLineChart(
    dataPoints: List<Float>,
    xAxisLabels: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    isYearSelected: Boolean
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
                    if (isYearSelected){
                        drawContext.canvas.nativeCanvas.drawText(
                            "${selectedIndex + 1}월 : ${dataPoints[selectedIndex].toInt()}원",
                            x,
                            y - 16.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 40f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }else{
                        drawContext.canvas.nativeCanvas.drawText(
                            "${selectedIndex + 1}일 : ${dataPoints[selectedIndex].toInt()}원",
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
fun HorizontalBarChartWithClick(
    data: List<Pair<String, Float>>, // 数据格式：标签和数值
    maxValue: Float,                 // 数据中的最大值，用于计算比例
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit, // 点击事件，传递类别名称
) {
    // 整体外框
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp) // 设置黑色框的固定高度
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(data.sortedByDescending { it.second }) { (label, value) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryClick(label) }, // 点击事件
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 显示标签
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            color = Color.Black
                        )

                        // 横向柱状条
                        Box(
                            modifier = Modifier
                                .weight(4f)
                                .height(20.dp)
                                .background(Color.White) // 设置白色背景
                                .border(
                                    width = 1.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = value / maxValue) // 动态调整黑条宽度
                                    .height(20.dp)
                                    .background(Color.Black)
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
                            color = Color.Black,
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
    /*
    val visibleItemCount = 4 // 默认显示 4 条数据
    val itemHeight = 36.dp // 单条柱状图高度 + 间距

    // 整体外框
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItemCount) // 限制 LazyColumn 高度
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(8.dp) // 黑色框内的内边距
    ) {
        // 使用 LazyColumn 实现滚动
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 将数据按数值降序排列
            val sortedData = data.sortedByDescending { it.second }

            items(sortedData) { (label, value) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryClick(label) }, // 点击事件
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

     */

/*
// 整体外框
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp) // 调整高度，根据需要更改
        .background(Color.White)
        .border(
            width = 2.dp,
            color = Color.Black,
            shape = RoundedCornerShape(10.dp)
        )
        .padding(8.dp)
){
    // 使用 LazyColumn 实现滚动
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 将数据按数值降序排列
        val sortedData = data.sortedByDescending { it.second }

        items(sortedData) { (label, value) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(label) }, // 点击事件
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
                                color = Color.White,
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}
}

        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(label) }, // 点击事件,
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
                                color = Color.White,
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
}


*/