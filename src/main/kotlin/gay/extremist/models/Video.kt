package gay.extremist.models

import gay.extremist.util.DatabaseFactory.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

object Videos : IntIdTable() {
    val creatorID: Column<EntityID<Int>> = reference("creatorID", Accounts, onDelete = ReferenceOption.CASCADE)
    val title: Column<String> = varchar("title", 255)
    val videoPath: Column<String> = varchar("videoPath", 255)
    val description: Column<String> = text("description")
    val viewCount: Column<Int> = integer("viewCount")
    val uploadDate: Column<java.time.LocalDateTime> = datetime("uploadDate").defaultExpression(CurrentDateTime)
}

class Video(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Video>(Videos)

    var creator by Account referencedOn Videos.creatorID
    var videoPath by Videos.videoPath
    var title by Videos.title
    var description by Videos.description
    var viewCount by Videos.viewCount
    var uploadDate by Videos.uploadDate

    var tags by Tag via TagLabelsVideo
    val comments by Comment referrersOn Comments.videoID
    val ratings by Rating referrersOn Ratings.videoID


    suspend fun toResponse(): VideoResponse {
        return VideoResponse(
            id = id.value,
            title = title,
            description = description,
            videoPath = videoPath,
            tags = dbQuery { tags.map { it.toResponse() } },
            creator = dbQuery { creator.toDisplayResponse() },
            viewCount = viewCount,
            uploadDate = uploadDate.toString(),
            rating = getRating()
        )
    }

    fun toDisplayResponse(): VideoDisplayResponse {
        return VideoDisplayResponse(
            id = id.value,
            title = title,
            videoPath = videoPath,
            creator = transaction { creator.toDisplayResponse()},
            uploadDate = uploadDate.toString(),
            viewCount = viewCount
        )
    }

    fun getRating(): Double = transaction {
        ratings.map { it.rating }.average().let { if (it.isNaN()) 0.0 else it }
    }

    override fun equals(other: Any?): Boolean {
        return when(other){
            is Video -> {
                this.id == other.id
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}

@Serializable
data class VideoResponse(
    val id: Int,
    val title: String,
    val description: String,
    val videoPath: String,
    val tags: List<TagResponse>,
    val creator: AccountDisplayResponse,
    val viewCount: Int,
    val uploadDate: String,
    val rating: Double
)

@Serializable
data class VideoDisplayResponse(
    val id: Int,
    val title: String,
    val videoPath: String,
    val creator: AccountDisplayResponse,
    val viewCount: Int,
    val uploadDate: String
)
