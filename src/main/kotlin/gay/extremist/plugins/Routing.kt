package gay.extremist.plugins

import gay.extremist.dao.*
import gay.extremist.models.*
import gay.extremist.plugins.routes.*
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

fun Application.configureRouting() {
    // TODO Configure Routing
    routing {
        createRatingRoutes()
        createCommentRoutes()
        createPlaylistRoutes()
        createAccountRoutes()
        createTagRoutes()
        createVideoRoutes()
    }
}
