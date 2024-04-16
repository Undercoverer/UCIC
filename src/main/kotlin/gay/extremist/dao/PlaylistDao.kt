package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Playlist
import gay.extremist.models.Video

interface PlaylistDao {
    suspend fun createPlaylist(owner: Account, name: String): Playlist
    suspend fun readPlaylist(id: Int): Playlist?
    suspend fun readPlaylistAll(): List<Playlist>
    suspend fun updatePlaylist(id: Int, name: String): Boolean
    suspend fun deletePlaylist(id: Int): Boolean
    suspend fun addVideoToPlaylist(id: Int, video: Video): Boolean
    suspend fun removeVideoFromPlaylist(id: Int, video: Video): Boolean
}