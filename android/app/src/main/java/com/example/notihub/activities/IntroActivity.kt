package com.example.notihub.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notihub.R
import com.example.notihub.databinding.ActivityIntroBinding
import com.example.notihub.fragments.IntroFragment1

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toast.makeText(this, "IntroActivity", Toast.LENGTH_SHORT).show()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, IntroFragment1())
            .commit()
    }
}

