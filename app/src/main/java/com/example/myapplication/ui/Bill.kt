package com.example.myapplication.ui

data class Bill(
    val id: Int = 0,
    val isIncome: Boolean = false,//收入或支出
    val category: String = "",
    val remarks: String = "",
    val amount: Double = 0.0,
    val date: String = ""
)