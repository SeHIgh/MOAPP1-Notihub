package com.example.notihub.algorithm


enum class Action {
    GOOD, BAD
}

data class KeywordWeight(
    val keyword: String,
    var weight: Long
)


fun shouldNotify(newKeywords: List<String>, userKeywords: List<String>): Boolean {
    // 새 글 키워드 중 하나라도 사용자 키워드에 포함되면 true 반환
    return newKeywords.any { it in userKeywords }
}

fun updateKeywordWeights(
    action: Action,
    notificationKeywords: List<String>,
    userKeywordWeights: MutableList<KeywordWeight>
) {
    // 함수 내부에서 사용할 상수 정의
    val maxWeightLimit = 10.0 // 최대 허용 가중치
    val increment = 1L        // 좋아요 시 증가 가중치
    val decrement = 1L        // 싫어요 시 감소 가중치

    when (action) {
        Action.GOOD -> {
            notificationKeywords.forEach { notifKeyword ->
                val keywordWeight = userKeywordWeights.find { it.keyword == notifKeyword }
                if (keywordWeight != null) {
                    keywordWeight.weight += increment
                } else {
                    // 새로운 키워드 추가
                    userKeywordWeights.add(KeywordWeight(notifKeyword, increment))
                }
            }
        }
        Action.BAD -> {
            notificationKeywords.forEach { notifKeyword ->
                val keywordWeight = userKeywordWeights.find { it.keyword == notifKeyword }
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

    // 최대 허용치를 초과하는 경우 비례 조정
    val maxWeight = userKeywordWeights.maxOfOrNull { it.weight } ?: 0L
    if (maxWeight > maxWeightLimit) {
        val scale = maxWeightLimit / maxWeight
        userKeywordWeights.forEach { it.weight *= scale as Long }
    }
}
