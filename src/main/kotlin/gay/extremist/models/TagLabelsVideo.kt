package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TagLabelsVideo: Table() {
    private val tag: Column<EntityID<Int>> = reference("tagID", Tags, onDelete = ReferenceOption.CASCADE)
    private val video: Column<EntityID<Int>> = reference("videoID", Videos, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(
        tag, video
    )
}