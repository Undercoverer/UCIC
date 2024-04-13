package gay.extremist.routes

import gay.extremist.BASE_VIDEO_STORAGE_PATH
import gay.extremist.TMP_VIDEO_STORAGE
import gay.extremist.dao.*
import gay.extremist.util.ErrorResponse
import gay.extremist.models.Tag
import gay.extremist.models.Video
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Route.createVideoRoutes() = route("/videos") {
    route("/{id}") {
        get { handleGetVideo() }
        delete { handleDeleteVideo() }
        post { handleAddTagsToVideo() }
        post { handleRemoveTagsFromVideo() }
    }

    route("/search"){
        get { handleVideoSearch() }
    }

    post { handleUploadVideo() }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleVideoSearch() {
    val queryParameters = optionalQueryParameters("tags", "title", "sortBy", "order")
    val tags = queryParameters["tags"]?.split(",") ?: emptyList()
    val title = queryParameters["title"] ?: ""
    val sortBy = queryParameters["sortBy"] ?: "alphabetic"
    val order = queryParameters["order"] ?: "desc"
    val videos = if (sortBy == "similarity") {
        videoDao.searchAndSortVideosByTitleFuzzy(title)
    } else {
        videoDao.searchVideosByTitleFuzzyAndTags(title, tags)
            .sortBy(SortMethod.fromString(sortBy), order != "desc")
    }

    call.respond(videos.map(Video::toDisplayResponse))
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleAddTagsToVideo() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val accountId = idParameter() ?: return
    val token = headers[headerToken]

    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val videoId = convert(call.parameters["id"], String::toInt) ?: return
    val video = videoDao.readVideo(videoId) ?: return call.respond(ErrorResponse.notFound("Video"))
    if (video.creator.id.value != accountId) return call.respond(ErrorResponse.notOwnedByAccount("Video"))

    val receiveTags = call.receive<Array<String>>()

    val tags = receiveTags.map {
        tagDao.findTagByName(it) ?: return call.respond(ErrorResponse.notFound("Tag"))
    }
    video.tags = SizedCollection(tags)

    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleRemoveTagsFromVideo() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val accountId = idParameter() ?: return
    val token = headers[headerToken]

    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val videoId = convert(call.parameters["id"], String::toInt) ?: return
    val video = videoDao.readVideo(videoId) ?: return call.respond(ErrorResponse.notFound("Video"))
    if (video.creator.id.value != accountId) return call.respond(ErrorResponse.notOwnedByAccount("Video"))

    val receiveTags = call.receive<Array<String>>()

    val tags = receiveTags.map {
        tagDao.findTagByName(it) ?: return call.respond(ErrorResponse.notFound("Tag"))
    }
    video.tags = SizedCollection(tags)

    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleUploadVideo() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return

    val token = headers[headerToken]
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val multiPartData = runCatching {
        call.receiveMultipart()
    }.getOrNull() ?: return call.respond(ErrorResponse.videoUploadFailed)

    var title = ""
    var videoDescription = ""
    var tags = emptyArray<String>()
    var originalFileName = ""

    val timestamp = System.currentTimeMillis()
    val fileUploadLocation = "$TMP_VIDEO_STORAGE/${account.id}/$timestamp"
    multiPartData.forEachPart {
        when (it) {
            is PartData.FormItem -> when (it.name) {
                "title" -> title = it.value
                "description" -> videoDescription = it.value
                "tags" -> tags = Json.decodeFromString<Array<String>>(it.value)
                else -> {}
            }

            is PartData.FileItem -> {
                originalFileName = it.originalFileName!!

                File(fileUploadLocation).apply {
                    mkdirs()
                    File("$fileUploadLocation/$originalFileName").apply {
                        appendBytes(it.streamProvider().readBytes())
                    }
                }
            }

            else -> {}
        }
        it.dispose()
    }
    if (originalFileName.isEmpty()) return call.respond(ErrorResponse.videoUploadFailed)

    when {
        title.isEmpty() -> return call.respond(ErrorResponse.videoTitleEmpty)
        title.length > 255 -> return call.respond(ErrorResponse.videoTitleTooLong)
        videoDescription.isEmpty() -> return call.respond(ErrorResponse.videoDescriptionEmpty)
        videoDescription.length > 4000 -> return call.respond(ErrorResponse.videoDescriptionTooLong)
        tags.isEmpty() -> return call.respond(ErrorResponse.videoTagsEmpty)
        tags.size > 16 -> return call.respond(ErrorResponse.videoTagsTooMany)
    }

    val tagObjects: SizedCollection<Tag> =
        SizedCollection(tags.map { tagDao.findTagByName(it) ?: tagDao.createTag(it) })

    val video = videoDao.createVideo(
        account,
        "$fileUploadLocation/$originalFileName",
        title,
        videoDescription,
        tagObjects,
    )

    call.respond(video.id.value)

    val newOutputDir = File("$BASE_VIDEO_STORAGE_PATH/${video.id.value}").apply(File::mkdirs)

    // May take some time (much)
    when (VideoConverter.convert(video.videoPath, newOutputDir.canonicalPath)) {
        "Success" -> {
            File(fileUploadLocation).deleteRecursively()
            videoDao.updateVideoPath(video.id.value, "/static/videos/${video.id.value}")
        }

        "Error" -> {
            File(fileUploadLocation).deleteRecursively()
            videoDao.deleteVideo(video.id.value)
            call.respond(ErrorResponse.videoConvertFailed)
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteVideo() = with(call) {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return

    val videoId = idParameter() ?: return
    val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.notFound("Video"))

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return

    if (transaction { account.id.value != video.creator.id.value }) {
        return respond(ErrorResponse.notOwnedByAccount("Video"))
    }
    if ((account.token != token)) return respond(ErrorResponse.accountTokenInvalid)
    if (File("$BASE_VIDEO_STORAGE_PATH/${video.id.value}").deleteRecursively()) return respond(ErrorResponse.videoDeleteFailed)

    videoDao.deleteVideo(videoId)
    respond(HttpStatusCode.NoContent)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetVideo() = with(call) {
    val videoId = idParameter() ?: return
    val video = videoDao.readVideo(videoId) ?: return respond(ErrorResponse.notFound("Video"))
    if (video.videoPath.contains("tmp")) return respond(ErrorResponse.videoNotProcessed)

    videoDao.incrementViewCount(videoId)
    respond(video.toResponse())
}


fun Application.staticRoutes() = routing {
    install(PartialContent)
    staticFiles("/static/videos", File(BASE_VIDEO_STORAGE_PATH))
}