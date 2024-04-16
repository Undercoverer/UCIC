package gay.extremist.util

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
        fun headersNotProvided(missing: List<String>) = ErrorResponse(
            error = "headers",
            message = "Headers not provided: $missing",
            status = HttpStatusCode.BadRequest.value,
        )

        fun conversionError(value: String, type: String, error: String) = ErrorResponse(
            error = "conversion",
            message = "Could not convert \"$value\" to $type: $error",
            status = HttpStatusCode.BadRequest.value,
        )

        fun notProvided(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s not provided",
            status = HttpStatusCode.BadRequest.value,
        )

        fun notFound(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s not found",
            status = HttpStatusCode.NotFound.value,
        )

        fun notOwnedByAccount(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s not owned by account",
            status = HttpStatusCode.Forbidden.value,
        )

        fun parametersNotProvided(missing: List<String>) = ErrorResponse(
            error = "parameters",
            message = "Parameters not provided: $missing",
            status = HttpStatusCode.BadRequest.value,
        )

        fun alreadyExists(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s already exists",
            status = HttpStatusCode.Conflict.value,
        )

        fun alreadyFollowed(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s already followed",
            status = HttpStatusCode.Conflict.value
        )

        fun notFollowed(s: String) = ErrorResponse(
            error = s.lowercase().replace(Regex("\\s+"), "_"),
            message = "$s not followed",
            status = HttpStatusCode.NotFound.value
        )
        val notInPlaylist = ErrorResponse(
            error = "playlist",
            message = "Video not in playlist",
            status = HttpStatusCode.NotFound.value,
        )
        val alreadyInPlaylist = ErrorResponse(
            error = "playlist",
            message = "Video already in playlist",
            status = HttpStatusCode.Conflict.value,
        )
        val videoConvertFailed = ErrorResponse(
            error = "video",
            message = "Video conversion failed",
            status = HttpStatusCode.InternalServerError.value,
        )
        val videoDeleteFailed = ErrorResponse(
            error = "video",
            message = "Video deletion failed",
            status = HttpStatusCode.InternalServerError.value,
        )
        val videoNotProcessed = ErrorResponse(
            error = "video", message = "Video not finished processing", status = HttpStatusCode.NotFound.value
        )
        val videoDescriptionTooLong = ErrorResponse(
            error = "video",
            message = "Video description cannot exceed 4000 characters",
            status = HttpStatusCode.BadRequest.value
        )
        val videoTitleTooLong = ErrorResponse(
            error = "video",
            message = "Video title cannot exceed 255 characters",
            status = HttpStatusCode.BadRequest.value
        )
        val videoTagsTooMany = ErrorResponse(
            error = "video",
            message = "Video tag count cannot exceed 16 tags",
            status = HttpStatusCode.BadRequest.value,
        )
        val videoTagsEmpty = ErrorResponse(
            error = "video", message = "Video tags cannot be empty", status = HttpStatusCode.BadRequest.value
        )
        val videoDescriptionEmpty = ErrorResponse(
            error = "video", message = "Video description cannot be empty", status = HttpStatusCode.BadRequest.value
        )
        val videoTitleEmpty = ErrorResponse(
            error = "video", message = "Video title cannot be empty", status = HttpStatusCode.BadRequest.value
        )
        val schema = ErrorResponse(
            error = "data", message = "Schema is invalid", status = HttpStatusCode.BadRequest.value
        )
        val accountUsernameOrEmailTaken = ErrorResponse(
            error = "account",
            message = "Username or email already taken",
            status = HttpStatusCode.Conflict.value,
        )
        val videoUploadFailed = ErrorResponse(
            error = "video", message = "Video upload failed", status = HttpStatusCode.InternalServerError.value
        )
        val accountTokenInvalid = ErrorResponse(
            error = "account", message = "Incorrect token", status = HttpStatusCode.Unauthorized.value
        )
        val accountIncorrectCredentials = ErrorResponse(
            error = "account", message = "Incorrect username or password", status = HttpStatusCode.Unauthorized.value
        )
    }
}