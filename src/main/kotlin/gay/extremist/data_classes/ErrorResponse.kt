package gay.extremist.data_classes

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val status: Int,
    var data: @Contextual Any? = null,
) {
    companion object {
        fun headersNotProvided(missing: List<String>): ErrorResponse {
            return ErrorResponse(
                error = "headers",
                message = "Headers not provided: $missing",
                status = HttpStatusCode.BadRequest.value,
            )
        }

        fun conversionError(value: String, type: String, error: String): Any {
            return ErrorResponse(
                error = "conversion",
                message = "Could not convert \"$value\" to $type: $error",
                status = HttpStatusCode.BadRequest.value,
            )
        }

        fun notProvided(s: String): Any {
            return ErrorResponse(
                error = s,
                message = "${s} not provided",
                status = HttpStatusCode.BadRequest.value,
            )
        }

        fun notFound(s: String): Any {
            return ErrorResponse(
                error = s.lowercase(),
                message = "$s not found",
                status = HttpStatusCode.NotFound.value,
            )
        }

        val commentSchema = ErrorResponse(
            error = "comment",
            message = "Comment schema is invalid",
            status = HttpStatusCode.BadRequest.value
        )
        val accountNotCommentAuthor = ErrorResponse(
            error = "account",
            message = "Account not comment author",
            status = HttpStatusCode.Forbidden.value,
        )
        val commentIdNotProvided = ErrorResponse(
            error = "comment", message = "commentId not provided", status = HttpStatusCode.BadRequest.value
        )
        val commentNotFound = ErrorResponse(
            error = "comment", message = "Comment not found", status = HttpStatusCode.NotFound.value
        )
        val commentNonNumericId = ErrorResponse(
            error = "comment",
            message = "commentId must be an integer",
            status = HttpStatusCode.BadRequest.value,
        )
        val commentNotProvided = ErrorResponse(
            error = "comment",
            message = "comment not provided",
            status = HttpStatusCode.BadRequest.value,
        )
        val videoNotOwnedByAccount = ErrorResponse(
            error = "video",
            message = "Video not owned by account",
            status = HttpStatusCode.Forbidden.value,
        )
        val accountUsernameOrEmailTaken = ErrorResponse(
            error = "account",
            message = "Username or email already taken",
            status = HttpStatusCode.Conflict.value,
        )
        val videoUploadFailed = ErrorResponse(
            error = "video", message = "Video upload failed", status = HttpStatusCode.InternalServerError.value
        )
        val videoNotFound = ErrorResponse(
            error = "video", message = "Video not found", status = HttpStatusCode.NotFound.value
        )
        val videoNonNumericId = ErrorResponse(
            error = "videoId", message = "videoId must be an integer", status = HttpStatusCode.BadRequest.value
        )
        val videoIdNotProvided = ErrorResponse(
            error = "videoId", message = "videoId not provided", status = HttpStatusCode.BadRequest.value
        )
        val accountTokenInvalid = ErrorResponse(
            error = "account", message = "Incorrect token", status = HttpStatusCode.Unauthorized.value
        )
        val accountTokenNotProvided = ErrorResponse(
            error = "token",
            message = "token not provided",
            status = HttpStatusCode.Unauthorized.value,
        )
        val accountIdNotProvided = ErrorResponse(
            error = "accountId", message = "accountId not provided", status = HttpStatusCode.BadRequest.value
        )
        val accountIncorrectCredentials = ErrorResponse(
            error = "account", message = "Incorrect username or password", status = HttpStatusCode.Unauthorized.value
        )
        val accountSchema = ErrorResponse(
            error = "account",
            message = "Account schema is invalid",
            status = HttpStatusCode.BadRequest.value,
        )
        val accountNotFound = ErrorResponse(
            error = "account",
            message = "Account not found",
            status = HttpStatusCode.NotFound.value,
        )
        val accountNonNumericId = ErrorResponse(
            error = "accountId", message = "accountId must be an integer", status = HttpStatusCode.BadRequest.value
        )
    }
}