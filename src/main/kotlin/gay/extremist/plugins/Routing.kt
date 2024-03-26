package gay.extremist.plugins

import gay.extremist.plugins.routes.*
import io.ktor.server.application.*
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
