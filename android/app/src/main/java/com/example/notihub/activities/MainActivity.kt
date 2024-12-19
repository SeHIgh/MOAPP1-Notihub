package com.example.notihub.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notihub.R
import com.example.notihub.adapters.MainListAdapter
import com.example.notihub.database.AppDatabase
import com.example.notihub.database.fromEntity
import com.example.notihub.databinding.ActivityMainBinding
import com.example.notihub.parsers.InfoPollingService
import com.example.notihub.parsers.KNUAnnouncement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        const val ANNUONCEMENTS = "announcements"
    }

    private lateinit var infoPollingServiceConnection: ServiceConnection
    private lateinit var listAdapter: MainListAdapter
    private val announcementItems = mutableListOf<KNUAnnouncement>()
    private val announcementDao by lazy { AppDatabase.getDatabase(applicationContext).knuAnnouncementDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionAndHandleFirstRun()
        displayUi(savedInstanceState)
    }

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch(Dispatchers.Default) {
            val announcements = announcementDao.getAllAnnouncements().map {
                KNUAnnouncement.fromEntity(it)
            }
            withContext(Dispatchers.Main) {
                announcementItems.clear()
                announcementItems.addAll(announcements)
                listAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray(ANNUONCEMENTS, announcementItems.toTypedArray())
    }

    private fun checkPermissionAndHandleFirstRun() {
        try {
            enableEdgeToEdge()

            // 권한 요청 코드
            val requestLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    handleFirstRun()
                } else {
                    Toast.makeText(this, "알림 권한이 거부되었습니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            // 권한 상태 체크
            val status = ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
            if (status == PackageManager.PERMISSION_GRANTED) {
                handleFirstRun()
            } else {
                // 권한 요청
                requestLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        } catch (e: Exception) {
            finish()
        }
    }

    // 첫 실행 처리 및 Fragment 전환을 위한 함수
    private fun handleFirstRun() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }

    private fun displayUi(savedInstanceState: Bundle?) {
        val binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)

        listAdapter = MainListAdapter(announcementItems)
        infoPollingServiceConnection = object: ServiceConnection {
            lateinit var infoPollingBinder: InfoPollingService.InfoBinder
            var done = false

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                done = true
                infoPollingBinder = (service as InfoPollingService.InfoBinder)
                infoPollingBinder.addNewItemsCallback {
                    announcementItems.addAll(0, it)
                    listAdapter.notifyDataSetChanged()
                    binding.fabRefresh.visibility = View.VISIBLE
                    unbindService(this)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                if (!done) {
                    binding.fabRefresh.visibility = View.VISIBLE
                    unbindService(this)
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = listAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
            )
        )
        binding.fabRefresh.setOnClickListener {
            binding.fabRefresh.visibility = View.INVISIBLE
            listAdapter.notifyDataSetChanged()

            val intent = Intent(applicationContext, InfoPollingService::class.java)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)
            bindService(
                intent,
                infoPollingServiceConnection,
                Context.BIND_AUTO_CREATE
            )
            Toast.makeText(this, R.string.refresh_started, Toast.LENGTH_SHORT).show()
        }

        savedInstanceState?.run {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                getParcelableArray(ANNUONCEMENTS, KNUAnnouncement::class.java)
            } else {
                getParcelableArray(ANNUONCEMENTS)
            }?.let {
                announcementItems.addAll(it.filterIsInstance<KNUAnnouncement>())
                listAdapter.notifyDataSetChanged()
            }
        } ?: lifecycleScope.launch(Dispatchers.Default) {
            val announcements = announcementDao.getAllAnnouncements().map {
                KNUAnnouncement.fromEntity(it)
            }
            withContext(Dispatchers.Main) {
                announcementItems.addAll(announcements)
                listAdapter.notifyDataSetChanged()
            }
        }
    }
}
