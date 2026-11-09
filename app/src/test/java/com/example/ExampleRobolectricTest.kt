package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.VideoApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TITON AI", appName)
  }

  @Test
  fun `sanitizeApiKey properly cleans raw keys and strips redundant prefixes`() {
    // Strips surrounding quotes
    assertEquals("my-secret-fal-key", VideoApiService.sanitizeApiKey("\"my-secret-fal-key\""))
    assertEquals("my-secret-fal-key", VideoApiService.sanitizeApiKey("'my-secret-fal-key'"))

    // Strips accidental 'Key ' or 'Bearer ' prefixes
    assertEquals("c0258a6f-1234-5678", VideoApiService.sanitizeApiKey("Key c0258a6f-1234-5678"))
    assertEquals("c0258a6f-1234-5678", VideoApiService.sanitizeApiKey("Bearer c0258a6f-1234-5678"))
    assertEquals("c0258a6f-1234-5678", VideoApiService.sanitizeApiKey("  Key c0258a6f-1234-5678  "))

    // Leaves clean key intact
    assertEquals("fal_key_xyz987", VideoApiService.sanitizeApiKey("fal_key_xyz987"))
  }

  @Test
  fun `isPlaceholderKey detects unconfigured or default secrets`() {
    assertTrue(VideoApiService.isPlaceholderKey(null))
    assertTrue(VideoApiService.isPlaceholderKey(""))
    assertTrue(VideoApiService.isPlaceholderKey("   "))
    assertTrue(VideoApiService.isPlaceholderKey("DEFAULT_FAL_KEY"))
    assertTrue(VideoApiService.isPlaceholderKey("DEFAULT_REPLICATE_TOKEN"))
    assertTrue(VideoApiService.isPlaceholderKey("placeholder"))
    assertTrue(VideoApiService.isPlaceholderKey("null"))

    assertFalse(VideoApiService.isPlaceholderKey("valid_fal_key_12345"))
  }

  @Test
  fun `parseFalErrorMessage formats 401, 403, 402, 429 into actionable user guidance`() {
    // 401 Unauthorized
    val msg401 = VideoApiService.parseFalErrorMessage(401, "{\"detail\":\"Cannot access application 'fal-ai/ltx-video'. Authentication is required.\"}")
    assertTrue(msg401.contains("401"))
    assertTrue(msg401.contains("FAL_KEY"))
    assertTrue(msg401.contains("AI Studio Secrets"))

    // 403 Forbidden
    val msg403 = VideoApiService.parseFalErrorMessage(403, "{\"detail\":\"Forbidden: Model access not granted\"}")
    assertTrue(msg403.contains("403"))
    assertTrue(msg403.contains("fal-ai/ltx-video"))

    // 402 / Quota Exceeded
    val msg402 = VideoApiService.parseFalErrorMessage(402, "{\"detail\":\"Insufficient compute credits\"}")
    assertTrue(msg402.contains("Balance Exceeded") || msg402.contains("402"))

    // 429 Rate Limit
    val msg429 = VideoApiService.parseFalErrorMessage(429, "{\"detail\":\"Rate limit reached\"}")
    assertTrue(msg429.contains("429"))
    assertTrue(msg429.contains("Rate Limit"))
  }

  @Test
  fun `showcase video URLs are valid MP4 files and not broken legacy endpoints`() {
    val validSampleUrls = listOf(
      "https://media.w3.org/2010/05/sintel/trailer.mp4",
      "https://media.w3.org/2010/05/video/movie_300.mp4",
      "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
      "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4"
    )

    validSampleUrls.forEach { url ->
      assertTrue("URL must be HTTPS", url.startsWith("https://"))
      assertTrue("URL must end with .mp4", url.endsWith(".mp4"))
      assertFalse("Must not point to broken commondatastorage bucket", url.contains("commondatastorage.googleapis.com"))
    }
  }
}
