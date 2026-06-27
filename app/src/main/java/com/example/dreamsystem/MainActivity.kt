package com.example.dreamsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamsystem.ui.DreamScreen
import com.example.dreamsystem.ui.WishPointScreen
import com.example.dreamsystem.ui.theme.DreamSystemTheme
import com.example.dreamsystem.viewmodel.DreamViewModel
import com.example.dreamsystem.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamSystemTheme(dynamicColor = false) {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val pagerState = rememberPagerState(pageCount = { 2 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> {
                val viewModel: TaskViewModel = viewModel()
                Box(modifier = Modifier.fillMaxSize()) {
                    WishPointScreen(viewModel = viewModel)
                }
            }
            1 -> {
                val viewModel: DreamViewModel = viewModel()
                Box(modifier = Modifier.fillMaxSize()) {
                    DreamScreen(viewModel = viewModel)
                }
            }
        }
    }
}
