package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.Account
import gay.extremist.models.Comment
import gay.extremist.models.Video

class CommentDaoImpl : CommentDao {
    override suspend fun createComment( account: Account, video: Video, parentComment: Comment?, comment: String ): Comment = dbQuery {
        Comment.new {
            this.account = account
            this.video = video
            this.parentComment = parentComment
            this.comment = comment
        }
    }

    override suspend fun readComment(id: Int): Comment? = dbQuery {
        Comment.findById(id)
    }

    override suspend fun readCommentAll(): List<Comment> = dbQuery {
        Comment.all().toList()
    }

    override suspend fun updateComment(id: Int, comment: String): Boolean = dbQuery {
        val oldComment = Comment.findById(id)
        oldComment?.comment = comment

        oldComment != null
    }

    override suspend fun deleteComment(id: Int): Boolean = dbQuery {
        val comment = Comment.findById(id)

        try {
            comment!!.delete()
            true
        } catch (e: NullPointerException) {
            false
        }
    }
}

val commentDao: CommentDao = CommentDaoImpl()