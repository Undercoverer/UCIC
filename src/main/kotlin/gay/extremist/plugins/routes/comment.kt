package gay.extremist.plugins.routes

import gay.extremist.dao.commentDao
import io.ktor.server.routing.*

fun Route.createCommentRoutes() = route("/comments") {
    val commentDao = commentDao
}
