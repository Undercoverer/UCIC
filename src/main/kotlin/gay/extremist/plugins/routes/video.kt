package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.tagDao
import gay.extremist.dao.videoDao
import gay.extremist.models.Tag
import gay.extremist.util.FFMPEGProcess
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SizedCollection
import java.io.File
import java.util.*

fun Route.createVideoRoutes() = route("/videos") {
    val videoDao = videoDao
    route("/{id}") {
        get {
            val video = call.parameters["id"]?.toInt()?.let { vid -> videoDao.readVideo(vid) }
            // Respond with link to manifest.mpd file
            val manifestPath = "${
                video?.videoPath ?: call.respondText(
                    "Video not found",
                    status = HttpStatusCode.NotFound
                )
            }/manifest.mpd"
            call.respondText(manifestPath)
        }
    }

    post {
        // Check to see if the id and token in the header match up to an account
        val accountId = call.request.headers["accountId"]?.let { it1 -> accountDao.getIdByUsername(it1) }
        if (accountId == null) {
            throw Exception("accountId not provided in header")
        }
        val account = accountId?.let { accountDao.readAccount(it) }
        if (account == null) {
            throw Exception("Account not found")
        }
        if (account.token != call.request.headers["token"]) {
            throw Exception("Account access token not provided")
        }

        val multipartData = call.receiveMultipart()
        var title: String? = null
        var fileName: String? = null
        var fileDescription: String? = null
        var tags: Array<String>? = null
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "title" -> title = part.value
                        "description" -> fileDescription = part.value
                        "tags" -> tags = Json.decodeFromString<Array<String>>(part.value)
                        else -> {} // Ignore other form fields
                    }
                }

                is PartData.FileItem -> {
                    fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()
                    val filePath = "uploads/$fileName"
                    File(filePath).writeBytes(fileBytes)
                }

                else -> {}
            }

            part.dispose()
        }
        if (title == null || fileName == null || fileDescription == null || tags == null) {
            throw Exception("Missing required fields")

        }
        // Convert String tags to Tag objects
        val tagObjects: SizedCollection<Tag> =
            if (tags!!.isEmpty()) SizedCollection(delegate = Collections.emptyList()) else
                SizedCollection(delegate = tags!!.map { tagDao.findTagByName(it) }.filterNotNull())


        val video = videoDao.createVideo(
            account,
            "null",
            fileName!!,
            fileDescription!!,
            tagObjects
        )

        // Start processing the video
//        FFMPEGProcess(video).start(video).getPath()

    }

}