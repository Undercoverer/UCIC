package gay.extremist.dao

import gay.extremist.models.Tag

interface TagDao {
    suspend fun allTags(): List<Tag>
    suspend fun tag(id: Int): Tag?
    suspend fun addNewTag(tag: String, isPreset: Boolean = false): Tag?
    suspend fun editTag(id: Int, isPreset: Boolean): Boolean
    suspend fun deleteTag(id: Int): Boolean
}