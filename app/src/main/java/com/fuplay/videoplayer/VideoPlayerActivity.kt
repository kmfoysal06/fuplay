package com.fuplay.videoplayer

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.fuplay.videoplayer.databinding.ActivityVideoPlayerBinding
import kotlin.math.abs

class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var allVideos: List<VideoFile>
    private var currentIndex: Int = 0
    private lateinit var gestureDetector: GestureDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get video data from intent
        allVideos = intent.getParcelableArrayListExtra<VideoFile>("ALL_VIDEOS") ?: emptyList()
        currentIndex = intent.getIntExtra("CURRENT_INDEX", 0)
        
        if (allVideos.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_loading_video), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setupGestureDetector()
        setupTapOverlay()
        initializePlayer(allVideos[currentIndex])
    }
    
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                togglePlayPause()
                return true
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                
                // Check if it's a vertical swipe (more vertical than horizontal)
                if (abs(diffY) > abs(diffX) && abs(diffY) > 100 && abs(velocityY) > 100) {
                    if (diffY > 0) {
                        // Swipe down - next video
                        playNextVideo()
                    } else {
                        // Swipe up - previous video
                        playPreviousVideo()
                    }
                    return true
                }
                return false
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
        
        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            binding.playerView.player = player
            
            val mediaItem = MediaItem.fromUri(video.uri)
            player.setMediaItem(mediaItem)
            player.playWhenReady = true
            player.prepare()
            
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(this@VideoPlayerActivity, 
                        getString(R.string.error_loading_video), Toast.LENGTH_LONG).show()
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
    
    private fun playNextVideo() {
        if (currentIndex < allVideos.size - 1) {
            currentIndex++
            initializePlayer(allVideos[currentIndex])
        }
    }
    
    private fun playPreviousVideo() {
        if (currentIndex > 0) {
            currentIndex--
            initializePlayer(allVideos[currentIndex])
        }
    }
    
    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }
    
    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
    
    private fun releasePlayer() {
        exoPlayer?.let { player ->
            player.release()
            exoPlayer = null
        }
    }
}