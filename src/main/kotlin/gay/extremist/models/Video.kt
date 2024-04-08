package gay.extremist.models

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.data_classes.TagResponse
import gay.extremist.data_classes.VideoCreator
import gay.extremist.data_classes.VideoListObject
import gay.extremist.data_classes.VideoResponse
import gay.extremist.models.Video.Companion.referrersOn
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Videos: IntIdTable() {
    val creatorID: Column<EntityID<Int>>  = reference("creatorID", Accounts, onDelete = ReferenceOption.CASCADE)
    val title: Column<String> = varchar("title", 255)
    val videoPath: Column<String> = varchar("videoPath", 255)
    val description: Column<String> = text("description")
    val viewCount: Column<Int> = integer("viewCount")
    val uploadDate: Column<java.time.LocalDateTime> = datetime("uploadDate").defaultExpression(CurrentDateTime)
}

class Video(id: EntityID<Int>): Entity<Int>(id) {
    companion object: EntityClass<Int, Video> (Videos)

    var creator by Account referencedOn Videos.creatorID
    var videoPath by Videos.videoPath
    var title by Videos.title
    var description by Videos.description
    var viewCount by Videos.viewCount
    var uploadDate by Videos.uploadDate

    var tags by Tag via TagLabelsVideo
    val comments by Comment referrersOn Comments.videoID
    val ratings by Rating referrersOn Ratings.videoID

    suspend fun toVideoResponse(): VideoResponse{
        return VideoResponse(
            this.id.value,
            this.title,
            this.description,
            this.videoPath,
            dbQuery { this.tags.map { TagResponse(it.id.value, it.tag, it.category) } },
            dbQuery { VideoCreator(this.creator.id.value, this.creator.username) },
            this.viewCount,
            this.uploadDate.toString(),
        )
    }

    suspend fun toVideoListObject(): VideoListObject{
        return VideoListObject(
            this.id.value,
            this.title,
            this.videoPath
        )
    }
}
