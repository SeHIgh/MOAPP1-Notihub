package com.example.notihub.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notihub.R
import com.example.notihub.adapters.MainListAdapter
import com.example.notihub.databinding.ActivityMainBinding
import com.example.notihub.parsers.InfoPollingService
import com.example.notihub.parsers.KNUAnnouncement

class MainActivity : AppCompatActivity() {
    companion object {
        const val ANNUONCEMENTS = "announcements"
    }

    private lateinit var infoPollingServiceConnection: ServiceConnection
    private val announcementItems = mutableListOf<KNUAnnouncement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionAndHandleFirstRun()
        displayUi(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray(ANNUONCEMENTS, announcementItems.toTypedArray())
    }

    private fun checkPermissionAndHandleFirstRun() {
        try {
            enableEdgeToEdge() // 이 부분에서 예외가 발생할 수 있으므로, 잘못된 구성이 있는지 확인 필요

            // 권한 요청 코드
            val requestLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Log.d("kkang", "callback.. granted..")
                    handleFirstRun()
                } else {
                    Log.d("kkang", "callback.. denied..")
                    Toast.makeText(this, "알림 권한이 거부되었습니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            // 권한 상태 체크
            val status = ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
            if (status == PackageManager.PERMISSION_GRANTED) {
                Log.d("kkang", "permission granted")
                handleFirstRun()
            } else {
                // 권한 요청
                requestLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그를 출력하고 종료되지 않도록 처리
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "앱을 시작하는 중에 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // 첫 실행 처리 및 Fragment 전환을 위한 함수
    private fun handleFirstRun() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            Toast.makeText(this, "앱이 처음 실행되었습니다!", Toast.LENGTH_SHORT).show()
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()

            // IntroFragment로 이동
            Toast.makeText(this, "앱이 처음 실행되었습니다?", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, IntroActivity::class.java))
        } else {
            Toast.makeText(this, "앱을 다시 실행했습니다.", Toast.LENGTH_SHORT).show()
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

        val adapter = MainListAdapter(announcementItems)
        infoPollingServiceConnection = object: ServiceConnection {
            lateinit var infoPollingBinder: InfoPollingService.InfoBinder
            var done = false

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                done = true
                infoPollingBinder = (service as InfoPollingService.InfoBinder)
                infoPollingBinder.addNewItemsCallback {
                    announcementItems.addAll(it)
                    adapter.notifyDataSetChanged()
                    binding.fabRefresh.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity, "Done. Count: ${it.size}", Toast.LENGTH_SHORT).show()
                    unbindService(this)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                if (!done) {
                    binding.fabRefresh.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity, "FAILED", Toast.LENGTH_SHORT).show()
                    unbindService(this)
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL
        )
        )
        binding.fabRefresh.setOnClickListener {
            binding.fabRefresh.visibility = View.INVISIBLE
            announcementItems.clear()
            adapter.notifyDataSetChanged()

            bindService(
                Intent(this, InfoPollingService::class.java),
                infoPollingServiceConnection,
                Context.BIND_AUTO_CREATE
            )
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }

        savedInstanceState?.run {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                getParcelableArray(ANNUONCEMENTS, KNUAnnouncement::class.java)
            } else {
                getParcelableArray(ANNUONCEMENTS)
            }?.let {
                announcementItems.addAll(it.filterIsInstance<KNUAnnouncement>())
                adapter.notifyDataSetChanged()
            }
        }
    }
}
