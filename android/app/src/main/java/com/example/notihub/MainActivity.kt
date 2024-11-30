package com.example.notihub

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.notihub.database.testDatabase
import com.example.notihub.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터베이스 테스트 실행
        val context: Context = binding.testTextView.context
        CoroutineScope(Dispatchers.IO).launch {
            testDatabase(context)
        }
    }
}