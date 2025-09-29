package com.fuplay.videoplayer

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fuplay.videoplayer.databinding.ItemVideoFolderBinding
import kotlinx.coroutines.*

class VideoFolderAdapter(
    private val folders: List<VideoFolder>,
    private val onFolderClick: (VideoFolder) -> Unit
) : RecyclerView.Adapter<VideoFolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemVideoFolderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size

    inner class FolderViewHolder(private val binding: ItemVideoFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun bind(folder: VideoFolder) {
            binding.tvFolderName.text = folder.name
            binding.tvVideoCount.text = "${folder.videos.size} videos"
            
            // Load thumbnail for the first video in the folder
            if (folder.videos.isNotEmpty()) {
                loadThumbnail(folder.videos[0])
            }

            binding.root.setOnClickListener {
                onFolderClick(folder)
            }
        }

        private fun loadThumbnail(video: VideoFile) {
            scope.launch {
                try {
                    val thumbnail = withContext(Dispatchers.IO) {
                        ThumbnailUtils.createVideoThumbnail(
                            video.uri.path ?: "",
                            MediaStore.Images.Thumbnails.MINI_KIND
                        )
                    }
                    
                    thumbnail?.let {
                        binding.ivThumbnail.setImageBitmap(it)
                    }
                } catch (e: Exception) {
                    // If thumbnail loading fails, keep the default background
                }
            }
        }
    }
}