package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column


@Serializable
data class Video(
    val videoID : Int,
    val creatorID : Int,
    val videoPath : String,
    val title : String,
    val description : String,
    val viewCount : Int,
    //val uploadDate : String
)

object Videos : IntIdTable() {
    val videoID : Column<Int> = integer("videoID")
        .autoIncrement()
    val creatorID : Column<EntityID<Int>>  = reference("creatorID", Accounts)
    val title : Column<String>  = varchar("title", 255)

}
