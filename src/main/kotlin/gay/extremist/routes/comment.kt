package gay.extremist.routes

import gay.extremist.util.DatabaseFactory.dbQuery
import gay.extremist.dao.accountDao
import gay.extremist.dao.commentDao
import gay.extremist.dao.videoDao
import gay.extremist.util.ErrorResponse
import gay.extremist.models.Comment
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*


fun Route.createCommentRoutes() = route("/comments") {
    route("/video/{id}") {
        get { handleGetComments() }
        post { handleCreateComment() }
    }
    route("/{id}") {
        get { handleGetCommentsOnComment() }
        put { handleUpdateComment() }
        delete { handleDeleteComment() }
    }
}


suspend fun PipelineContext<Unit, ApplicationCall>.handleGetCommentsOnComment() = with(call) {
    val comment = commentDao.readComment(idParameter() ?: return) ?: return respond(ErrorResponse.notFound("Comment"))
    respond(commentDao.getCommentsOnComment(comment.id.value).map(Comment::toResponse))
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleGetComments() = with(call) {
    val video = videoDao.readVideo(idParameter() ?: return) ?: return respond(ErrorResponse.notFound("Video"))
    respond(commentDao.getToplevelCommentsOnVideo(video.id.value).map(Comment::toResponse))
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleCreateComment() = with(call) {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val optionalHeaders = optionalHeaders(headerParentCommentId)

    val videoId = idParameter() ?: return
    val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.notFound("Video"))

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)

    val commentText = receiveText()

    val parentCommentIdString = optionalHeaders[headerParentCommentId]
    val parentComment: Comment?
    val comment = if (parentCommentIdString == null) {
        commentDao.createComment(account, video, null, commentText)
    } else {
        parentComment =
            commentDao.readComment(convert(parentCommentIdString, String::toInt) ?: return) ?: return respond(
                ErrorResponse.notFound("Comment")
            )
        commentDao.createComment(account, video, parentComment, commentText)
    }

    respond(comment.toResponse())
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteComment() = with(call) {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val commentId = idParameter() ?: return

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)

    val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.notFound("Comment"))
    if (dbQuery { account.id.value != comment.account.id.value }) return respond(ErrorResponse.notOwnedByAccount("Comment"))

    commentDao.deleteComment(commentId)

    respond(status = HttpStatusCode.Accepted, message = "Comment deleted")
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateComment() = with(call) {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return

    val commentId = idParameter() ?: return
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



