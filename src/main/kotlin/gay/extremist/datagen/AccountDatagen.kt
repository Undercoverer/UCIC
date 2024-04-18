package gay.extremist.datagen

import gay.extremist.dao.accountDao
import gay.extremist.dao.playlistDao
import gay.extremist.dao.tagDao
import gay.extremist.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

const val TOTAL_ACCOUNTS = 10
val RANDOM = Random.Default

fun main() {
    init()
    transaction {
        for (i in 1..TOTAL_ACCOUNTS) {
            runBlocking { accountDao.createAccount("user$i", "user$i@example.com", "12345678") }
        }
        // Make everyone follow between 1 and 5 other people
        for (i in 1..TOTAL_ACCOUNTS) {
            runBlocking {
                for (j in 1..(RANDOM.nextInt(5)) + 1) {
                    // Exclude current account num as possibility
                    val otherAccountNum = ((1..TOTAL_ACCOUNTS) - i).random()

                    accountDao.readAccount(otherAccountNum)?.let { accountDao.addFollowedAccount(i, it) }
                }
            }
        }
        // Make everyone follow between 3 and 10 tags
        for (i in 1..TOTAL_ACCOUNTS) {
            runBlocking {
                val j = (RANDOM.nextInt(10 + 1 - 3) + 3)
                // Generate random sublist from default tags
                tagDao.getPresetTags().shuffled().subList(0, j).forEach { accountDao.addFollowedTag(i, it) }
            }
        }

        // Make everyone create between 1 and 5 playlists
        for (i in 1..TOTAL_ACCOUNTS) {
            runBlocking {
                for (j in 1..(RANDOM.nextInt(5)) + 1) {
                    val account = accountDao.readAccount(i) ?: continue
                    playlistDao.createPlaylist(account, "Playlist $j")
                }
            }
        }
    }
}

fun init() {
    val database = Database.connect(
        url = """
            jdbc:postgresql://${
            System.getenv("DB_HOST") ?: "localhost"
        }:${
            System.getenv("DB_PORT") ?: "5432"
        }/${
            System.getenv("DB_NAME") ?: "capstone_db"
        }
            """.trimIndent(),
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "12345678"
    )
    transaction(database) {
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