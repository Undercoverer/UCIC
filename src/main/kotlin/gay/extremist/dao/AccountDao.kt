package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Playlist
import gay.extremist.models.Tag
import gay.extremist.models.Video

interface AccountDao {

    suspend fun createAccount(username: String, email: String, password: String): Account?
    suspend fun readAccount(id: Int): Account?
    suspend fun readAccountAll(): List<Account>
    suspend fun updateAccount(id: Int, username: String, email: String, password: String): Boolean
    suspend fun deleteAccount(id: Int): Boolean
    suspend fun getToken(email: String, password: String): String?
    suspend fun getIdByUsername(username: String): Int?
    suspend fun addFollowedAccount(id: Int, account: Account): Boolean
    suspend fun removeFollowedAccount(id: Int, account: Account): Boolean
    suspend fun addFollowedTag(id: Int, tag: Tag): Boolean
    suspend fun removeFollowedTag(id: Int, tag: Tag): Boolean
    suspend fun getVideosFromAccount(accountId: Int): List<Video>
    suspend fun getPlaylistsFromAccount(accountId: Int): List<Playlist>
}