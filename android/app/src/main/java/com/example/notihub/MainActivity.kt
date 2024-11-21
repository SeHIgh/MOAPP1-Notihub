package com.example.notihub

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notihub.databinding.ActivityMainBinding
import com.example.notihub.parsers.InfoPollingService
import com.example.notihub.parsers.KNUAnnouncement

class MainActivity : AppCompatActivity() {
    companion object {
        const val ANNUONCEMENTS = "announcements"
    }

    private lateinit var infoPollingServiceConnection: ServiceConnection
    private val announcementItems = mutableListOf<KNUAnnouncement>()

    @SuppressLint("NotifyDataSetChanged")
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

        val adapter = MainRecyclerAdapter(announcementItems)
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
        binding.recyclerView.addItemDecoration(DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL
        ))
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray(ANNUONCEMENTS, announcementItems.toTypedArray())
    }
}