package com.example.notihub

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notihub.databinding.ItemMainBinding
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource

class MainRecyclerAdapter(private val items: MutableList<KNUAnnouncement>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return MainViewHolder(ItemMainBinding.inflate(
            LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MainViewHolder).binding
        val boardText = when(items[position].source) {
            KNUAnnouncementSource.CSE -> context.getText(R.string.cse_name)
            KNUAnnouncementSource.IT -> context.getText(R.string.it_name)
        }

        binding.textViewTitle.text = items[position].title
        binding.textViewTime.text = items[position].time.toString()
        binding.textViewSummary.text = items[position].summary.ifEmpty {
            items[position].body
        }
        binding.textViewSource.text = boardText

        binding.root.setOnClickListener {
            context.startActivity(Intent(context, DetailActivity::class.java).apply {
                putExtra(DetailActivity.DATA, items[position])
            })
        }
    }

    override fun getItemCount(): Int = items.size
}