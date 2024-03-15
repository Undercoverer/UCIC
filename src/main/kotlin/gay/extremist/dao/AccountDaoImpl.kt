package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class AccountDaoImpl : AccountDao{

    private fun resultRowToAccount(row : ResultRow) = Account(
        accountID = row[Accounts.accountID],
        username =  row[Accounts.username],
        email = row[Accounts.email],
        password = row[Accounts.password],
        token = row[Accounts.token]

    )

    override suspend fun allAccounts(): List<Account> = dbQuery{
        Accounts
            .selectAll()
            .map(::resultRowToAccount)
    }

    override suspend fun account(accountID: Int): Account? = dbQuery{
        Accounts
            .select {Accounts.accountID eq accountID}
            .map(::resultRowToAccount)
            .singleOrNull()
    }

    override suspend fun addNewAccount(username: String, email: String, password: String, token: String): Account? = dbQuery {
        val insertStatement = Accounts.insert {
            it[Accounts.username] = username
            it[Accounts.email] = email
            it[Accounts.password] = password
            it[Accounts.token] = token
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAccount)
    }

    override suspend fun editAccount(accountID: Int, username: String, email: String, password: String): Boolean = dbQuery {
        Accounts.update({ Accounts.accountID eq accountID }) {
            it[Accounts.username] = username
            it[Accounts.email] = email
            it[Accounts.password] = password
        } > 0
    }

    override suspend fun deleteAccount(accountID: Int): Boolean = dbQuery {
        Accounts.deleteWhere { Accounts.accountID eq accountID } > 0
    }

    override suspend fun getToken(email: String) {
        TODO("Not yet implemented")
    }

}

val dao: AccountDao = AccountDaoImpl().apply {
    runBlocking {
        if(allAccounts().isEmpty()) {
            addNewAccount("admin","admin@fakemail.com","password","token" )
        }
    }
}