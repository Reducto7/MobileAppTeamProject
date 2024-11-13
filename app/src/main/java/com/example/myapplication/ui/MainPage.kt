package com.example.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainPage(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black
            ) {
                Spacer(modifier = Modifier.width(60.dp))
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "List",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                IconButton(onClick = {navController.navigate("chart")}) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Graph",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(70.dp))
                IconButton(onClick = { navController.navigate("setting") }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Setting",
                        tint = Color.White
                    )
                }

            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {navController.navigate("addNewBill")},
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text =
                "Main Content"
            )
        }
    }
}