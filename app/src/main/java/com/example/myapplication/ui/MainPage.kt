package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.models.Bill
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.models.FinanceViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun BillItem(bill: Bill, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = bill.description, style = MaterialTheme.typography.bodyLarge)
            Text(text = "￥${"%.2f".format(bill.amount)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "日期: ${bill.date}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController, modifier: Modifier = Modifier) {
    // 获取 FinanceViewModel 实例
    val financeViewModel: FinanceViewModel = viewModel()

    // 获取账单数据
    val bills by financeViewModel.bills.collectAsState(emptyList())

    // 观察总收入和总支出的变化
    val totalIncome by financeViewModel.totalIncome.collectAsState()
    val totalExpense by financeViewModel.totalExpense.collectAsState()

    // 下拉刷新状态
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 创建 SwipeRefreshState
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

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
                IconButton(onClick = {navController.navigate("chart")}) {
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
                containerColor = Black,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 使用 swipeRefresh 组件
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        // 模拟刷新操作
                        delay(1000)
                        financeViewModel.refreshData()
                        isRefreshing = false
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 显示总收入和总支出
                    Text(
                        text = "本月总收入: ￥${"%.2f".format(totalIncome)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本月总支出: ￥${"%.2f".format(totalExpense)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // 显示账单记录
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "账单记录", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 使用 LazyColumn 来显示账单记录
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