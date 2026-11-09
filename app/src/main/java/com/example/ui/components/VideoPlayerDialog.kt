package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoJobEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerDialog(
    video: VideoJobEntity,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onUsePromptInStudio: (String) -> Unit
) {
    val context = LocalContext.current

    // Player States
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf((video.duration * 1000).toLong()) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionRatio by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Auto-hide controls after 4 seconds of inactivity if playing
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember(video.videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            video.videoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ONE // Loop AI generation video smoothly
                prepare()
                playWhenReady = true
            }
        }
    }

    // Attach ExoPlayer Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        playbackError = null
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        playbackError = null
                        if (exoPlayer.duration > 0) {
                            totalDurationMs = exoPlayer.duration
                        }
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        isPlaying = false
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                isPlaying = false
                playbackError = "Unable to stream video: ${error.localizedMessage ?: "Network or format issue"}. Tap Retry to reload."
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Position polling loop
    LaunchedEffect(isPlaying, isScrubbing) {
        while (isPlaying && !isScrubbing) {
            delay(200)
            currentPositionMs = exoPlayer.currentPosition
            if (exoPlayer.duration > 0) {
                totalDurationMs = exoPlayer.duration
            }
        }
    }

    Dialog(
        onDismissRequest = {
            exoPlayer.stop()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = !isFullscreen
        )
    ) {
        val dialogModifier = if (isFullscreen) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        } else {
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Brush.linearGradient(listOf(ElectricBlue, NeonPurple)), RoundedCornerShape(20.dp))
                .background(TitonDarkVoid.copy(alpha = 0.98f))
        }

        Box(
            modifier = dialogModifier
                .testTag("video_player_dialog")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullscreen) 0.dp else 14.dp)
            ) {
                // Header (Only show when not in fullscreen mode)
                if (!isFullscreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) StatusSuccess else if (isBuffering) ElectricBlue else StatusPending)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TITON CINEMATIC PLAYER",
                                color = ElectricBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Fullscreen Button
                            IconButton(
                                onClick = { isFullscreen = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = ElectricBlue
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = {
                                    exoPlayer.stop()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("close_player_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Video Surface View Container
                val aspectModifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    when (video.aspectRatio) {
                        "9:16" -> Modifier
                            .fillMaxWidth(0.72f)
                            .aspectRatio(9f / 16f)
                        "1:1" -> Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                        else -> Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = aspectModifier
                            .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp))
                            .background(Color.Black)
                            .border(
                                width = if (isFullscreen) 0.dp else 1.dp,
                                color = if (isFullscreen) Color.Transparent else TitonBorderGlow,
                                shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Real Media3 PlayerView with AndroidView
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false // Use custom TITON AI M3 cyber controls
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                                }
                            },
                            update = { pv ->
                                if (pv.player != exoPlayer) {
                                    pv.player = exoPlayer
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Buffering / Loading Indicator
                        if (isBuffering && playbackError == null) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ElectricBlue,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Playback Error Banner & Retry Button
                        playbackError?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = StatusError,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = err,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            playbackError = null
                                            isBuffering = true
                                            exoPlayer.prepare()
                                            exoPlayer.play()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Playback", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Top Overlay Pills: Format, Duration, Quality
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showControls || !isPlaying,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = video.aspectRatio,
                                        color = ElectricBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f)
                                ) {
                                    Text(
                                        text = "${video.duration}s • ${video.quality}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Exit Fullscreen Button in Overlay
                        if (isFullscreen) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showControls,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                IconButton(
                                    onClick = { isFullscreen = false },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .border(1.dp, ElectricBlue, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "Exit Fullscreen",
                                        tint = ElectricBlue
                                    )
                                }
                            }
                        }

                        // Center Play / Pause Floating Button
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showControls || !isPlaying,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // -10s skip
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "Rewind 10s",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Primary Play/Pause toggle
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .border(1.5.dp, Brush.linearGradient(listOf(ElectricBlue, NeonPurple)), CircleShape)
                                        .testTag("play_pause_video_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                // +10s forward
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                        exoPlayer.seekTo(newPos)
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "Forward 10s",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Transport Controls: Scrubber, Mute, Fullscreen, Speed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isFullscreen) 16.dp else 4.dp, vertical = 6.dp)
                ) {
                    val progressValue = if (isScrubbing) {
                        scrubPositionRatio
                    } else if (totalDurationMs > 0) {
                        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = progressValue,
                        onValueChange = {
                            isScrubbing = true
                            scrubPositionRatio = it
                        },
                        onValueChangeFinished = {
                            val seekTargetMs = (scrubPositionRatio * totalDurationMs).toLong()
                            exoPlayer.seekTo(seekTargetMs)
                            isScrubbing = false
                            if (isPlaying) exoPlayer.play()
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = TitonSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("video_scrubber_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current time / Total duration
                        val currentSeconds = (if (isScrubbing) (scrubPositionRatio * totalDurationMs / 1000).toInt() else (currentPositionMs / 1000).toInt())
                        val totalSeconds = (totalDurationMs / 1000).toInt().coerceAtLeast(video.duration)
                        Text(
                            text = String.format("%02d:%02d / %02d:%02d", currentSeconds / 60, currentSeconds % 60, totalSeconds / 60, totalSeconds % 60),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Auxiliary Controls: Mute, Speed, Fullscreen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Volume / Mute button
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else 1f
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (isMuted) "Unmute" else "Mute",
                                    tint = if (isMuted) StatusError else ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Speed toggle button
                            TextButton(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        2.0f -> 0.5f
                                        else -> 1.0f
                                    }
                                    exoPlayer.setPlaybackSpeed(playbackSpeed)
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Prompt Details Card & Bottom Actions (Hidden when in Fullscreen)
                if (!isFullscreen) {
                    Surface(
                        color = TitonSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Prompt Details",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("TITON AI Prompt", video.prompt)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Prompt",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy",
                                        color = ElectricBlue,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = video.prompt,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Action Buttons: Remix & Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exoPlayer.stop()
                                onUsePromptInStudio(video.prompt)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonMagenta),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("remix_prompt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Remix in Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDownload,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("download_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Download MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
