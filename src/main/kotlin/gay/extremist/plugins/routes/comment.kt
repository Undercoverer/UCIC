package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.commentDao
import gay.extremist.dao.videoDao
import gay.extremist.data_classes.CommentModel
import gay.extremist.data_classes.CommentObject
import gay.extremist.data_classes.ErrorResponse
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
        put { handleUpdateComment() }
        delete { handleDeleteComment() }
    }
    get("/account/{id}") { handleGetCommentsForAccount() }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleGetComments() = with(call) {
    val video = videoDao.readVideo(idParameter() ?: return) ?: return respond(ErrorResponse.notFound("Video"))
    respond(commentDao.getCommentsOnVideo(video.id.value))
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleCreateComment() = with(call) {
    val headers = requiredHeaders(headerVideoId, headerAccountId, headerToken) ?: return
    val optionalHeader = optionalHeaders(headerParentCommentId)

    val videoId = convert(headers[headerVideoId], String::toInt) ?: return
    val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.videoNotFound)

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)

    val token = headers[headerToken] ?: return
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)

    val commentObject = receiveCatching<CommentObject>().onFailureOrNull {
        call.respond(ErrorResponse.commentSchema.apply { data = it.message })
    } ?: return


    val parentCommentIdString = optionalHeader[headerParentCommentId]
    val comment = if (parentCommentIdString == null) {
        commentDao.createComment(account, video, null, commentObject.comment)
    } else {
        val parentComment =
            commentDao.readComment(convert(parentCommentIdString, String::toInt) ?: return) ?: return respond(
                ErrorResponse.commentNotFound
            )
        commentDao.createComment(account, video, parentComment, commentObject.comment)
    }

    respond(
        status = HttpStatusCode.Created, message = CommentModel(
            comment.id.value, videoId, accountId, commentObject.comment
        )
    )
}

// TODO CONTINUE REFACTORING HERE

suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateComment(vararg params: String) = with(call) {
    val commentId = request.headers["commentId"]?.toIntOrNull() ?: return respond(ErrorResponse.commentIdNotProvided)
    val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.commentNotFound)
    val accountId = request.headers["accountId"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)
    if (account.id.value != comment.account.id.value) return respond(ErrorResponse.accountNotCommentAuthor)
    val token = request.headers["token"] ?: return respond(ErrorResponse.accountTokenNotProvided)
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
    val commentText = receive<String>()
    commentDao.updateComment(commentId, commentText)

    respond(status = HttpStatusCode.Accepted, message = "Comment updated")
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteComment(vararg params: String) = with(call) {
    val commentId = request.headers["commentId"]?.toIntOrNull() ?: return respond(ErrorResponse.commentIdNotProvided)
    val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.commentNotFound)
    val accountId = request.headers["accountId"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)
    if (account.id.value != comment.account.id.value) return respond(ErrorResponse.accountNotCommentAuthor)
    val token = request.headers["token"] ?: return respond(ErrorResponse.accountTokenNotProvided)
    if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
    commentDao.deleteComment(commentId)

    respond(status = HttpStatusCode.Accepted, message = "Comment deleted")
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleGetCommentsForAccount() = with(call) {
    val accountId = idParameter() ?: return
    respond(commentDao.getCommentsOnAccount(accountId))
}

