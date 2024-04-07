package gay.extremist.routes

import gay.extremist.dao.accountDao
import gay.extremist.dao.playlistDao
import gay.extremist.dao.videoDao
import gay.extremist.util.ErrorResponse
import gay.extremist.models.NewPlaylistData
import gay.extremist.models.PlaylistResponse
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

// DONE
fun Route.createPlaylistRoutes() = route("/playlists") {
    post { handlePlaylistCreation() }

    route("/{id}") {
        get { handleGetPlaylist() }
        put { handlePlaylistUpdate() }
        delete { handlePlaylistDeletion() }
        post("/add") { handleAddVideoToPlaylist() }
        post("/remove") { handleRemoveVideoFromPlaylist() }
    }
}


private suspend fun PipelineContext<Unit, ApplicationCall>.handleRemoveVideoFromPlaylist() {
    val headers = requiredHeaders(headerAccountId, headerToken, headerVideoId) ?: return
    val playlistId = idParameter() ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val token = headers[headerToken] ?: return
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)
    val videoId = convert(headers[headerVideoId], String::toInt) ?: return call.respond(ErrorResponse.schema)
    val video = videoDao.readVideo(videoId) ?: return call.respond(ErrorResponse.notFound("Video"))

    val playlist = playlistDao.readPlaylist(playlistId) ?: return call.respond(ErrorResponse.notFound("Playlist"))
    if (!playlist.videos.contains(video)) return call.respond(ErrorResponse.notInPlaylist)

    playlistDao.removeVideoFromPlaylist(playlistId, video)
    call.respond(HttpStatusCode.OK, message = "Video Removed from Playlist")
}
private suspend fun PipelineContext<Unit, ApplicationCall>.handleAddVideoToPlaylist() {
    val headers = requiredHeaders(headerAccountId, headerToken, headerVideoId) ?: return
    val playlistId = idParameter() ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val token = headers[headerToken] ?: return
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)
    val videoId = convert(headers[headerVideoId], String::toInt) ?: return call.respond(ErrorResponse.schema)
    val video = videoDao.readVideo(videoId) ?: return call.respond(ErrorResponse.notFound("Video"))

    val playlist = playlistDao.readPlaylist(playlistId) ?: return call.respond(ErrorResponse.notFound("Playlist"))
    if (playlist.videos.contains(video)) return call.respond(ErrorResponse.alreadyInPlaylist)


    playlistDao.addVideoToPlaylist(playlistId, video)
    call.respond(HttpStatusCode.OK, message = "Video Added to Playlist")
}
private suspend fun PipelineContext<Unit, ApplicationCall>.handlePlaylistDeletion() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val playlistId = idParameter() ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val token = headers[headerToken] ?: return
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val playlist = playlistDao.readPlaylist(playlistId) ?: return call.respond(ErrorResponse.notFound("Playlist"))
    if (playlist.owner.id != account.id) return call.respond(ErrorResponse.notOwnedByAccount("Playlist"))

    playlistDao.deletePlaylist(playlistId)
    call.respond(HttpStatusCode.OK, message = "Playlist Deleted")
}
private suspend fun PipelineContext<Unit, ApplicationCall>.handlePlaylistUpdate() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val playlistId = idParameter() ?: return

    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val playlistInfo = call.receiveCatching<NewPlaylistData>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return

    val playlist = playlistDao.readPlaylist(playlistId) ?: return call.respond(ErrorResponse.notFound("Playlist"))
    if (playlist.owner.id != account.id) return call.respond(ErrorResponse.notOwnedByAccount("Playlist"))

    playlistDao.updatePlaylist(playlist.id.value, playlistInfo.name, playlistInfo.description)
    call.respond(HttpStatusCode.OK, message = "Playlist Updated")
}
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetPlaylist() {
    val playlistId = idParameter() ?: return
    val playlist = playlistDao.readPlaylist(playlistId) ?: return call.respond(ErrorResponse.notFound("Playlist"))

    call.respond(playlist.toResponse())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handlePlaylistCreation() {
    val headers = requiredHeaders(headerAccountId, headerToken) ?: return
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    val token = headers[headerToken] ?: return
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)
    val playlistInfo = call.receiveCatching<NewPlaylistData>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return
    val playlist = playlistDao.createPlaylist(account, playlistInfo.name, playlistInfo.description)
    call.respond(
        PlaylistResponse(
            playlist.id.value, playlist.owner.id.value, playlist.name, playlist.description, emptyList()
        )
    )
}
