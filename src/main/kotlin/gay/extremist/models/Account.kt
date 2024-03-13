package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

@Serializable
data class Account(
    val accountID : Int,
    val username : String,
    val email : String,
    val password : String,
    val token : String
)

object Accounts : IntIdTable()  {
    val accountID : Column<Int> = integer("accountID")
        .uniqueIndex()
        .autoIncrement()
    val username : Column<String> = varchar("username", 255)
    val email : Column<String>  = varchar("email", 255)
    val password : Column<String>  = varchar("password", 255)
    val token : Column<String>  = varchar("token", 255)

}
