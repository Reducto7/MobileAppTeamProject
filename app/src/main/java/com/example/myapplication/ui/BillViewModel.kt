package com.example.myapplication.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
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

    // 根据 ID 获取账单
    fun getBillById(id: Int): Bill? {
        return _bills.find { it.id == id }
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
    fun deleteBill(id: Int) {
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
}
