package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.*
import gay.extremist.util.similarity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.or
import java.util.*


class AccountDaoImpl : AccountDao {
    override suspend fun createAccount(username: String, email: String, password: String): Account? = dbQuery {
        val account = Account.find { (Accounts.username eq username) or (Accounts.email eq email) }.firstOrNull()
        val hashedPassword = password.hashCode().toString()
        when (account) {
            null -> Account.new {
                this.username = username
                this.email = email
                this.password = hashedPassword
                this.token = UUID.nameUUIDFromBytes((username + hashedPassword).toByteArray()).toString()
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

    override suspend fun updateAccount(id: Int, username: String, email: String, password: String): Account? = dbQuery {
        when (val account = Account.findById(id)) {
            null -> return@dbQuery null
            else -> {
                account.username = username
                account.email = email
                account.password = password.hashCode().toString()
                account.token = UUID.nameUUIDFromBytes((username + password.hashCode().toString()).toByteArray()).toString()
                return@dbQuery account
            }
        }
    }

    override suspend fun deleteAccount(id: Int): Boolean = dbQuery {
        when (val account = Account.findById(id)) {
            null -> return@dbQuery false
            else -> {
                account.delete()
                return@dbQuery true
            }
        }
    }

    override suspend fun getToken(email: String, password: String): String? = dbQuery {
        Account.find {
            Accounts.email eq email
            Accounts.password eq password.hashCode().toString().also {
                println(password.hashCode().toString())
            }
        }.firstOrNull()?.token
    }

    override suspend fun getIdByUsername(username: String): Int? = dbQuery {
        Account.find { Accounts.username eq username }.firstOrNull()?.id?.value
    }

    override suspend fun getIdByEmail(email: String): Int? = dbQuery {
        Account.find { Accounts.email eq email }.firstOrNull()?.id?.value
    }

    override suspend fun addFollowedAccount(id: Int, account: Account): Boolean = dbQuery {
        val yourAccount = Account.findById(id) ?: return@dbQuery false
        val yourFollowedAccounts = yourAccount.followedAccounts

        return@dbQuery if (yourFollowedAccounts.contains(account)) {
            false
        } else {
            yourAccount.followedAccounts = SizedCollection(yourFollowedAccounts + account)
            true
        }
    }

    override suspend fun getFollowedTags(id: Int): List<Tag> = dbQuery {
        Account.findById(id)?.followedTags?.toList() ?: emptyList()
    }

    override suspend fun getFollowedAccounts(id: Int): List<Account> = dbQuery {
        Account.findById(id)?.followedAccounts?.toList() ?: emptyList()
    }

    override suspend fun searchAccounts(username: String): List<Account> {
        return Account.find {
            Accounts.username like "%$username%"
        }.toList()
    }

    override suspend fun searchAccountsFuzzy(username: String): List<Account> = dbQuery {
//        val conn = TransactionManager.current().connection
//        val query = "SELECT * FROM accounts WHERE similarity(username, ?) > 0.5 ORDER BY similarity(username, ?) DESC"
//        val statement = conn.prepareStatement(query, false).apply { set(1, username) }
//        val resultSet = statement.executeQuery()
//        val videos = mutableListOf<Account>()
//        while (resultSet.next()) Account.findById(resultSet.getInt("id")).let { videos.add(it!!) }
//        return@dbQuery videos
        // TODO MAKE SURE THIS WORKS RIGHT
        val nameSimilarity = Accounts.username similarity username
        Account.find {
            nameSimilarity greater 0.5
        }.orderBy(
            nameSimilarity to SortOrder.DESC
        ).toList()
    }

    override suspend fun getRecommendedVideosByFollowedTags(accountId: Int): List<Video> = dbQuery {
        val account = Account.findById(accountId) ?: return@dbQuery emptyList()
        val tags = account.followedTags
        val videos = mutableListOf<Video>()

        for (tag in tags) {
            videos.addAll(tag.videos)
        }

        return@dbQuery videos.sortBy(SortMethod.DATE, false)
    }

    override suspend fun getRecommendedVideosByFollowedAccounts(accountId: Int): List<Video> = dbQuery{
        val account = Account.findById(accountId) ?: return@dbQuery emptyList()
        val accounts = account.followedAccounts
        val videos = mutableListOf<Video>()
        for (theAccount in accounts) {
            videos.addAll(theAccount.videos)
        }
        return@dbQuery videos.sortBy(SortMethod.DATE, false)
    }


    override suspend fun removeFollowedAccount(id: Int, account: Account): Boolean = dbQuery {
        val yourAccount = Account.findById(id) ?: return@dbQuery false
        val yourFollowedAccounts = yourAccount.followedAccounts

        try {
            yourAccount.followedAccounts = SizedCollection(yourFollowedAccounts.filter { it != account })
            return@dbQuery true
        } catch (e: NullPointerException) {
            return@dbQuery false
        }
    }
    override suspend fun addFollowedTag(id: Int, tag: Tag): Boolean = dbQuery {
        val follower = Account.findById(id) ?: return@dbQuery false
        val followedAccounts = follower.followedTags

        runCatching {
            follower.followedTags = SizedCollection(followedAccounts + tag)
        }.isSuccess
    }

    override suspend fun removeFollowedTag(id: Int, tag: Tag): Boolean = dbQuery {
        val follower = Account.findById(id) ?: return@dbQuery false
        val followedAccounts = follower.followedTags

        runCatching {
            follower.followedTags = SizedCollection(followedAccounts.filter { it != tag })
        }.isSuccess
    }

    override suspend fun getVideosFromAccount(accountId: Int): List<Video> =
        dbQuery { readAccount(accountId)?.videos?.toList() ?: emptyList() }

    override suspend fun getPlaylistsFromAccount(accountId: Int): List<Playlist> = dbQuery {
        readAccount(accountId)?.playlists?.toList() ?: emptyList()
    }
}

val accountDao: AccountDao = AccountDaoImpl()