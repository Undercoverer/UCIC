package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*


class VideoDaoImpl : VideoDao {
    override suspend fun createVideo(creator: Account, videoPath: String, title: String, description: String): Video = dbQuery {
        Video.new {
            this.creator = creator
            this.videoPath = videoPath
            this.title = title
            this.description = description
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

    override suspend fun deleteVideo(id: Int): Boolean = dbQuery {
        val video = Video.findById(id)

        try {
            video!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }
}