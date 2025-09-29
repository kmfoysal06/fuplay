package com.fuplay.videoplayer

import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.SeekBar
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

    private val viewHolders = mutableMapOf<Int, VideoViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        viewHolders[position] = holder
        holder.bind(videos[position], position)
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
        viewHolders.values.removeAll { it == holder }
    }

    override fun getItemCount(): Int = videos.size

    fun pauseAllExcept(currentPosition: Int) {
        viewHolders.forEach { (position, holder) ->
            if (position != currentPosition) {
                holder.pausePlayer()
            }
        }
    }

    fun playVideo(position: Int) {
        viewHolders[position]?.playPlayer()
    }

    fun pauseVideo(position: Int) {
        viewHolders[position]?.pausePlayer()
    }

    inner class VideoViewHolder(private val binding: ItemVideoPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private lateinit var gestureDetector: GestureDetector
        private var isPlayerInitialized = false
        private val progressHandler = Handler(Looper.getMainLooper())
        private var isUserSeeking = false

        private val updateProgressRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                progressHandler.postDelayed(this, 100) // Update every 100ms
            }
        }

        fun bind(video: VideoFile, position: Int) {
            setupGestureDetector()
            setupTapOverlay()
            setupProgressBar()
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

        private fun setupProgressBar() {
            binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        exoPlayer?.let { player ->
                            val duration = player.duration
                            if (duration > 0) {
                                val seekPosition = (progress * duration) / 100
                                player.seekTo(seekPosition)
                            }
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = false
                }
            })
        }

        private fun initializePlayer(video: VideoFile) {
            if (isPlayerInitialized) return
            
            releasePlayer()
            
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                binding.playerView.player = player
                
                val mediaItem = MediaItem.fromUri(video.uri)
                player.setMediaItem(mediaItem)
                player.playWhenReady = false // Don't auto-play
                player.repeatMode = Player.REPEAT_MODE_OFF // No automatic repeat
                player.prepare()
                
                player.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        // Handle error silently to avoid disrupting user experience
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                // Player is ready, start progress updates
                                startProgressUpdates()
                            }
                            Player.STATE_ENDED -> {
                                // Video ended, restart but keep paused
                                player.seekTo(0)
                                player.playWhenReady = false
                                player.prepare()
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            startProgressUpdates()
                        } else {
                            stopProgressUpdates()
                        }
                    }
                })
            }
            isPlayerInitialized = true
        }

        private fun updateProgress() {
            if (!isUserSeeking) {
                exoPlayer?.let { player ->
                    val duration = player.duration
                    val currentPosition = player.currentPosition
                    
                    if (duration > 0) {
                        val progress = ((currentPosition * 100) / duration).toInt()
                        binding.progressBar.progress = progress
                    }
                }
            }
        }

        private fun startProgressUpdates() {
            stopProgressUpdates()
            progressHandler.post(updateProgressRunnable)
        }

        private fun stopProgressUpdates() {
            progressHandler.removeCallbacks(updateProgressRunnable)
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
            stopProgressUpdates()
        }

        fun playPlayer() {
            exoPlayer?.play()
            startProgressUpdates()
        }

        fun releasePlayer() {
            stopProgressUpdates()
            exoPlayer?.let { player ->
                player.release()
                exoPlayer = null
            }
            isPlayerInitialized = false
        }
    }
}