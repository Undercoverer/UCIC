package gay.extremist.routes

import gay.extremist.dao.tagDao
import gay.extremist.util.ErrorResponse
import gay.extremist.util.idParameter
import gay.extremist.util.requiredQueryParameters
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
    }

    //    TODO Please let me know any more tag endpoints you want as I cannot think of any more
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetPresetTags() {
    val tags = tagDao.getPresetTags()
    call.respond(tags.map { it.toResponse() })
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleFindTagsBySubstring()  {
    val queryParameters = requiredQueryParameters("q") ?: return
    val search = queryParameters["q"] ?: return
    if (search.isEmpty()) return call.respond(ErrorResponse("tag", "Search query is empty", HttpStatusCode.BadRequest.value))
    if (search.length < 3) return call.respond(ErrorResponse("tag", "Search query is too short", HttpStatusCode.BadRequest.value))
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
