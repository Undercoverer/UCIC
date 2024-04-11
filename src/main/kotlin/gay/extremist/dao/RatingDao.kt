package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Rating
import gay.extremist.models.Video
import org.jetbrains.exposed.dao.id.EntityID

interface RatingDao {
    suspend fun createRating(video: Video, account: Account, rating: Int): Rating
    suspend fun readRating(id: Int): Rating?
    suspend fun readRatingAll(): List<Rating>
    suspend fun updateRating(id: Int, rating: Int): Boolean
    suspend fun deleteRating(id: Int): Boolean
    suspend fun getIdByVideoAndAccount(video: Video, account: Account): Int?
    suspend fun createOrUpdateRating(video: Video, account: Account, rating: Int)
}