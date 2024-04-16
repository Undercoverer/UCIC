package gay.extremist.routes

import gay.extremist.dao.accountDao
import io.ktor.server.routing.*
import gay.extremist.dao.ratingDao
import gay.extremist.dao.videoDao
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

fun Route.createRatingRoutes() = route("/ratings") {
    post { handleCreateRating() }

    route("/{id}") {
        get { handleReadRating() }
        post { handleUpdateRating() }
        delete { handleDeleteRating() }
    }
    route("/on") {
        get { handleGetIdByVideoAndAccount() }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleCreateRating() {
    val headers = requiredHeaders(headerVideoId, headerAccountId, headerToken) ?: return
    val videoId = videoDao.readVideo(convert(headers[headerVideoId], String::toInt) ?: return) ?: return call.respond(
        ErrorResponse.notFound("Video")
    )
    val accountId =
        accountDao.readAccount(convert(headers[headerAccountId], String::toInt) ?: return) ?: return call.respond(
            ErrorResponse.notFound("Account")
        )

    if (headers[headerToken] != accountId.token) return call.respond(ErrorResponse.accountTokenInvalid)
    if (ratingDao.getIdByVideoAndAccount(videoId, accountId) != null) return call.respond(
        ErrorResponse.alreadyExists("Rating")
    )

    val rating = call.receiveCatching<Int>().getOrNull() ?: return call.respond(ErrorResponse.schema)
    val daoRating = ratingDao.createRating(videoId, accountId, rating)
    call.respond(status = HttpStatusCode.OK, daoRating.toResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleReadRating() {
    val id = idParameter() ?: return
    val rating = ratingDao.readRating(id) ?: return call.respond(ErrorResponse.notFound("Rating"))

    call.respond(rating.toResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateRating() {
    val id = idParameter() ?: return
    val rating = ratingDao.readRating(id) ?: return call.respond(ErrorResponse.notFound("Rating"))
    val newRating = call.receiveCatching<Int>().getOrNull() ?: return call.respond(ErrorResponse.schema)
    ratingDao.updateRating(id, newRating)
    call.respond(status = HttpStatusCode.OK, rating.toResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteRating() {
    val id = idParameter() ?: return
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val account =
        accountDao.readAccount(convert(headers[headerAccountId], String::toInt) ?: return) ?: return call.respond(
            ErrorResponse.notFound("Account")
        )
    if (headers[headerToken] != account.token) return call.respond(ErrorResponse.accountTokenInvalid)
    val rating = ratingDao.readRating(id) ?: return call.respond(ErrorResponse.notFound("Rating"))
    ratingDao.deleteRating(id)
    call.respondText(status = HttpStatusCode.NoContent) { "Rating successfully deleted" }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetIdByVideoAndAccount() {
    val headers = requiredHeaders(headerVideoId, headerAccountId, headerToken) ?: return
    val videoId = videoDao.readVideo(convert(headers[headerVideoId], String::toInt) ?: return) ?: return call.respond(
        ErrorResponse.notFound("Video")
    )
    val accountId =
        accountDao.readAccount(convert(headers[headerAccountId], String::toInt) ?: return) ?: return call.respond(
            ErrorResponse.notFound("Account")
        )
    if (headers[headerToken] != accountId.token) return call.respond(ErrorResponse.accountTokenInvalid)
    val id = ratingDao.getIdByVideoAndAccount(videoId, accountId) ?: return call.respond(
        ErrorResponse.notFound("Rating")
    )
    call.respond(id)
}