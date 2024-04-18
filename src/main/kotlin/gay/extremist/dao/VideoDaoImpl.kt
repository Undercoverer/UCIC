package gay.extremist.dao

import gay.extremist.models.*
import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.util.similarity
import org.jetbrains.exposed.sql.*
import java.io.File
import java.time.Clock
import java.time.Duration


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
        Video.find {
            Videos.uploadDate greater java.time.LocalDateTime.from(
                Clock.systemUTC().instant().minus(Duration.ofDays(7))
            )
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
//        val conn = TransactionManager.current().connection
//        val query = "SELECT * FROM videos WHERE similarity(title, ?) > 0.5 ORDER BY similarity(title, ?) DESC"
//        val statement = conn.prepareStatement(query, false).apply { set(1, title) }
//        val resultSet = statement.executeQuery()
//        val videos = mutableListOf<Video>()
//        while (resultSet.next()) Video.findById(resultSet.getInt("id")).let { videos.add(it!!) }
//        return@dbQuery videos
        // TODO MAKE SURE THIS WORKS RIGHT
        val titleSimilarity = Videos.title similarity title
        Video.find { titleSimilarity greater 0.5 }.orderBy(titleSimilarity to SortOrder.DESC).toList()
    }

    override suspend fun searchVideosByTitleFuzzyAndTags(title: String, tags: List<String>): List<Video> = dbQuery {
//        val conn = TransactionManager.current().connection
//        val query = "SELECT * FROM videos v INNER JOIN tag_labels_video tlv ON v.id = tlv.video_id INNER JOIN tags t ON tlv.tag_id = t.id WHERE similarity(v.title, ?) > 0.5 AND t.tag IN (?) ORDER BY similarity(v.title, ?) DESC"
//        val statement = conn.prepareStatement(query, false).apply {
//            set(1, title)
//            set(2, tags.joinToString(","))
//            set(3, title)
//        }
//        val resultSet = statement.executeQuery()
//        val videos = mutableListOf<Video>()
//        while (resultSet.next()) Video.findById(resultSet.getInt("id")).let { videos.add(it!!) }
//        return@dbQuery videos
        // TODO MAKE SURE THIS WORKS RIGHT (NO IDEA IF THIS ONE DOES AT ALL)
        val titleSimilarity = Videos.title similarity title
        (Videos innerJoin TagLabelsVideo innerJoin Tags).select { titleSimilarity greater 0.5 and Tags.tag.inList(tags) }
            .orderBy(titleSimilarity to SortOrder.DESC).map { Video.findById(it[Videos.id])!! }

    }


    override suspend fun getCommentsOnVideo(id: Int): List<Comment> = dbQuery {
        readVideo(id)?.comments?.toList() ?: emptyList()
    }
}

val videoDao: VideoDao = VideoDaoImpl()