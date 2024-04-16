package gay.extremist.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.tagDao
import gay.extremist.models.toCategorizedResponse
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Route.createTagRoutes() = route("/tags") {
    get { handleGetAllTags() }
    post("/search") { handleFindTagsBySubstring() }
    get("/preset") { handleGetPresetTags() }
    get("/by-name") { handleFindTagByName() }
    route("/{id}") {
        get { handleGetTag() }
        get("/videos") { handleGetVideosForTag() }
        post("/follow") { handleFollowTag() }
        post("/unfollow") { handleUnfollowTag() }
    }

    //    TODO Please let me know any more tag endpoints you want as I cannot think of any more
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleFollowTag() {
    val id = idParameter() ?: return
    val tag = tagDao.readTag(id) ?: return call.respond(ErrorResponse.notFound("Tag"))

    val headers = requiredHeaders(headerToken, headerAccountId) ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val token = headers[headerToken] ?: return

    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    call.respond(
        if (accountDao.addFollowedTag(accountId, tag)) "Tag followed successfully" else ErrorResponse.alreadyFollowed("Tag")
    )
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleUnfollowTag() {
    val id = idParameter() ?: return
    val tag = tagDao.readTag(id) ?: return call.respond(ErrorResponse.notFound("Tag"))

    val headers = requiredHeaders(headerToken, headerAccountId) ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val token = headers[headerToken] ?: return

    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    call.respond(
        if (accountDao.removeFollowedTag(accountId, tag)) "Tag unfollowed successfully" else ErrorResponse.notFollowed("Tag")
    )
}


private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetPresetTags() {
    val tags = tagDao.getPresetTags()
    call.respond(tags.toCategorizedResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleFindTagsBySubstring() {
    val queryParameters = requiredQueryParameters("q") ?: return
    val search = queryParameters["q"] ?: return
    if (search.isEmpty()) return call.respond(
        ErrorResponse(
            "tag", "Search query is empty", HttpStatusCode.BadRequest.value
        )
    )
    if (search.length < 3) return call.respond(
        ErrorResponse(
            "tag", "Search query is too short", HttpStatusCode.BadRequest.value
        )
    )
    val tags = tagDao.findTagsBySubstring(search)
    call.respond(tags.map { it.toResponse() })
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetVideosForTag() {
    val id = idParameter() ?: return
    val videos = tagDao.getVideosForTag(id)
    call.respond(videos.map { it.toResponse() })
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleFindTagByName() {
    val queryParameters = requiredQueryParameters("q") ?: return
    val name = queryParameters["q"] ?: return
    val tag = tagDao.findTagByName(name) ?: return call.respond(ErrorResponse.notFound("Tag"))

    call.respond(tag.toResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAllTags() {
    val tags = tagDao.readTagAll()
    call.respond(tags.map { it.toResponse() })
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetTag() {
    val id = idParameter() ?: return
    val tag = tagDao.readTag(id) ?: return call.respond(ErrorResponse.notFound("Tag"))
    call.respond(tag.toResponse())
}
