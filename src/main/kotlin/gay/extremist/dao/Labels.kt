package gay.extremist.dao

import gay.extremist.models.Tags
import gay.extremist.models.Videos
import org.jetbrains.exposed.sql.Table

object Labels : Table() {
    private val tag = reference("tagID", Tags)
    private val video = reference("videoID", Videos)
    override val primaryKey = PrimaryKey(
        tag, video, name="PK_Labels_tag_vid"
    )
}