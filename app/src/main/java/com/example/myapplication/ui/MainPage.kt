package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel

data class Bill(
    val id: Int,
    val isIncome: Boolean,
    val category: String,
    val remarks: String,
    val amount: Double,
    val date: String
)

class BillViewModel : ViewModel() {
    private val _bills = mutableStateListOf<Bill>()
    val bills: List<Bill> get() = _bills

    fun addBill(bill: Bill) {
        _bills.add(bill)
    }

    fun refreshBills() {
        // 模拟数据
        _bills.clear()
        _bills.addAll(
            listOf(
                Bill(id = 1, isIncome = false, category = "Food", remarks = "Lunch", amount = 30.0, date = "2024-11-22"),
                Bill(id = 2, isIncome = false, category = "Drink", remarks = "Coffee", amount = 15.0, date = "2024-11-21")
            )
        )
    }
}



@Composable
fun BillItem(bill: Bill, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // 透明背景
        ),
        border = BorderStroke(1.dp, Color.Black), // 黑色边框
        elevation = CardDefaults.cardElevation(0.dp) // 去掉阴影效果
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = bill.remarks,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "￥${"%.2f".format(bill.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (bill.isIncome) Color.Green else Color.Red,
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController, modifier: Modifier = Modifier, viewModel: BillViewModel = viewModel()) {
    val bills by remember { derivedStateOf { viewModel.bills } }
    var totalIncome by remember { mutableStateOf(0.0) }
    var totalExpense by remember { mutableStateOf(0.0) }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // 添加模拟账单数据
    LaunchedEffect(Unit) {
        viewModel.addBill(Bill(id = 1, isIncome = false, category = "Food", remarks = "Lunch", amount = 30.0, date = "2024-11-22"))
        viewModel.addBill(Bill(id = 2, isIncome = false, category = "Drink", remarks = "Coffee", amount = 15.0, date = "2024-11-21"))
        viewModel.addBill(Bill(id = 3, isIncome = false, category = "Transport", remarks = "Bus Ticket", amount = 10.0, date = "2024-11-20"))
        viewModel.addBill(Bill(id = 4, isIncome = true, category = "Salary", remarks = "November Salary", amount = 3000.0, date = "2024-11-19"))
        viewModel.addBill(Bill(id = 5, isIncome = false, category = "Entertainment", remarks = "Movie", amount = 50.0, date = "2024-11-18"))
        viewModel.addBill(Bill(id = 6, isIncome = false, category = "Groceries", remarks = "Supermarket", amount = 120.0, date = "2024-11-17"))
        viewModel.addBill(Bill(id = 7, isIncome = true, category = "Freelance", remarks = "Freelance Project", amount = 500.0, date = "2024-11-16"))
        viewModel.addBill(Bill(id = 8, isIncome = false, category = "Utilities", remarks = "Electricity Bill", amount = 60.0, date = "2024-11-15"))
        viewModel.addBill(Bill(id = 9, isIncome = false, category = "Shopping", remarks = "Clothes", amount = 200.0, date = "2024-11-14"))
        viewModel.addBill(Bill(id = 10, isIncome = true, category = "Gift", remarks = "Birthday Gift", amount = 100.0, date = "2024-11-13"))

        // 更新总收入和总支出
        totalIncome = viewModel.bills.filter { it.isIncome }.sumOf { it.amount }
        totalExpense = viewModel.bills.filter { !it.isIncome }.sumOf { it.amount }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black
            ) {
                Spacer(modifier = Modifier.width(60.dp))
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "List",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                IconButton(onClick = { navController.navigate("chart") }) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Graph",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                IconButton(onClick = { navController.navigate("mypage") }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Setting",
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addNewBill") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }
    )

    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        delay(1000)
                        // 在此刷新操作中重新加载数据
                        isRefreshing = false
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "11月",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "本月总收入: ￥${"%.2f".format(totalIncome)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = Color.Black
                        )
                        Text(
                            text = "本月总支出: ￥${"%.2f".format(totalExpense)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                        color = Color.LightGray
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(bills.sortedByDescending { it.date }) { bill ->
                            BillItem(bill) {
                                navController.navigate("editBill/${bill.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}
