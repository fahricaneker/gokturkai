package com.gokturk.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GokturkApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun chat(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val body = buildJsonObject { putJsonArray("messages") { messages.takeLast(20).forEach { m -> addJsonObject { put("role", m.role); put("content", m.content) } } } }
        post("/v1/chat", body)["reply"]?.jsonPrimitive?.content ?: "Şu anda cevap alınamadı."
    }
    suspend fun generateImage(prompt: String): String = withContext(Dispatchers.IO) {
        post("/v1/images", buildJsonObject { put("prompt", prompt) })["url"]?.jsonPrimitive?.content ?: error("Görsel adresi alınamadı")
    }
    private fun post(path: String, payload: JsonObject): JsonObject {
        val base = AppConfig.API_BASE_URL.trimEnd('/')
        require(!base.contains("API-ADRESINIZ")) { "Önce AppConfig içindeki API adresini ayarlayın." }
        val request = Request.Builder().url(base + path).post(payload.toString().toRequestBody(mediaType)).build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Sunucu hatası: ${response.code}")
            return json.parseToJsonElement(text).jsonObject
        }
    }
}
