package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.BillViewModel
import com.example.myapplication.ui.ChartPage
import com.example.myapplication.ui.DetailsPage
import com.example.myapplication.ui.EditBillPage
import com.example.myapplication.ui.LoginPage
import com.example.myapplication.ui.MainPage
import com.example.myapplication.ui.MyPage
import com.example.myapplication.ui.RegisterPage
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.teamproject.ui.AddNewBillPage
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: BillViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable(route = "login") {
                        LoginPage(navController, context = this@MainActivity)
                    }
                    composable("register") {
                        RegisterPage(navController)
                    }
                    composable("main") {
                        MainPage(navController)
                    }
                    composable("addNewBill") {
                        AddNewBillPage(navController)
                    }
                    composable("my"){
                        MyPage(navController)
                    }
                    composable("chart") {
                        ChartPage(navController = navController, viewModel = viewModel)
                    }
                    composable("details/{category}/{isIncome}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: ""
                        val isIncome = backStackEntry.arguments?.getString("isIncome")?.toBoolean() ?: false
                        DetailsPage(navController = navController, category = category, isIncome = isIncome, viewModel = viewModel)
                    }
                    composable("editBill/{billId}") { backStackEntry ->
                        val billId = backStackEntry.arguments?.getString("billId")?.toIntOrNull()
                        EditBillPage(navController, billId)
                    }
                }
            }
        }
    }
}
