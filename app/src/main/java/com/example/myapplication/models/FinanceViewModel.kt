package com.example.myapplication.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class Bill(
    val id: Int,            // 账单 ID
    val category: String,   // 种类
    val description: String, // 备注
    val amount: Float,      // 金额
    val date: Long          // 时间戳
)


class FinanceViewModel : ViewModel() {
    private val _bills = MutableStateFlow<List<Bill>>(
        listOf(
            Bill(1, "食物", "午餐", 50f, 1633024800000),
            Bill(2, "交通", "地铁", 20f, 1633111200000),
            Bill(3, "娱乐", "电影", 80f, 1633197600000)
        )
    )
    val bills: StateFlow<List<Bill>> = _bills

    val totalIncome = MutableStateFlow(150f)
    val totalExpense = MutableStateFlow(150f)

    // 模拟刷新账单数据
    fun refreshData() {
        val newBills = listOf(
            Bill(4, "学习", "书籍", 100f, System.currentTimeMillis()),
            Bill(5, "饮料", "咖啡", 25f, System.currentTimeMillis() - 1000 * 60 * 60),
            Bill(6, "健身", "健身房", 200f, System.currentTimeMillis() - 1000 * 60 * 60 * 2)
        )
        _bills.value = newBills
        totalIncome.value = 200f
        totalExpense.value = 325f
    }
}