package gay.extremist.util

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import gay.extremist.models.*

object DatabaseFactory {
    fun init() {
        val database = Database.connect(
            url = """
            jdbc:postgresql://${
                System.getenv("DB_HOST") ?: "db"
            }:${
                System.getenv("DB_PORT") ?: "5432"
            }/${
                System.getenv("DB_NAME") ?: "capstone_db"
            }
            """.trimIndent(),
            driver = "org.postgresql.Driver",
            user = System.getenv("DB_USER") ?: "postgres",
            password = System.getenv("DB_PASSWORD")  ?: "12345678"
        )
        transaction (database) {
            SchemaUtils.create(
                Accounts,
                AccountFollowsAccount,
                AccountFollowsTag,
                Playlists,
                PlaylistContainsVideo,
                Videos,
                Comments,
                Ratings,
                Tags,
                TagLabelsVideo
            )
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}