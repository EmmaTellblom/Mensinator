package com.mensinator.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mensinator.app.ui.theme.MensinatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MensinatorTheme {
                CalendarScreen()  // Call the CalendarScreen composable from the ui package
            }
        }
    }
}