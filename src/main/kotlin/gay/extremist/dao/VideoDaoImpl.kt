package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import org.jetbrains.exposed.sql.SizedCollection


class VideoDaoImpl : VideoDao {
    override suspend fun createVideo(
        creator: Account,
        videoPath: String,
        title: String,
        description: String,
        tags: SizedCollection<Tag>
    ): Video = dbQuery {
        Video.new {
            this.creator = creator
            this.videoPath = videoPath
            this.title = title
            this.description = description
            this.tags = tags
        }
    }

    override suspend fun readVideo(id: Int): Video? = dbQuery {
        Video.findById(id)
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

    override suspend fun updateVideoPath(id: Int, videoPath: String): Boolean = dbQuery{
        val video = Video.findById(id)
        video?.videoPath = videoPath

        video != null
    }

    override suspend fun deleteVideo(id: Int): Boolean = dbQuery {
        val video = Video.findById(id)

        try {
            video!!.delete()
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
}

val videoDao: VideoDao = VideoDaoImpl()