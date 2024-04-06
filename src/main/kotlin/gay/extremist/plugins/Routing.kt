package gay.extremist.plugins

import gay.extremist.plugins.routes.*
import gay.extremist.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        createRatingRoutes()
        createCommentRoutes()
        createPlaylistRoutes()
        createAccountRoutes()
        createTagRoutes()
        createVideoRoutes()
    }
}
