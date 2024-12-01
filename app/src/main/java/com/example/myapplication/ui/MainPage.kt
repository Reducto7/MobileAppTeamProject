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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BillViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("bills")
    private val _bills = mutableStateListOf<Bill>()
    val bills: List<Bill> get() = _bills

    init {
        fetchBillsFromDatabase()
    }

    private fun fetchBillsFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _bills.clear()
                    for (child in snapshot.children) {
                        val id = child.child("id").getValue(Int::class.java) ?: 0
                        val isIncome = child.child("income").getValue(Boolean::class.java) ?: false
                        val category = child.child("category").getValue(String::class.java) ?: ""
                        val remarks = child.child("remarks").getValue(String::class.java) ?: ""
                        val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                        val date = child.child("date").getValue(String::class.java) ?: ""

                        val bill = Bill(id, isIncome, category, remarks, amount, date)
                        _bills.add(bill)
                    }
                    println("加载的账单数据: $_bills")
                }

            override fun onCancelled(error: DatabaseError) {
                // Log error (if needed)
            }
        })
    }}
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
fun MainPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BillViewModel = viewModel()
) {
    val bills by remember { derivedStateOf { viewModel.bills } }
    val totalIncome by remember {
        derivedStateOf {
            bills.filter { it.isIncome }.sumOf { it.amount }
        }
    }
    val totalExpense by remember {
        derivedStateOf {
            bills.filter { !it.isIncome }.sumOf { it.amount }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    isRefreshing = true
                    isRefreshing = false
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
                    Divider(
                        modifier = Modifier
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
