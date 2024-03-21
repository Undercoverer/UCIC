package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Video

interface VideoDao {
    suspend fun createVideo(creator: Account, videoPath: String, title: String, description: String): Video?
    suspend fun readVideo(id: Int): Video?
    suspend fun readVideoAll(): List<Video>
    suspend fun updateVideo(id: Int, title: String, description: String): Boolean
    suspend fun deleteVideo(id: Int): Boolean
}