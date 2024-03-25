package gay.extremist.plugins.routes

import gay.extremist.dao.tagDao
import io.ktor.server.routing.*

fun Route.createTagRoutes() = route("/tags") {
    val tagDao = tagDao
}