package com.example.notihub.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.notihub.R
import com.example.notihub.databinding.ActivityContentBinding
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource

class ContentActivity : AppCompatActivity() {
    companion object {
        const val DATA = "DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        val binding = ActivityContentBinding.inflate(layoutInflater)
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
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

        }
    }
}