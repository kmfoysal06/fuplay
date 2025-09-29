package com.fuplay.videoplayer

import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.fuplay.videoplayer.databinding.ItemVideoPlayerBinding

class VideoPlayerAdapter(
    private val videos: List<VideoFile>,
    private val context: VideoPlayerActivity
) : RecyclerView.Adapter<VideoPlayerAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(private val binding: ItemVideoPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private lateinit var gestureDetector: GestureDetector

        fun bind(video: VideoFile) {
            setupGestureDetector()
            setupTapOverlay()
            initializePlayer(video)
        }

        private fun setupGestureDetector() {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    togglePlayPause()
                    return true
                }
            })
        }

        private fun setupTapOverlay() {
            binding.tapOverlay.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }

        private fun initializePlayer(video: VideoFile) {
            releasePlayer()
            
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                binding.playerView.player = player
                
                val mediaItem = MediaItem.fromUri(video.uri)
                player.setMediaItem(mediaItem)
                player.playWhenReady = true
                player.prepare()
                
                player.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        // Handle error silently to avoid disrupting user experience
                    }
                })
            }
        }

        private fun togglePlayPause() {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }

        fun pausePlayer() {
            exoPlayer?.pause()
        }

        fun playPlayer() {
            exoPlayer?.play()
        }

        fun releasePlayer() {
            exoPlayer?.let { player ->
                player.release()
                exoPlayer = null
            }
        }
    }
}