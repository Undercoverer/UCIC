package gay.extremist.models

import gay.extremist.dao.Labels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Videos : IntIdTable() {
    val creatorID : Column<EntityID<Int>>  = reference("creatorID", Accounts)
    val title : Column<String> = varchar("title", 255)
    val videoPath : Column<String> = varchar("videoPath", 255)
    val description : Column<String> = text("description")
    val viewCount : Column<Int> = integer("viewCount")
    val uploadDate : Column<java.time.LocalDateTime> = datetime("uploadDate").defaultExpression(CurrentDateTime)
}

class Video(id: EntityID<Int>) : Entity<Int>(id){
    companion object : EntityClass<Int, Video> (Videos)

    var creator by Account referencedOn Videos.creatorID
    var videoPath by Videos.videoPath
    var title by Videos.title
    var description by Videos.description
    var viewCount by Videos.viewCount
    var uploadDate by Videos.uploadDate

    var tags by Tag via Labels
}
