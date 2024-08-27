package com.mensinator.app
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mensinator.app.navigation.BottomBar
import com.mensinator.app.ui.theme.MensinatorTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MensinatorTheme {
                BottomBar {
                    isScreenProtectionEnabled ->
                    // Sets the flags for screen protection if
                    // isScreenProtectionEnabled == true
                    // If isScreenProtectionEnabled == false it removes the flags
                    if(isScreenProtectionEnabled){
                        window?.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    }else{
                        window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }
    }
}