package com.example.myapplication

import SettingPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.LoginPage
import com.example.myapplication.ui.MainPage
import com.example.myapplication.ui.RegisterPage
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.teamproject.ui.AddNewBillPage
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ){
                    composable(route = "login") {
                        LoginPage(navController, context = this@MainActivity)
                    }
                    composable("register") {
                        RegisterPage(navController)
                    }
                    composable("main") {
                        MainPage(navController)
                    }
                    composable("setting") {
                        SettingPage(navController)
                    }
                    composable("addNewBill") {
                        AddNewBillPage(navController)
                    }
                }
            }
        }
    }
}






