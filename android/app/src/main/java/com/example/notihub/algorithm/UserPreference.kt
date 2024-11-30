package com.example.notihub.algorithm

import com.example.notihub.database.UserPreferenceEntity


fun shouldNotify(newKeywords: List<String>, topKeywords: List<String>): Boolean {
    // 새 글 키워드 중 하나라도 사용자 키워드에 포함되면 true 반환
    return newKeywords.any { it in topKeywords }
}

fun updateKeywordWeights(
    keywords: List<String>,
    userKeywordWeights: MutableList<UserPreferenceEntity>,
    preferred: Boolean
) {
    // 함수 내부에서 사용할 상수 정의
    val maxWeightLimit = 10.0 // 최대 허용 가중치
    val increment = 1.0       // 좋아요 시 증가 가중치
    val decrement = 1.0       // 싫어요 시 감소 가중치

    if (preferred) {
        keywords.forEach { keyword ->
            val keywordWeight = userKeywordWeights.find { it.keyword == keyword }
            if (keywordWeight != null) {
                keywordWeight.weight += increment
            } else {
                userKeywordWeights.add(UserPreferenceEntity(keyword, increment))
            }
        }
        val maxWeight = userKeywordWeights.maxOfOrNull { it.weight } ?: 0.0
        if (maxWeight > maxWeightLimit) {
            val scale = maxWeightLimit / maxWeight
            userKeywordWeights.forEach { it.weight *= scale as Long }
        }
    } else {
        keywords.forEach { keyword ->
            val keywordWeight = userKeywordWeights.find { it.keyword == keyword }
            if (keywordWeight != null) {
                // 가중치 감소
                keywordWeight.weight -= decrement
                if (keywordWeight.weight <= 0) {
                    userKeywordWeights.remove(keywordWeight)
                }
            }
        }
    }
}
