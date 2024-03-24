package gay.extremist.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Tags: IntIdTable()  {
    val tag: Column<String> = varchar("tag", 255)
        .uniqueIndex()
    val category: Column<String?> = varchar("category", 255)
        .nullable()
    val isPreset: Column<Boolean> = bool("preset")
}

class Tag(id: EntityID<Int>): Entity<Int>(id) {
    companion object: EntityClass<Int, Tag>(Tags)

    var tag by Tags.tag
    var category by Tags.category
    var isPreset by Tags.isPreset

    var videos by Video via TagLabelsVideo
}
