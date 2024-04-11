package gay.extremist.dao

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.models.Account
import gay.extremist.models.Comment
import gay.extremist.models.Video

class CommentDaoImpl : CommentDao {
    override suspend fun createComment(
        account: Account, video: Video, parentComment: Comment?, comment: String
    ): Comment = dbQuery {
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
        val oldComment = Comment.findById(id) ?: return@dbQuery false

        oldComment.comment = comment
        return@dbQuery true
    }

    override suspend fun deleteComment(id: Int): Boolean = dbQuery {
        val comment = Comment.findById(id) ?: return@dbQuery false

        comment.delete()
        return@dbQuery true
    }

    override suspend fun getToplevelCommentsOnVideo(videoId: Int): List<Comment> = dbQuery {
        videoDao.readVideo(videoId)?.comments?.filter { it.parentComment == null }?.toList() ?: emptyList()
    }

    override suspend fun getCommentsOnComment(commentId: Int): List<Comment> = dbQuery {
        Comment.findById(commentId)?.childComments?.toList() ?: emptyList()
    }
}

val commentDao: CommentDao = CommentDaoImpl()