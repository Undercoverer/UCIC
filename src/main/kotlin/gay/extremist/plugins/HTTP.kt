package gay.extremist.plugins

import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    install(CORS) {
        anyHost() // or allowHost(yourHost)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.ContentDisposition)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(headerAccountId)
        allowHeader(headerToken)
        allowHeader(headerVideoId)
        allowHeader(headerParentCommentId)
        allowHeader(headerCommentId)
        allowNonSimpleContentTypes = true
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
}
