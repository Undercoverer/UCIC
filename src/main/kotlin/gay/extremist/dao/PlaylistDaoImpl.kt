package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.Account
import gay.extremist.models.Playlist
import gay.extremist.models.Video
import org.jetbrains.exposed.sql.SizedCollection

class PlaylistDaoImpl : PlaylistDao {
    override suspend fun createPlaylist(owner: Account, name: String): Playlist = dbQuery {
        Playlist.new {
            this.owner = owner
            this.name = name
        }
    }

    override suspend fun readPlaylist(id: Int): Playlist? = dbQuery {
        Playlist.findById(id)
    }

    override suspend fun readPlaylistAll(): List<Playlist> = dbQuery {
        Playlist.all().toList()
    }

    override suspend fun updatePlaylist(id: Int, name: String): Boolean = dbQuery {
        val playlist = Playlist.findById(id)
        playlist?.name = name

        playlist!= null
    }

    override suspend fun deletePlaylist(id: Int): Boolean = dbQuery {
        val playlist = Playlist.findById(id)

        try {
            playlist!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun addVideoToPlaylist(id: Int, video: Video): Boolean = dbQuery {
        val playlist = Playlist.findById(id)
        val videoList = playlist?.videos

        try {
            playlist?.videos = SizedCollection(videoList!! + video)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun removeVideoFromPlaylist(id: Int, video: Video): Boolean = dbQuery {
        val playlist = Playlist.findById(id) ?: return@dbQuery false
        val videoList = playlist.videos

        try {
            playlist.videos = SizedCollection(videoList.filter{ it != video })
            return@dbQuery true
        } catch (e: NullPointerException) {
            return@dbQuery false
        }
    }
}

val playlistDao: PlaylistDao = PlaylistDaoImpl()