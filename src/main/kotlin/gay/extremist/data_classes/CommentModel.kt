package gay.extremist.data_classes

import kotlinx.serialization.Serializable

@Serializable
data class CommentModel(val videoId: Int, val accountId: Int, val commentId: Int, val comment: String)
