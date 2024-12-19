package com.example.notihub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notihub.R
import com.example.notihub.activities.ContentActivity
import com.example.notihub.databinding.ItemMainBinding
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource

class MainListAdapter(private val items: MutableList<KNUAnnouncement>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private class MainViewHolder(val binding: ItemMainBinding): RecyclerView.ViewHolder(binding.root)

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return MainViewHolder(ItemMainBinding.inflate(
            LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MainViewHolder).binding
        val announcement = items[position]

        binding.textViewTitle.text = announcement.title
        binding.textViewTime.text = announcement.time.toString()
        binding.textViewSummary.text = announcement.summary.ifEmpty {
            announcement.body
        }
        binding.textViewSource.text = when(announcement.source) {
            KNUAnnouncementSource.CSE -> context.getText(R.string.cse_name)
            KNUAnnouncementSource.IT -> context.getText(R.string.it_name)
        }

        binding.root.setOnClickListener {
            context.startActivity(Intent(context, ContentActivity::class.java).putExtra(
                ContentActivity.DATA, announcement
            ))
        }
    }

    override fun getItemCount(): Int = items.size
}