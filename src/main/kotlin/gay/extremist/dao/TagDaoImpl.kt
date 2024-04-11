package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking

class TagDaoImpl : TagDao {
    override suspend fun readTagAll(): List<Tag> = dbQuery {
        Tag.all().toList()
    }

    override suspend fun readTag(id: Int): Tag? = dbQuery {
        Tag.findById(id)
    }

    override suspend fun createTag(tag: String, isPreset: Boolean, category: String?): Tag = dbQuery {
        Tag.new {
            this.tag = tag
            this.category = category
            this.isPreset = isPreset
        }
    }

    override suspend fun updateTag(id: Int, isPreset: Boolean, category: String?): Boolean = dbQuery {
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

    override suspend fun findTagByName(name: String): Tag? = dbQuery {
        Tag.find { Tags.tag eq name }
            .firstOrNull()
    }

    override suspend fun findTagsBySubstring(substring: String): List<Tag> = dbQuery {
        Tag.find {
            Tags.tag like "%$substring%"
        }.toList()
    }

    override suspend fun getPresetTags(): List<Tag> = dbQuery {
        Tag.find {
            Tags.isPreset eq true
        }.toList()
    }

    override suspend fun getVideosForTag(tagId: Int): List<Video> = dbQuery {
        readTag(tagId)?.videos?.toList() ?: emptyList()
    }

}

val tagDao: TagDao = TagDaoImpl().apply {
    runBlocking {
        if (readTagAll().isEmpty()) {
            // General Tags
            createTag("food", true, "general")
            createTag("art", true, "general")
            createTag("animals", true, "general")
            createTag("nature", true, "general")
            createTag("politics", true, "general")
            createTag("business", true, "general")
            createTag("news", true, "general")
            createTag("home", true, "general")
            createTag("agriculture", true, "general")
            createTag("tech", true, "general")
            createTag("fashion", true, "general")
            createTag("exercise", true, "general")
            createTag("industry", true, "general")
            createTag("environment", true, "general")

            // Music Tags
            createTag("music", true, "music")
            createTag("rock", true, "music")
            createTag("pop", true, "music")
            createTag("jazz", true, "music")
            createTag("indie", true, "music")
            createTag("classical", true, "music")
            createTag("rap", true, "music")
            createTag("instrumental", true, "music")
            createTag("lyrics", true, "music")
            createTag("music video", true, "music")

            // Entertainment Tags
            createTag("entertainment", true, "entertainment")
            createTag("gaming", true, "entertainment")
            createTag("movies", true, "entertainment")
            createTag("tv shows", true, "entertainment")
            createTag("animation", true, "entertainment")
            createTag("horror", true, "entertainment")
            createTag("romance", true, "entertainment")
            createTag("comedy", true, "entertainment")
            createTag("fantasy", true, "entertainment")
            createTag("literature", true, "entertainment")
            createTag("sci-fi", true, "entertainment")
            createTag("theater", true, "entertainment")
            createTag("crime", true, "entertainment")

            // Education Tags
            createTag("educational", true, "educational")
            createTag("science", true, "educational")
            createTag("school", true, "educational")
            createTag("mathematics", true, "educational")
            createTag("culture", true, "educational")
            createTag("language", true, "educational")
            createTag("history", true, "educational")
            createTag("chemistry", true, "educational")
            createTag("biology", true, "educational")
            createTag("physics", true, "educational")
            createTag("health", true, "educational")
            createTag("space", true, "educational")

            // Video-Type Tags
            createTag("short", true, "video-type")
            createTag("podcast", true, "video-type")
            createTag("video essay", true, "video-type")
            createTag("vlog", true, "video-type")
            createTag("how-to", true, "video-type")
            createTag("review", true, "video-type")
        }
    }
}