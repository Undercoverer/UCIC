package gay.extremist.dao

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import gay.extremist.models.*

object DatabaseFactory {
    fun init() {
        val database = Database.connect(
            url = "jdbc:postgresql://localhost:5432/capstone_db",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "12345678"
        )
        transaction (database) {
            SchemaUtils.create(Accounts, Videos, Tags, TagLabelsVideo)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}