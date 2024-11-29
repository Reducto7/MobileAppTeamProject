package com.example.myapplication.ui

data class Bill(
    val id: Int = 0,
    val isIncome: Boolean = false,
    val category: String = "",
    val remarks: String = "",
    val amount: Double = 0.0,
    val date: String = ""
)