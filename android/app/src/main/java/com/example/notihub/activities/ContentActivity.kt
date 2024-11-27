package com.example.notihub.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.notihub.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notihub.databinding.ActivityContentBinding
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource

class ContentActivity : AppCompatActivity() {
    companion object {
        const val DATA = "DATA"
    }

    lateinit var binding: ActivityContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        onBackPressedDispatcher.addCallback {
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setContentFromIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        setContentFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { setContentFromIntent(it) }
    }

    private fun setContentFromIntent(intent: Intent): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(DATA, KNUAnnouncement::class.java)
        } else {
            intent.getParcelableExtra(DATA)
        }?.let {
            binding.textViewTitle.text = it.title
            binding.textViewTime.text = it.time.toString()
            binding.textViewBoard.text = when(it.source) {
                KNUAnnouncementSource.CSE -> getText(R.string.cse_name)
                KNUAnnouncementSource.IT -> getText(R.string.it_name)
            }
            binding.textViewSummary.text = it.summary
            binding.textViewDetail.text = it.body
            true
        } ?: false
    }
}