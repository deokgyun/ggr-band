package com.study.ggrband.service

import io.ktor.http.*
import kotlinx.serialization.json.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@Service
class BandService {

    @Value("\${band.token}")
    lateinit var accessToken: String

    @Value("\${band.api}")
    lateinit var apiHost: String

    @Value("\${band.key}")
    lateinit var bandKey: String

    val greetings = listOf(
        // 1월
        listOf(
            "올 한해 건강하시고 새로운 출발! 힘차게 달리고 모두 새해복!",
            "새해에는 소망하는 일 모두 이루시길 바라며 늘 변함없는 관심 부탁드립니다.",
            "1월 끝까지 힘내서 화이팅",
            "설 명절이 주는 풍성함이 기분좋은 때입니다. 행복한 한 해 되십시오.",
            "1월의 마무리입니다. 웃음 가득한 그런 한 달이 되었길 바랍니다."
        ),
        // 2월
        listOf(
            "벌써 한달이 훌쩍 지나가 버렸네요.",
            "입춘이 지났지만 아직 춥네요. 감기 조심하세요.",
            "남은 겨울 건강히 보내세요.",
            "어느새 두달이 훌쩍 지나가고 있습니다.",
            "곧 따뜻해지는 날씨와 함께 새로운 시작의 설렘이 가득한 봄입니다."
        ),
        // 3월
        listOf(
            "일교차가 크니 항상 감기 조심",
            "따뜻한 봄을 기다리며 오늘도 힘찬 하루 되세요.",
            "조금은 따뜻해지고 있네요. 활기차게 보내세요.",
            "낮이 제법 길어졌네요.",
            "아침, 저녁으로 봄공기가 상쾌하네요."
        ),
        // 4월
        listOf(
            "4월입니다. 벚꽃 구경 갈 준비 되셨죠?",
            "희망과 긍정으로 가득 찬 시간을 보내셨으면 합니다.",
            "봄을 따라 모두 행복하세요.",
            "벌써 4월도 지나가고 있네요. 모두 잘 즐기고 계시죠?",
            "이 좋은 봄날에 모두가 항상 행복하고 평안하기를 바랍니다."
        ),
        // 5월
        listOf(
            "행복하고 웃음 가득한 5월 되세요.",
            "걱정은 내려놓고 좋은분들과 좋은 시간 보내세요.",
            "계속 초록이 짙어지고 있습니다. 모두 싱그럽게 보네세요.",
            "화창하면서 점점 더위도 느껴지네요. 슬슬 여름 준비 하셔야죠?",
            "어느새 올해 반환점인 6월이 되어가네요. 모두 좋은일만 있길 바랍니다."
        ),
        // 6월
        listOf(
            "이제 날씨가 더워지고 지치기 쉬운 6월입니다. 휴식과 행복 모두 챙기세요.",
            "여름의 문턱입니다. 점점 더워지지만 상쾌하게 시작하세요.",
            "규칙적인 수분 섭취와 운동으로 이겨냅시다.",
            "매일 덥지만 신선하고 향기로운 순간들로 가득 차길 바랍니다.",
            "주변을 살펴보세요. 작은 것에서도 행복을 찾을 수 있어요."
        ),
        // 7월
        listOf(
            "휴가 계획은 세우셨나요? 재충전하시길 바랍니다.",
            "계획하고 계신 휴가가 있다면 행복한 휴가 보내시길 바랍니다. ",
            "뜨거운 여름, 열정적인 당신을 막을 순 없어요. 피할 수 없다면 즐기죠.",
            "갑작스러운 소나기처럼, 뜻밖의 행운이 찾아오길 바라며 건강하고 행복하세요.",
            "덥지만 긍정의 에너지를 발산하며 행복한 하루하루를 보내시길 바랍니다."
        ),
        // 8월
        listOf(
            "점점 높아지는 불쾌지수로 짜증이 날 수 있는 날씨입니다. 휴식을 취하며 긍정적인 생각으로 여유를 찾으세요.",
            "간밤에 더위로 여러 번 잠에서 깰수도 있지만 힘내서 밝고 활기찬 하루 보내세요.",
            "외출이 두려울 정도이지만 여유로운 하루를 보내세요.",
            "늦은 휴가 계획 하신분들 모두 스트레스 해소하시길 바랍니다.",
            "더위가 조금 더 가겠지만 8월도 막바지입니다. 여름 마무리 잘하세요."
        ),
        // 9월
        listOf(
            "9월을 맞이하여 따뜻한 인사를 전합니다. 오늘 하루도 행복하세요.",
            "가을바람이 찾아와야 하는 9월이네요. 빨리 시원해졌으면 좋겠습니다.",
            "가을의 풍요로움이 우리 주변을 둘러싸는 시기입니다. 소망하는 모든 일들이 이루어지길 기원합니다.",
            "엄청난 연휴가 다가옵니다. 다치지 마시고 행복한 연휴 보내세요.",
            "가을의 문턱이 오고 있습니다. 모두 밝게 웃고, 행복을 느끼세요."
        ),
        // 10월
        listOf(
            "즐거운 명절되십시요.",
            "올해도 3개월밖에 남지 않았어요. 남은 기간 더욱 행복하고 건강한 시간 되시길 바랍니다. ",
            "단풍구경도하고 행복한 추억 많이 만드시길 바랍니다.",
            "바람이 살짝은 쌀쌀해 지고 있네요. 늘 좋은 일만 가득하기를 바랍니다.",
            "올해 세운 계획들이 잘 마무리될 수 있는 10월이 되길 기원합니다."
        ),
        // 11월
        listOf(
            "이 순간을 충분히 즐기시길 바랍니다.",
            "가을의 향기가 더 짙어지는 시기입니다. 모두 따뜻한 시간을 보내세요.",
            "곧 겨울의 문턱에 다다르고 있어요. 올해 남은 시간을 가치 있게 보내시길 바랍니다.",
            "점점 더 차가워지고 있어요. 곧 겨울이 찾아올 테니, 남은 가을 충분히 즐기시길 바랍니다.",
            "올해도 얼마 남지 않았어요. 세워둔 계획들은 잘 진행되고 있나요? 마무리까지 멋지게 해내세요."
        ),
        // 12월
        listOf(
            "12월의 마지막을 멋지게 장식하길 바라며, 소중한 연말 보내시길 바랍니다.",
            "올 한 해 정말 고생하셨습니다. 한 해가 저물어 가고있네요. 후회 없이 마무리했으면 좋겠습니다.",
            "한 해를 돌아보며, 함께 할 수 있어서 행복했습니다.",
            "올 한 해 정리와 함께 새로운 시작을 꿈꾸는 시간이 되길 기원하겠습니다.",
            "한 해 동안 아쉬움이나 속상한 일들은 모두 잊고, 새로운 시작을 준비하세요."
        )
    )

    @Bean
    @Profile("production")
    fun productionConfig(): String {
        bandKey = System.getenv("GGR_PRD_KEY") ?: bandKey
        return bandKey
    }

    @Scheduled(cron = "0 30 9 * * Sat")
//    @Scheduled(fixedDelay = 10000)
    fun schedule() {
        createNewPost()
    }

    fun queryMyBands() {
        val apiVersion = "v2.1"
        val jsonResult = httpRequest(apiVersion, "bands") ?: return
        val resultData = jsonResult.jsonObject["result_data"] ?: return
        val bands = resultData.jsonObject["bands"] ?: return
        bands.jsonArray.map {
            println("band: ${it.jsonObject["name"]}, band key: ${it.jsonObject["band_key"]}")
        }
    }


    fun createNewPost() {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        var today: LocalDate = LocalDate.now()
        val gameDate = today.plusDays(6).format(formatter)
        val endDate = today.plusDays(2).format(formatter)

        val currentMonth = today.monthValue - 1 // 월에서 -1을 해야 바른 배열을 꺼냄.
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = today.get(weekFields.weekOfMonth()) - 1

        val apiVersion = "v2.2"

        val params = mapOf(
            "band_key" to bandKey,
            "content" to """
                |${gameDate} 일정 빠른 투표 부탁드립니다.
                |
                |투표마감일은 ${endDate}까지 입니다.
                |
                |투표는 메인화면에서 다가오는 일정이나 일정 탭에서 가능합니다.
            """.trimMargin(),
            "do_push" to "yes"
        )

        val jsonResult = httpRequest(apiVersion, "band/post/create", params, "POST") ?: return
        val resultData = jsonResult.jsonObject["result_data"] ?: return
        val postKey = resultData.jsonObject["post_key"] ?: return
        println("postKey: $postKey")
    }

    fun queryPosts() {
        val apiVersion = "v2"
        val params = mapOf("band_key" to bandKey)
        val jsonResult = httpRequest(apiVersion, "band/posts", params) ?: return
        val resultData = jsonResult.jsonObject["result_data"] ?: return
        val posts = resultData.jsonObject["items"] ?: return
        posts.jsonArray.map {
            println("post_key: ${it.jsonObject["post_key"]}, content: ${it.jsonObject["content"]}")
            val postKey: String = it.jsonObject["post_key"]!!.jsonPrimitive.content
            deletePost(postKey)
        }
    }

    fun deletePost(postKey: String) {
        val apiVersion = "v2"
        val params = mapOf(
            "band_key" to bandKey, "post_key" to postKey
        )
        val jsonResult = httpRequest(apiVersion, "band/post/remove", params, "POST") ?: return
        val resultData = jsonResult.jsonObject["result_data"] ?: return
        val message = resultData.jsonObject["message"] ?: return
        println("message: $message")
    }

    private fun httpRequest(
        apiVersion: String, api: String, param: Map<String, String> = mapOf(), requestMethod: String = "GET"
    ): JsonElement? {

        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = apiHost
            path(apiVersion, api)
            parameters.append("access_token", accessToken)
            param.forEach {
                parameters.append(it.key, it.value)
            }
        }.buildString()

        val uri = URI(url)
        val connection = uri.toURL().openConnection() as HttpURLConnection
        connection.requestMethod = requestMethod

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonElement = Json.parseToJsonElement(reader.readText())
                reader.close()
                return jsonElement
            } else {
                println("Failed to get a valid response")
                return null
            }
        } finally {
            connection.disconnect()
        }
    }
}