package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class AccountDaoImpl : AccountDao {

    override suspend fun allAccounts(): List<Account> = dbQuery {
        Account.all().toList()
    }

    override suspend fun account(id: Int): Account? = dbQuery {
        Account.findById(id)
    }

    override suspend fun addNewAccount(username: String, email: String, password: String): Account = dbQuery {
        Account.new {
            this.username = username
            this.email = email
            this.password = password
            this.token = UUID.nameUUIDFromBytes((username + password).toByteArray()).toString()
        }
    }

    override suspend fun editAccount(id: Int, username: String, email: String, password: String): Boolean = dbQuery {
        val account = Account.findById(id)
        account?.username = username
        account?.email = email
        account?.password = password

        account != null
    }

    override suspend fun deleteAccount(id: Int): Boolean = dbQuery {
        val account = Account.findById(id)
        try {
            account!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun getToken(email: String, password: String): String = dbQuery {
        Account.find {
            Accounts.email eq email
            Accounts.password eq password
        }
            .first()
            .token
    }

    override suspend fun getIdByUsername(username: String): Int = dbQuery {
        Account.find { Accounts.username eq username }
            .first()
            .id
            .value
    }

}

val dao: AccountDao = AccountDaoImpl().apply {
    runBlocking {
        if (allAccounts().isEmpty()) {
            addNewAccount("admin", "admin@fakemail.com", "password")
        }
    }
}