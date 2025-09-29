package com.fuplay.videoplayer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fuplay.videoplayer.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var allVideos: List<VideoFile>
    private var currentIndex: Int = 0
    private lateinit var adapter: VideoPlayerAdapter
    
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
        
        setupViewPager()
    }
    
    private fun setupViewPager() {
        adapter = VideoPlayerAdapter(allVideos, this)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        
        // Set current item to the selected video
        binding.viewPager.setCurrentItem(currentIndex, false)
        
        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentIndex = position
                
                // Pause all other videos and play current one
                pauseAllVideosExcept(position)
                
                // Start playing the current video
                adapter.playVideo(position)
            }
        })
        
        // Start playing the initial video
        binding.viewPager.post {
            adapter.playVideo(currentIndex)
        }
    }
    
    private fun pauseAllVideosExcept(currentPosition: Int) {
        adapter.pauseAllExcept(currentPosition)
    }
    
    override fun onPause() {
        super.onPause()
        // Pause current video when activity is paused
        adapter.pauseVideo(currentIndex)
    }
    
    override fun onResume() {
        super.onResume()
        // Resume current video when activity is resumed
        adapter.playVideo(currentIndex)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources - adapter will handle individual player cleanup
    }
}