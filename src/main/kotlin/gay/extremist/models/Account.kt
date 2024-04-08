package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Accounts : IntIdTable() {
    val username: Column<String> = varchar("username", 255).uniqueIndex()
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val token: Column<String> = varchar("token", 255)

}

@Serializable
data class AccountDisplayResponse(val id: Int, val username: String)

class Account(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Account>(Accounts)

    var username by Accounts.username

    var email by Accounts.email
    var password by Accounts.password
    var token by Accounts.token
    val playlists by Playlist referrersOn Playlists.ownerId

    val videos by Video referrersOn Videos.creatorID
    var followedAccounts by Account.via(
        AccountFollowsAccount.follower, AccountFollowsAccount.account
    )

    var followedTags by Tag via AccountFollowsTag


    // Response Functions
    fun toResponse() = AccountResponse(id.value, username, email, password, token)
    fun toRegisteredAccountResponse() = RegisteredAccount(id.value, token)
    fun toDisplayResponse() = AccountDisplayResponse(id.value, username)
}


@Serializable
data class RegistrationAccount(val username: String, val email: String, val password: String)

@Serializable
data class RegisteredAccount(val accountId: Int, val token: String)

@Serializable
data class LoginAccount(val username: String, val password: String)

@Serializable
data class AccountResponse(
    val accountID: Int, val username: String, val email: String, val password: String, val token: String
)