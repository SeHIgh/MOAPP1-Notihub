// 좋아요 싫어요 적용 함수
// ver 6

import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

data class Notification(val message: String, val keywords: List<String>, val timestamp: Long)
data class KeywordWeight(val keyword: String, var weight: Double, var count: Int)

class UserPreference {
    private val keywordWeights: MutableMap<String, KeywordWeight> = mutableMapOf()

    private fun sigmoid(x: Double, L: Double, k: Double): Double {
        return L / (1 + exp(-k * (x - L / 2)))
    }

    // Ver.4 키워드 가중치 업데이트 (키워드 횟수와 가중치 감소율 반영)
    fun updateKeywordWeights(notification: Notification) {
        val currentTime = System.currentTimeMillis()
        val decayRate = 0.1  // 가중치 감소 비율
        val maxWeight = 5.0  // 동적 상한치 (예: 5로 설정)
        val k = 0.2          // 시그모이드 함수의 기울기 조절 값

        for (keyword in notification.keywords) {
            val timeDifference = (currentTime - notification.timestamp) / 86400000 // 하루 단위로 변환
            val decayFactor = exp(-decayRate * timeDifference)

            val keywordWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(keyword, 0.0, 0) }
            val adjustedNewWeight = 1.0 * decayFactor  // 새로운 가중치에 감소율 적용

            // 가중치의 점진적 업데이트: 누적된 가중치와 새로운 가중치를 가중 평균으로 계산 후 시그모이드 함수 적용
            keywordWeight.weight = sigmoid(
                (keywordWeight.weight * keywordWeight.count + adjustedNewWeight) / (keywordWeight.count + 1),
                maxWeight,
                k
            )
            keywordWeight.count += 1  // 키워드 등장 횟수 증가
            keywordWeights[keyword] = keywordWeight
        }
    }

    // 알림에 대한 사용자 피드백 처리
    fun onNotificationFeedback(notification: Notification, feedback: String) {
        val maxWeight = 5.0  // 동적 상한치
        val k = 0.2          // 시그모이드 함수의 기울기 조절 값

        for (keyword in notification.keywords) {
            val keywordWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(keyword, 0.0, 0) }
            when (feedback) {
                "like" -> {
                    keywordWeight.weight = sigmoid(keywordWeight.weight + 0.2, maxWeight, k) // 좋아요: 가중치 증가
                }
                "dislike" -> {
                    keywordWeight.weight = sigmoid(keywordWeight.weight - 0.2, maxWeight, k) // 싫어요: 가중치 감소
                }
                else -> {
                    // 아무것도 하지 않음
                }
            }
            keywordWeights[keyword] = keywordWeight // 업데이트된 가중치 저장
        }
    }

    // 관심 키워드 기반 알림 추천 함수
    fun recommendNotifications(notifications: List<Notification>): List<Notification> {
        return notifications
            .sortedByDescending { notification ->
                notification.keywords.sumOf { keyword ->
                    keywordWeights[keyword]?.weight ?: 0.0
                }
            }
            .take(5)  // 상위 5개 알림만 추천
    }
}

fun main() {
    val userPreference = UserPreference()

    // 예시 알림 리스트
    val notifications = listOf(
        Notification("안녕하세요! 오늘의 뉴스입니다.", listOf("뉴스", "오늘"), System.currentTimeMillis() - 24.hours.inWholeMilliseconds),
        Notification("이번 주 인기 영화 개봉!", listOf("영화", "인기"), System.currentTimeMillis() - 48.hours.inWholeMilliseconds),
        Notification("오늘의 주식 시장 소식", listOf("주식", "오늘"), System.currentTimeMillis() - 2.days.inWholeMilliseconds),
    )

    // 각 알림에 대해 키워드 가중치 업데이트
    for (notification in notifications) {
        userPreference.updateKeywordWeights(notification)
    }

    // 사용자가 특정 알림에 대해 좋아요/싫어요 버튼을 클릭한 경우
    val clickedNotification = notifications[0]
    userPreference.onNotificationFeedback(clickedNotification, "like")  // 예시로 '좋아요' 클릭
    userPreference.onNotificationFeedback(notifications[1], "dislike") // 예시로 '싫어요' 클릭
    userPreference.onNotificationFeedback(notifications[2], "")         // 아무것도 누르지 않음

    // 추천 알림 리스트 출력
    val recommended = userPreference.recommendNotifications(notifications)
    println("추천 알림: ${recommended.map { it.message }}")
}