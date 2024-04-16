package gay.extremist.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption

object Comments: IntIdTable() {
    val accountID: Column<EntityID<Int>>  = reference("accountID", Accounts, onDelete = ReferenceOption.CASCADE)
    val videoID: Column<EntityID<Int>>  = reference("videoID", Videos, onDelete = ReferenceOption.CASCADE)
    val parentID: Column<EntityID<Int>?>  = reference("parentID", Comments, onDelete = ReferenceOption.CASCADE).nullable()
    val comment: Column<String> = text("comment")
}

class Comment(id: EntityID<Int>): Entity<Int>(id){
    companion object: EntityClass<Int, Comment> (Comments)

    var account by Account referencedOn Comments.accountID

    var video by Video referencedOn Comments.videoID
    var parentComment by Comment optionalReferencedOn Comments.parentID
    var comment by Comments.comment
    val childComments by Comment optionalReferrersOn Comments.parentID

    fun toResponse() = CommentResponse(
        id.value,
        account.toDisplayResponse(),
        video.toDisplayResponse(),
        parentComment?.toResponseNonrecursive(),
        comment
    )

    private fun toResponseNonrecursive() = CommentResponseNonrecursive(
        id.value,
        account.toDisplayResponse(),
        video.toDisplayResponse(),
        comment
    )

    override fun equals(other: Any?): Boolean {
        return when(other){
            is Comment -> {
                this.id == other.id
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@Serializable
data class CommentResponse(
    val id: Int,
    val account: AccountDisplayResponse,
    val video: VideoDisplayResponse,
    val parentComment: CommentResponseNonrecursive?,
    val comment: String
)

@Serializable
data class CommentResponseNonrecursive(
    val id: Int,
    val account: AccountDisplayResponse,
    val video: VideoDisplayResponse,
    val comment: String
)
