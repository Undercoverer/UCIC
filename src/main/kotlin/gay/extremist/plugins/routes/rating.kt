package gay.extremist.plugins.routes

import io.ktor.server.routing.*
import gay.extremist.dao.ratingDao

fun Route.createRatingRoutes() = route("/ratings") {
    val ratingDao = ratingDao
}
