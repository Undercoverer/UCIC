package gay.extremist.dao

import gay.extremist.models.Account
import gay.extremist.models.Comment
import gay.extremist.models.Video

interface CommentDao {
    suspend fun createComment(account: Account, video: Video, parentComment: Comment?, comment: String): Comment
    suspend fun readComment(id: Int): Comment?
    suspend fun readCommentAll(): List<Comment>
    suspend fun updateComment(id: Int, comment: String): Boolean
    suspend fun deleteComment(id: Int): Boolean
    suspend fun getToplevelCommentsOnVideo(videoId: Int): List<Comment>
    suspend fun getCommentsOnComment(commentId: Int): List<Comment>
    companion object : CommentDao by CommentDaoImpl()
}