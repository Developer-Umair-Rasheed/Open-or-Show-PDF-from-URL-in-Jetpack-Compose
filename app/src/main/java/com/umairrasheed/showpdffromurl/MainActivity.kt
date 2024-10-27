package com.umairrasheed.showpdffromurl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.umairrasheed.showpdffromurl.ui.theme.ShowPdfFromUrlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowPdfFromUrlTheme {
                App()
            }
        }
    }

    @Composable
    fun App() {

        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "home") {

            composable(route = "home") {
                HomeScreen {
                    navController.navigate("show_pdf")
                }
            }

            composable(route = "show_pdf") {
                ShowPdfScreen()
            }

        }

    }

}