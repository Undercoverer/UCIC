package gay.extremist.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    // TODO Configure CORS
    install(CORS) {

    }

    // TODO Configure Default Headers
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
}
