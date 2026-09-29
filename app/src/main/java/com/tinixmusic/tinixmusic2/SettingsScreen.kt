package com.tinixmusic.tinixmusic2


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import androidx.compose.material3.Divider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isDarkMode by SettingsRepository.isDarkMode(context)
        .collectAsState(initial = false)
    val defaultQuality by SettingsRepository.getDefaultQuality(context)
        .collectAsState(initial = "320")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // تم تاریک
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تم تاریک", modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            SettingsRepository.setDarkMode(context, enabled)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // کیفیت پیش‌فرض
            Text(
                "کیفیت پیش‌فرض دانلود",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = defaultQuality == "320",
                    onClick = {
                        scope.launch {
                            SettingsRepository.setDefaultQuality(context, "320")
                        }
                    }
                )
                Text("320 kbps (کیفیت بالا)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = defaultQuality == "128",
                    onClick = {
                        scope.launch {
                            SettingsRepository.setDefaultQuality(context, "128")
                        }
                    }
                )
                Text("128 kbps (حجم کم)")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // دکمه‌ی پاک کردن کش
            Button(
                onClick = { /* بعداً پیاده‌سازی می‌کنیم */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("پاک کردن کش")
            }
        }
    }
}