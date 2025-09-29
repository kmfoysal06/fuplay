package com.fuplay.videoplayer

import android.net.Uri

data class VideoFile(
    val id: Long,
    val displayName: String,
    val uri: Uri,
    val size: Long,
    val duration: Long,
    val folderName: String
)

data class VideoFolder(
    val name: String,
    val videos: MutableList<VideoFile> = mutableListOf()
)