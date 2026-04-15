package com.reciperoulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.reciperoulette.ui.theme.RecipeRouletteTheme
import com.reciperoulette.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeRouletteTheme {
                AppNavigation()
            }
        }
    }
}