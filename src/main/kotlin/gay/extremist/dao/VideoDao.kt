package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Video

interface VideoDao {
    suspend fun allVideos(): List<Video>
    suspend fun video(videoID: Int): Video?
    suspend fun addNewVideo(creatorID: Int, videoPath: String, title: String, description: String): Video?
    suspend fun editVideo(videoID: Int, title: String, description: String): Boolean
    suspend fun deleteVideo(videoID: Int): Boolean
    suspend fun getCreator(videoID: Int): Account
}