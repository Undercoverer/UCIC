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
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.createCommentRoutes() = route("/comments") {
    val commentDao = commentDao
    // Comments for a video
    route("/video/{id}") {
        // Get
        get {
            val videoId = call.parameters["id"]
            videoId ?: return@get call.respond(ErrorResponse.videoIdNotProvided)

            runCatching {
                videoId.toInt()
            }.onFailure {
                return@get call.respond(ErrorResponse.videoNonNumericId)
            }
            call.respond(commentDao.getCommentsOnVideo(videoId.toInt()).map {
                transaction { CommentModel(it.video.id.value, it.account.id.value, it.id.value, it.comment) }
            })
        }
        // Create
        post {
            val videoId = call.parameters["id"]
            videoId ?: return@post call.respond(ErrorResponse.videoIdNotProvided)

            runCatching {
                videoId.toInt()
            }.onFailure {
                return@post call.respond(ErrorResponse.videoNonNumericId)
            }

            val video = videoDao.readVideo(videoId.toInt())
            video ?: return@post call.respond(ErrorResponse.videoNotFound)


            val accountId = call.request.headers["accountId"]
            accountId ?: return@post call.respond(ErrorResponse.accountIdNotProvided)

            runCatching {
                accountId.toInt()
            }.onFailure {
                return@post call.respond(ErrorResponse.accountNonNumericId)
            }

            val account = accountDao.readAccount(accountId.toInt())
            account ?: return@post call.respond(ErrorResponse.accountNotFound)

            val token = call.request.headers["token"]
            token ?: return@post call.respond(ErrorResponse.accountTokenNotProvided)

            if (account.token != token) {
                return@post call.respond(ErrorResponse.accountTokenInvalid)
            }

            var commentText = ""
            runCatching {
                commentText = call.receive<String>()
            }.onFailure {
                return@post call.respond(ErrorResponse.commentNotProvided)
            }

            val parentCommentId = call.request.headers["commentId"]
            val parentComment = parentCommentId?.let {
                runCatching {
                    parentCommentId.toInt()
                }.onFailure {
                    return@post call.respond(ErrorResponse.commentNonNumericId)
                }
                val parentComment = commentDao.readComment(parentCommentId.toInt())
                parentComment ?: return@post call.respond(ErrorResponse.commentNotFound)
            }


            val comment = commentDao.createComment(
                account, video, parentComment, commentText
            )

            call.respond(status = HttpStatusCode.Created, message = "Comment created")

        }
        // Update
        post {
            val commentId = call.request.headers["commentId"]
            commentId ?: return@post call.respond(ErrorResponse.commentIdNotProvided)

            runCatching {
                commentId.toInt()
            }.onFailure {
                return@post call.respond(ErrorResponse.commentNonNumericId)
            }

            val comment = commentDao.readComment(commentId.toInt())
            comment ?: return@post call.respond(ErrorResponse.commentNotFound)

            val accountId = call.request.headers["accountId"]
            accountId ?: return@post call.respond(ErrorResponse.accountIdNotProvided)

            runCatching {
                accountId.toInt()
            }.onFailure {
                return@post call.respond(ErrorResponse.accountNonNumericId)
            }

            val account = accountDao.readAccount(accountId.toInt())
            account ?: return@post call.respond(ErrorResponse.accountNotFound)

            if (account.id.value != comment.account.id.value) {
                return@post call.respond(ErrorResponse.accountNotCommentAuthor)
            }
            val token = call.request.headers["token"]
            token ?: return@post call.respond(ErrorResponse.accountTokenNotProvided)
            if (account.token != token) {
                return@post call.respond(ErrorResponse.accountTokenInvalid)
            }

            var commentText = ""
            runCatching {
                commentText = call.receive<String>()
                commentDao.updateComment(commentId.toInt(), commentText)
                call.respond(status = HttpStatusCode.Accepted, message = "Comment updated")
            }.onFailure {
                return@post call.respond(ErrorResponse.commentNotProvided)
            }
        }
        delete {
            val commentId = call.request.headers["commentId"]
            commentId ?: return@delete call.respond(ErrorResponse.commentIdNotProvided)
            runCatching {
                commentId.toInt()
            }.onFailure {
                return@delete call.respond(ErrorResponse.commentNonNumericId)
            }

            val comment = commentDao.readComment(commentId.toInt())
            comment ?: return@delete call.respond(ErrorResponse.commentNotFound)
            val accountId = call.request.headers["accountId"]
            accountId ?: return@delete call.respond(ErrorResponse.accountIdNotProvided)

            runCatching {
                accountId.toInt()
            }.onFailure {
                return@delete call.respond(ErrorResponse.accountNonNumericId)
            }

            val account = accountDao.readAccount(accountId.toInt())
            account ?: return@delete call.respond(ErrorResponse.accountNotFound)

            if (account.id.value != comment.account.id.value) {
                return@delete call.respond(ErrorResponse.accountNotCommentAuthor)
            }

            val token = call.request.headers["token"]
            token ?: return@delete call.respond(ErrorResponse.accountTokenNotProvided)

            if (account.token != token) {
                return@delete call.respond(ErrorResponse.accountTokenInvalid)
            }
            commentDao.deleteComment(commentId.toInt())
            call.respond(status = HttpStatusCode.Accepted, message = "Comment deleted")
        }
    }
    // Comments for a user
    get("/account/{id}") {
        val accountId = call.parameters["id"]
        accountId ?: return@get call.respond(ErrorResponse.accountIdNotProvided)

        runCatching {
            accountId.toInt()
        }.onFailure {
            return@get call.respond(ErrorResponse.accountNonNumericId)
        }

        call.respond(commentDao.getCommentsOnAccount(accountId.toInt()))
    }
}
