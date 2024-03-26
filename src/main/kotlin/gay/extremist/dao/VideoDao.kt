package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Tag
import gay.extremist.models.Video
import org.jetbrains.exposed.sql.SizedCollection

interface VideoDao {
    suspend fun createVideo(
        creator: Account,
        videoPath: String,
        title: String,
        description: String,
        tags: SizedCollection<Tag>
    ): Video
    suspend fun readVideo(id: Int): Video?
    suspend fun readVideoAll(): List<Video>
    suspend fun updateVideo(id: Int, title: String, description: String): Boolean
    suspend fun updateVideoPath(id: Int, videoPath: String): Boolean
    suspend fun deleteVideo(id: Int): Boolean
}