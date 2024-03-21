package gay.extremist.dao

import gay.extremist.models.Account
import org.jetbrains.exposed.sql.SizedIterable

interface AccountDao {

    suspend fun allAccounts(): List<Account>
    suspend fun account(id: Int): Account?
    suspend fun addNewAccount(username: String, email: String, password: String): Account?
    suspend fun editAccount(id: Int, username: String, email: String, password: String): Boolean
    suspend fun deleteAccount(id: Int): Boolean
    suspend fun getToken(email: String, password: String): String
    suspend fun getIdByUsername(username: String): Int
}