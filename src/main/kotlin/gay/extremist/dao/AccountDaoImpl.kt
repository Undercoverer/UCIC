package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Accounts
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert

class AccountDaoImpl : AccountDao{

    override suspend fun allAccounts(): List<Account> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewAccount(username: String, email: String, password: String, token: String) {
        val insertStatement = Accounts.insert {
            it[Accounts.username] = username
            it[Accounts.email] = email
            it[Accounts.password] = password
            it[Accounts.token] = token
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAccount)
    }

    private fun resultRowToAccount(row : ResultRow) = Account(
        accountID = row[Accounts.accountID],
        username =  row[Accounts.username],
        email = row[Accounts.email],
        password = row[Accounts.password],
        token = row[Accounts.token]

    )

}