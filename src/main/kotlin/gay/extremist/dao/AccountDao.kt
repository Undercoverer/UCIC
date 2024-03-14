package gay.extremist.dao

import gay.extremist.models.Account

interface AccountDao {

    suspend fun allAccounts(): List<Account>
    suspend fun addNewAccount(username : String, email : String, password : String, token : String)
    suspend fun deleteAccount(accountID : Int)
    suspend fun getToken(email : String)
}