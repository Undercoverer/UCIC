package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.commentDao
import gay.extremist.dao.videoDao
import gay.extremist.data_classes.CommentModel
import gay.extremist.data_classes.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.createCommentRoutes() = route("/comments") {
    route("/video/{id}") {
        // Get
        get { handleGetComments() }
        // Create
        post { handleCreateComment() }
        // Update
        put { handleUpdateComment() }
        delete { handleDeleteComment() }
    }
    // Comments for a user
    get("/account/{id}") { handleGetCommentsForAccount() }
}


suspend fun PipelineContext<Unit, ApplicationCall>.handleGetComments() = with(call) {
    val videoId = parameters["id"]?.toIntOrNull() ?: return respond(ErrorResponse.videoIdNotProvided)
    respond(commentDao.getCommentsOnVideo(videoId).map {
        transaction { CommentModel(it.video.id.value, it.account.id.value, it.id.value, it.comment) }
    })
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleCreateComment() {
    with(call) {
        val videoId = parameters["id"]?.toIntOrNull() ?: return respond(ErrorResponse.videoIdNotProvided)
        val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.videoNotFound)
        val accountId =
            request.headers["accountId"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
        val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)
        val token = request.headers["token"] ?: return respond(ErrorResponse.accountTokenNotProvided)
        if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
        val commentText = receive<String>()
        val parentCommentId = request.headers["commentId"]?.toIntOrNull()
        val parentComment =
            parentCommentId?.let { commentDao.readComment(it) } ?: return respond(ErrorResponse.commentNotFound)
        commentDao.createComment(account, video, parentComment, commentText)
        respond(status = HttpStatusCode.Created, message = "Comment created")
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateComment() {
    with(call) {
        val commentId =
            request.headers["commentId"]?.toIntOrNull() ?: return respond(ErrorResponse.commentIdNotProvided)
        val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.commentNotFound)
        val accountId =
            request.headers["accountId"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
        val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)
        if (account.id.value != comment.account.id.value) return respond(ErrorResponse.accountNotCommentAuthor)
        val token = request.headers["token"] ?: return respond(ErrorResponse.accountTokenNotProvided)
        if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
        val commentText = receive<String>()
        commentDao.updateComment(commentId, commentText)
        respond(status = HttpStatusCode.Accepted, message = "Comment updated")
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteComment() {
    with(call) {
        val commentId =
            request.headers["commentId"]?.toIntOrNull() ?: return respond(ErrorResponse.commentIdNotProvided)
        val comment = commentDao.readComment(commentId) ?: return respond(ErrorResponse.commentNotFound)
        val accountId =
            request.headers["accountId"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
        val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.accountNotFound)
        if (account.id.value != comment.account.id.value) return respond(ErrorResponse.accountNotCommentAuthor)
        val token = request.headers["token"] ?: return respond(ErrorResponse.accountTokenNotProvided)
        if (account.token != token) return respond(ErrorResponse.accountTokenInvalid)
        commentDao.deleteComment(commentId)
        respond(status = HttpStatusCode.Accepted, message = "Comment deleted")
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleGetCommentsForAccount() {
    with(call) {
        val accountId = parameters["id"]?.toIntOrNull() ?: return respond(ErrorResponse.accountIdNotProvided)
        respond(commentDao.getCommentsOnAccount(accountId))
    }
}
