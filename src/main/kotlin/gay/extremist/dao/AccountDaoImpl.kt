package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.or
import java.util.*

import java.security.MessageDigest


class AccountDaoImpl : AccountDao {
    // Used to hash passwords
    private val sha256 = MessageDigest.getInstance("SHA-256")

    override suspend fun createAccount(username: String, email: String, password: String): Account? = dbQuery {
        // Ensures no duplicate usernames and no reused emails
        val account = Account.find { (Accounts.username eq username) or (Accounts.email eq email) }.firstOrNull()
        val hashedPassword = password.hashCode().toString()
        when (account) {
            null -> Account.new {
                this.username = username
                this.email = email

                this.password = sha256.digest(password.toByteArray()).toString()
                this.token = UUID.nameUUIDFromBytes((username + password).toByteArray()).toString()
            }
            else -> null
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
                account.password = sha256.digest(password.toByteArray()).toString()
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
            Accounts.password eq sha256.digest(password.toByteArray()).toString()
        }.firstOrNull()?.token
    }

    override suspend fun getIdByUsername(username: String): Int? = dbQuery {
        Account.find { Accounts.username eq username }.firstOrNull()?.id?.value
    }

    override suspend fun addFollowedAccount(id: Int, account: Account): Boolean = dbQuery {
        val follower = Account.findById(id)
        val followedAccounts = follower?.followedAccounts

        try {
            follower?.followedAccounts = SizedCollection(followedAccounts!! + account)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun removeFollowedAccount(id: Int, account: Account): Boolean = dbQuery {
        val follower = Account.findById(id)
        val followedAccounts = follower?.followedAccounts

        try {
            follower?.followedAccounts = SizedCollection(followedAccounts!! - account)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun addFollowedTag(id: Int, tag: Tag): Boolean = dbQuery {
        val follower = Account.findById(id)
        val followedAccounts = follower?.followedTags

        try {
            follower?.followedTags = SizedCollection(followedAccounts!! + tag)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun removeFollowedTag(id: Int, tag: Tag): Boolean = dbQuery {
        val follower = Account.findById(id)
        val followedAccounts = follower?.followedTags

        try {
            follower?.followedTags = SizedCollection(followedAccounts!! - tag)
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun getVideosFromAccount(accountId: Int): List<Video> {
        return dbQuery {
            readAccount(accountId)?.videos?.toList() ?: emptyList()
        }
    }

    override suspend fun getPlaylistsFromAccount(accountId: Int): List<Playlist> {
        return dbQuery {
            readAccount(accountId)?.playlists?.toList() ?: emptyList()
        }
    }

}

val accountDao: AccountDao = AccountDaoImpl().apply {
    runBlocking {
        if (readAccountAll().isEmpty()) {
            createAccount("admin", "admin@fakemail.com", "password")
        }
    }
}
