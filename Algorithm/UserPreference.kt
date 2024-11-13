import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

data class Notification(val message: String, val keywords: List<String>, val timestamp: Long)
data class KeywordWeight(val keyword: String, var weight: Double)

class UserPreference {
    private val keywordWeights: MutableMap<String, KeywordWeight> = mutableMapOf()
    
    // Ver.1 키워드 가중치를 업데이트하고 가중치가 일정 시간에 따라 감소하게 설정
    fun updateKeywordWeights(notification: Notification) {
        val currentTime = System.currentTimeMillis()
        for (keyword in notification.keywords) {
            val timeDifference = (currentTime - notification.timestamp).days.inWholeDays
            val decayFactor = exp(-0.1 * timeDifference)  // 시간에 따라 가중치가 줄어드는 지수 함수
            val newWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(keyword, 0.0) } // 가중치 0.0부분을 기존에 있던 단어들의 모든 가중치의 평균 혹은 75th percentile 값을 적용시켜야 함.
            
            

            
            // 기존 가중치에 새로운 가중치를 합산하여 업데이트
            newWeight.weight = newWeight.weight * decayFactor + 1.0
            keywordWeights[keyword] = newWeight
        }
    }
    //

    // Ver.2 키워드 가중치 업데이트 (키워드 횟수와 가중치 감소율 반영)
    fun updateKeywordWeights(notification: Notification) {
        val currentTime = System.currentTimeMillis()
        val decayRate = 0.1  // 가중치 감소 비율

        for (keyword in notification.keywords) {
            val timeDifference = (currentTime - notification.timestamp).days.inWholeDays
            // 시간에 따라 가중치가 줄어드는 지수 함수
            val decayFactor = exp(-decayRate * timeDifference)
            // 최신 알림에서 새롭게 계산된 가중치야. 1.0은 새로운 알림이 처음 등장할 때의 기본 가중치로 설정한 값이야. decayFactor는 시간이 지남에 따라 가중치가 점차 감소하도록 만들어, 오래된 알림은 상대적으로 중요도가 낮아지게 해.
            // 예를 들어, 시간이 지났다면 decayFactor가 0.8이나 0.5 같은 값으로 작아질 거고, adjustedNewWeight도 그에 비례해 줄어들어.

            val keywordWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(0.0, 0) }

            // 새로운 가중치를 추가하되, 기존 값과 조화되도록 가중치를 점진적으로 업데이트
            val adjustedNewWeight = 1.0 * decayFactor  // 새로운 가중치에 감소율 적용
            keywordWeight.weight = (keywordWeight.weight * keywordWeight.count + adjustedNewWeight) / (keywordWeight.count + 1)
            // (현재까지 누적된 총 가중치 + 새로운 가중치) / (누적된 가중치 count + 1(새로운 Keyword count))
            keywordWeight.count += 1  // 키워드 등장 횟수 증가

            keywordWeights[keyword] = keywordWeight  // 업데이트된 가중치 저장

            // 알림을 클릭해서 글의 양 대비 머무는 시간도 고려해서 함수를 짜야 함.
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

    // 추천 알림 리스트 출력
    val recommended = userPreference.recommendNotifications(notifications)
    println("추천 알림: ${recommended.map { it.message }}")
}


//ver3
data class UserInterest(val keyword: String, var weight: Double)

class NotificationService(private val userInterests: MutableList<UserInterest>) {
    
    // 1. Gemini API LLM을 통해 텍스트에서 주요 키워드 추출
    fun extractKeywordsFromText(text: String): List<String> {
        // Gemini API LLM 호출하여 키워드 추출 (예시 API 호출, 실제 구현에 따라 다를 수 있음)
        return GeminiAPI.extractKeywords(text)
    }

    // 2. 추출된 키워드와 사용자 관심사를 매칭하여 알림 여부 판단
    fun isRelevantNotification(keywords: List<String>): Boolean {
        return keywords.any { keyword -> userInterests.any { it.keyword == keyword } }
    }

    // 3. 알림을 사용자에게 표시하고 관심 키워드 업데이트
    fun showNotificationIfRelevant(text: String) {
        val keywords = extractKeywordsFromText(text)
        
        if (isRelevantNotification(keywords)) {
            displayNotification(text) // 알림 표시 함수
            
            // 4. 사용자가 알림을 클릭할 때 호출하는 함수 (클릭 후 관심사 업데이트)
            onNotificationClick(keywords)
        }
    }
    
    // 알림 클릭 시 관심사 업데이트 함수
    private fun onNotificationClick(keywords: List<String>) {
        val readTime = measureReadTime() // 텍스트 당 머무는 시간 계산 (임의 함수)

        keywords.forEach { keyword ->
            val interest = userInterests.find { it.keyword == keyword }
            if (interest != null) {
                // 평균 머무는 시간보다 높으면 관심사 유지 또는 새로운 키워드 추가
                interest.weight += if (readTime >= 0.1) 0.1 else -0.05
            } else if (readTime >= 0.15) {
                // 새로운 키워드 추가 (기본 가중치 설정)
                userInterests.add(UserInterest(keyword, 0.1))
            }
        }
        
        // 가중치 감소 로직
        decayInterestWeights()
    }

    // 시간 경과에 따라 가중치 감소 (예: 0.9로 곱해 감소)
    private fun decayInterestWeights() {
        userInterests.forEach { it.weight *= 0.9 }
        // 가중치가 0에 가까워지면 관심사 제거
        userInterests.removeAll { it.weight < 0.01 }
    }

    // 알림 표시 (임의 함수)
    private fun displayNotification(text: String) {
        println("New Notification: $text")
    }

    // 읽기 시간 측정 함수 (실제 구현에서는 텍스트의 길이와 시간을 바탕으로 계산)
    private fun measureReadTime(): Double {
        return 0.1 // 예시로 평균값 설정
    }
}




//ver4

import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

data class Notification(val message: String, val keywords: List<String>, val timestamp: Long)
data class KeywordWeight(val keyword: String, var weight: Double, var count: Int)

class UserPreference {
    private val keywordWeights: MutableMap<String, KeywordWeight> = mutableMapOf()

    // Ver.2 키워드 가중치 업데이트 (키워드 횟수와 가중치 감소율 반영)
    fun updateKeywordWeights(notification: Notification) {
        val currentTime = System.currentTimeMillis()
        val decayRate = 0.1  // 가중치 감소 비율

        for (keyword in notification.keywords) {
            val timeDifference = (currentTime - notification.timestamp) / 86400000 // 하루 단위로 변환
            val decayFactor = exp(-decayRate * timeDifference)

            val keywordWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(keyword, 0.0, 0) }
            val adjustedNewWeight = 1.0 * decayFactor  // 새로운 가중치에 감소율 적용
            
            // 가중치의 점진적 업데이트: 누적된 가중치와 새로운 가중치를 가중 평균
            keywordWeight.weight = (keywordWeight.weight * keywordWeight.count + adjustedNewWeight) / (keywordWeight.count + 1)
            keywordWeight.count += 1  // 키워드 등장 횟수 증가
            keywordWeights[keyword] = keywordWeight
        }
    }

    // 사용자가 알림을 클릭할 때 호출하여 머무는 시간을 고려하여 가중치 업데이트
    fun onNotificationClick(notification: Notification, readDuration: Duration) {
        val longReadThreshold = 0.15 // 긴 시간 머문 기준 (예: 400글자당 60초 이상)
        
        for (keyword in notification.keywords) {
            val keywordWeight = keywordWeights.getOrPut(keyword) { KeywordWeight(keyword, 0.0, 0) }

            // 읽은 시간이 충분히 길면 가중치 증가, 새로운 관심사로 추가
            if (readDuration.inWholeMilliseconds / 400.0 >= longReadThreshold) {
                keywordWeight.weight += 0.2  // 긴 시간 머문 경우 추가 가중치
            } else {
                keywordWeight.weight += 0.1  // 기본 가중치 증가
            }
            
            keywordWeights[keyword] = keywordWeight  // 업데이트된 가중치 저장
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

    // 사용자가 클릭하고 머문 시간을 기준으로 업데이트
    val clickedNotification = notifications[0]
    userPreference.onNotificationClick(clickedNotification, Duration.parse("0.2s"))  // 예시로 머문 시간 추가

    // 추천 알림 리스트 출력
    val recommended = userPreference.recommendNotifications(notifications)
    println("추천 알림: ${recommended.map { it.message }}")
}
