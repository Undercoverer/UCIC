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
        val videoNotFound = ErrorResponse(
            error = "video", message = "Video not found", status = HttpStatusCode.NotFound.value
        )
        val videoNonNumericId = ErrorResponse(
            error = "videoId", message = "videoId must be an integer", status = HttpStatusCode.BadRequest.value
        )
        val videoIdNotProvided = ErrorResponse(
            error = "videoId", message = "videoId not provided", status = HttpStatusCode.BadRequest.value
        )
        val tokenInvalid = ErrorResponse(
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
            message = "Data does not match schema",
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


