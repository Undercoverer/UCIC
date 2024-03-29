package gay.extremist.dao

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.models.Account
import gay.extremist.models.Comment
import gay.extremist.models.Comments
import gay.extremist.models.Video
import org.jetbrains.exposed.sql.and

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

    override suspend fun getCommentsOnVideo(videoId: Int): List<Comment> = dbQuery {
        return@dbQuery Comment.find { (Comments.videoID eq videoId) and (Comments.parentID eq null) }.toList()
    }

    override suspend fun getCommentsOnComment(commentId: Int): List<Comment> = dbQuery {
        return@dbQuery Comment.find { Comments.parentID eq commentId }.toList()
    }

    override suspend fun getCommentsOnAccount(accountId: Int): List<Comment> = dbQuery {
        return@dbQuery Comment.find { Comments.accountID eq accountId }.toList()
    }
}

val commentDao: CommentDao = CommentDaoImpl()