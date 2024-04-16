package gay.extremist.dao

import gay.extremist.models.*

interface AccountDao {

    suspend fun createAccount(username: String, email: String, password: String): Account?
    suspend fun readAccount(id: Int): Account?
    suspend fun readAccountAll(): List<Account>
    suspend fun updateAccount(id: Int, username: String, email: String, password: String): Account?
    suspend fun deleteAccount(id: Int): Boolean
    suspend fun getToken(email: String, password: String): String?
    suspend fun getIdByEmail(email: String): Int?
    suspend fun getIdByUsername(username: String): Int?
    suspend fun addFollowedAccount(id: Int, account: Account): Boolean
    suspend fun removeFollowedAccount(id: Int, account: Account): Boolean
    suspend fun addFollowedTag(id: Int, tag: Tag): Boolean
    suspend fun removeFollowedTag(id: Int, tag: Tag): Boolean
    suspend fun getVideosFromAccount(accountId: Int): List<Video>
    suspend fun getPlaylistsFromAccount(accountId: Int): List<Playlist>
    suspend fun getFollowedTags(id: Int): List<Tag>
    suspend fun getFollowedAccounts(id: Int): List<Account>
    suspend fun searchAccounts(username: String): List<Account>
    suspend fun searchAccountsFuzzy(username: String): List<Account>
    suspend fun getRecommendedVideosByFollowedTags(accountId: Int): List<Video>
    suspend fun getRecommendedVideosByFollowedAccounts(accountId: Int): List<Video>
}