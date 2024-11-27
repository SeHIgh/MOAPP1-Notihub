package com.example.notihub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notihub.R
import com.example.notihub.adapters.KeywordAdapter
import com.example.notihub.databinding.ActivitySelectPreferenceBinding

class SelectPreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keywords = listOf("Sports", "Music", "Travel", "Study", "Cooking", "Movies", "Fitness", "Tech", "Art", "Fashion")
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = KeywordAdapter(this, keywords) { selectedKeywords ->
            // 선택된 키워드 변경 시 호출되는 콜백
            // 선택된 키워드가 변경될 때마다 콜백 호출됨
            Toast.makeText(this, "선택된 키워드: $selectedKeywords", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // "시작하기" 버튼 클릭 시 ListActivity로 선택된 키워드들 전달
        val buttonFinish: Button = binding.finishButton
        buttonFinish.setOnClickListener {
            val selectedKeywords = adapter.getSelectedKeywords() // 선택된 키워드들 가져오기
            val intent = Intent(this, ItemListActivity::class.java)
            intent.putStringArrayListExtra("selected_keywords", ArrayList(selectedKeywords)) // 키워드 리스트 전달
            startActivity(intent)
        }
    }
}
