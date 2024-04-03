package gay.extremist.plugins.routes

import gay.extremist.dao.DatabaseFactory.dbQuery
import gay.extremist.dao.accountDao
import gay.extremist.dao.commentDao
import gay.extremist.dao.videoDao
import gay.extremist.data_classes.CommentModel
import gay.extremist.data_classes.CommentObject
import gay.extremist.data_classes.ErrorResponse
import gay.extremist.models.Comment
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*


// DONE I THINK
fun Route.createCommentRoutes() = route("/comments") {
    route("/video/{id}") {
        get { handleGetComments() }
        post { handleCreateComment() }
        put { handleUpdateComment() }
        delete { handleDeleteComment() }
    }
    route("/{id}"){
        get { handleGetCommentsOnComment() }
    }
}


suspend fun PipelineContext<Unit, ApplicationCall>.handleGetCommentsOnComment() = with(call) {
    val comment = commentDao.readComment(idParameter() ?: return) ?: return respond(ErrorResponse.notFound("Comment"))
    respond(commentDao.getCommentsOnComment(comment.id.value).map {
        dbQuery {
            CommentModel(
                it.id.value, comment.video.id.value, it.account.id.value, it.parentComment?.id?.value, it.comment
            )
        }
    })
}
suspend fun PipelineContext<Unit, ApplicationCall>.handleGetComments() = with(call) {
    val video = videoDao.readVideo(idParameter() ?: return) ?: return respond(ErrorResponse.notFound("Video"))
    respond(commentDao.getToplevelCommentsOnVideo(video.id.value).map {
        dbQuery {
            CommentModel(
                it.id.value, video.id.value, it.account.id.value, it.parentComment?.id?.value, it.comment
            )
        }
    })
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleCreateComment() = with(call) {
    val headers = requiredHeaders(headerVideoId, headerAccountId, headerToken) ?: return
    val optionalHeader = optionalHeaders(headerParentCommentId)

    val videoId = convert(headers[headerVideoId], String::toInt) ?: return
    val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.notFound("Video"))

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)

    val commentObject = receiveCatching<CommentObject>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return


    val parentCommentIdString = optionalHeader[headerParentCommentId]
    var parentComment: Comment? = null
    val comment = if (parentCommentIdString == null) {
        commentDao.createComment(account, video, null, commentObject.comment)
    } else {
        parentComment =
            commentDao.readComment(convert(parentCommentIdString, String::toInt) ?: return) ?: return respond(
                ErrorResponse.notFound("Comment")
            )
        commentDao.createComment(account, video, parentComment, commentObject.comment)
    }

    respond(
        status = HttpStatusCode.Created, message = CommentModel(
            comment.id.value, videoId, accountId, parentComment?.id?.value, commentObject.comment
        )
    )
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteComment() = with(call) {
    val headers = requiredHeaders(headerAccountId, headerToken, headerCommentId) ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)

    val commentId = convert(headers[headerCommentId], String::toInt) ?: return
    val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.notFound("Comment"))
    if (dbQuery { account.id.value != comment.account.id.value }) return respond(ErrorResponse.notOwnedByAccount("Comment"))

    commentDao.deleteComment(commentId)

    respond(status = HttpStatusCode.Accepted, message = "Comment deleted")
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateComment() = with(call) {
    val headers = requiredHeaders(headerCommentId, headerAccountId, headerToken) ?: return

    val commentId = convert(headers[headerCommentId], String::toInt) ?: return
    val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.notFound("Comment"))

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    if (dbQuery { account.id.value != comment.account.id.value }) return respond(ErrorResponse.notOwnedByAccount("Comment"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
    val commentText = receiveText()
    commentDao.updateComment(commentId, commentText)

    respond(status = HttpStatusCode.Accepted, message = "Comment updated")
}



