package com.example.teamproject.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppViewModel:ViewModel() {
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
}