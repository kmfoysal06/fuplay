package com.fuplay.videoplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fuplay.videoplayer.databinding.ItemVideoFolderBinding

class VideoFolderAdapter(
    private val folders: List<VideoFolder>,
    private val onVideoClick: (VideoFile) -> Unit
) : RecyclerView.Adapter<VideoFolderAdapter.FolderViewHolder>() {

    private val expandedFolders = mutableSetOf<String>()

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

        fun bind(folder: VideoFolder) {
            binding.tvFolderName.text = "${folder.name} (${folder.videos.size})"
            
            val isExpanded = expandedFolders.contains(folder.name)
            
            if (isExpanded) {
                binding.rvVideos.visibility = android.view.View.VISIBLE
                setupVideoRecyclerView(folder.videos)
            } else {
                binding.rvVideos.visibility = android.view.View.GONE
            }

            binding.tvFolderName.setOnClickListener {
                if (isExpanded) {
                    expandedFolders.remove(folder.name)
                } else {
                    expandedFolders.add(folder.name)
                }
                notifyItemChanged(adapterPosition)
            }
        }

        private fun setupVideoRecyclerView(videos: List<VideoFile>) {
            binding.rvVideos.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvVideos.adapter = VideoAdapter(videos, onVideoClick)
        }
    }
}