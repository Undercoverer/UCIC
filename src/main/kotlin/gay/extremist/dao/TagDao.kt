package gay.extremist.dao

import gay.extremist.models.Tag

interface TagDao {
    suspend fun createTag(tag: String, isPreset: Boolean = false, category: String? = null): Tag
    suspend fun readTag(id: Int): Tag?
    suspend fun readTagAll(): List<Tag>
    suspend fun updateTag(id: Int, isPreset: Boolean, category: String?): Boolean
    suspend fun deleteTag(id: Int): Boolean
}