package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 账单数据类，用于表示每一条账单
/*data class Bill(
    val id: Int = 0, // 唯一 ID
    val isIncome: Boolean = false, // 是否为收入
    val category: String = "", // 类别
    val remarks: String = "", // 备注
    val amount: Double = 0.0, // 金额
    val date: String = "" // 日期
)*/

class BillViewModel : ViewModel() {

    // Firebase 数据库引用
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("bills")

    // 账单数据缓存
    private val _bills = mutableStateListOf<Bill>()
    val bills: List<Bill> get() = _bills // 供 UI 层使用的只读账单列表

    // 收入类别列表
    val incomeCategories = listOf(
        "월급" to R.drawable.yen,
        "알바" to R.drawable.time,
        "재테크" to R.drawable.data,
        "용돈" to R.drawable.savings,
        "상금" to R.drawable.gift,
        "기타" to R.drawable.exchange
    )

    // 支出类别列表
    val expenditureCategories = listOf(
        "식비" to R.drawable.dining,
        "쇼핑" to R.drawable.shopping,
        "생필품" to R.drawable.chair,
        "교통" to R.drawable.bus,
        "오락" to R.drawable.celebration,
        "의료" to R.drawable.vaccines,
        "숙박" to R.drawable.house,
        "여행" to R.drawable.airport,
        "미용" to R.drawable.brush,
        "기타" to R.drawable.bubble
    )

    var isIncomeSelected by mutableStateOf(false) // 控制收入或支出

    init {
        // 初始化时加载账单数据
        fetchBillsFromDatabase()
    }

    // 从 Firebase 数据库加载账单数据
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
                    println("加载账单失败: ${error.message}")
                }
            })
        }
    }

    // 上传账单到 Firebase 数据库
    fun uploadBillToDatabase(bill: Bill) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // 创建一个新的账单条目
            val billId = database.child(userId).push().key ?: return  // 生成一个唯一的 ID
            val billData = mapOf(
                "id" to bill.id,
                "income" to bill.isIncome,
                "category" to bill.category,
                "remarks" to bill.remarks,
                "amount" to bill.amount,
                "date" to bill.date
            )

            // 上传账单数据到 Firebase
            database.child(userId).child(billId).setValue(billData)
                .addOnSuccessListener {
                    println("账单上传成功")
                }
                .addOnFailureListener { error ->
                    println("账单上传失败: ${error.message}")
                }
        }
    }


    // 根据账单 ID 获取账单信息，ID 为 Int? 类型
    fun fetchBillById(billId: Int?, onResult: (Bill?) -> Unit) {
        if (billId == null) {
            onResult(null) // 如果 billId 为 null，直接返回 null
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.child(userId).orderByChild("id").equalTo(billId.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var bill: Bill? = null
                        for (child in snapshot.children) {
                            val id = child.child("id").getValue(Int::class.java) ?: 0
                            val isIncome = child.child("income").getValue(Boolean::class.java) ?: false
                            val category = child.child("category").getValue(String::class.java) ?: ""
                            val remarks = child.child("remarks").getValue(String::class.java) ?: ""
                            val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                            val date = child.child("date").getValue(String::class.java) ?: ""

                            bill = Bill(id, isIncome, category, remarks, amount, date)
                        }
                        onResult(bill) // 将结果回调到调用方
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("获取账单失败: ${error.message}")
                        onResult(null) // 错误时返回 null
                    }
                })
        } else {
            onResult(null) // 用户未登录时返回 null
        }
    }

    // 更新账单
    fun updateBill(bill: Bill) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child(userId).child(bill.id.toString()).setValue(bill)
            .addOnSuccessListener {
                val index = _bills.indexOfFirst { it.id == bill.id }
                if (index != -1) {
                    _bills[index] = bill
                }
                println("账单更新成功: $bill")
            }
            .addOnFailureListener { error ->
                println("账单更新失败: ${error.message}")
            }
    }

    // 删除账单
    fun deleteBill(id: Int?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val billRef = database.child(userId).child(id.toString())

        // Firebase 删除操作
        billRef.removeValue()
            .addOnSuccessListener {
                _bills.removeAll { it.id == id } // 本地数据同步删除
                println("账单删除成功: ID = $id")
            }
            .addOnFailureListener { error ->
                println("账单删除失败: ${error.message}")
            }
    }

    // 获取当前日期，格式为 yyyy-MM-dd
    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 获取账单数量和最早账单到当前日期的天数
    fun getBillCountAndDays(): Int {
        if (_bills.size == 0) {
            return 0 // 没有账单，返回 (0, 0)
        }

        // 获取最早的账单日期
        val earliestBillDate = _bills.minByOrNull { it.date }?.date
        val today = Date()

        if (earliestBillDate != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val earliestDate = dateFormat.parse(earliestBillDate)
                val diffInMillis = today.time - earliestDate.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() // 将毫秒转换为天数
                return diffInDays
            } catch (e: Exception) {
                e.printStackTrace()
                return 0
            }
        }

        return 0
    }


    // 计算某年某月每天的收入或支出总和
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

    //计算某年的各类别收入和支出year: Int, month: Int, isIncome: Boolean
    fun getYearlyCategoryIncomeExpenditure(year: Int, isIncome: Boolean): Map<String, Double> {
        return _bills
            .filter {
                val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
                billDate.size == 3 && billDate[0] == year && it.isIncome == isIncome
            }
            .groupBy { it.category }
            .mapValues { (_, bills) -> bills.sumOf { it.amount } }
    }

    //计算某年某月的各类别收入和支出
    fun getMonthlyCategoryIncomeExpenditure(year: Int, month: Int, isIncome: Boolean): Map<String, Double> {
        return _bills
            .filter {
                val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
                billDate.size == 3 && billDate[0] == year && billDate[1] == month && it.isIncome == isIncome

            }

            .groupBy { it.category }
            .mapValues { (_, bills) -> bills.sumOf { it.amount } }
    }

    // 判断是否为闰年
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    //详情分析页面
    fun getBillsForCategory(category: String, year: Int, month: Int?): List<Bill> {
        return _bills.filter {
            val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    billDate[1] == month &&
                    it.category == category
        }
    }

    fun getMonthlyTotalsForCategory(category: String, year: Int, isIncome: Boolean): List<Float> {
        val filteredBills = _bills.filter {
            val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    it.category == category &&
                    it.isIncome == isIncome
        }
        val monthlyTotals = MutableList(12) { 0f }
        filteredBills.forEach {
            val month = it.date.split("-")[1].toIntOrNull() ?: 1
            if (month in 1..12) monthlyTotals[month - 1] += it.amount.toFloat()
        }
        return monthlyTotals
    }

    fun getPieChartDataForCategory(category: String, year: Int, month: Int, isIncome: Boolean): List<Pair<String, Float>> {
        val filteredBills = _bills.filter {
            val billDate = it.date.split("-").map { part -> part.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    billDate[1] == month &&
                    it.category == category &&
                    it.isIncome == isIncome
        }
        val totalAmount = filteredBills.sumOf { it.amount }
        return filteredBills.map {
            it.remarks to (it.amount / totalAmount * 100).toFloat()
        }.sortedByDescending { it.second }
    }

    fun getYearlyTotalsForCategory(
        category: String,
        year: Int,
        isIncome: Boolean
    ): List<Float> {
        // 按月份汇总指定类别的收入或支出
        val monthlyTotals = MutableList(12) { 0f }

        bills.filter { bill ->
            val billDate = bill.date.split("-").mapNotNull { it.toIntOrNull() }
            billDate.size == 3 &&
                    billDate[0] == year &&
                    bill.category == category &&
                    bill.isIncome == isIncome
        }.forEach { bill ->
            val month = bill.date.split("-")[1].toIntOrNull() ?: 1
            if (month in 1..12) {
                monthlyTotals[month - 1] += bill.amount.toFloat()
            }
        }

        return monthlyTotals
    }

}
