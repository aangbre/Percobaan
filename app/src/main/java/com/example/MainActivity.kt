package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.EduDatabase
import com.example.data.repository.EduRepository
import com.example.ui.EduApp
import com.example.ui.EduViewModel
import com.example.ui.theme.EduTKTheme

class MainActivity : ComponentActivity() {

    private val viewModel: EduViewModel by viewModels {
        val database = EduDatabase.getInstance(applicationContext)
        val repository = EduRepository(database.eduDao())
        EduViewModel.provideFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTKTheme {
                EduApp(viewModel = viewModel)
            }
        }
    }
}
