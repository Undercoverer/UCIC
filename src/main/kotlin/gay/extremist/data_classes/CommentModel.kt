package gay.extremist.data_classes

import kotlinx.serialization.Serializable

@Serializable
data class CommentModel(val commentId: Int, val videoId: Int, val accountId: Int, val parentCommentId: Int?, val comment: String)

@Serializable
data class CommentObject(val comment: String)