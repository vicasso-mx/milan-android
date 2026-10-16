package com.doceolab.milan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.doceolab.milan.ui.MilanApp
import com.doceolab.milan.ui.theme.MilanTheme
import com.doceolab.milan.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    private val storeViewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MilanTheme {
                MilanApp(storeViewModel)
            }
        }
    }
}
