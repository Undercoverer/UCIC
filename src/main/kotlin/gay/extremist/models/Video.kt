package gay.extremist.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime


@Serializable
data class Video(
    val videoID : Int,
    val creatorID : Int,
    val videoPath : String,
    val title : String,
    val description : String,
    val viewCount : Int,
    val uploadDate : LocalDateTime
)

object Videos : IntIdTable() {
    val videoID : Column<Int> = integer("videoID")
        .uniqueIndex()
        .autoIncrement()
    val creatorID : Column<EntityID<Int>>  = reference("creatorID", Accounts)
    val title : Column<String> = varchar("title", 255)
    val videoPath : Column<String> = varchar("videoPath", 255)
    val description : Column<String> = text("description")
    val viewCount : Column<Int> = integer("viewCount")
    val uploadDate : Column<java.time.LocalDateTime> = datetime("uploadDate").defaultExpression(CurrentDateTime)


}
