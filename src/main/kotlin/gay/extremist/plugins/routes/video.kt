package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.tagDao
import gay.extremist.dao.videoDao
import gay.extremist.data_classes.ErrorResponse
import gay.extremist.models.Tag
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

val BASE_STORAGE_PATH = System.getenv("STORAGE_PATH") ?: "/var/storage"

fun Route.createVideoRoutes() = route("/videos") {
    val videoDao = videoDao
    route("/{id}") {
        get {
            val videoId = call.parameters["id"]
            videoId ?: return@get call.respond(ErrorResponse.videoIdNotProvided)

            runCatching { videoId.toInt() }.onFailure {
                return@get call.respond(ErrorResponse.videoNonNumericId)
            }

            val video = videoDao.readVideo(videoId.toInt())
            video ?: return@get call.respond(ErrorResponse.videoNotFound)

            // TODO WORK ON FFMPEG PROCESSING AND FINAL FILE LOCATIONS
//            // Respond with link to manifest.mpd file
//            val manifestPath = "${
//                video?.videoPath ?: call.respondText("Video not found", status = HttpStatusCode.NotFound)
//            }/manifest.mpd"
//            call.respondText(manifestPath)

            call.respond(video.videoPath)
        }
        delete {
            val videoId = call.parameters["id"]
            videoId ?: return@delete call.respond(ErrorResponse.videoIdNotProvided)
            var id: Int = -1
            runCatching { id = videoId.toInt() }.onFailure {
                return@delete call.respond(ErrorResponse.videoNonNumericId)
            }

            val video = videoDao.readVideo(id)
            video ?: return@delete call.respond(ErrorResponse.videoNotFound)

            val accountId = call.request.headers["accountId"]
            accountId ?: return@delete call.respond(ErrorResponse.accountIdNotProvided)

            runCatching { accountId.toInt() }.onFailure {
                return@delete call.respond(ErrorResponse.accountNonNumericId)
            }

            val account = accountDao.readAccount(accountId.toInt())
            account ?: return@delete call.respond(ErrorResponse.accountNotFound)

            val token = call.request.headers["token"]
            token ?: return@delete call.respond(ErrorResponse.accountTokenNotProvided)

            if (transaction {
                    account.id.value != video.creator.id.value
                }) {
                return@delete call.respond(ErrorResponse.videoNotOwnedByAccount)
            }

            if (account.token != token) {
                return@delete call.respond(ErrorResponse.accountTokenInvalid)
            }

            videoDao.deleteVideo(id)
            call.respond("\"${video.title}\" deleted")
        }
    }

    post {
        val accountId = call.request.headers["accountId"]
        accountId ?: return@post call.respond(ErrorResponse.accountIdNotProvided)

        runCatching { accountId.toInt() }.onFailure {
            return@post call.respond(ErrorResponse.accountNonNumericId)
        }

        val token = call.request.headers["token"]
        token ?: return@post call.respond(ErrorResponse.accountTokenNotProvided)

        val account = accountDao.readAccount(accountId.toInt())
        account ?: return@post call.respond(ErrorResponse.accountNotFound)

        if (account.token != token) return@post call.respond(ErrorResponse.accountTokenInvalid)


        var title = ""
        var fileName = ""
        var fileDescription = ""
        var tags: Array<String> = emptyArray()

        val multiPartData = runCatching {
            call.receiveMultipart()
        }.getOrNull() ?: return@post call.respond(ErrorResponse.videoUploadFailed)

        val timestamp = System.currentTimeMillis()
        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "title" -> title = part.value
                        "description" -> fileDescription = part.value
                        "tags" -> tags = Json.decodeFromString<Array<String>>(part.value)
                        else -> {}
                    }
                }

                is PartData.FileItem -> {
                    fileName = part.originalFileName ?: ""
                    val file = File("$BASE_STORAGE_PATH/videos/${account.id}/$timestamp/$fileName")
                    file.parentFile.mkdirs()
                    file.appendBytes(part.streamProvider().readBytes())
                }

                else -> {}
            }
            part.dispose()
        }


        // Convert String tags to Tag objects
        val tagObjects: SizedCollection<Tag> = when {
            tags.isEmpty() -> {
                SizedCollection(delegate = Collections.emptyList())
            }

            else -> {
                SizedCollection(delegate = tags.map {
                    tagDao.findTagByName(it)
                }.filterNotNull())
            }
        }

        val video = videoDao.createVideo(
            account, "$BASE_STORAGE_PATH/videos/${account.id}/$timestamp/$fileName", title, fileDescription, tagObjects,
        )

        call.respond(video.id.value)
    }
}