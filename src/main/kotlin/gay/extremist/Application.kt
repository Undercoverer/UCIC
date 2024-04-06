package gay.extremist

import gay.extremist.dao.DatabaseFactory
import gay.extremist.plugins.configureHTTP
import gay.extremist.plugins.configureRouting
import gay.extremist.plugins.configureSecurity
import gay.extremist.plugins.configureSerialization
import gay.extremist.routes.staticRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
    staticRoutes()
}
