package com.example.notihub

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notihub.databinding.ActivityMainBinding
import com.example.notihub.parsers.InfoPollingService
import com.example.notihub.parsers.KNUAnnouncement

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        setSupportActionBar(binding.toolbar)

        val announcementItems = mutableListOf<KNUAnnouncement>()
        val adapter = MainRecyclerAdapter(announcementItems)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL
        ))

        binding.fabRefresh.setOnClickListener {
            bindService(
                Intent(this, InfoPollingService::class.java),
                object: ServiceConnection {
                    lateinit var infoPollingBinder: InfoPollingService.InfoBinder
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        infoPollingBinder = (service as InfoPollingService.InfoBinder)
                        infoPollingBinder.addItemCallback {
                            announcementItems.add(0, it)
                            adapter.notifyItemChanged(0)
                        }
                        infoPollingBinder.addAllDoneCallback {
                            Toast.makeText(this@MainActivity, "Done. Count: $it", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onServiceDisconnected(name: ComponentName?) {
                    }
                },
                Context.BIND_AUTO_CREATE
            )
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }
}