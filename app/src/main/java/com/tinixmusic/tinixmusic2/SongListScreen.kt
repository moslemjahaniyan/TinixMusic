package com.tinixmusic.tinixmusic2
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(navController: NavController, viewModel: SongListViewModel = viewModel()) {

    val allSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val favorites by FavoritesRepository.getFavorite(context).collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf(0) } // 👈 گیومه‌ها حذف شدند
    //خط زیر از قبل بوده ولی با خط بالا جایگزین شده برای برطرف کردن خطای نوع
    //var selectedTab by remember { mutableStateOf("0") }



    // انتخاب لیست بر اساس تب
    val songsBasedOnTab = if (selectedTab == 1){
        allSongs.filter {it.id in favorites}
    } else{
        allSongs
    }

    // اعمال جستجو
    val songs = if (searchQuery.isBlank()){
        songsBasedOnTab
    }else{
        songsBasedOnTab.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    (song.artist?.contains(searchQuery, ignoreCase = true) == true)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("تینیکس موزیک") },
            actions = {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("همه آهنگ‌ها") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("علاقه‌مندی‌ها (${favorites.size})") }
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("جستجوی آهنگ یا خواننده...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true
        )



        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("خطا: $error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchSongs() }) {
                            Text("تلاش مجدد")
                        }
                    }
                }
            }
            songs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            searchQuery.isNotBlank() -> "نتیجه‌ای برای \"$searchQuery\" یافت نشد"
                            selectedTab == 1 -> "هنوز آهنگی به علاقه‌مندی‌ها اضافه نکردی ❤️"
                            else -> "هیچ آهنگی یافت نشد"
                        }
                    )
                }
            }
            else -> {
                LazyColumn {
                    items(songs) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                navController.navigate("songDetail/${song.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.imageUrl ?: R.drawable.music_logo,
            contentDescription = "تصویر ${song.title}",
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(

                text = song.artist ?: "خواننده نامشخص",
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }

}