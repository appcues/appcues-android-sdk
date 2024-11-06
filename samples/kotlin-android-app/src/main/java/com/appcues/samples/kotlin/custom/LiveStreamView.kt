package com.appcues.samples.kotlin.custom

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.appcues.AppcuesCustomComponentView
import com.appcues.AppcuesExperienceActions
import com.appcues.samples.kotlin.R

internal class LiveStreamView(private val context: Context) : AppcuesCustomComponentView {

    override fun getDebugConfig(): Map<String, Any> =
        mapOf(
            "url" to "https://zssd-koala.hls.camzonecdn.com/CamzoneStreams/zssd-koala/Playlist.m3u8",
            "showControls" to false,
            "isMuted" to false,
        )

    @SuppressLint("SetTextI18n")
    override fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup {
        val url = config?.get("url") as String?
        val showControls = config?.get("showControls") as Boolean? ?: false
        val isMuted = config?.get("isMuted") as Boolean? ?: false

        return LiveStreamView(context).apply {

            showControls(showControls)
            mute(isMuted)

            url?.let { setStreamUrl(it) }
        }
    }

    private class LiveStreamView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {

        private val playerView: PlayerView
        private val player: ExoPlayer
        private var streamUrl: String? = null

        fun mute(mute: Boolean) {
            player.volume = if (mute) 0f else 1f
        }

        @OptIn(UnstableApi::class)
        fun showControls(show: Boolean) {
            if (show) {
                playerView.showController()
                playerView.useController = true
            } else {
                playerView.hideController()
                playerView.useController = false
            }
        }

        init {
            LayoutInflater.from(context).inflate(R.layout.live_stream_view, this)
            playerView = findViewById(R.id.player_view)

            // Initialize ExoPlayer (now part of Media3)
            player = ExoPlayer.Builder(context).build()
            playerView.player = player

            // Listen for playback state changes
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            // SHOW buffering indicator
                        }
                        Player.STATE_READY -> {
                            // Hide buffering indicator
                        }
                        Player.STATE_ENDED -> {
                            // Stream ended
                        }
                        Player.STATE_IDLE -> {
                            // Player idle
                        }
                    }
                }
            })
        }

        fun setStreamUrl(url: String?) {
            this.streamUrl = url
            if (url != null) {
                startPlayback()
            } else {
                stopPlayback()
            }
        }

        private fun startPlayback() {
            if (!streamUrl.isNullOrEmpty()) {
                val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
            }
        }

        private fun stopPlayback() {
            player.stop()
            player.clearMediaItems()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            // Release player resources
            player.release()
        }
    }
}
