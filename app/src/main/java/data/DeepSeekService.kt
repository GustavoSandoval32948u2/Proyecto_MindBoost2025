package data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object DeepSeekService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val BASE_URL = "https://api.deepseek.com/v1/chat/completions"
    private const val API_KEY = "sk-00458f11d9a94a1d9a027279fde599b2"

    @Serializable
    data class DeepSeekMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class DeepSeekRequest(
        val model: String,
        val messages: List<DeepSeekMessage>
    )

    @Serializable
    data class DeepSeekResponse(
        val choices: List<Choice>
    ) {
        @Serializable
        data class Choice(val message: DeepSeekMessage)
    }

    suspend fun getRecommendation(prompt: String): String {
        val requestBody = DeepSeekRequest(
            model = "deepseek-chat",
            messages = listOf(
                DeepSeekMessage("system", "Eres un asistente que recomienda hábitos saludables."),
                DeepSeekMessage("user", prompt)
            )
        )

        val response: HttpResponse = client.post(BASE_URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $API_KEY")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(requestBody)
        }

        val result: DeepSeekResponse = response.body()
        return result.choices.firstOrNull()?.message?.content ?: "No se obtuvo respuesta."
    }
}