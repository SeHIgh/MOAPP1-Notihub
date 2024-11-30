package com.example.notihub.algorithm

import com.example.notihub.MAX_WEIGHT_LINIT
import com.example.notihub.WEIGHT_DECREMENT
import com.example.notihub.WEIGHT_INCREMENT
import com.example.notihub.database.UserPreferenceEntity


fun shouldNotify(newKeywords: List<String>, topKeywords: List<String>): Boolean {
    // 새 글 키워드 중 하나라도 사용자 키워드에 포함되면 true 반환
    return newKeywords.any { it in topKeywords }
}

fun updateKeywordWeights(
    keywords: List<String>,
    userKeywordWeights: MutableList<UserPreferenceEntity>,
    preferred: Boolean
): MutableList<UserPreferenceEntity> {
    val updatedItems = mutableListOf<UserPreferenceEntity>()
    if (preferred) {
        keywords.forEach { keyword ->
            val keywordWeight = userKeywordWeights.find { it.keyword == keyword }
            if (keywordWeight != null) {
                keywordWeight.weight += WEIGHT_INCREMENT
                updatedItems.add(keywordWeight)
            } else {
                UserPreferenceEntity(keyword, WEIGHT_INCREMENT).let {
                    userKeywordWeights.add(it)
                    updatedItems.add(it)
                }
            }
        }
    } else {
        keywords.forEach { keyword ->
            val keywordWeight = userKeywordWeights.find { it.keyword == keyword }
            if (keywordWeight != null) {
                // 가중치 감소
                keywordWeight.weight -= WEIGHT_DECREMENT
                updatedItems.add(keywordWeight)
            }
        }
    }
    return updatedItems
}
