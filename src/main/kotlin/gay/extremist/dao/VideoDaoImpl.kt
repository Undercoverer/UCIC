package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File


class VideoDaoImpl : VideoDao {
    override suspend fun createVideo(
        creator: Account, videoPath: String, title: String, description: String, tags: SizedCollection<Tag>
    ): Video = dbQuery {
        Video.new {
            this.creator = creator
            this.videoPath = videoPath
            this.title = title
            this.description = description
            this.tags = tags
            this.viewCount = 0
        }
    }

    override suspend fun readVideo(id: Int): Video? = dbQuery {
        return@dbQuery Video.findById(id)
    }

    override suspend fun readVideoAll(): List<Video> = dbQuery {
        Video.all().toList()
    }

    override suspend fun updateVideo(id: Int, title: String, description: String): Boolean = dbQuery {
        val video = Video.findById(id)
        video?.title = title
        video?.description = description

        video != null
    }

    override suspend fun updateVideoPath(id: Int, videoPath: String): Boolean = dbQuery {
        val video = Video.findById(id)
        video?.videoPath = videoPath

        video != null
    }

    override suspend fun deleteVideo(id: Int): Boolean = dbQuery {
        val video = Video.findById(id)

        // Delete folders in path
        File(video!!.videoPath).parentFile.deleteRecursively()

        // Delete video from database
        try {
            video.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun addTagsToVideo(id: Int, tags: List<Tag>): Boolean = dbQuery {
        val video = Video.findById(id)
        val tagList = video?.tags

        try {
            video?.tags = SizedCollection(tagList!! + tags)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun removeTagsFromVideo(id: Int, tags: List<Tag>): Boolean = dbQuery {
        val video = Video.findById(id)
        val tagList = video?.tags

        try {
            video?.tags = SizedCollection(tagList!! - tags.toSet())
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun incrementViewCount(id: Int): Boolean = dbQuery{
        return@dbQuery when (val video = Video.findById(id)) {
            null -> false
            else -> {
                video.viewCount += 1
                true
            }
        }
    }

    override suspend fun searchAndSortVideosByTitleFuzzy(title: String): List<Video> = dbQuery {
        val conn = TransactionManager.current().connection
        val query = "SELECT * FROM videos ORDER BY similarity(title, ?) DESC"
        val statement = conn.prepareStatement(query, false).apply { set(1, title) }
        val resultSet = statement.executeQuery()
        val videos = mutableListOf<Video>()
        while (resultSet.next()) Video.findById(resultSet.getInt("id")).let { videos.add(it!!) }
        return@dbQuery videos
    }

    override suspend fun searchVideosByTags(tags: List<String>): List<Video> {
        TODO("Not yet implemented")
    }

    override suspend fun searchVideosByTitleFuzzyAndTags(title: String, tags: List<String>): List<Video> {
        TODO("Not yet implemented")
    }

    override suspend fun getCommentsOnVideo(id: Int): List<Comment> = dbQuery {
        readVideo(id)?.comments?.toList() ?: emptyList()
    }
}

val videoDao: VideoDao = VideoDaoImpl()