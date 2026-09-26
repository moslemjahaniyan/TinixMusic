package com.tinixmusic.tinixmusic2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = "تینیکس موزیک"
        val message = createWelcomeMessage(appName)
        val version = 1
        setContent {
            Text(message + "\n" + createAppInfo( appName, version))
        }
    }

    fun createWelcomeMessage(appName: String): String{
        return "به $appName خوش آمدید"
    }
    fun createAppInfo(name: String, version: Int): String{
        return "$name نسخه  $version"
    }

}

