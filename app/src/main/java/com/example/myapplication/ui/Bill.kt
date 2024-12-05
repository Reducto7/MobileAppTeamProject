package com.example.myapplication.ui

// 账单数据类，用于表示每一条账单
data class Bill(
    val id: Int = 0,    // 唯一 ID
    val isIncome: Boolean = false,  // 是否为收入
    val category: String = "",  // 类别
    val remarks: String = "",   // 备注
    val amount: Double = 0.0,   // 金额
    val date: String = ""   // 日期
)