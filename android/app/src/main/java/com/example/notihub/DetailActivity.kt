package com.example.notihub

import android.app.LauncherActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notihub.databinding.ActivityDetailBinding
import com.example.notihub.databinding.ActivityMainBinding

class DetailActivity : AppCompatActivity() {
    companion object {
        const val TITLE = "title"
        const val TIME = "time"
        const val BOARD = "board"
        const val SUMMARY = "summary"
        const val BODY = "body"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // }
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.run {
            // setNavigationIcon(R.drawable.)
            setNavigationOnClickListener {
                // startActivity(Intent(this@DetailActivity, MainActivity::class.java).apply {
                //     // setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // })
                finish()
            }
        }
        // onBackPressedDispatcher.addCallback {
        //     startActivity(Intent(this@DetailActivity, MainActivity::class.java).apply {
        //         // setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //     })
        // }

        intent.getStringExtra(TITLE)?.let { binding.textViewTitle.text = it }
        intent.getStringExtra(TIME)?.let { binding.textViewTime.text = it }
        intent.getStringExtra(BOARD)?.let { binding.textViewBoard.text = it }
        intent.getStringExtra(SUMMARY)?.let { binding.textViewSummary.text = it }
        intent.getStringExtra(BODY)?.let { binding.textViewDetail.text = it }
    }
}