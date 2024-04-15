package gay.extremist

import gay.extremist.util.DatabaseFactory
import gay.extremist.plugins.configureHTTP
import gay.extremist.plugins.configureRouting
import gay.extremist.plugins.configureSerialization
import gay.extremist.routes.staticRoutes
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

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
