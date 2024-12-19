package com.example.notihub.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.notihub.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.notihub.MAX_WEIGHT_LINIT
import com.example.notihub.algorithm.updateKeywordWeights
import com.example.notihub.database.AppDatabase
import com.example.notihub.database.toEntity
import com.example.notihub.databinding.ActivityContentBinding
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource
import com.example.notihub.parsers.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentActivity : AppCompatActivity() {
    companion object {
        const val DATA = "DATA"
    }

    private lateinit var binding: ActivityContentBinding
    private lateinit var announcement: KNUAnnouncement
    private val announcementDao by lazy { AppDatabase.getDatabase(applicationContext).knuAnnouncementDao() }
    private val userPreferenceDao by lazy { AppDatabase.getDatabase(applicationContext).userPreferenceDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.likeButton.setOnClickListener {
            setSelected(Preference.LIKE)
            lifecycleScope.launch(Dispatchers.Default) {
                val allWeights = userPreferenceDao.getAllPreferences().toMutableList()
                val updated = updateKeywordWeights(
                    announcement.keywords,
                    allWeights,
                    true
                )
                val maxWeight = updated.maxOfOrNull { it.weight } ?: 0.0
                if (maxWeight > MAX_WEIGHT_LINIT) {
                    val scale = MAX_WEIGHT_LINIT / maxWeight
                    allWeights.forEach {
                        it.weight *= scale
                        userPreferenceDao.insertOrUpdatePreference(it)
                    }
                } else {
                    updated.forEach { userPreferenceDao.insertOrUpdatePreference(it) }
                    announcement.preference = Preference.LIKE
                    announcementDao.insertOrUpdateAnnouncement(announcement.toEntity())
                }
            }
        }
        binding.hateButton.setOnClickListener {
            setSelected(Preference.HATE)
            lifecycleScope.launch(Dispatchers.Default) {
                val updated = updateKeywordWeights(
                    announcement.keywords,
                    userPreferenceDao.getAllPreferences().toMutableList(),
                    false
                )
                updated.forEach {
                    if (it.weight <= 0) {
                        userPreferenceDao.deletePreferenceByKeyword(it.keyword)
                    } else {
                        userPreferenceDao.insertOrUpdatePreference(it)
                    }
                }
                announcement.preference = Preference.HATE
                announcementDao.insertOrUpdateAnnouncement(announcement.toEntity())
            }
        }
        binding.buttonToggleDetail.setOnClickListener {
            when (binding.textViewDetail.visibility) {
                View.GONE -> {
                    binding.textViewDetail.visibility = View.VISIBLE
                    binding.buttonToggleDetail.text = getString(R.string.hide_original)
                }
                View.VISIBLE -> {
                    binding.textViewDetail.visibility = View.GONE
                    binding.buttonToggleDetail.text = getString(R.string.show_original)
                }
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
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
            announcement = it
            binding.textViewTitle.text = it.title
            binding.textViewTime.text = it.time.toString()
            binding.textViewBoard.text = when(it.source) {
                KNUAnnouncementSource.CSE -> getText(R.string.cse_name)
                KNUAnnouncementSource.IT -> getText(R.string.it_name)
            }
            binding.textViewSummary.text = it.summary
            binding.textViewDetail.text = it.body
            setSelected(it.preference)
            true
        } ?: false
    }

    private fun setSelected(preference: Preference) {
        when (preference) {
            Preference.LIKE -> {
                binding.likeButton.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.likeButton.backgroundTintList =
                    ColorStateList.valueOf(Color.rgb(3, 152, 253))
                binding.hateButton.isEnabled = false
            }
            Preference.HATE -> {
                binding.hateButton.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.hateButton.backgroundTintList =
                    ColorStateList.valueOf(Color.rgb(3, 152, 253))
                binding.likeButton.isEnabled = false
            }
            Preference.NEUTRAL -> {
                binding.hateButton.isEnabled = true
                binding.likeButton.isEnabled = true
            }
        }
    }
}