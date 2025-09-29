package com.fuplay.videoplayer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fuplay.videoplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var folderAdapter: VideoFolderAdapter
    private val videoFolders = mutableListOf<VideoFolder>()
    private val allVideos = mutableListOf<VideoFile>()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadVideos()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
            showNoVideosMessage()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        checkPermissionAndLoadVideos()
    }
    
    private fun setupRecyclerView() {
        folderAdapter = VideoFolderAdapter(videoFolders) { folder ->
            playVideosFromFolder(folder)
        }
        binding.rvVideoFolders.layoutManager = LinearLayoutManager(this)
        binding.rvVideoFolders.adapter = folderAdapter
    }
    
    private fun checkPermissionAndLoadVideos() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                loadVideos()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun loadVideos() {
        showLoading()
        
        // Use coroutine to load videos in background
        CoroutineScope(Dispatchers.IO).launch {
            val videos = getVideosFromMediaStore()
            val folders = groupVideosByFolder(videos)
            
            withContext(Dispatchers.Main) {
                allVideos.clear()
                allVideos.addAll(videos)
                
                videoFolders.clear()
                videoFolders.addAll(folders)
                folderAdapter.notifyDataSetChanged()
                
                if (folders.isEmpty()) {
                    showNoVideosMessage()
                } else {
                    showVideoList()
                }
            }
        }
    }
    
    private fun getVideosFromMediaStore(): List<VideoFile> {
        val videos = mutableListOf<VideoFile>()
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )
        
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val size = it.getLong(sizeColumn)
                val duration = it.getLong(durationColumn)
                val data = it.getString(dataColumn)
                
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                
                // Extract folder name from file path
                val folderName = getFolderNameFromPath(data)
                
                videos.add(VideoFile(id, name, uri, size, duration, folderName, data))
            }
        }
        
        return videos
    }
    
    private fun getFolderNameFromPath(path: String): String {
        return try {
            val parts = path.split("/")
            if (parts.size > 1) {
                parts[parts.size - 2] // Get parent directory name
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun groupVideosByFolder(videos: List<VideoFile>): List<VideoFolder> {
        val folderMap = mutableMapOf<String, VideoFolder>()
        
        videos.forEach { video ->
            val folder = folderMap.getOrPut(video.folderName) {
                VideoFolder(video.folderName)
            }
            folder.videos.add(video)
        }
        
        return folderMap.values.sortedBy { it.name }
    }
    
    private fun playVideosFromFolder(folder: VideoFolder) {
        // Only pass videos from the selected folder, not all videos
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putParcelableArrayListExtra("ALL_VIDEOS", ArrayList(folder.videos))
            putExtra("CURRENT_INDEX", 0) // Start with first video in folder
        }
        startActivity(intent)
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvVideoFolders.visibility = View.GONE
        binding.tvNoVideos.visibility = View.GONE
    }
    
    private fun showVideoList() {
        binding.progressBar.visibility = View.GONE
        binding.rvVideoFolders.visibility = View.VISIBLE
        binding.tvNoVideos.visibility = View.GONE
    }
    
    private fun showNoVideosMessage() {
        binding.progressBar.visibility = View.GONE
        binding.rvVideoFolders.visibility = View.GONE
        binding.tvNoVideos.visibility = View.VISIBLE
    }
}