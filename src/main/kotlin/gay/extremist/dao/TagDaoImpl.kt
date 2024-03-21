package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.Tag

class TagDaoImpl : TagDao {
    override suspend fun allTags(): List<Tag> = dbQuery {
        Tag.all().toList()
    }

    override suspend fun tag(id: Int): Tag? = dbQuery {
        Tag.findById(id)
    }

    override suspend fun addNewTag(tag: String, isPreset: Boolean): Tag = dbQuery {
        Tag.new {
            this.tag = tag
            this.isPreset = isPreset
        }
    }

    override suspend fun editTag(id: Int, isPreset: Boolean): Boolean = dbQuery {
        val tag = Tag.findById(id)
        tag?.isPreset = isPreset

        tag != null
    }

    override suspend fun deleteTag(id: Int): Boolean = dbQuery {
        val tag = Tag.findById(id)

        try {
            tag!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }

}