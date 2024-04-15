package gay.extremist

import gay.extremist.util.DatabaseFactory
import gay.extremist.plugins.configureHTTP
import gay.extremist.plugins.configureRouting
import gay.extremist.plugins.configureSerialization
import gay.extremist.routes.staticRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureHTTP()
    configureSerialization()
    configureRouting()
    staticRoutes()
}
