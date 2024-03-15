package gay.extremist.dao

import gay.extremist.models.Account

interface AccountDao {

    suspend fun allAccounts(): List<Account>
    suspend fun account(accountID: Int): Account?
    suspend fun addNewAccount(username: String, email: String, password: String, token: String): Account?
    suspend fun editAccount(accountID: Int, username: String, email: String, password: String): Boolean
    suspend fun deleteAccount(accountID: Int): Boolean
    suspend fun getToken(email: String)
}