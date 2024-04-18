package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Tags : IntIdTable() {
    val tag: Column<String> = varchar("tag", 255).uniqueIndex()
    val category: Column<String?> = varchar("category", 255).nullable()
    val isPreset: Column<Boolean> = bool("preset")
}

class Tag(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Tag>(Tags)

    var tag by Tags.tag

    var category by Tags.category
    var isPreset by Tags.isPreset
    var videos by Video via TagLabelsVideo

    fun toResponse() = TagResponse(id.value, tag, category, isPreset)
    fun toDisplayResponse() = TagDisplayResponse(id.value, tag)

    override fun equals(other: Any?): Boolean {
        return when(other){
            is Tag -> {
                this.id == other.id
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}

fun List<Tag>.toCategorizedResponse(): TagCategorizedResponse {
    return TagCategorizedResponse (
        this.filter {
            it.category != null
        }.groupBy {
            it.category
        }.map { (category, tags) ->
            Category(
                category!!,
                tags.map{ it.toDisplayResponse() }
            )
        }
    )


}

@Serializable
data class TagDisplayResponse(val id: Int, val tag: String)

@Serializable
data class TagResponse(val id: Int, val tag: String, val category: String?, val isPreset: Boolean)

@Serializable
data class Category(val category: String, val tags:List<TagDisplayResponse>)

@Serializable
data class TagCategorizedResponse(val categories: List<Category>)