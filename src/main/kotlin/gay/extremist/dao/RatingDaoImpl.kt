package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.*
import org.jetbrains.exposed.sql.and

class RatingDaoImpl : RatingDao {
    override suspend fun createRating(video: Video, account: Account, rating: Int): Rating = dbQuery {
        Rating.new{
            this.video = video
            this.account = account
            this.rating = rating
        }
    }

    override suspend fun readRating(id: Int): Rating? = dbQuery {
        Rating.findById(id)
    }

    override suspend fun readRatingAll(): List<Rating> = dbQuery {
        Rating.all().toList()
    }

    override suspend fun updateRating(id: Int, rating: Int): Boolean = dbQuery {
        val oldRating = Rating.findById(id)
        oldRating?.rating = rating

        oldRating != null
    }

    override suspend fun deleteRating(id: Int): Boolean = dbQuery {
        val rating = Rating.findById(id)

        try {
            rating!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override suspend fun getIdByVideoAndAccount(video: Video, account: Account): Int? = dbQuery {
        Rating.find {
            (Ratings.videoID eq video.id) and (Ratings.accountID eq account.id)
        }.firstOrNull()?.id?.value
    }

    override suspend fun createOrUpdateRating(video: Video, account: Account, rating: Int): Unit = dbQuery {
        val id = getIdByVideoAndAccount(video, account)

        if (id == null) createRating(video, account, rating) else updateRating(id, rating)
    }

    override suspend fun getAverageRating(video: Video) = dbQuery {
        video.getRating()
    }
}

val ratingDao: RatingDao = RatingDaoImpl()