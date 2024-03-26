package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking
import java.util.*


class AccountDaoImpl : AccountDao {

    override suspend fun createAccount(username: String, email: String, password: String): Account = dbQuery {
        Account.new {
            this.username = username
            this.email = email
            this.password = password
            this.token = UUID.nameUUIDFromBytes((username + password).toByteArray()).toString()
        }
    }

    override suspend fun readAccount(id: Int): Account? = dbQuery {
        Account.findById(id)
    }

    override suspend fun readAccountAll(): List<Account> = dbQuery {
        Account.all().toList()
    }

    override suspend fun updateAccount(id: Int, username: String, email: String, password: String): Boolean = dbQuery {
        return@dbQuery when (val account = Account.findById(id)) {
            null -> false
            else -> {
                account.username = username
                account.email = email
                account.password = password
                true
            }
        }
    }

    override suspend fun deleteAccount(id: Int): Boolean = dbQuery {
        return@dbQuery when (val account = Account.findById(id)) {
            null -> false
            else -> {
                account.delete()
                true
            }
        }
    }

    override suspend fun getToken(email: String, password: String): String? = dbQuery {
        Account.find {
            Accounts.email eq email
            Accounts.password eq password
        }.firstOrNull()?.token
    }

    override suspend fun getIdByUsername(username: String): Int? = dbQuery {
        Account.find { Accounts.username eq username }
            .firstOrNull()
            ?.id
            ?.value
    }

}

val accountDao: AccountDao = AccountDaoImpl().apply {
    runBlocking {
        if (readAccountAll().isEmpty()) {
            createAccount("admin", "admin@fakemail.com", "password")
        }
    }
}