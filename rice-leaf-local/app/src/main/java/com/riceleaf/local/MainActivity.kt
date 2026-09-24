package com.riceleaf.local

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riceleaf.local.ui.navigation.AppNavigation
import com.riceleaf.local.ui.theme.RiceLeafTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiceLeafTheme {
                AppNavigation()
            }
        }
    }
}
