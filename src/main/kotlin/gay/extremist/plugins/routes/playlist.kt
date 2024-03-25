package gay.extremist.plugins.routes

import gay.extremist.dao.playlistDao
import io.ktor.server.routing.*

fun Route.createPlaylistRoutes() = route("/playlists") {
    val playlistDao = playlistDao
}