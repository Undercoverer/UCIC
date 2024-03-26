package gay.extremist.dao

import gay.extremist.models.Account

interface AccountDao {

    suspend fun createAccount(username: String, email: String, password: String): Account
    suspend fun readAccount(id: Int): Account?
    suspend fun readAccountAll(): List<Account>
    suspend fun updateAccount(id: Int, username: String, email: String, password: String): Boolean
    suspend fun deleteAccount(id: Int): Boolean
    suspend fun getToken(email: String, password: String): String?
    suspend fun getIdByUsername(username: String): Int?
}