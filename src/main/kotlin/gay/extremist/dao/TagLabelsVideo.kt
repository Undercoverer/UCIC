package gay.extremist.dao

import gay.extremist.models.Tags
import gay.extremist.models.Videos
import org.jetbrains.exposed.sql.Table

object TagLabelsVideo : Table() {
    private val tag = reference("tagID", Tags)
    private val video = reference("videoID", Videos)
    override val primaryKey = PrimaryKey(
        tag, video, name="PK_TagLabelsVideo_tag_vid"
    )
}