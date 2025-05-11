package com.cipherxzc.clockinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cipherxzc.clockinapp.ui.ClockInApp
import com.cipherxzc.clockinapp.ui.theme.ScaffoldExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScaffoldExampleTheme {
                ClockInApp()
            }
        }
    }
}