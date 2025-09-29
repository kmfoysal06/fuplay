package com.fuplay.videoplayer

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoFile(
    val id: Long,
    val displayName: String,
    val uri: Uri,
    val size: Long,
    val duration: Long,
    val folderName: String,
    val path: String?
) : Parcelable

data class VideoFolder(
    val name: String,
    val videos: MutableList<VideoFile> = mutableListOf()
)