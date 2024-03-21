package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Video

interface VideoDao {
    suspend fun allVideos(): List<Video>
    suspend fun video(id: Int): Video?
    suspend fun addNewVideo(creator: Account, videoPath: String, title: String, description: String): Video?
    suspend fun editVideo(id: Int, title: String, description: String): Boolean
    suspend fun deleteVideo(id: Int): Boolean
}