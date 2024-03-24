package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TagLabelsVideo: Table() {
    private val tag: Column<EntityID<Int>> = reference("tagID", Tags)
    private val video: Column<EntityID<Int>> = reference("videoID", Videos)
    override val primaryKey = PrimaryKey(
        tag, video, name="PK_TagLabelsVideo_tag_vid"
    )
}