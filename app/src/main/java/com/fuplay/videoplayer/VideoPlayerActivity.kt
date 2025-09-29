package com.fuplay.videoplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.fuplay.videoplayer.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val videoUriString = intent.getStringExtra("VIDEO_URI")
        if (videoUriString != null) {
            val videoUri = Uri.parse(videoUriString)
            initializePlayer(videoUri)
        } else {
            Toast.makeText(this, getString(R.string.error_loading_video), Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initializePlayer(videoUri: Uri) {
        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            binding.playerView.player = player
            
            val mediaItem = MediaItem.fromUri(videoUri)
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
    
    override fun onStart() {
        super.onStart()
        if (exoPlayer == null) {
            val videoUriString = intent.getStringExtra("VIDEO_URI")
            videoUriString?.let { 
                initializePlayer(Uri.parse(it))
            }
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
    
    override fun onStop() {
        super.onStop()
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