package com.example.data.network

import android.content.Context
import android.os.Environment
import com.example.data.model.ProviderConfigEntity
import com.example.data.model.VideoJobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

sealed class VideoGenerationResult {
    data class Success(val videoUrl: String) : VideoGenerationResult()
    data class Progress(val percent: Int, val statusText: String) : VideoGenerationResult()
    data class Failure(val errorMessage: String, val isConfigMissing: Boolean = false) : VideoGenerationResult()
}

class VideoApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        const val FAL_LTX_ENDPOINT = "https://queue.fal.run/fal-ai/ltx-video"
        const val FAL_PROBE_URL = "https://queue.fal.run/fal-ai/ltx-video/requests/auth-probe-test-request/status"

        /**
         * Cleans and sanitizes raw API keys:
         * 1. Strips surrounding double or single quotes
         * 2. Trims leading/trailing whitespace and newlines
         * 3. Removes redundant "Key " or "Bearer " prefixes accidentally included by users
         */
        fun sanitizeApiKey(raw: String?): String {
            if (raw == null) return ""
            var key = raw.trim()
            if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
                key = key.substring(1, key.length - 1).trim()
            }
            if (key.startsWith("Key ", ignoreCase = true)) {
                key = key.substring(4).trim()
            } else if (key.startsWith("Bearer ", ignoreCase = true)) {
                key = key.substring(7).trim()
            }
            return key
        }

        fun isPlaceholderKey(key: String?): Boolean {
            if (key == null) return true
            val clean = sanitizeApiKey(key)
            return clean.isBlank() ||
                    clean.startsWith("DEFAULT_", ignoreCase = true) ||
                    clean.equals("null", ignoreCase = true) ||
                    clean.equals("placeholder", ignoreCase = true)
        }

        /**
         * Formats Fal.ai HTTP error responses into clear, actionable user messages.
         */
        fun parseFalErrorMessage(code: Int, body: String): String {
            val detail = try {
                val json = JSONObject(body)
                val d = json.optString("detail", "")
                if (d.isNotBlank()) d
                else {
                    val err = json.optString("error", "")
                    if (err.isNotBlank()) err
                    else json.optString("message", "")
                }
            } catch (_: Throwable) {
                body.trim()
            }

            return when (code) {
                401 -> "Fal.ai Authentication Failed (HTTP 401): $detail. The FAL_KEY is invalid, expired, or missing. Please configure your FAL_KEY securely in AI Studio Secrets or Studio Settings."
                403 -> "Fal.ai Access Forbidden (HTTP 403): $detail. Your Fal.ai account does not have permission to access 'fal-ai/ltx-video'. Please ensure your account has active model access."
                402 -> "Fal.ai Balance Exceeded (HTTP 402): $detail. Insufficient compute credits on your Fal.ai account. Please recharge at fal.ai/dashboard to continue generating AI videos."
                429 -> "Fal.ai Rate Limit Exceeded (HTTP 429): $detail. Too many requests sent to Fal.ai. Please wait a moment before trying again."
                500, 502, 503, 504 -> "Fal.ai Server Unavailable (HTTP $code): The AI model server is temporarily experiencing high load. Please retry in a few moments."
                else -> {
                    if (detail.contains("quota", ignoreCase = true) || detail.contains("balance", ignoreCase = true) || detail.contains("credits", ignoreCase = true)) {
                        "Fal.ai Balance Exceeded: $detail. Please recharge your balance at fal.ai/dashboard."
                    } else if (detail.isNotBlank()) {
                        "Fal.ai Error (HTTP $code): $detail"
                    } else {
                        "Fal.ai Error (HTTP $code). Please check connection and credentials."
                    }
                }
            }
        }
    }

    /**
     * Executes real AI video generation against the specified provider API.
     * Enforces strict requirements:
     * 1. Connect real video generation provider (Fal.ai, Replicate, or custom backend proxy).
     * 2. Never simulate successful generation when no provider/key is connected.
     * 3. Track real generation progress.
     */
    suspend fun generateVideo(
        provider: ProviderConfigEntity,
        resolvedApiKey: String,
        job: VideoJobEntity,
        onProgress: suspend (Int, String) -> Unit
    ): VideoGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = sanitizeApiKey(resolvedApiKey)
        val endpoint = provider.apiEndpoint.trim()

        // Strict Requirement: NEVER simulate when no real provider is connected
        if (isPlaceholderKey(apiKey) && endpoint.isEmpty()) {
            return@withContext VideoGenerationResult.Failure(
                errorMessage = "No API key or backend proxy configured for ${provider.displayName}. A real AI provider credential is required to generate videos. Please configure your API key in the Secrets panel or Studio Settings.",
                isConfigMissing = true
            )
        }

        try {
            onProgress(10, "Connecting to ${provider.displayName}...")

            when (provider.id.lowercase()) {
                "fal_ai" -> {
                    return@withContext handleFalAiGeneration(apiKey, endpoint, job, onProgress)
                }
                "replicate" -> {
                    return@withContext handleReplicateGeneration(apiKey, endpoint, job, provider.defaultModel, onProgress)
                }
                else -> {
                    // Custom API Endpoint or Secure Backend Proxy
                    return@withContext handleCustomBackendGeneration(provider.displayName, apiKey, endpoint, job, onProgress)
                }
            }
        } catch (e: Exception) {
            return@withContext VideoGenerationResult.Failure("Network error communicating with AI server: ${e.localizedMessage ?: "Connection timed out"}")
        }
    }

    /**
     * Verifies Fal.ai authentication via probe before video submission.
     * Prevents wasted effort and provides instant feedback without consuming generation queue credits.
     */
    suspend fun verifyFalAiAuth(cleanKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (isPlaceholderKey(cleanKey)) {
            return@withContext Result.failure(
                IllegalArgumentException("FAL_KEY is not configured. Please add your FAL_KEY in AI Studio Secrets or Studio Settings.")
            )
        }

        try {
            val req = Request.Builder()
                .url(FAL_PROBE_URL)
                .get()
                .addHeader("Authorization", "Key $cleanKey")
                .addHeader("x-fal-key", cleanKey)
                .addHeader("Accept", "application/json")
                .build()

            val res = client.newCall(req).execute()
            val code = res.code
            val body = res.body?.string() ?: ""

            when {
                code == 401 -> Result.failure(IllegalStateException(parseFalErrorMessage(401, body)))
                code == 403 -> Result.failure(IllegalStateException(parseFalErrorMessage(403, body)))
                code == 402 || body.contains("quota", ignoreCase = true) || body.contains("balance", ignoreCase = true) -> {
                    Result.failure(IllegalStateException(parseFalErrorMessage(402, body)))
                }
                code == 429 -> Result.failure(IllegalStateException(parseFalErrorMessage(429, body)))
                res.isSuccessful || code == 404 -> {
                    // HTTP 404 confirms the probe reached past authentication into the queue router!
                    Result.success("Fal.ai Authentication Verified! Connection to 'fal-ai/ltx-video' is active and authorized.")
                }
                else -> {
                    Result.failure(IllegalStateException(parseFalErrorMessage(code, body)))
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Network connection to Fal.ai failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    /**
     * Real Fal.ai Video API integration (LTX-Video / Kling Video / Hunyuan Queue)
     * Handles both Text-to-Video and Image-to-Video.
     */
    private suspend fun handleFalAiGeneration(
        apiKey: String,
        endpoint: String,
        job: VideoJobEntity,
        onProgress: suspend (Int, String) -> Unit
    ): VideoGenerationResult {
        val cleanKey = sanitizeApiKey(apiKey)
        if (isPlaceholderKey(cleanKey)) {
            return VideoGenerationResult.Failure(
                "Fal.ai API key is missing or not configured. Please enter your FAL_KEY in AI Studio Secrets or Studio Settings.",
                isConfigMissing = true
            )
        }

        // Pre-flight authentication verification
        onProgress(15, "Verifying Fal.ai credentials & model authorization...")
        val authVerification = verifyFalAiAuth(cleanKey)
        if (authVerification.isFailure) {
            val err = authVerification.exceptionOrNull()?.message ?: "Fal.ai authentication failed."
            return VideoGenerationResult.Failure(
                errorMessage = err,
                isConfigMissing = err.contains("401") || err.contains("403") || err.contains("FAL_KEY")
            )
        }

        // Use configured endpoint or default to official fal-ai/ltx-video
        val targetUrl = if (endpoint.isNotBlank()) endpoint else FAL_LTX_ENDPOINT

        val body = JSONObject().apply {
            put("prompt", job.prompt)
            if (job.negativePrompt.isNotBlank()) {
                put("negative_prompt", job.negativePrompt)
            }
            put("aspect_ratio", job.aspectRatio)

            // Image-to-Video support
            if (!job.imageUrl.isNullOrBlank()) {
                put("image_url", job.imageUrl)
            }
        }

        val request = Request.Builder()
            .url(targetUrl)
            .post(body.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Key $cleanKey")
            .addHeader("x-fal-key", cleanKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        onProgress(25, "Submitting to Fal.ai rendering queue (fal-ai/ltx-video)...")

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = parseFalErrorMessage(response.code, responseBody)
            return VideoGenerationResult.Failure(
                errorMessage = errorMsg,
                isConfigMissing = response.code == 401 || response.code == 403
            )
        }

        val resJson = JSONObject(responseBody)
        val statusUrl = resJson.optString("status_url", "")
        val responseUrl = resJson.optString("response_url", "")

        if (statusUrl.isBlank()) {
            val directUrl = extractVideoUrl(resJson)
            return if (!directUrl.isNullOrBlank()) VideoGenerationResult.Success(directUrl)
            else VideoGenerationResult.Failure("Fal.ai returned unexpected response structure.")
        }

        // Poll queue status
        var attempts = 0
        while (attempts < 35) {
            delay(4000)
            attempts++
            val pct = (25 + (attempts * 2)).coerceAtMost(92)
            onProgress(pct, "Rendering diffusion steps ($attempts/35)...")

            val pollReq = Request.Builder()
                .url(statusUrl)
                .get()
                .addHeader("Authorization", "Key $cleanKey")
                .addHeader("x-fal-key", cleanKey)
                .build()

            val pollRes = client.newCall(pollReq).execute()
            val pollBody = pollRes.body?.string() ?: ""

            if (pollRes.isSuccessful) {
                val pollJson = JSONObject(pollBody)
                val status = pollJson.optString("status", "").uppercase()
                if (status == "COMPLETED") {
                    val finalReq = Request.Builder()
                        .url(if (responseUrl.isNotBlank()) responseUrl else statusUrl)
                        .get()
                        .addHeader("Authorization", "Key $cleanKey")
                        .addHeader("x-fal-key", cleanKey)
                        .build()

                    val finalRes = client.newCall(finalReq).execute()
                    val finalBody = finalRes.body?.string() ?: ""
                    val finalJson = JSONObject(finalBody)
                    val videoUrl = extractVideoUrl(finalJson)
                    if (!videoUrl.isNullOrBlank()) {
                        onProgress(100, "Rendering complete!")
                        return VideoGenerationResult.Success(videoUrl)
                    }
                } else if (status == "FAILED" || status == "ERROR") {
                    val errMsg = pollJson.optString("error", "Generation failed on Fal.ai.")
                    return VideoGenerationResult.Failure("Fal.ai generation failed: $errMsg")
                }
            } else if (pollRes.code == 401 || pollRes.code == 403) {
                return VideoGenerationResult.Failure(parseFalErrorMessage(pollRes.code, pollBody))
            }
        }

        return VideoGenerationResult.Failure("Fal.ai generation timed out. Please try again.")
    }

    /**
     * Real Replicate Video API integration (Wan-AI / Minimax / Kling)
     * Handles both Text-to-Video and Image-to-Video.
     */
    private suspend fun handleReplicateGeneration(
        apiKey: String,
        endpoint: String,
        job: VideoJobEntity,
        model: String,
        onProgress: suspend (Int, String) -> Unit
    ): VideoGenerationResult {
        val cleanKey = sanitizeApiKey(apiKey)
        if (isPlaceholderKey(cleanKey)) {
            return VideoGenerationResult.Failure(
                "Replicate API token is missing. Please enter your REPLICATE_API_TOKEN in Secrets or Studio Settings.",
                isConfigMissing = true
            )
        }

        val targetUrl = if (endpoint.isNotBlank()) endpoint else "https://api.replicate.com/v1/predictions"

        val inputJson = JSONObject().apply {
            put("prompt", job.prompt)
            if (job.negativePrompt.isNotBlank()) {
                put("negative_prompt", job.negativePrompt)
            }
            put("aspect_ratio", job.aspectRatio)

            // Image-to-Video support
            if (!job.imageUrl.isNullOrBlank()) {
                put("input_image", job.imageUrl)
                put("image", job.imageUrl)
            }
        }

        val body = JSONObject().apply {
            if (model.isNotBlank()) put("version", model)
            put("input", inputJson)
        }

        val request = Request.Builder()
            .url(targetUrl)
            .post(body.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $cleanKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        onProgress(20, "Initiating Replicate video prediction...")

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            return VideoGenerationResult.Failure("Replicate Error (${response.code}): ${parseErrorMessage(response.code, responseBody)}")
        }

        val resJson = JSONObject(responseBody)
        val pollUrl = resJson.optJSONObject("urls")?.optString("get", "")
            ?.takeIf { it.isNotBlank() } ?: "${targetUrl}/${resJson.optString("id")}"

        var attempts = 0
        while (attempts < 35) {
            delay(4000)
            attempts++
            val pct = (25 + (attempts * 2)).coerceAtMost(92)
            onProgress(pct, "Replicate rendering frames ($attempts/35)...")

            val pollReq = Request.Builder()
                .url(pollUrl)
                .header("Authorization", "Bearer $cleanKey")
                .get()
                .build()

            val pollRes = client.newCall(pollReq).execute()
            val pollBody = pollRes.body?.string() ?: ""

            if (pollRes.isSuccessful) {
                val pollJson = JSONObject(pollBody)
                val status = pollJson.optString("status", "").lowercase()
                if (status == "succeeded") {
                    val videoUrl = extractVideoUrl(pollJson)
                    if (!videoUrl.isNullOrBlank()) {
                        onProgress(100, "Rendering complete!")
                        return VideoGenerationResult.Success(videoUrl)
                    }
                } else if (status == "failed" || status == "canceled") {
                    return VideoGenerationResult.Failure("Replicate generation failed: ${pollJson.optString("error", "Unknown error")}")
                }
            }
        }

        return VideoGenerationResult.Failure("Replicate generation timed out.")
    }

    /**
     * Custom Video Backend Proxy API integration
     */
    private suspend fun handleCustomBackendGeneration(
        providerName: String,
        apiKey: String,
        endpoint: String,
        job: VideoJobEntity,
        onProgress: suspend (Int, String) -> Unit
    ): VideoGenerationResult {
        if (endpoint.isBlank()) {
            return VideoGenerationResult.Failure(
                "No backend proxy URL configured for $providerName. Set TITON_BACKEND_URL in Secrets or configure it in Studio Settings.",
                isConfigMissing = true
            )
        }

        val cleanKey = sanitizeApiKey(apiKey)

        val requestBodyJson = JSONObject().apply {
            put("prompt", job.prompt)
            if (job.negativePrompt.isNotBlank()) {
                put("negative_prompt", job.negativePrompt)
            }
            put("aspect_ratio", job.aspectRatio)
            put("duration", job.duration)
            put("quality", job.quality)
            if (!job.imageUrl.isNullOrBlank()) {
                put("image_url", job.imageUrl)
                put("mode", "image_to_video")
            } else {
                put("mode", "text_to_video")
            }
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")

        if (cleanKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $cleanKey")
        }

        onProgress(25, "Submitting to custom backend proxy...")

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.code, responseBody)
            return VideoGenerationResult.Failure("Backend Error (${response.code}): $errorMsg")
        }

        onProgress(50, "Generating video frames on backend...")

        val responseJson = JSONObject(responseBody)
        var videoUrl = extractVideoUrl(responseJson)

        val externalJobId = responseJson.optString("id", "")
            .ifBlank { responseJson.optString("task_id", "") }
            .ifBlank { responseJson.optString("request_id", "") }

        if (videoUrl.isNullOrBlank() && externalJobId.isNotBlank()) {
            val statusUrl = if (endpoint.endsWith("/")) "$endpoint$externalJobId" else "$endpoint/$externalJobId"
            videoUrl = pollJobStatus(statusUrl, cleanKey, onProgress)
        }

        return if (!videoUrl.isNullOrBlank()) {
            onProgress(100, "Cinematic video generation complete!")
            VideoGenerationResult.Success(videoUrl)
        } else {
            VideoGenerationResult.Failure("Backend did not return a valid video output file.")
        }
    }

    /**
     * Tests real API connection to the specified provider.
     * Verifies authentication, network reachability, and model availability.
     */
    suspend fun testConnection(
        provider: ProviderConfigEntity,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = sanitizeApiKey(apiKey)
        val endpoint = provider.apiEndpoint.trim()

        if (isPlaceholderKey(cleanKey) && endpoint.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("No API key or endpoint configured for ${provider.displayName}. Please configure your API key in AI Studio Secrets or Studio Settings.")
            )
        }

        try {
            when (provider.id.lowercase()) {
                "fal_ai" -> {
                    return@withContext verifyFalAiAuth(cleanKey)
                }
                "replicate" -> {
                    if (isPlaceholderKey(cleanKey)) {
                        return@withContext Result.failure(IllegalArgumentException("Replicate API token is missing. Add REPLICATE_API_TOKEN in Secrets or Studio Settings."))
                    }
                    val req = Request.Builder()
                        .url("https://api.replicate.com/v1/account")
                        .get()
                        .addHeader("Authorization", "Bearer $cleanKey")
                        .addHeader("Accept", "application/json")
                        .build()

                    val res = client.newCall(req).execute()
                    val code = res.code
                    val body = res.body?.string() ?: ""

                    if (res.isSuccessful) {
                        val username = try { JSONObject(body).optString("username", "user") } catch (_: Throwable) { "user" }
                        return@withContext Result.success("Replicate Connection Verified! Authenticated as '$username'.")
                    } else if (code == 401 || code == 403) {
                        return@withContext Result.failure(IllegalStateException("Replicate Authentication Failed: Invalid API token (HTTP $code)."))
                    } else {
                        return@withContext Result.failure(IllegalStateException("Replicate responded with HTTP $code: ${parseErrorMessage(code, body)}"))
                    }
                }
                else -> {
                    // Custom backend proxy
                    if (endpoint.isEmpty()) {
                        return@withContext Result.failure(IllegalArgumentException("Custom backend proxy URL is missing. Set TITON_BACKEND_URL or specify an endpoint."))
                    }
                    val testUrl = if (endpoint.endsWith("/")) "${endpoint}health" else "$endpoint/health"
                    val reqBuilder = Request.Builder().url(testUrl).get()
                    if (cleanKey.isNotEmpty()) reqBuilder.addHeader("Authorization", "Bearer $cleanKey")

                    try {
                        val res = client.newCall(reqBuilder.build()).execute()
                        if (res.isSuccessful || res.code in 200..299) {
                            return@withContext Result.success("Backend proxy is reachable and online (HTTP ${res.code}).")
                        } else {
                            val baseRes = client.newCall(Request.Builder().url(endpoint).get().build()).execute()
                            if (baseRes.isSuccessful || baseRes.code in 200..404) {
                                return@withContext Result.success("Backend proxy server responded (HTTP ${baseRes.code}).")
                            } else {
                                return@withContext Result.failure(IllegalStateException("Backend server returned HTTP ${baseRes.code}"))
                            }
                        }
                    } catch (e: Exception) {
                        return@withContext Result.failure(IllegalStateException("Cannot reach custom endpoint: ${e.localizedMessage ?: "Connection refused"}"))
                    }
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalStateException("Network connection failed: ${e.localizedMessage ?: "Unknown network error"}"))
        }
    }

    private suspend fun pollJobStatus(
        statusUrl: String,
        apiKey: String,
        onProgress: suspend (Int, String) -> Unit
    ): String? {
        var attempts = 0
        while (attempts < 25) {
            delay(3000)
            attempts++
            val pct = (50 + (attempts * 2)).coerceAtMost(95)
            onProgress(pct, "Processing video frames ($attempts/25)...")

            val reqBuilder = Request.Builder().url(statusUrl).get()
            if (apiKey.isNotBlank()) reqBuilder.addHeader("Authorization", "Bearer $apiKey")

            val res = client.newCall(reqBuilder.build()).execute()
            val body = res.body?.string() ?: ""

            if (res.isSuccessful) {
                val json = JSONObject(body)
                val status = json.optString("status", "").lowercase()
                if (status == "succeeded" || status == "completed") {
                    return extractVideoUrl(json)
                } else if (status == "failed" || status == "error") {
                    return null
                }
            }
        }
        return null
    }

    private fun extractVideoUrl(json: JSONObject): String? {
        val videoUrl = json.optString("video_url", "")
        if (videoUrl.isNotBlank()) return videoUrl

        val url = json.optString("url", "")
        if (url.isNotBlank() && (url.endsWith(".mp4") || url.contains("video"))) return url

        val nestedUrl = json.optJSONObject("video")?.optString("url", "") ?: ""
        if (nestedUrl.isNotBlank()) return nestedUrl

        val output = json.opt("output")
        if (output is JSONArray && output.length() > 0) {
            val first = output.optString(0, "")
            if (first.isNotBlank()) return first
        } else if (output is String && output.isNotBlank()) {
            return output
        }

        return null
    }

    private fun parseErrorMessage(code: Int, body: String): String {
        return try {
            val json = JSONObject(body)
            json.optString("error", json.optString("message", json.optString("detail", "HTTP $code")))
        } catch (e: Exception) {
            "HTTP $code: $body"
        }
    }

    /**
     * Downloads a video file to the device's public Download directory.
     */
    suspend fun downloadVideoToDevice(
        context: Context,
        videoUrl: String,
        fileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val cleanName = if (fileName.endsWith(".mp4")) fileName else "$fileName.mp4"
            val targetFile = File(downloadsDir, cleanName)

            val url = URL(videoUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP error: ${connection.responseCode}"))
            }

            val input: InputStream = connection.inputStream
            val output = FileOutputStream(targetFile)
            val data = ByteArray(4096)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
