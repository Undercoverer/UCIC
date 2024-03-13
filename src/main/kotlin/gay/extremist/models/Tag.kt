package gay.extremist.models

import gay.extremist.models.Accounts.autoIncrement
import gay.extremist.models.Accounts.uniqueIndex
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

@Serializable
data class Tag(
    val tagID : Int,
    val tag : String,
    val isPreset : Boolean
)

object Tags : IntIdTable()  {
    val tagID : Column<Int> = integer("tagID")
        .uniqueIndex()
        .autoIncrement()
    val tag : Column<String> = varchar("tag", 255)
        .uniqueIndex()
    val isPreset : Column<Boolean> = bool("preset")
}
