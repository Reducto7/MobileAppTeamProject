package com.example.teamproject.ui

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.Bill
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppViewModel:ViewModel() {
    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> get() = _bills

    private val database = FirebaseDatabase.getInstance()
    private val reference = database.getReference("bills")

    init {
        fetchBills()
    }
    //监听 Firebase 数据库并将其加载到 UI 可用的 LiveData 中
    private fun fetchBills() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val billList = mutableListOf<Bill>()
                for (billSnapshot in snapshot.children) {
                    val bill = billSnapshot.getValue(Bill::class.java)
                    bill?.let { billList.add(it) }
                }
                _bills.value = billList
            }

            override fun onCancelled(error: DatabaseError) {
                // 错误处理
            }
        })
    }

    // 弹出日期选择器对话框
    fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // 创建一个黑白风格的主题样式
        val datePickerDialog = DatePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // 使用 Holo Light 主题
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                onDateSelected(formattedDate) // 回调选中的日期
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show() // 显示日期选择器对话框
    }


    // 获取当前日期
    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun showMonthPicker(context: Context, onMonthSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // 使用 Holo Light 主题
            { _, selectedYear, selectedMonth, _ -> // 忽略 dayOfMonth
                val formattedMonth = String.format("%04d-%02d", selectedYear, selectedMonth + 1)
                onMonthSelected(formattedMonth) // 回调选中的年和月
            },
            year,
            month,
            1 // 设置默认日为 1
        )

        // 隐藏“日”选择器
        try {
            val daySpinnerId = context.resources.getIdentifier("android:id/day", null, null)
            val daySpinner = datePickerDialog.datePicker.findViewById<View>(daySpinnerId)
            daySpinner?.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }

        datePickerDialog.show() // 显示日期选择器对话框
    }

    fun getTodayMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault()) // 格式化到月份
        return sdf.format(Date())
    }
}