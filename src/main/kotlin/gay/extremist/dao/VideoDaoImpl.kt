package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class VideoDaoImpl : VideoDao {

    private fun resultRowToVideo(row: ResultRow) = Video(
        videoID = row[Videos.videoID],
        creatorID = row[Videos.creatorID].value,
        videoPath = row[Videos.videoPath],
        title = row[Videos.title],
        description = row[Videos.description],
        viewCount = row[Videos.viewCount],
        uploadDate = row[Videos.uploadDate].toKotlinLocalDateTime()
    )

    private fun resultRowToAccount(row: ResultRow) = Account(
        accountID = row[Accounts.accountID],
        username = row[Accounts.username],
        email = row[Accounts.email],
        password = row[Accounts.password],
        token = row[Accounts.token]

    )

    override suspend fun allVideos(): List<Video> = dbQuery {
        Videos
            .selectAll()
            .map(::resultRowToVideo)
    }

    override suspend fun video(videoID: Int): Video? = dbQuery {
        Videos
            .select { Videos.videoID eq videoID}
            .map(::resultRowToVideo)
            .singleOrNull()
    }

    override suspend fun addNewVideo(creatorID: Int, videoPath: String, title: String, description: String): Video? = dbQuery{
        val creatorEntityID = Accounts.select{ Accounts.accountID eq creatorID}.single()[Accounts.id]
        val insertStatement = Videos.insert {
            it[Videos.creatorID] = creatorEntityID
            it[Videos.videoPath] = videoPath
            it[Videos.title] = title
            it[Videos.description] = description
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToVideo)
    }

    override suspend fun editVideo(videoID: Int, title: String, description: String): Boolean = dbQuery{
        Videos.update({ Videos.videoID eq videoID }) {
            it[Videos.title] = title
            it[Videos.description] = description
        } > 0
    }

    override suspend fun deleteVideo(videoID: Int): Boolean = dbQuery {
        Videos.deleteWhere { Videos.videoID eq videoID } > 0
    }

    override suspend fun getCreator(videoID: Int): Account = dbQuery {
        val creatorID =
            Videos
                .select { Videos.videoID eq videoID }
                .map{ it[Videos.creatorID]}.single()
                .value
        Accounts
            .select{ Accounts.accountID eq creatorID }
            .map (::resultRowToAccount)
            .single()
    }
}