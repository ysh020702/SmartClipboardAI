package com.samsung.smartclipboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiManager {
    companion object {
        private const val MODEL = "gemini-3.1-flash-lite"
        private const val API_KEY = "AIzaSyA9xmiPxuLnoV64tk8Cr8nyLtwgURmZq30"
    }
    private val client = OkHttpClient()
    suspend fun run(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", prompt)
                    }))
                }))
            }
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$API_KEY")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            val res = client.newCall(request).execute()
            val json = JSONObject(res.body?.string() ?: "")
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            ""
        }
    }
}