import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

// hello

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


