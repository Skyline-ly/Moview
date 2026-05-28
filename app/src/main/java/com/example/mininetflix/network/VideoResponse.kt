package com.example.mininetflix.network

data class VideoResponse(
    val id: Int,
    val results: List<Video>
)
