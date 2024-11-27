package com.example.notihub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notihub.databinding.ItemKeywordBinding

class KeywordAdapter(
    private val context: Context,
    private val keywords: List<String>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>() {

    private val selectedKeywords = mutableSetOf<String>() // 선택된 키워드 저장

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemKeywordBinding.inflate(inflater, parent, false)
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        val keyword = keywords[position]
        holder.bind(keyword)

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            if (selectedKeywords.contains(keyword)) {
                // 선택 해제
                selectedKeywords.remove(keyword)
            } else {
                if (selectedKeywords.size < 5) {
                    // 최대 5개까지 선택 가능
                    selectedKeywords.add(keyword)
                }
            }
            onSelectionChanged(selectedKeywords.toList()) // 선택된 키워드 변경 시 콜백 호출
            notifyItemChanged(position) // UI 업데이트
        }

        // 선택 상태에 따른 UI 업데이트
        holder.itemView.alpha = if (selectedKeywords.contains(keyword)) 1.0f else 0.5f
        holder.itemView.setBackgroundColor(
            if (selectedKeywords.contains(keyword)) Color.LTGRAY else Color.TRANSPARENT
        )
    }

    override fun getItemCount(): Int = keywords.size

    // 선택된 키워드들을 가져오는 함수
    fun getSelectedKeywords(): Set<String> {
        return selectedKeywords
    }

    // ViewHolder 클래스 정의
    class KeywordViewHolder(private val binding: ItemKeywordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(keyword: String) {
            binding.keywordTextView.text = keyword
        }
    }
}
