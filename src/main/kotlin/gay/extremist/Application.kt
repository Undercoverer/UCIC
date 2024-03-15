package gay.extremist

import gay.extremist.dao.AccountDao
import gay.extremist.dao.AccountDaoImpl
import gay.extremist.dao.DatabaseFactory
import gay.extremist.plugins.*
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

}
