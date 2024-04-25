package gay.extremist.dao

import gay.extremist.models.*
import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.util.similarity
import org.jetbrains.exposed.sql.*
import java.io.File
import java.time.LocalDateTime


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

    override suspend fun readGeneralVideos(): List<Video> = dbQuery {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        Video.find {
            Videos.uploadDate greaterEq sevenDaysAgo
        }.orderBy(Videos.viewCount to SortOrder.DESC).toList()
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
        val video = Video.findById(id) ?: return@dbQuery false
        val tagList = video.tags

        try {
            video.tags = SizedCollection(tagList.filter { it.id !in tags.map{ tag -> tag.id} } )
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun incrementViewCount(id: Int): Boolean = dbQuery {
        return@dbQuery when (val video = Video.findById(id)) {
            null -> false
            else -> {
                video.viewCount += 1
                true
            }
        }
    }

    override suspend fun searchAndSortVideosByTitleFuzzy(title: String): List<Video> = dbQuery {
        val titleSimilarity = Videos.title similarity title
        Video.all().orderBy(titleSimilarity to SortOrder.DESC).toList()
    }

    override suspend fun searchVideosByTitleFuzzyAndTags(title: String, tags: List<String>): List<Video> = dbQuery {
        val titleSimilarity = Videos.title similarity title
        (Videos innerJoin TagLabelsVideo innerJoin Tags)
            .select { Tags.tag.inList(tags) }
            .orderBy(titleSimilarity to SortOrder.DESC)
            .map { Video.findById(it[Videos.id])!! }

    }

    override suspend fun getCommentsOnVideo(id: Int): List<Comment> = dbQuery {
        readVideo(id)?.comments?.toList() ?: emptyList()
    }
}

val videoDao: VideoDao = VideoDaoImpl()