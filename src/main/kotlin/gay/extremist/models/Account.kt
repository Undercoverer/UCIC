package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Accounts: IntIdTable()  {
    val username: Column<String> = varchar("username", 255)
        .uniqueIndex()
    val email: Column<String>  = varchar("email", 255)
        .uniqueIndex()
    val password: Column<String>  = varchar("password", 255)
    val token: Column<String>  = varchar("token", 255)

}

class Account(id: EntityID<Int>): Entity<Int>(id) {
    companion object: EntityClass<Int, Account>(Accounts)

    var username by Accounts.username
    var email by Accounts.email
    var password by Accounts.password
    var token by Accounts.token

    val playlists by Playlist referrersOn Playlists.ownerId
}

@Serializable
data class LoginAccount(val username: String, val password: String)

@Serializable
data class RegisterAccount(val username: String, val email: String, val password: String)

@Serializable
data class UnprivilegedAccount(val accountID: Int, val username: String, val email: String)
