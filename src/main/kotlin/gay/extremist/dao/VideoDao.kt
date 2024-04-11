package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Comment
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
    suspend fun addTagsToVideo(id: Int, tags: List<Tag>): Boolean
    suspend fun removeTagsFromVideo(id: Int, tags: List<Tag>): Boolean
    suspend fun incrementViewCount(id: Int): Boolean
    suspend fun searchAndSortVideosByTitleFuzzy(title: String): List<Video>
    suspend fun getCommentsOnVideo(id: Int): List<Comment>
    suspend fun searchVideosByTags(tags: List<String>): List<Video>
    suspend fun searchVideosByTitleFuzzyAndTags(title: String, tags: List<String>): List<Video>
}

fun List<Video>.sortBy(sortMethod: SortMethod, reverse: Boolean): List<Video> = when (sortMethod){
    SortMethod.DATE ->
        this.sortedBy { it.uploadDate }.let { if (reverse) it.reversed() else it }
    SortMethod.VIEWS ->
        this.sortedBy { it.viewCount }.let { if (reverse) it.reversed() else it }
    SortMethod.RATING ->
        this.sortedBy { it.getRating() }.let { if (reverse) it.reversed() else it }
    SortMethod.ALPHABETIC_ORDER ->
        this.sortedBy { it.title }.let { if (reverse) it.reversed() else it }
}

enum class SortMethod {
    DATE,
    VIEWS,
    RATING,
    ALPHABETIC_ORDER
}

