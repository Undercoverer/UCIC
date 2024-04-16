package gay.extremist.util

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import gay.extremist.models.*
import org.jetbrains.exposed.sql.*

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

            exec("CREATE EXTENSION IF NOT EXISTS pg_trgm;")
            exec("CREATE INDEX IF NOT EXISTS video_title_gin_idx ON videos USING GIN(title gin_trgm_ops);")
            exec("CREATE INDEX IF NOT EXISTS account_username_gin_idx ON accounts USING GIN(username gin_trgm_ops);")
        }
    }
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
}

infix fun <T : String?> Expression<T>.similarity(expression: String): CustomFunction<Double> {
    return CustomFunction("similarity", DoubleColumnType(), this, stringLiteral(expression))
}